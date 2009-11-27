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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.jboss.beans.metadata.plugins.AbstractClassLoaderMetaData;
import org.jboss.beans.metadata.plugins.AbstractValueMetaData;
import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.ClassLoaderMetaData;
import org.jboss.beans.metadata.spi.ValueMetaData;
import org.jboss.dependency.spi.Controller;
import org.jboss.dependency.spi.ScopeInfo;
import org.jboss.dependency.spi.ControllerContext;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.helpers.AbstractSimpleRealDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.deployer.helpers.BeanMetaDataDeployerPlugin;
import org.jboss.kernel.Kernel;
import org.jboss.kernel.plugins.dependency.AbstractKernelControllerContext;
import org.jboss.kernel.spi.dependency.KernelControllerContext;

/**
 * BeanMetaDataDeployer.<p>
 * 
 * This deployer is responsible for deploying all metadata of
 * type {@link org.jboss.beans.metadata.spi.BeanMetaData}.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
public class BeanMetaDataDeployer extends AbstractSimpleRealDeployer<BeanMetaData>
{
   /** The controller */
   private Controller controller;
   
   /** List of bean metadata plugins */
   private List<BeanMetaDataDeployerPlugin> plugins = new ArrayList<BeanMetaDataDeployerPlugin>();
   
   private ReadWriteLock lock = new ReentrantReadWriteLock();

   /** Records which plugin was used to deploy a context */
   private Map<String, BeanMetaDataDeployerPlugin> deployedWithPlugin = new ConcurrentHashMap<String, BeanMetaDataDeployerPlugin>();

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
   public void addControllerContextCreator(BeanMetaDataDeployerPlugin creator)
   {
      if (creator == null)
         return;
      
      lock.writeLock().lock();
      try
      {
         plugins.add(creator);
         Collections.sort(plugins, KernelControllerContextComparator.getInstance());
      }
      finally
      {
         lock.writeLock().unlock();
      }
   }
   
   /**
    * Uncallback for the controller context creators
    * 
    * @param creator The controller context creator to be removed
    */
   public void removeControllerContextCreator(BeanMetaDataDeployerPlugin creator)
   {
      if (creator == null)
         return;
      
      lock.writeLock().lock();
      try
      {
         plugins.remove(creator);
      }
      finally
      {
         lock.writeLock().unlock();
      }
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
         putContext(context, unit);
      }
      catch (Throwable t)
      {
         throw DeploymentException.rethrowAsDeploymentException("Error deploying: " + deployment.getName(), t);
      }
   }

   /**
    * Creates a kernel controller context using the controller context creators in controllerContextCreators.
    * The first controller context creator that returns a context is used. If no matching controller context
    * creator is found, a plain KernelControllerContext is created.
    * @param unit The deployment unit
    * @param deployment The bean metadata being deployed
    * @return the created KernelControllerContext
    */
   protected KernelControllerContext createControllerContext(DeploymentUnit unit, BeanMetaData deployment)
   {
      if (plugins.size() > 0)
      {
         lock.readLock().lock();
         try
         {
            for (BeanMetaDataDeployerPlugin plugin : plugins)
            {
               KernelControllerContext context = plugin.createContext(controller, unit, deployment);
               if (context != null)
               {
                  deployedWithPlugin.put(deployment.getName(), plugin);
                  return context;
               }
            }
         }
         finally
         {
            lock.readLock().unlock();
         }
      }
      return new AbstractKernelControllerContext(null, deployment, null);
   }

   @Override
   public void undeploy(DeploymentUnit unit, BeanMetaData deployment)
   {
      BeanMetaDataDeployerPlugin plugin = deployedWithPlugin.remove(deployment.getName());
      if (plugin == null || plugin.uninstallContext(controller, unit, deployment) == false)
      {
         ControllerContext context = controller.uninstall(deployment.getName());
         removeContext(context, unit);
      }
      
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
