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

import java.util.Collections;
import java.util.List;

import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.deployers.spi.deployer.helpers.AbstractComponentDeployer;
import org.jboss.kernel.spi.deployment.KernelDeployment;

public class KernelDeploymentDeployer extends AbstractComponentDeployer<KernelDeployment, BeanMetaData>
{
   /**
    * Create a new KernelDeploymentDeployer.
    */
   public KernelDeploymentDeployer()
   {
      setDeploymentVisitor(new KernelDeploymentVisitor());
      setComponentVisitor(new BeanMetaDataVisitor());
   }

   /**
    * KernelDeploymentVisitor.
    */
   public static class KernelDeploymentVisitor extends BeanMetaDataFactoryVisitor<KernelDeployment>
   {
      public Class<KernelDeployment> getVisitorType()
      {
         return KernelDeployment.class;
      }

      protected List<? extends BeanMetaData> getComponents(KernelDeployment deployment)
      {
         return deployment.getBeans();
      }
   }

   /**
    * BeanMetaDataVisitor.
    */
   public static class BeanMetaDataVisitor extends BeanMetaDataFactoryVisitor<BeanMetaData>
   {
      public Class<BeanMetaData> getVisitorType()
      {
         return BeanMetaData.class;
      }

      protected List<? extends BeanMetaData> getComponents(BeanMetaData deployment)
      {
         return Collections.singletonList(deployment);
      }
   }
}
