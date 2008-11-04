/*
* JBoss, Home of Professional Open Source
* Copyright 2006, JBoss Inc., and individual contributors as indicated
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
package org.jboss.deployers.vfs.deployer.kernel.managed;

import java.io.Serializable;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Set;

import org.jboss.beans.info.spi.BeanInfo;
import org.jboss.beans.info.spi.PropertyInfo;
import org.jboss.beans.metadata.plugins.AbstractPropertyMetaData;
import org.jboss.beans.metadata.plugins.AbstractValueMetaData;
import org.jboss.beans.metadata.plugins.AbstractBeanMetaData;
import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.PropertyMetaData;
import org.jboss.beans.metadata.spi.ValueMetaData;
import org.jboss.kernel.plugins.config.Configurator;
import org.jboss.managed.api.ManagedProperty;
import org.jboss.managed.spi.factory.InstanceClassFactory;
import org.jboss.metadata.spi.MetaData;
import org.jboss.metatype.api.values.MetaValue;
import org.jboss.metatype.api.values.MetaValueFactory;

/**
 * The InstanceClassFactory implementation for BeanMetaData.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 * @author Scott.Stark@jboss.org
 * @version $Revision:$
 */
public class BeanInstanceClassFactory implements InstanceClassFactory<AbstractBeanMetaData>
{
   /** The meta value factory */
   private MetaValueFactory metaValueFactory = MetaValueFactory.getInstance();

   /**
    * Get the classloader.
    *
    * @param beanMetaData the bean meta data
    * @return meta data's classloader
    * @throws UndeclaredThrowableException for any error
    */
   protected ClassLoader getClassLoader(BeanMetaData beanMetaData)
   {
      try
      {
         return Configurator.getClassLoader(beanMetaData);
      }
      catch (Throwable t)
      {
         throw new UndeclaredThrowableException(t);
      }
   }

   public Class<AbstractBeanMetaData> getType()
   {
      return AbstractBeanMetaData.class;
   }

   @SuppressWarnings("unchecked")
   public Class<? extends Serializable> getManagedObjectClass(AbstractBeanMetaData attachment) throws ClassNotFoundException
   {
      return (Class)getClassLoader(attachment).loadClass(attachment.getBean());
   }

   /**
    * Get the property meta data.
    *
    * @param attachment bean meta data
    * @param name property name
    * @return property meta data or null if no match
    */
   protected PropertyMetaData getPropertyMetaData(AbstractBeanMetaData attachment, String name)
   {
      PropertyMetaData propertyMetaData = null;
      Set<PropertyMetaData> properties = attachment.getProperties();
      if (properties != null && properties.isEmpty() == false)
      {
         for(PropertyMetaData pmd : properties)
         {
            if (name.equals(pmd.getName()))
            {
               propertyMetaData = pmd;
               break;
            }
         }
      }
      return propertyMetaData;
   }

   /**
    * Get the property meta data.
    *
    * @param attachment bean meta data
    * @param name property name
    * @return property meta data or exception if no match
    * @throws IllegalArgumentException for no matching property meta data
    */
   protected PropertyMetaData getExactPropertyMetaData(AbstractBeanMetaData attachment, String name)
   {
      PropertyMetaData propertyMetaData = getPropertyMetaData(attachment, name);
      if (propertyMetaData == null)
         throw new IllegalArgumentException("No matching property meta data: " + name + "/" + attachment);
      return propertyMetaData;
   }

   public MetaValue getValue(BeanInfo beanInfo, ManagedProperty property,
         MetaData metaData,
         AbstractBeanMetaData attachment)
   {
      String name = property.getName();
      PropertyMetaData pmd = getExactPropertyMetaData(attachment, name);
      PropertyInfo propertyInfo = beanInfo.getProperty(name);
      ValueMetaData valueMetaData = pmd.getValue();
      try
      {
         Object value = valueMetaData.getValue(propertyInfo.getType(), getClassLoader(attachment));
         return metaValueFactory.create(value, propertyInfo.getType());
      }
      catch (Throwable t)
      {
         throw new UndeclaredThrowableException(t);
      }
   }

   public void setValue(BeanInfo beanInfo, ManagedProperty property, AbstractBeanMetaData attachment, MetaValue value)
   {
      String name = property.getName();
      PropertyMetaData pmd = getExactPropertyMetaData(attachment, name);
      PropertyInfo propertyInfo = beanInfo.getProperty(name);
      if (pmd instanceof AbstractPropertyMetaData)
      {
         AbstractPropertyMetaData apmd = (AbstractPropertyMetaData)pmd;
         apmd.setValue(new AbstractValueMetaData(metaValueFactory.unwrap(value, propertyInfo.getType())));
      }
   }

   public Object getComponentName(BeanInfo beanInfo, ManagedProperty property, AbstractBeanMetaData attachment, MetaValue value)
   {
      return (beanInfo == null || property == null || value == null) ? attachment.getName() : null;
   }
}
