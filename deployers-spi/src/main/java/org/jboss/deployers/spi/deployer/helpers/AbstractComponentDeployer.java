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
package org.jboss.deployers.spi.deployer.helpers;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.structure.spi.DeploymentUnit;

/**
 * AbstractComponentDeployer.
 * 
 * @param <D> the deployment type
 * @param <C> the component type
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 * @version $Revision: 1.1 $
 */
public abstract class AbstractComponentDeployer<D, C> extends AbstractRealDeployerWithInput<D>
{
   /** The component visitor */
   protected DeploymentVisitor<C> compVisitor;

   /**
    * Create a new AbstractComponentDeployer.
    */
   public AbstractComponentDeployer()
   {
      setAllInputs(true);
   }

   @SuppressWarnings("unchecked")
   public Class<? extends C> getOutput()
   {
      Class<?> output = super.getOutput();
      if (output == null)
         throw new IllegalStateException("No output for " + this);
      return (Class<? extends C>) output;
   }

   /**
    * Set the component visitor
    * 
    * @param visitor the visitor
    * @throws IllegalArgumentException if the visitor is null
    */
   protected void setComponentVisitor(DeploymentVisitor<C> visitor)
   {
      if (visitor == null)
         throw new IllegalArgumentException("Null visitor");
      this.compVisitor = visitor;
      Class<C> componentType = visitor.getVisitorType();
      if (componentType == null)
         throw new IllegalArgumentException("Null visitor type");
      setOutput(componentType);
      addInput(componentType);
   }

   public void internalDeploy(DeploymentUnit unit) throws DeploymentException
   {
      super.internalDeploy(unit);

      try
      {
         deployComponents(unit);
      }
      catch (Throwable t)
      {
         super.internalUndeploy(unit);
         throw DeploymentException.rethrowAsDeploymentException("Error deploying: " + unit.getName(), t);
      }
   }
   
   public void internalUndeploy(DeploymentUnit unit)
   {
      undeployComponents(unit);
      super.internalUndeploy(unit);
   }

   protected void deployComponents(DeploymentUnit unit) throws DeploymentException
   {
      if (compVisitor == null)
         return;

      deploy(unit, compVisitor);
   }
   
   protected void undeployComponents(DeploymentUnit unit)
   {
      undeploy(unit, compVisitor);
   }
}
