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
package org.jboss.test.deployers.vfs.matchers.test;

import org.jboss.dependency.spi.ControllerContext;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.deployers.client.spi.DeployerClient;
import org.jboss.kernel.Kernel;
import org.jboss.kernel.plugins.bootstrap.basic.BasicBootstrap;
import org.jboss.kernel.spi.dependency.KernelController;
import org.jboss.test.deployers.BaseDeployersVFSTest;
import org.jboss.test.deployers.vfs.matchers.support.FeedbackDeployer;

/**
 * Holds a Kernel instance
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public abstract class KernelHolderDeployersTest extends BaseDeployersVFSTest
{
   private Kernel kernel;
   private KernelController controller;

   protected KernelHolderDeployersTest(String name)
   {
      super(name);
   }

   protected void setUp() throws Exception
   {
      super.setUp();

      BasicBootstrap bootstrap = new BasicBootstrap();
      bootstrap.run();

      kernel = bootstrap.getKernel();
      controller = kernel.getController();
   }

   protected KernelController getController()
   {
      return controller;
   }

   protected void tearDown() throws Exception
   {
      try
      {
         if (controller != null)
            controller.shutdown();

         controller = null;
         kernel = null;
      }
      finally
      {
         super.tearDown();
      }
   }

   protected FeedbackDeployer addDeployer(DeployerClient main, String name)
   {
      ControllerContext context = controller.getInstalledContext(name);
      assertNotNull("Missing deployer: " + name, context);
      assertEquals(ControllerState.INSTALLED, context.getState());
      Object target = context.getTarget();
      assertNotNull(target);
      assertInstanceOf(target, FeedbackDeployer.class);
      FeedbackDeployer deployer = (FeedbackDeployer)target;
      addDeployer(main, deployer);
      return deployer;
   }
}
