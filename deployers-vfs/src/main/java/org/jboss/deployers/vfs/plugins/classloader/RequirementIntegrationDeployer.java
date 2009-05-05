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
package org.jboss.deployers.vfs.plugins.classloader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.jboss.classloading.spi.metadata.ClassLoadingMetaData;
import org.jboss.classloading.spi.metadata.ClassLoadingMetaDataFactory;
import org.jboss.classloading.spi.metadata.Requirement;
import org.jboss.classloading.spi.metadata.RequirementsMetaData;
import org.jboss.classloading.spi.metadata.helpers.AbstractRequirement;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.vfs.spi.deployer.AbstractOptionalVFSRealDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;

/**
 * Integration deployer.
 * Adds integration requirement to deployment classpath.
 *
 * @param <T> exact output type
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 */
public abstract class RequirementIntegrationDeployer<T> extends AbstractOptionalVFSRealDeployer<T>
{
   /** The jboss integration module names */
   private Set<String> integrationModuleNames;

   public RequirementIntegrationDeployer(Class<T> input)
   {
      super(input);
      // We have to run before the classloading is setup
      setStage(DeploymentStages.DESCRIBE);
      // We modify the classloading imports/requirements
      addInput(ClassLoadingMetaData.class);
      setOutput(ClassLoadingMetaData.class);
   }

   /**
    * Get the integration module name.
    *
    * @return the integration modeule name
    */
   public String getIntegrationModuleName()
   {
      if (integrationModuleNames == null || integrationModuleNames.isEmpty())
         return null;
      else if (integrationModuleNames.size() == 1)
         return integrationModuleNames.iterator().next();
      else
         throw new IllegalArgumentException("Multiple integration module names: " + integrationModuleNames);
   }

   /**
    * Set the intagration module name.
    *
    * @param integrationModuleName the integration module name
    */
   public void setIntegrationModuleName(String integrationModuleName)
   {
      this.integrationModuleNames = Collections.singleton(integrationModuleName);
   }

   /**
    * Get integration module names.
    *
    * @return the integration module names
    */
   public Set<String> getIntegrationModuleNames()
   {
      return integrationModuleNames;
   }

   /**
    * Set integration module names
    *
    * @param integrationModuleNames the integration modeul names
    */
   public void setIntegrationModuleNames(Set<String> integrationModuleNames)
   {
      this.integrationModuleNames = integrationModuleNames;
   }

   /**
    * Check if integration urls exist.
    */
   public void start()
   {
      if (integrationModuleNames == null || integrationModuleNames.isEmpty())
         throw new IllegalArgumentException("No integration module names.");
   }

   @Override
   public void deploy(VFSDeploymentUnit unit, T metaData) throws DeploymentException
   {
      ClassLoadingMetaData clmd = unit.getAttachment(ClassLoadingMetaData.class);
      if (clmd == null)
      {
         log.warn("Missing ClassLoadingMetaData: " + unit);
         return;
      }

      RequirementsMetaData requirements = clmd.getRequirements();
      AbstractRequirement integrationModule = hasIntegrationModuleRequirement(unit, requirements);
      // If we are importing integration core then import the integration at the same version
      if (integrationModule != null)
      {
         ClassLoadingMetaDataFactory factory = ClassLoadingMetaDataFactory.getInstance();
         List<Requirement> added = new ArrayList<Requirement>();
         try
         {
            for (String integrationModuleName : integrationModuleNames)
            {
               Requirement integrationRequirement = factory.createRequireModule(integrationModuleName, integrationModule.getVersionRange());
               requirements.addRequirement(integrationRequirement);
               added.add(integrationModule);
            }
         }
         catch (Throwable t)
         {
            for (int i = added.size() - 1; i >=0; i--)
            {
               requirements.removeRequirement(added.get(i));
            }
            throw DeploymentException.rethrowAsDeploymentException("Error adding integration requirement.", t);
         }
      }
   }

   @Override
   public void undeploy(VFSDeploymentUnit unit, T deployment)
   {
      ClassLoadingMetaData clmd = unit.getAttachment(ClassLoadingMetaData.class);
      if (clmd != null)
      {
         RequirementsMetaData requirements = clmd.getRequirements();
         AbstractRequirement integrationModule = hasIntegrationModuleRequirement(unit, requirements);
         if (integrationModule != null)
         {
            ClassLoadingMetaDataFactory factory = ClassLoadingMetaDataFactory.getInstance();
            for (String integrationModuleName : integrationModuleNames)
            {
               try
               {
                  Requirement integrationRequirement = factory.createRequireModule(integrationModuleName, integrationModule.getVersionRange());
                  requirements.removeRequirement(integrationRequirement);
               }
               catch (Throwable t)
               {
                  log.warn("Error during requirement removal: " + integrationModuleName, t);
               }
            }
         }
      }
   }

   /**
    * Do we have integration module requirements.
    *
    * @param unit the deployment unit
    * @param requirements the current requirements
    * @return integration core requirement
    */
   protected AbstractRequirement hasIntegrationModuleRequirement(VFSDeploymentUnit unit, RequirementsMetaData requirements)
   {
      return hasIntegrationModuleRequirement(requirements);
   }

   /**
    * Do we have integration module requirements.
    *
    * @param requirements the current requirements
    * @return integration core requirement
    */
   protected abstract AbstractRequirement hasIntegrationModuleRequirement(RequirementsMetaData requirements);
}