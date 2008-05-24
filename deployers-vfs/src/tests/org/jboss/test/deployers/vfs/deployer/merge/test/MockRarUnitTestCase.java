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
package org.jboss.test.deployers.vfs.deployer.merge.test;

import junit.framework.Test;
import org.jboss.test.deployers.vfs.deployer.AbstractDeployerUnitTest;
import org.jboss.test.deployers.vfs.deployer.merge.support.MockRarDeployer;
import org.jboss.test.deployers.vfs.deployer.merge.support.RarDeploymentDeployer;
import org.jboss.kernel.Kernel;
import org.jboss.deployers.vfs.deployer.kernel.KernelDeploymentDeployer;
import org.jboss.deployers.vfs.deployer.kernel.BeanMetaDataDeployer;
import org.jboss.deployers.vfs.spi.client.VFSDeployment;
import org.jboss.beans.metadata.plugins.AbstractBeanMetaData;
import org.jboss.dependency.spi.ControllerContext;

/**
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class MockRarUnitTestCase extends AbstractDeployerUnitTest
{
   public MockRarUnitTestCase(String name) throws Throwable
   {
      super(name);
   }

   public static Test suite()
   {
      return suite(MockRarUnitTestCase.class);
   }

   protected void addDeployers(Kernel kernel)
   {
      MockRarDeployer rard = new MockRarDeployer();
      try
      {
         controller.install(new AbstractBeanMetaData("rard", MockRarDeployer.class.getName()), rard);
      }
      catch (Throwable t)
      {
         throw new RuntimeException(t);
      }
      addDeployer(main, rard);
      addDeployer(main, new RarDeploymentDeployer());
      addDeployer(main, new KernelDeploymentDeployer());
      addDeployer(main, new BeanMetaDataDeployer(kernel));
   }

   public void testRarMerge() throws Exception
   {
      VFSDeployment deployment = createDeployment("/bean", "multiple/allrar.jar");
      assertDeploy(deployment);
      ControllerContext context = controller.getInstalledContext("Test");
      assertNotNull(context);
      Object target = context.getTarget();
      assertNotNull(target);
      assertEquals(Object.class, target.getClass());

      assertUndeploy(deployment);
      assertNull(controller.getContext("Test", null));
   }
}
