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
package org.jboss.test.deployers.vfs.deployer.bean.test;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.deployer.kernel.BeanDeployer;
import org.jboss.deployers.vfs.deployer.kernel.BeanMetaDataDeployer;
import org.jboss.deployers.vfs.deployer.kernel.KernelDeploymentDeployer;
import org.jboss.deployers.vfs.spi.client.VFSDeployment;
import org.jboss.kernel.Kernel;
import org.jboss.test.deployers.vfs.deployer.AbstractDeployerUnitTest;
import org.jboss.test.deployers.vfs.deployer.bean.support.Simple;
import org.jboss.test.deployers.vfs.deployer.bean.support.TestClassLoaderDeployer;

/**
 * BeanDeployerUnitTestCase.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class BeanDeployerClassLoaderUnitTestCase extends AbstractDeployerUnitTest
{
   public static Test suite()
   {
      return new TestSuite(BeanDeployerClassLoaderUnitTestCase.class);
   }

   public BeanDeployerClassLoaderUnitTestCase(String name) throws Throwable
   {
      super(name);
   }

   protected void addDeployers(Kernel kernel)
   {
      BeanDeployer beanDeployer = new BeanDeployer();
      TestClassLoaderDeployer classLoaderDeployer = new TestClassLoaderDeployer();
      KernelDeploymentDeployer kernelDeploymentDeployer = new KernelDeploymentDeployer();
      BeanMetaDataDeployer beanMetaDataDeployer = new BeanMetaDataDeployer(kernel);
      addDeployer(main, beanDeployer);
      addDeployer(main, classLoaderDeployer);
      addDeployer(main, kernelDeploymentDeployer);
      addDeployer(main, beanMetaDataDeployer);
   }

   public void testClassLoader() throws Exception
   {
      VFSDeployment context = createDeployment("/bean", "toplevel/my-beans.xml");
      assertDeploy(context);
      try
      {
         assertNotNull(controller.getInstalledContext("Test"));
         DeploymentUnit unit = assertDeploymentUnit(main, context.getName());
         assertEquals(unit.getClassLoader(), Simple.getAndResetClassLoader());
      }
      finally
      {
         assertUndeploy(context);
      }
   }
}
