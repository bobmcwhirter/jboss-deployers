/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.deployers.spi.deployer.helpers;

import java.io.Serializable;
import java.util.Map;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.managed.ManagedObjectCreator;
import org.jboss.deployers.spi.deployer.matchers.JarExtensionProvider;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.managed.api.ManagedObject;
import org.jboss.managed.api.factory.ManagedObjectFactory;
import org.jboss.managed.plugins.factory.ManagedObjectFactoryBuilder;

/**
 * AbstractParsingDeployerWithOutput. 
 * 
 * @param <T> the type of output
 * @author <a href="adrian@jboss.org">Adrian Brock</a>
 * @author Scott.Stark@jboss.org
 * @version $Revision: 1.1 $
 */
public abstract class AbstractParsingDeployerWithOutput<T> extends AbstractParsingDeployer
   implements ManagedObjectCreator, JarExtensionProvider
{
   /** The metadata file name */
   private String name;
   
   /** The suffix */
   private String suffix;
   
   /** The jar extension */
   private String jarExtension;

   /** Include the deployment file */
   private boolean includeDeploymentFile = false;

   /** Should the ManagedObjects be created for the output metadata */
   private boolean buildManagedObject = false;
   
   /**
    * Create a new AbstractParsingDeployerWithOutput.
    * 
    * @param output the type of output
    * @throws IllegalArgumentException for null output
    */
   public AbstractParsingDeployerWithOutput(Class<T> output)
   {
      if (output == null)
         throw new IllegalArgumentException("Null output");
      setOutput(output);
   }
   
   @SuppressWarnings("unchecked")
   @Override
   public Class<T> getOutput()
   {
      return (Class<T>) super.getOutput();
   }

   /**
    * Get the name.
    * 
    * @return the name.
    */
   public String getName()
   {
      return name;
   }

   /**
    * Set the name.
    * 
    * @param name the name.
    */
   public void setName(String name)
   {
      this.name = name;
   }

   /**
    * Get the suffix.
    * 
    * @return the suffix.
    */
   public String getSuffix()
   {
      return suffix;
   }

   /**
    * Set the suffix.
    * 
    * @param suffix the suffix.
    */
   public void setSuffix(String suffix)
   {
      this.suffix = suffix;
   }

   /**
    * Get the jar extension.
    *
    * @return the jar extension
    */
   public String getJarExtension()
   {
      return jarExtension;
   }

   /**
    * Set the jar extension.
    *
    * @param jarExtension the jar extension
    */
   public void setJarExtension(String jarExtension)
   {
      this.jarExtension = jarExtension;
   }

   /**
    * Get the includeDeploymentFile.
    * 
    * @return the includeDeploymentFile.
    */
   public boolean isIncludeDeploymentFile()
   {
      return includeDeploymentFile;
   }

   /**
    * Set the includeDeploymentFile.
    * 
    * @param includeDeploymentFile the includeDeploymentFile.
    */
   public void setIncludeDeploymentFile(boolean includeDeploymentFile)
   {
      this.includeDeploymentFile = includeDeploymentFile;
   }

   public boolean isBuildManagedObject()
   {
      return buildManagedObject;
   }

   public void setBuildManagedObject(boolean buildManagedObject)
   {
      this.buildManagedObject = buildManagedObject;
   }

   /**
    * A flag indicating whether createMetaData should execute a parse even if a non-null metadata value exists.
    * 
    * @return false if a parse should be performed only if there is no existing metadata value. True indicates
    * that parse should be done regardless of an existing metadata value.
    */
   protected boolean allowsReparse()
   {
      return false;
   }

   public void deploy(DeploymentUnit unit) throws DeploymentException
   {
      if (accepts(unit) == false)
         return;
      createMetaData(unit, name, suffix);
   }

   /**
    * Callback to do prechecking on the deployment
    * 
    * @param unit the unit
    * @return true by default
    * @throws DeploymentException for any error
    */
   protected boolean accepts(DeploymentUnit unit) throws DeploymentException
   {
      return true;
   }
   
   /**
    * Get some meta data
    * 
    * @param unit the deployment unit
    * @param key the key into the managed objects
    * @return the metadata or null if it doesn't exist
    */
   protected T getMetaData(DeploymentUnit unit, String key)
   {
      return unit.getAttachment(key, getOutput());
   }
   
   /**
    * Create some meta data. Calls createMetaData(unit, name, suffix, getDeploymentType().getName()).
    * 
    * @param unit the deployment unit
    * @param name the name
    * @param suffix the suffix
    * @throws DeploymentException for any error
    */
   protected void createMetaData(DeploymentUnit unit, String name, String suffix) throws DeploymentException
   {
      createMetaData(unit, name, suffix, getOutput().getName());
   }
   
   /**
    * Create some meta data. Invokes parse(unit, name, suffix) if there is not already a
    * metadata
    * 
    * @param unit the deployment unit
    * @param name the name
    * @param suffix the suffix
    * @param key the key into the managed objects
    * @throws DeploymentException for any error
    */
   protected void createMetaData(DeploymentUnit unit, String name, String suffix, String key) throws DeploymentException
   {
      // First see whether it already exists
      T result = getMetaData(unit, key);
      if (result != null && allowsReparse() == false)
         return;

      // Create it
      try
      {
         if (suffix == null)
            result = parse(unit, name, result);
         else
            result = parse(unit, name, suffix, result);
      }
      catch (Exception e)
      {
         throw DeploymentException.rethrowAsDeploymentException("Error creating managed object for " + unit.getName(), e);
      }
      
      // Doesn't exist
      if (result == null)
         return;
      
      // Register it
      unit.getTransientManagedObjects().addAttachment(key, result, getOutput());
   }

   /**
    * Parse an exact file name
    * 
    * @param unit the unit
    * @param name the exact name to match
    * @param root - possibly null pre-existing root
    * @return the metadata or null if it doesn't exist
    * @throws Exception for any error
    */
   protected abstract T parse(DeploymentUnit unit, String name, T root) throws Exception;
   
   /**
    * Parse an exact file name or look for a suffix
    * 
    * @param unit the unit
    * @param name the exact name to match
    * @param suffix the suffix to match
    * @param root - possibly null pre-existing root
    * @return the metadata or null if it doesn't exist
    * @throws Exception for any error
    */
   protected abstract T parse(DeploymentUnit unit, String name, String suffix, T root) throws Exception;

   /**
    * Build managed object.
    *
    * @param unit the deployment unit
    * @param managedObjects map of managed objects
    * @throws DeploymentException for any deployment exception
    */
   public void build(DeploymentUnit unit, Map<String, ManagedObject> managedObjects) throws DeploymentException
   {
      if (buildManagedObject)
      {
         T deployment = unit.getAttachment(getOutput());
         if (deployment != null)
         {
            // must be Serializable - see getAttachment method contract (expectedType.cast(result))
            if ( (deployment instanceof Serializable) == false)
            {
               // Probably an error if the deployer enabled buildManagedObject?
               log.debug("Skipping ManagedObject since T(" + deployment + ") is not Serializable");
               return;
            }
            
            Serializable instance = (Serializable) deployment;
            ManagedObjectFactory factory = ManagedObjectFactoryBuilder.create();
            ManagedObject mo = factory.initManagedObject(instance, null, null);
            if (mo != null)
               managedObjects.put(mo.getName(), mo);
         }
      }
   }
}
