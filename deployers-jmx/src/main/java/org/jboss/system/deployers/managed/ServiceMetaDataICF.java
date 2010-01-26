/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.system.deployers.managed;

import java.io.Serializable;
import java.util.List;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.beans.info.spi.BeanInfo;
import org.jboss.beans.info.spi.PropertyInfo;
import org.jboss.logging.Logger;
import org.jboss.managed.api.ManagedProperty;
import org.jboss.managed.spi.factory.InstanceClassFactory;
import org.jboss.metadata.spi.MetaData;
import org.jboss.metatype.api.values.MetaValue;
import org.jboss.metatype.api.values.MetaValueFactory;
import org.jboss.system.metadata.ServiceAnnotationMetaData;
import org.jboss.system.metadata.ServiceAttributeMetaData;
import org.jboss.system.metadata.ServiceDependencyValueMetaData;
import org.jboss.system.metadata.ServiceMetaData;
import org.jboss.system.metadata.ServiceTextValueMetaData;
import org.jboss.system.metadata.ServiceValueMetaData;

/**
 * The InstanceClassFactory implementation for ServiceMetaData.
 * 
 * @author Scott.Stark@jboss.org
 * @author Dimitris.Andreadis@jboss.org
 * @version $Revision$
 */
public class ServiceMetaDataICF implements InstanceClassFactory<ServiceMetaData>
{
   private static final Logger log = Logger.getLogger(ServiceMetaDataICF.class);

   private static final String MOCLASS_ANNOTATION = '@' + ManagementObjectClass.class.getName();
   
   private MBeanServer mbeanServer;
   
   /** The meta value factory */
   private MetaValueFactory metaValueFactory = MetaValueFactory.getInstance(); 

   public MBeanServer getMbeanServer()
   {
      return mbeanServer;
   }

   public void setMbeanServer(MBeanServer mbeanServer)
   {
      this.mbeanServer = mbeanServer;
   }

   public Class<ServiceMetaData> getType()
   {
      return ServiceMetaData.class;
   }

   @SuppressWarnings("unchecked")
   public Class<? extends Serializable> getManagedObjectClass(ServiceMetaData md)
      throws ClassNotFoundException
   {
      ClassLoader prevLoader = SecurityActions.getContextClassLoader();
      try
      {
         ClassLoader loader = getServiceMetaDataCL(md);
         Class moClass = loader.loadClass(md.getCode());
         // Set the mbean class loader as the TCL
         SecurityActions.setContextClassLoader(loader);

         // Looks for a ManagementObjectClass annotation that defines
         // an alternate class to scan for management annotations
         List<ServiceAnnotationMetaData> samlist = md.getAnnotations();
         for (ServiceAnnotationMetaData sam : samlist)
         {
            // Annotations are not yet introduced to the actual mbean
            // so just look for the annotation string
            String anString = sam.getAnnotation();
            if (anString.startsWith(MOCLASS_ANNOTATION))
            {
               Class<?> originalClass = moClass;
               ManagementObjectClass moc = (ManagementObjectClass)sam.getAnnotationInstance(loader);
               moClass = moc.code();
               log.debugf("Using alternate class '%1s' for class %2s",  moClass, originalClass);
               break;
            }  
         }
         return moClass;
      }
      catch(InstanceNotFoundException e)
      {
         throw new ClassNotFoundException("Failed to obtain mbean class loader", e);
      }
      finally
      {
         SecurityActions.setContextClassLoader(prevLoader);
      }
   }

   public MetaValue getValue(BeanInfo beanInfo, ManagedProperty property,
         MetaData metaData,
         ServiceMetaData md)
   {
      // First look to the mapped name
      String name = property.getMappedName();
      if (name == null)
         name = property.getName();

      ClassLoader prevLoader = SecurityActions.getContextClassLoader();
      Object value = null;
      MetaValue mvalue = null;
      try
      {
         ClassLoader loader = getServiceMetaDataCL(md);
         // Set the mbean class loader as the TCL
         SecurityActions.setContextClassLoader(loader);

         // Get the attribute value from the metadata
         for (ServiceAttributeMetaData amd : md.getAttributes())
         {
            // The compare is case-insensitve due to the attribute/javabean case mismatch
            if (amd.getName().equalsIgnoreCase(name))
            {
               value = amd.getValue();
               break;
            }
         }
         // If the value is null, look to mbean for the value
         if (value == null)
         {
            ObjectName mbean = md.getObjectName();
            try
            {
               if(getMbeanServer() != null)
                  value = getMbeanServer().getAttribute(mbean, name);
            }
            catch (AttributeNotFoundException e)
            {
               // Try the alternate name
               String attribute = name;
               if(Character.isUpperCase(name.charAt(0)))
                  attribute = Character.toLowerCase(name.charAt(0)) + name.substring(1);
               else
                  attribute = Character.toUpperCase(name.charAt(0)) + name.substring(1);
               try
               {
                  value = getMbeanServer().getAttribute(mbean, attribute);
               }
               catch(Exception e2)
               {
                  log.debugf(e2, "Failed to get value from mbean for: %1s", attribute);
               }               
            }
            catch(Exception e)
            {
               log.debugf(e, "Failed to get value from mbean for: %1s", name);
            }
         }
   
         // Unwrap the ServiceValueMetaData types
         if (value instanceof ServiceTextValueMetaData)
         {
            ServiceTextValueMetaData text = (ServiceTextValueMetaData) value;
            value = text.getText();
         }
         else if (value instanceof ServiceDependencyValueMetaData)
         {
            ServiceDependencyValueMetaData depends = (ServiceDependencyValueMetaData) value;
            value = depends.getDependency();
         }
         // TODO: unwrap other ServiceValueMetaData types
   
         PropertyInfo propertyInfo = beanInfo.getProperty(name);
         try
         {
            mvalue = metaValueFactory.create(value, propertyInfo.getType());
         }
         catch(Exception e)
         {
            log.debugf(e, "Failed to get property value for bean: %1s, property: %2s", beanInfo.getName(), propertyInfo.getName());
            mvalue = metaValueFactory.create(null, propertyInfo.getType());
            return mvalue;
         }
      }
      catch(InstanceNotFoundException e)
      {
         throw new IllegalStateException("Failed to obtain mbean class loader", e);
      }
      finally
      {
         SecurityActions.setContextClassLoader(prevLoader);
      }
      return mvalue;
   }

   public void setValue(BeanInfo beanInfo, ManagedProperty property, ServiceMetaData md, MetaValue value)
   {
      ClassLoader prevLoader = SecurityActions.getContextClassLoader();
      try
      {
         ClassLoader loader = getServiceMetaDataCL(md);
         // Set the mbean class loader as the TCL
         SecurityActions.setContextClassLoader(loader);

         // First look to the mapped name
         String name = property.getMappedName();
         if (name == null)
            property.getName();
   
         // Get the attribute value
         ServiceValueMetaData attributeValue = null;
         for (ServiceAttributeMetaData amd : md.getAttributes())
         {
            // The compare is case-insensitive due to the attribute/javabean case mismatch
            if (amd.getName().equalsIgnoreCase(name))
            {
               attributeValue = amd.getValue();
               break;
            }
         }

         // Unwrap
         PropertyInfo propertyInfo = beanInfo.getProperty(name);
         Object plainValue = metaValueFactory.unwrap(value, propertyInfo.getType());
         
         // There may not be an attribute value, see if there is a matching property
         if (attributeValue == null)
         {
            // FIXME ignore null values
            if(plainValue == null) return;
            
            String aname = mapAttributeName(md, name);
            if(aname != null)
            {
               ServiceAttributeMetaData attr = new ServiceAttributeMetaData();
               attr.setName(aname);
               md.addAttribute(attr);
               attributeValue = new ServiceTextValueMetaData("");
               attr.setValue(attributeValue);
            }
         }
         if (attributeValue != null)
         {
            // Unwrap the ServiceValueMetaData types
            if (attributeValue instanceof ServiceTextValueMetaData)
            {
               ServiceTextValueMetaData text = (ServiceTextValueMetaData) attributeValue;
               text.setText(String.valueOf(plainValue));
            }
            else if (value instanceof ServiceDependencyValueMetaData)
            {
               ServiceDependencyValueMetaData depends = (ServiceDependencyValueMetaData) attributeValue;
               depends.setDependency(String.valueOf(plainValue));
            }
            // TODO: unwrap other ServiceValueMetaData types
            else
            {
               throw new IllegalArgumentException("Unhandled attribute value type: " + name + "/" + md+", class="+attributeValue.getClass());               
            }
         }
         else
            throw new IllegalArgumentException("No matching attribute found: " + name + "/" + md);
      }
      catch(InstanceNotFoundException e)
      {
         throw new IllegalStateException("Failed to obtain mbean class loader", e);
      }
      finally
      {
         SecurityActions.setContextClassLoader(prevLoader);
      }
   }

   public Object getComponentName(BeanInfo beanInfo, ManagedProperty property, ServiceMetaData md, MetaValue value)
   {
      ObjectName objectName = md.getObjectName();
      String canonicalName = objectName.getCanonicalName();
      return canonicalName;
   }

   /**
    * Obtains the ServiceMetaData class loader from the
    * getClassLoaderName value if there is an mbeanServer.
    * 
    * @param md - the mbean metadata
    * @return the ServiceMetaData.ClassLoaderName class loader if
    *    the mbeanServer has been set, the current TCL otherwise.
    * @throws InstanceNotFoundException if no mbean class loader can be
    *    found by the ServiceMetaData.ClassLoaderName
    */
   private ClassLoader getServiceMetaDataCL(ServiceMetaData md)
      throws InstanceNotFoundException
   {
      ClassLoader loader = null;
      if(mbeanServer != null)
         loader = mbeanServer.getClassLoader(md.getClassLoaderName());
      // Fallback to TCL if there is no mbeanServer
      if(loader == null)
         loader = Thread.currentThread().getContextClassLoader();
      return loader;
   }

   /**
    * Try to find a matching mbean attribute
    * @param name
    * @return
    */
   private String mapAttributeName(ServiceMetaData md, String name)
   {
      ObjectName mbean = md.getObjectName();
      String attrName = null;
      try
      {
         mbeanServer.getAttribute(mbean, name);
         attrName = name;
      }
      catch(Exception e)
      {
         char c = name.charAt(0);
         if(Character.isLowerCase(c))
            name = Character.toUpperCase(c) + name.substring(1);
         else
            name = Character.toLowerCase(c) + name.substring(1);
         try
         {
            mbeanServer.getAttribute(mbean, name);
            attrName = name;
         }
         catch(Exception e2)
         {
         }
      }
      // FIXME 
      if(attrName == null) 
      {
         char c = name.charAt(0);
         name = Character.toUpperCase(c) + name.substring(1);
         return name;
      }
      return attrName;
   }
}
