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
package org.jboss.deployers.spi.deployer.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.structure.spi.DeploymentUnit;

/**
 * AbstractRealDeployerWithInput.
 * 
 * @param <T> the type of the input
 * @author <a href="adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public abstract class AbstractRealDeployerWithInput<T> extends AbstractRealDeployer
{
   /** The visitor */
   private DeploymentVisitor<T> visitor;

   /** Whether the warning has been displayed */
   private boolean warned;

   /**
    * Create a new AbstractRealDeployerWithInput.
    */
   public AbstractRealDeployerWithInput()
   {
   }

   /**
    * Create a new AbstractRealDeployerWithInput.
    * 
    * @param input the input
    */
   public AbstractRealDeployerWithInput(Class<T> input)
   {
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

   /**
    * Set the deployment visitor
    * 
    * @param visitor the visitor
    * @throws IllegalArgumentException if the visitor is null
    */
   protected void setDeploymentVisitor(DeploymentVisitor<T> visitor)
   {
      if (visitor == null)
         throw new IllegalArgumentException("Null visitor");
      this.visitor = visitor;
      Class<T> input = visitor.getVisitorType();
      if (input == null)
         throw new IllegalArgumentException("Null visitor type");
      setInput(input);
   }

   @SuppressWarnings("unchecked")
   public void internalDeploy(DeploymentUnit unit) throws DeploymentException
   {
      if (visitor == null)
      {
         if (warned == false)
         {
            log.error("INTERNAL ERROR: Visitor is null for " + getClass().getName());
            warned = true;
         }
         return;
      }

      List<T> visited = new ArrayList();
      try
      {
         Set<? extends T> deployments = unit.getAllMetaData(getInput());
         for (T deployment : deployments)
         {
            visitor.deploy(unit, deployment);
            visited.add(deployment);
         }
      }
      catch (Throwable t)
      {
         for (int i = visited.size()-1; i >= 0; --i)
         {
            try
            {
               visitor.undeploy(unit, visited.get(i));
            }
            catch (Throwable ignored)
            {
               log.warn("Error during undeploy: " + unit.getName(), ignored);
            }
         }
         throw DeploymentException.rethrowAsDeploymentException("Error deploying: " + unit.getName(), t);
      }
   }

   public void internalUndeploy(DeploymentUnit unit)
   {
      if (visitor == null)
         return;
      Set<? extends T> deployments = unit.getAllMetaData(getInput());
      for (T deployment : deployments)
         visitor.undeploy(unit, deployment);
   }
}
