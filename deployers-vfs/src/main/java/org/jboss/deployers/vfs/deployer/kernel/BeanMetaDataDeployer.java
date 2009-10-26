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
package org.jboss.deployers.vfs.deployer.kernel;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jboss.beans.metadata.plugins.AbstractClassLoaderMetaData;
import org.jboss.beans.metadata.plugins.AbstractValueMetaData;
import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.ClassLoaderMetaData;
import org.jboss.beans.metadata.spi.ValueMetaData;
import org.jboss.dependency.spi.Controller;
import org.jboss.dependency.spi.ScopeInfo;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.helpers.AbstractSimpleRealDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.deployer.helpers.KernelControllerContextCreator;
import org.jboss.kernel.Kernel;
import org.jboss.kernel.plugins.dependency.AbstractKernelControllerContext;
import org.jboss.kernel.spi.dependency.KernelControllerContext;
import org.jboss.metadata.spi.scope.Scope;
import org.jboss.metadata.spi.scope.ScopeKey;

/**
 * BeanMetaDataDeployer.<p>
 * 
 * This deployer is responsible for deploying all metadata of
 * type {@link org.jboss.beans.metadata.spi.BeanMetaData}.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class BeanMetaDataDeployer extends AbstractSimpleRealDeployer<BeanMetaData>
{
   /** The controller */
   private Controller controller;
   
   /** List of controller context creators */
   private List<KernelControllerContextCreator> controllerContextCreators = new CopyOnWriteArrayList<KernelControllerContextCreator>();
   
   /** The default controller context creator */
   
   
   /**
    * Create a new BeanDeployer.
    * 
    * @param kernel the kernel
    * @throws IllegalArgumentException for a null kernel
    * @deprecated use other constructor
    */
   public BeanMetaDataDeployer(Kernel kernel)
   {
      super(BeanMetaData.class);
      if (kernel == null)
         throw new IllegalArgumentException("Null kernel");
      init(kernel.getController());
   }

   /**
    * Create a new BeanDeployer.
    *
    * @param controller the controller
    * @throws IllegalArgumentException for a null controller
    */
   public BeanMetaDataDeployer(Controller controller)
   {
      super(BeanMetaData.class);
      init(controller);
   }

   /**
    * Simple init.
    *
    * @param controller the controller
    */
   protected void init(Controller controller)
   {
      if (controller == null)
         throw new IllegalArgumentException("Null controller");
      this.controller = controller;
      setComponentsOnly(true);
      setUseUnitName(true);
   }

   /**
    * Incallback for the controller context creators
    * 
    * @param creator The controller context creator to be added
    */
   public void addControllerContextCreator(KernelControllerContextCreator creator)
   {
      if (creator != null)
         controllerContextCreators.add(creator);
   }
   
   /**
    * Uncallback for the controller context creators
    * 
    * @param creator The controller context creator to be removed
    */
   public void removeControllerContextCreator(KernelControllerContextCreator creator)
   {
      if (creator != null)
         controllerContextCreators.remove(creator);
   }
   
   @Override
   public void deploy(DeploymentUnit unit, BeanMetaData deployment) throws DeploymentException
   {
      // No explicit classloader, use the deployment's classloader
      if (deployment.getClassLoader() == null)
      {
         try
         {
            // Check the unit has a classloader
            unit.getClassLoader();
            // TODO clone the metadata?
            deployment.setClassLoader(new DeploymentClassLoaderMetaData(unit));
         }
         catch (Exception e)
         {
            log.debug("Unable to retrieve classloader for deployment: " + unit.getName() + " reason=" + e.toString());
         }
      }
      KernelControllerContext context = createControllerContext(unit, deployment);
      ScopeInfo scopeInfo = context.getScopeInfo();
      scopeInfo.setScope(unit.getScope());
      scopeInfo.setMutableScope(unit.getMutableScope());
      
      try
      {
         controller.install(context);
      }
      catch (Throwable t)
      {
         throw DeploymentException.rethrowAsDeploymentException("Error deploying: " + deployment.getName(), t);
      }
   }

   private KernelControllerContext createControllerContext(DeploymentUnit unit, BeanMetaData deployment)
   {
      if (controllerContextCreators.size() > 0)
      {
         for (KernelControllerContextCreator creator : controllerContextCreators)
         {
            KernelControllerContext context = creator.createContext(controller, unit, deployment);
            if (context != null)
               return context;
         }
      }
      return new AbstractKernelControllerContext(null, deployment, null);
   }
   
   /**
    * Merge scope keys.
    *
    * @param contextKey the context key
    * @param unitKey the unit key
    * @deprecated no longer in use
    */
   @Deprecated
   protected static void mergeScopes(ScopeKey contextKey, ScopeKey unitKey)
   {
      if (contextKey == null)
         return;
      if (unitKey == null)
         return;

      Collection<Scope> unitScopes = unitKey.getScopes();
      if (unitScopes == null || unitScopes.isEmpty())
         return;

      for (Scope scope : unitScopes)
         contextKey.addScope(scope);
   }

   @Override
   public void undeploy(DeploymentUnit unit, BeanMetaData deployment)
   {
      controller.uninstall(deployment.getName());
      
      // Remove any classloader metadata we added (not necessary if we clone above)
      ClassLoaderMetaData classLoader = deployment.getClassLoader();
      if (classLoader instanceof DeploymentClassLoaderMetaData)
         deployment.setClassLoader(null);
   }
   
   /**
    * DeploymentClassLoaderMetaData.
    */
   private class DeploymentClassLoaderMetaData extends AbstractClassLoaderMetaData
   {
      /** The serialVersionUID */
      private static final long serialVersionUID = 1L;
      
      /** The deployment unit */
      private DeploymentUnit unit;

      /**
       * Create a new DeploymentClassLoaderMetaData.
       * 
       * @param unit the deployment unit
       */
      public DeploymentClassLoaderMetaData(DeploymentUnit unit)
      {
         if (unit == null)
            throw new IllegalArgumentException("Null unit");
         this.unit = unit;
      }
      
      @Override
      public ValueMetaData getClassLoader()
      {
         return new AbstractValueMetaData(unit.getClassLoader());
      }
   }
}
