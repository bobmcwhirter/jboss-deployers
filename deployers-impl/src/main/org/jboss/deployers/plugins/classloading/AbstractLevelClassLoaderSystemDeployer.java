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
package org.jboss.deployers.plugins.classloading;

import org.jboss.classloader.spi.ClassLoaderDomain;
import org.jboss.classloader.spi.ClassLoaderSystem;
import org.jboss.classloading.spi.dependency.ClassLoading;
import org.jboss.classloading.spi.dependency.Module;
import org.jboss.classloading.spi.dependency.policy.ClassLoaderPolicyModule;
import org.jboss.deployers.spi.deployer.helpers.AbstractClassLoaderDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;

/**
 * AbstractLevelClassLoaderSystemDeployer.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class AbstractLevelClassLoaderSystemDeployer extends AbstractClassLoaderDeployer
{
   /** The classloading */
   private ClassLoading classLoading;
   
   /** The classloader system */
   private ClassLoaderSystem system;
   
   /**
    * Get the classLoading.
    * 
    * @return the classLoading.
    */
   public ClassLoading getClassLoading()
   {
      return classLoading;
   }

   /**
    * Set the classLoading.
    * 
    * @param classLoading the classLoading.
    */
   public void setClassLoading(ClassLoading classLoading)
   {
      this.classLoading = classLoading;
   }

   /**
    * Get the system.
    * 
    * @return the system.
    */
   public ClassLoaderSystem getSystem()
   {
      return system;
   }

   /**
    * Set the system.
    * 
    * @param system the system.
    */
   public void setSystem(ClassLoaderSystem system)
   {
      this.system = system;
   }

   /**
    * Validate the config
    */
   public void create()
   {
      if (classLoading == null)
         throw new IllegalStateException("The classLoading has not been set");
      if (system == null)
         throw new IllegalStateException("The system has not been set");
   }
   
   public ClassLoader createClassLoader(DeploymentUnit unit) throws Exception
   {
      if (classLoading == null)
         throw new IllegalStateException("The classLoading has not been set");
      if (system == null)
         throw new IllegalStateException("The system has not been set");

      Module module = unit.getAttachment(Module.class);
      if (module == null)
      {
         if (isTopLevelOnly())
            throw new IllegalStateException("No module for top level deployment " + unit.getName());
         else
            return unit.getParent().getClassLoader();
      }

      if (module instanceof ClassLoaderPolicyModule == false)
         throw new IllegalStateException("Module is not an instance of " + ClassLoaderPolicyModule.class.getName() + " actual=" + module.getClass().getName());
      ClassLoaderPolicyModule classLoaderPolicyModule = (ClassLoaderPolicyModule) module;

      if (unit.isTopLevel() || module.getParentDomainName() != null)
      {
         // Top level, just create the classloader
         return classLoaderPolicyModule.registerClassLoaderPolicy(system);
      }
      else
      {
         // Subdeployment that wants a classloader
         ClassLoader parentClassLoader = unit.getParent().getClassLoader();
         return classLoaderPolicyModule.registerClassLoaderPolicy(system, parentClassLoader);
      }
   }

   @Override
   public void removeClassLoader(DeploymentUnit unit) throws Exception
   {
      Module module = unit.getAttachment(Module.class);
      if (module == null)
         return;

      ClassLoader classLoader = unit.getClassLoader();
      try
      {
         try
         {
            // Remove the classloader
            system.unregisterClassLoader(classLoader);
         }
         finally
         {
            // Try to tidy up empty domains
            String domainName = module.getDeterminedDomainName();
            if (ClassLoaderSystem.DEFAULT_DOMAIN_NAME.equals(domainName) == false)
            {
               ClassLoaderDomain domain = system.getDomain(domainName);
               if (domain.hasClassLoaders() == false)
                  system.unregisterDomain(domain);
            }
         }
      }
      finally
      {
         cleanup(unit, module);
         module.reset();
      }
  }
   
   /**
    * Hook to perform cleanup on destruction of classloaader
    * 
    * @param unit the deployment unit
    * @param module the module
    * @throws Exception for any error
    */
   protected void cleanup(DeploymentUnit unit, Module module) throws Exception
   {
   }
}
