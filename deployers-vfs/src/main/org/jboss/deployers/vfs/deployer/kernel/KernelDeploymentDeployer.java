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

import java.util.List;

import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.helpers.AbstractComponentDeployer;
import org.jboss.deployers.spi.deployer.helpers.DeploymentVisitor;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.kernel.spi.deployment.KernelDeployment;
import org.jboss.logging.Logger;

public class KernelDeploymentDeployer extends AbstractComponentDeployer<KernelDeployment, BeanMetaData>
{
   private static Logger log = Logger.getLogger(KernelDeploymentDeployer.class);
   /**
    * Create a new KernelDeploymentDeployer.
    */
   public KernelDeploymentDeployer()
   {
      setDeploymentVisitor(new KernelDeploymentVisitor());
      setComponentVisitor(new BeanMetaDataVisitor());
   }

   protected static void addBeanComponent(DeploymentUnit unit, BeanMetaData bean)
   {
      DeploymentUnit component = unit.addComponent(bean.getName());
      component.addAttachment(BeanMetaData.class.getName(), bean);
      log.debug("addBeanComponent, unit="+unit.getSimpleName()+", bean="+bean.getName());
   }

   protected static void removeBeanComponent(DeploymentUnit unit, BeanMetaData bean)
   {
      unit.removeComponent(bean.getName());
   }
   
   /**
    * KernelDeploymentVisitor.
    */
   public static class KernelDeploymentVisitor implements DeploymentVisitor<KernelDeployment>
   {
      public Class<KernelDeployment> getVisitorType()
      {
         return KernelDeployment.class;
      }

      public void deploy(DeploymentUnit unit, KernelDeployment deployment) throws DeploymentException
      {
         List<BeanMetaData> beans = deployment.getBeans();
         if (beans != null && beans.isEmpty() == false)
         {
            for (BeanMetaData bean : beans)
               addBeanComponent(unit, bean);
         }
      }

      public void undeploy(DeploymentUnit unit, KernelDeployment deployment)
      {
         List<BeanMetaData> beans = deployment.getBeans();
         if (beans != null && beans.isEmpty() == false)
         {
            for (BeanMetaData bean : beans)
               removeBeanComponent(unit, bean);
         }
      }
   }

   /**
    * BeanMetaDataVisitor.
    */
   public static class BeanMetaDataVisitor implements DeploymentVisitor<BeanMetaData>
   {
      public Class<BeanMetaData> getVisitorType()
      {
         return BeanMetaData.class;
      }

      public void deploy(DeploymentUnit unit, BeanMetaData deployment) throws DeploymentException
      {
         addBeanComponent(unit, deployment);
      }

      public void undeploy(DeploymentUnit unit, BeanMetaData deployment)
      {
         removeBeanComponent(unit, deployment);
      }
   }
}
