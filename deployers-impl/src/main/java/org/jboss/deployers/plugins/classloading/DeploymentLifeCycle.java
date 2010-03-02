/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Inc., and individual contributors
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

import java.util.LinkedHashSet;
import java.util.Set;

import org.jboss.classloading.spi.dependency.LifeCycle;
import org.jboss.deployers.client.spi.DeployerClientChangeExt;
import org.jboss.deployers.client.spi.main.MainDeployer;
import org.jboss.deployers.spi.deployer.DeploymentStages;

/**
 * Our lifecycle
 */
public class DeploymentLifeCycle extends LifeCycle
{
   /**
    * Get deployment lifecycles for the lifecycles
    * 
    * @param lifecycles the lifecycles
    * @return the lifecycles or null if there are no lifecycles or some of the lifecycles are not for deployments
    */
   protected static Set<DeploymentLifeCycle> getDeploymentLifeCycles(LifeCycle[] lifecycles)
   {
      if (lifecycles == null || lifecycles.length == 0)
         return null;
      
      Set<DeploymentLifeCycle> result = new LinkedHashSet<DeploymentLifeCycle>(lifecycles.length);
      for (LifeCycle lifecycle : lifecycles)
      {
         if (lifecycle == null)
            throw new IllegalArgumentException("Null lifecycle");
         if (lifecycle instanceof DeploymentLifeCycle)
            return null;
         result.add((DeploymentLifeCycle) lifecycle);
      }
      return result;
   }

   /**
    * Create a new DeploymentLifeCycle
    *
    * @param module the module
    */
   public DeploymentLifeCycle(AbstractDeploymentClassLoaderPolicyModule module)
   {
      super(module);
   }

   @Override
   public AbstractDeploymentClassLoaderPolicyModule getModule()
   {
      return (AbstractDeploymentClassLoaderPolicyModule) super.getModule();
   }

   @Override
   public void resolve() throws Exception
   {
      if (isResolved() == false)
         getMainDeployer().change(getModule().getDeploymentUnit().getName(), DeploymentStages.CLASSLOADER);
   }

   @Override
   public void resolve(LifeCycle... lifecycles) throws Exception
   {
      DeployerClientChangeExt changer = getChanger();
      Set<DeploymentLifeCycle> deploymentLifeCycles = null;
      if (changer != null)
         deploymentLifeCycles = getDeploymentLifeCycles(lifecycles);
      if (deploymentLifeCycles == null)
      {
         super.resolve(lifecycles);
         return;
      }
      Set<String> names = new LinkedHashSet<String>(lifecycles.length);
      for (DeploymentLifeCycle lifeCycle : deploymentLifeCycles)
      {
         if (lifeCycle.isResolved() == false)
            names.add(lifeCycle.getModule().getDeploymentUnit().getName());
      }
      if (names.isEmpty() == false)
         changer.change(DeploymentStages.CLASSLOADER, false, names.toArray(new String[names.size()]));
   }

   @Override
   public void unresolve() throws Exception
   {
      if (isResolved())
         getMainDeployer().change(getModule().getDeploymentUnit().getName(), DeploymentStages.DESCRIBE);
   }

   @Override
   public void unresolve(LifeCycle... lifecycles) throws Exception
   {
      DeployerClientChangeExt changer = getChanger();
      Set<DeploymentLifeCycle> deploymentLifeCycles = null;
      if (changer != null)
         deploymentLifeCycles = getDeploymentLifeCycles(lifecycles);
      if (deploymentLifeCycles == null)
      {
         super.unresolve(lifecycles);
         return;
      }
      Set<String> names = new LinkedHashSet<String>(lifecycles.length);
      for (DeploymentLifeCycle lifeCycle : deploymentLifeCycles)
      {
         if (lifeCycle.isResolved())
            names.add(lifeCycle.getModule().getDeploymentUnit().getName());
      }
      if (names.isEmpty() == false)
         changer.change(DeploymentStages.DESCRIBE, false, names.toArray(new String[names.size()]));
   }

   @Override
   public void bounce() throws Exception
   {
      DeployerClientChangeExt changer = getChanger();
      if (changer == null)
         throw new IllegalStateException("Don't know how to bounce " + getModule().getDeploymentUnit().getName());
      changer.bounce(DeploymentStages.DESCRIBE, false, getModule().getDeploymentUnit().getName());
   }

   @Override
   public void bounce(LifeCycle... lifecycles) throws Exception
   {
      DeployerClientChangeExt changer = getChanger();
      Set<DeploymentLifeCycle> deploymentLifeCycles = null;
      if (changer != null)
         deploymentLifeCycles = getDeploymentLifeCycles(lifecycles);
      if (deploymentLifeCycles == null)
      {
         super.bounce(lifecycles);
         return;
      }
      Set<String> names = new LinkedHashSet<String>(lifecycles.length);
      for (DeploymentLifeCycle lifeCycle : deploymentLifeCycles)
         names.add(lifeCycle.getModule().getDeploymentUnit().getName());
      changer.change(DeploymentStages.DESCRIBE, false, names.toArray(new String[names.size()]));
   }

   @Override
   public void start() throws Exception
   {
      if (isStarted() == false)
         getMainDeployer().change(getModule().getDeploymentUnit().getName(), DeploymentStages.INSTALLED);
   }

   @Override
   public void start(LifeCycle... lifecycles) throws Exception
   {
      DeployerClientChangeExt changer = getChanger();
      Set<DeploymentLifeCycle> deploymentLifeCycles = null;
      if (changer != null)
         deploymentLifeCycles = getDeploymentLifeCycles(lifecycles);
      if (deploymentLifeCycles == null)
      {
         super.start(lifecycles);
         return;
      }
      Set<String> names = new LinkedHashSet<String>(lifecycles.length);
      for (DeploymentLifeCycle lifeCycle : deploymentLifeCycles)
      {
         if (lifeCycle.isStarted() == false)
            names.add(lifeCycle.getModule().getDeploymentUnit().getName());
      }
      if (names.isEmpty() == false)
         changer.change(DeploymentStages.INSTALLED, false, names.toArray(new String[names.size()]));
   }

   @Override
   public void stop() throws Exception
   {
      if (isResolved())
         getMainDeployer().change(getModule().getDeploymentUnit().getName(), DeploymentStages.CLASSLOADER);
   }

   @Override
   public void stop(LifeCycle... lifecycles) throws Exception
   {
      DeployerClientChangeExt changer = getChanger();
      Set<DeploymentLifeCycle> deploymentLifeCycles = null;
      if (changer != null)
         deploymentLifeCycles = getDeploymentLifeCycles(lifecycles);
      if (deploymentLifeCycles == null)
      {
         super.stop(lifecycles);
         return;
      }
      Set<String> names = new LinkedHashSet<String>(lifecycles.length);
      for (DeploymentLifeCycle lifeCycle : deploymentLifeCycles)
      {
         if (lifeCycle.isResolved())
            names.add(lifeCycle.getModule().getDeploymentUnit().getName());
      }
      if (names.isEmpty() == false)
         changer.change(DeploymentStages.CLASSLOADER, false, names.toArray(new String[names.size()]));
   }
   
   /**
    * Get the main deployer 
    * 
    * @return the main deployer
    */
   protected MainDeployer getMainDeployer()
   {
      MainDeployer result = getModule().getDeploymentUnit().getAttachment(MainDeployer.class);
      if (result == null)
         throw new IllegalStateException("Unable to access main deployer");
      return result;
   }
   
   /**
    * Get the change extension
    * 
    * @return the changer or null if one isn't available
    */
   protected DeployerClientChangeExt getChanger()
   {
      MainDeployer main = getMainDeployer();
      if (main instanceof DeployerClientChangeExt)
         return (DeployerClientChangeExt) main;
      return null;
   }
}