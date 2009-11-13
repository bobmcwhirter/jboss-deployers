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
package org.jboss.deployers.plugins.deployers;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.Deployer;
import org.jboss.deployers.spi.deployer.DeploymentStage;
import org.jboss.deployers.spi.deployer.managed.ManagedObjectCreator;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.structure.spi.helpers.DeployerTracking;
import org.jboss.logging.Logger;
import org.jboss.managed.api.ManagedObject;

/**
 * DeployerWrapper.<p>
 * 
 * To avoid any problems with error handling by the deployers.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class DeployerWrapper implements Deployer, ManagedObjectCreator
{
   /** The log */
   private Logger log; 
   
   /** The deployer */
   private Deployer deployer;

   /** The managed object creator */
   private ManagedObjectCreator managedObjectCreator;
 
   /** The context classloader of the person registering the deployer */
   private ClassLoader classLoader;
   
   /**
    * Create a new DeployerWrapper.
    * 
    * @param deployer the deployer
    */
   public DeployerWrapper(Deployer deployer)
   {
      if (deployer == null)
         throw new IllegalArgumentException("Null deployer");
      this.deployer = deployer;
      if (deployer instanceof ManagedObjectCreator)
         this.managedObjectCreator = (ManagedObjectCreator) deployer;
      this.log = Logger.getLogger(deployer.getClass());
      this.classLoader = SecurityActions.getContextClassLoader();
   }
   
   /**
    * Get the managedObjectCreator.
    * 
    * @return the managedObjectCreator.
    */
   public ManagedObjectCreator getManagedObjectCreator()
   {
      return managedObjectCreator;
   }

   /**
    * Set the managedObjectCreator.
    * 
    * @param managedObjectCreator the managedObjectCreator.
    */
   public void setManagedObjectCreator(ManagedObjectCreator managedObjectCreator)
   {
      this.managedObjectCreator = managedObjectCreator;
   }

   public int getRelativeOrder()
   {
      return deployer.getRelativeOrder();
   }

   public void setRelativeOrder(int order)
   {
      deployer.setRelativeOrder(order);
   }

   public boolean isAllInputs()
   {
      return deployer.isAllInputs();
   }

   public boolean isComponentsOnly()
   {
      return deployer.isComponentsOnly();
   }

   public boolean isWantComponents()
   {
      return deployer.isWantComponents();
   }

   public boolean isTopLevelOnly()
   {
      return deployer.isTopLevelOnly();
   }

   public Class<?> getInput()
   {
      return deployer.getInput();
   }

   public Class<?> getOutput()
   {
      return deployer.getOutput();
   }

   public Set<String> getInputs()
   {
      Set<String> result = deployer.getInputs();
      if (result == null)
         return Collections.emptySet();
      return result;
   }

   public Set<String> getRequiredInputs()
   {
      Set<String> result = deployer.getRequiredInputs();
      if (result == null)
         return Collections.emptySet();
      return result;
   }

   public Set<String> getOutputs()
   {
      Set<String> result = deployer.getOutputs();
      if (result == null)
         return Collections.emptySet();
      return result;
   }

   public DeploymentStage getStage()
   {
      return deployer.getStage();
   }

   public boolean isParentFirst()
   {
      return deployer.isParentFirst();
   }

   public void deploy(DeploymentUnit unit) throws DeploymentException
   {
      if (unit == null)
         throw new IllegalArgumentException("Null unit");

      DeployerTracking.push(deployer.toString());
      ClassLoader previous = SecurityActions.setContextClassLoader(classLoader);
      try
      {
         log.trace("Deploying: " + unit.getName());
         deployer.deploy(unit);
         log.trace("Deployed:  " + unit.getName());
      }
      catch (Throwable t)
      {
         log.debug("Error during deploy: " + unit.getName(), t);
         throw DeploymentException.rethrowAsDeploymentException("Error during deploy: " + unit.getName(), t);
      }
      finally
      {
         DeployerTracking.pop();
         SecurityActions.resetContextClassLoader(previous);
      }
   }

   public void undeploy(DeploymentUnit unit)
   {
      if (unit == null)
         throw new IllegalArgumentException("Null unit");

      DeployerTracking.push(deployer.toString());
      ClassLoader previous = SecurityActions.setContextClassLoader(classLoader);
      try
      {
         log.trace("Undeploying: " + unit.getName());
         deployer.undeploy(unit);
         log.trace("Undeployed:  " + unit.getName());
      }
      catch (Throwable t)
      {
         log.error("Error during undeploy: " + unit.getName(), t);
      }
      finally
      {
         DeployerTracking.pop();
         SecurityActions.resetContextClassLoader(previous);
      }
   }

   public void build(DeploymentUnit unit, Set<String> outputs,
      Map<String, ManagedObject> managedObjects)
      throws DeploymentException
   {
      ClassLoader previous = SecurityActions.setContextClassLoader(classLoader);
      try
      {
         ManagedObjectCreator creator = getManagedObjectCreator();
         if (creator != null)
         {
            creator.build(unit, outputs, managedObjects);
         }
      }
      catch (Throwable t)
      {
         throw DeploymentException.rethrowAsDeploymentException("Error building managed objects for " + unit.getName(), t);
      }
      finally
      {
         SecurityActions.resetContextClassLoader(previous);
      }
   }

   @Override
   public boolean equals(Object obj)
   {
      if (obj == this)
         return true;
      if (obj == null || obj instanceof Deployer == false)
         return false;
      if (obj instanceof DeployerWrapper)
         obj = ((DeployerWrapper) obj).deployer;
      return deployer.equals(obj);
   }
   
   @Override
   public int hashCode()
   {
      return deployer.hashCode();
   }
   
   @Override
   public String toString()
   {
      return deployer.toString();
   }
}
