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
package org.jboss.test.deployers.vfs.deployer.bean.support;

import java.util.Collections;

import org.jboss.beans.metadata.plugins.AbstractBeanMetaData;
import org.jboss.beans.metadata.spi.BeanMetaDataFactory;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.kernel.plugins.deployment.AbstractKernelDeployment;

/**
 * TestBeanDeployer.<p>
 * 
 * This just creates some metadata for use by the KernelDeployer
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class TestBeanDeployer extends AbstractDeployer
{
   public TestBeanDeployer()
   {
      setStage(DeploymentStages.PARSE);
   }

   public void deploy(DeploymentUnit unit) throws DeploymentException
   {

      AbstractKernelDeployment deployment = new AbstractKernelDeployment();
      deployment.setName("KernelDeployerTest");
      
      BeanMetaDataFactory metaData = new AbstractBeanMetaData("Test", Simple.class.getName());
      deployment.setBeanFactories(Collections.singletonList(metaData));
      
      unit.getTransientManagedObjects().addAttachment("KernelDeployerTest", deployment);
   }
}
