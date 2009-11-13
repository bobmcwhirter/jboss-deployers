/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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

import java.util.Set;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.util.collection.CollectionsFactory;

/**
 * Check all required inputs.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class AbstractAllInputDeployer extends AbstractDeployer
{
   /** Optional inputs */
   private Set<String> optionalInputs = CollectionsFactory.createLazySet();

   /**
    * Do we have all required inputs.
    *
    * @param unit the deployment unit
    * @return true if all inputs exist
    */
   protected boolean hasAllRequiredInputs(DeploymentUnit unit)
   {
      Set<String> inputs = getInputs();
      if (inputs != null && inputs.isEmpty() == false)
      {
         for (String input : inputs)
         {
            if ((optionalInputs == null || optionalInputs.contains(input) == false) && unit.isAttachmentPresent(input) == false)
            {
               return false;
            }
         }
      }
      return true;
   }

   public final void deploy(DeploymentUnit unit) throws DeploymentException
   {
      if (hasAllRequiredInputs(unit))
      {
         internalDeploy(unit);
      }
   }

   /**
    * Deploy a deployment
    *
    * @param unit the unit
    * @throws DeploymentException for any error
    */
   protected abstract void internalDeploy(DeploymentUnit unit) throws DeploymentException;

   public final void undeploy(DeploymentUnit unit)
   {
      if (hasAllRequiredInputs(unit))
      {
         internalUndeploy(unit);
      }
   }

   /**
    * Undeploy an deployment
    *
    * @param unit the unit
    */
   protected void internalUndeploy(DeploymentUnit unit)
   {
      // nothing
   }

   /**
    * Add optional input.
    *
    * @param input the input
    */
   public void addOptionalInput(Class<?> input)
   {
      if (input == null)
         throw new IllegalArgumentException("Null input");

      addOptionalInput(input.getName());
   }

   /**
    * Add optional input.
    *
    * @param input the input
    */
   public void addOptionalInput(String input)
   {
      addInput(input); // this already checks for null

      if (optionalInputs == null)
         optionalInputs = CollectionsFactory.createLazySet();

      optionalInputs.add(input);
   }

   /**
    * Set optional inputs.
    *
    * @param optionalInputs the optional inputs
    */
   public void setOptionalInputs(Set<String> optionalInputs)
   {
      if (optionalInputs != null)
      {
         for (String input : optionalInputs)
            addOptionalInput(input);
      }
      else
      {
         this.optionalInputs = optionalInputs;
      }
   }
}
