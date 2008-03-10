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
 * AbstractSimpleRealDeployer.
 * 
 * @param <T> the input type
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public abstract class AbstractSimpleRealDeployer<T> extends AbstractRealDeployer
{
   /**
    * Create a new AbstractSimpleRealDeployer
    *  
    * @param input the input type
    * @throws IllegalArgumentException for a null input
    */
   public AbstractSimpleRealDeployer(Class<T> input)
   {
      if (input == null)
         throw new IllegalArgumentException("Null input");
      setInput(input);
   }

   @SuppressWarnings("unchecked")
   public Class<? extends T> getInput()
   {
      Class<?> input = super.getInput();
      if (input == null)
         throw new IllegalStateException("No input for " + this);
      return (Class<? extends T>) input;
   }

   public void internalDeploy(DeploymentUnit unit) throws DeploymentException
   {
      T deployment = unit.getAttachment(getInput());
      if (deployment != null)
      {
         // Set the deployer type
         unit.getTypes().add(getType());
         deploy(unit, deployment);
      }
   }

   public void internalUndeploy(DeploymentUnit unit)
   {
      T deployment = unit.getAttachment(getInput());
      if (deployment != null)
         undeploy(unit, deployment);
   }
   
   public abstract void deploy(DeploymentUnit unit, T deployment) throws DeploymentException;

   public void undeploy(DeploymentUnit unit, T deployment)
   {
      // Nothing
   }
}
