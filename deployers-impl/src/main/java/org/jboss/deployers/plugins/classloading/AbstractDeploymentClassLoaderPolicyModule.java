/*
* JBoss, Home of Professional Open Source
* Copyright 2008, JBoss Inc., and individual contributors as indicated
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
package org.jboss.deployers.plugins.classloading;

import java.util.Set;

import org.jboss.classloading.spi.dependency.policy.ClassLoaderPolicyModule;
import org.jboss.classloading.spi.metadata.ClassLoadingMetaData;
import org.jboss.dependency.spi.ControllerContext;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.dependency.spi.Controller;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.structure.spi.DeploymentUnit;

/**
 * AbstractDeploymentClassLoaderPolicyModule.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 * @version $Revision: 1.1 $
 */
public abstract class AbstractDeploymentClassLoaderPolicyModule extends ClassLoaderPolicyModule
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;

   /** The classloader state for deployments */
   private static ControllerState CLASSLOADER_STATE = ControllerState.newState(DeploymentStages.CLASSLOADER.getName());
   
   /** The deployment unit */
   private DeploymentUnit unit;
   
   /**
    * Determine the classloading metadata for the deployment unit 
    * 
    * @param unit the deployment unit
    * @return the classloading metadata
    */
   private static ClassLoadingMetaData determineClassLoadingMetaData(DeploymentUnit unit)
   {
      if (unit == null)
         throw new IllegalArgumentException("Null unit");
      return unit.getAttachment(ClassLoadingMetaData.class);
   }

   /**
    * Determine the classloading metadata for the deployment unit 
    * 
    * @param unit the deployment unit
    * @param addAlias should we add alias or remove
    * @return the classloading metadata
    */
   private static String determineContextName(DeploymentUnit unit, boolean addAlias)
   {
      if (unit == null)
         throw new IllegalArgumentException("Null unit");

      ControllerContext context = unit.getTopLevel().getAttachment(ControllerContext.class);
      if (context == null)
         throw new IllegalStateException("Deployment has no controller context");
      
      // We use the deployment name
      String contextName = unit.getName();

      // Check to see whether we need to add our name as an alias
      if (contextName.equals(context.getName()) == false)
      {
         Set<Object> aliases = context.getAliases();
         if (aliases == null || (aliases != null && aliases.contains(contextName) == false))
         {
            Controller controller = context.getController();
            if (addAlias)
            {
               try
               {
                  controller.addAlias(contextName, context.getName());
               }
               catch (Throwable t)
               {
                  throw new RuntimeException("Error adding deployment alias " + contextName + " to " + context, t);
               }
            }
            else
            {
               controller.removeAlias(contextName);
            }
         }
      }
      
      return contextName;
   }
   
   /**
    * Create a new AbstractDeploymentClassLoaderPolicyModule.
    * 
    * @param unit the deployment unit
    * @throws IllegalArgumentException for a null unit
    */
   public AbstractDeploymentClassLoaderPolicyModule(DeploymentUnit unit)
   {
      super(determineClassLoadingMetaData(unit), determineContextName(unit, true));
      this.unit = unit;
      ControllerContext context = unit.getTopLevel().getAttachment(ControllerContext.class);
      setControllerContext(context);
   }

   /**
    * Get the unit.
    * 
    * @return the unit.
    */
   public DeploymentUnit getDeploymentUnit()
   {
      return unit;
   }

   @Override
   public ControllerState getClassLoaderState()
   {
      return CLASSLOADER_STATE;
   }

   @Override
   public void release()
   {
      try
      {
         super.release();
      }
      finally
      {
         determineContextName(unit, false);
      }
   }
}
