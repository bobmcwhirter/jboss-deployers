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

import java.util.List;
import java.util.ArrayList;

import org.jboss.logging.Logger;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.spi.DeploymentException;

/**
 * Simple deployment visitor.
 *
 * @param <C> exact component type
 * @param <T> exact deployment type
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public abstract class AbstractDeploymentVisitor<C, T> extends ComponentAdapter<C> implements DeploymentVisitor<T>
{
   private Logger log = Logger.getLogger(getClass());

   protected String getAttachmentName(C attachment)
   {
      return getComponentType().getName();
   }

   /**
    * Get components from deployment.
    *
    * @param deployment the deployment
    * @return list of components
    */
   // TODO - change to Iterable   
   protected abstract List<? extends C> getComponents(T deployment);

   /**
    * Get the component type.
    *
    * @return the component type
    */
   protected abstract Class<C> getComponentType();

   public void deploy(DeploymentUnit unit, T deployment) throws DeploymentException
   {
      List<? extends C> components = getComponents(deployment);
      if (components != null && components.isEmpty() == false)
      {
         List<C> visited = new ArrayList<C>();
         try
         {
            for (C component : components)
            {
               addComponent(unit, component);
               visited.add(component);
            }
         }
         catch (Throwable t)
         {
            for (int i = visited.size() - 1; i >= 0; i--)
            {
               safeRemoveComponent(unit, visited.get(i));
            }
            throw DeploymentException.rethrowAsDeploymentException("Error deploying: " + unit.getName(), t);
         }
      }
   }

   public void undeploy(DeploymentUnit unit, T deployment)
   {
      List<? extends C> components = getComponents(deployment);
      if (components != null && components.isEmpty() == false)
      {
         for (C component : components)
         {
            safeRemoveComponent(unit, component);
         }
      }
   }

   /**
    * Ignore all error during component removal.
    *
    * @param unit the deployment unit
    * @param attachment the attachment
    */
   protected void safeRemoveComponent(DeploymentUnit unit, C attachment)
   {
      try
      {
         removeComponent(unit, attachment);
      }
      catch (Throwable ignored)
      {
         log.warn("Error during component removal: " + unit.getName(), ignored);
      }
   }
}