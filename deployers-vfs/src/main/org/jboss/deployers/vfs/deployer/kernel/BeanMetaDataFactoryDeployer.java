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
import org.jboss.beans.metadata.spi.BeanMetaDataFactory;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.helpers.AbstractSimpleRealDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;

/**
 * BeanMetaDataFactoryDeployer.<p>
 *
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 * @param <T> exact attachment type
 */
public class BeanMetaDataFactoryDeployer<T extends BeanMetaDataFactory> extends AbstractSimpleRealDeployer<T>
{
   public BeanMetaDataFactoryDeployer(Class<T> clazz)
   {
      super(clazz);
      setOutput(BeanMetaData.class);
   }

   public void deploy(DeploymentUnit unit, T deployment) throws DeploymentException
   {
      List<BeanMetaData> beans = deployment.getBeans();
      if (beans != null && beans.isEmpty() == false)
      {
         for (BeanMetaData bean : beans)
            KernelDeploymentDeployer.addBeanComponent(unit, bean);
      }
   }

   public void undeploy(DeploymentUnit unit, T deployment)
   {
      List<BeanMetaData> beans = deployment.getBeans();
      if (beans != null && beans.isEmpty() == false)
      {
         for (BeanMetaData bean : beans)
            KernelDeploymentDeployer.removeBeanComponent(unit, bean);
      }
   }
}