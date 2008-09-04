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
import java.util.List;

import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.helpers.DeploymentVisitor;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.logging.Logger;

/**
 * BeanMetaDataVisitor.<p>
 *
 * @param <T> exact attachment type
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 */
public abstract class BeanMetaDataFactoryVisitor<T> implements DeploymentVisitor<T>
{
   private Logger log = Logger.getLogger(getClass());

   /**
    * Add bean component.
    *
    * @param unit the deployment unit
    * @param bean the bean metadata
    */
   protected static void addBeanComponent(DeploymentUnit unit, BeanMetaData bean)
   {
      DeploymentUnit component = unit.addComponent(bean.getName());
      component.addAttachment(BeanMetaData.class.getName(), bean);
   }

   /**
    * Remove bean component.
    *
    * @param unit the deployment unit
    * @param bean the bean metadata
    */

   protected static void removeBeanComponent(DeploymentUnit unit, BeanMetaData bean)
   {
      unit.removeComponent(bean.getName());
   }

   /**
    * Ignore all error during component removal.
    *
    * @param unit the deployment unit
    * @param bean the bean metadata
    */
   protected void safeRemoveBeanComponent(DeploymentUnit unit, BeanMetaData bean)
   {
      try
      {
         removeBeanComponent(unit, bean);
      }
      catch (Throwable ignored)
      {
         log.warn("Error during component removal: " + unit.getName(), ignored);
      }
   }

   /**
    * Get beans from deployment.
    *
    * @param deployment the deployment
    * @return list of beans
    */
   protected abstract List<BeanMetaData> getBeans(T deployment);

   public void deploy(DeploymentUnit unit, T deployment) throws DeploymentException
   {
      List<BeanMetaData> beans = getBeans(deployment);
      if (beans != null && beans.isEmpty() == false)
      {
         List<BeanMetaData> visited = new ArrayList<BeanMetaData>();
         try
         {
            for (BeanMetaData bean : beans)
            {
               addBeanComponent(unit, bean);
               visited.add(bean);
            }
         }
         catch (Throwable t)
         {
            for (int i = visited.size()-1; i >= 0; --i)
            {
               safeRemoveBeanComponent(unit, visited.get(i));
            }
            throw DeploymentException.rethrowAsDeploymentException("Error deploying: " + unit.getName(), t);
         }
      }
   }

   public void undeploy(DeploymentUnit unit, T deployment)
   {
      List<BeanMetaData> beans = getBeans(deployment);
      if (beans != null && beans.isEmpty() == false)
      {
         for (BeanMetaData bean : beans)
         {
            safeRemoveBeanComponent(unit, bean);
         }
      }
   }
}
