/*
* JBoss, Home of Professional Open Source.
* Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.deployers.vfs.deployer.bean.test;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.dependency.spi.ControllerContext;
import org.jboss.deployers.vfs.deployer.kernel.BeanDeployer;
import org.jboss.deployers.vfs.deployer.kernel.BeanMetaDataDeployer;
import org.jboss.deployers.vfs.deployer.kernel.KernelDeploymentDeployer;
import org.jboss.deployers.vfs.spi.client.VFSDeployment;
import org.jboss.kernel.Kernel;
import org.jboss.kernel.plugins.dependency.AbstractKernelControllerContext;
import org.jboss.test.deployers.support.TCCLClassLoaderDeployer;
import org.jboss.test.deployers.vfs.deployer.AbstractDeployerUnitTest;
import org.jboss.test.deployers.vfs.deployer.bean.support.NoopControllerContextCreator;
import org.jboss.test.deployers.vfs.deployer.bean.support.SpecialControllerContextCreator;
import org.jboss.test.deployers.vfs.deployer.bean.support.TriggerSpecialControllerContextDeployer;

/**
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
public class KernelControllerContextCreatorTestCase extends AbstractDeployerUnitTest
{
   BeanMetaDataDeployer beanMetaDataDeployer;
   
   public static Test suite()
   {
      return new TestSuite(KernelControllerContextCreatorTestCase.class);
   }

   public KernelControllerContextCreatorTestCase(String name) throws Throwable
   {
      super(name);
   }

   @Override
   protected void addDeployers(Kernel kernel)
   {
      BeanDeployer beanDeployer = new BeanDeployer();
      KernelDeploymentDeployer kernelDeploymentDeployer = new KernelDeploymentDeployer();
      beanMetaDataDeployer = new BeanMetaDataDeployer(kernel);
      addDeployer(main, new TCCLClassLoaderDeployer());
      addDeployer(main, beanDeployer);
      addDeployer(main, kernelDeploymentDeployer);
      addDeployer(main, beanMetaDataDeployer);
   }

   public void testStandardDeployment() throws Exception
   {
      VFSDeployment context = createDeployment("/bean", "toplevel/my-beans.xml");
      assertDeploy(context);
      ControllerContext cc = controller.getInstalledContext("Test");
      assertNotNull(cc);
      assertEquals(AbstractKernelControllerContext.class, cc.getClass());
      assertUndeploy(context);
      assertNull(controller.getContext("Test", null));
   }

   public void testNoopControllerContextCreator() throws Throwable
   {
      NoopControllerContextCreator noop = new NoopControllerContextCreator();
      beanMetaDataDeployer.addControllerContextCreator(noop);
      try
      {

         VFSDeployment context = createDeployment("/bean", "toplevel/my-beans.xml");
         assertDeploy(context);
         
         assertTrue(noop.isTriggered());
         
         ControllerContext test = controller.getInstalledContext("Test");
         assertNotNull(test);
         assertEquals(AbstractKernelControllerContext.class, test.getClass());
         assertUndeploy(context);
         assertNull(controller.getContext("Test", null));
      }
      finally
      {
         beanMetaDataDeployer.removeControllerContextCreator(noop);
      }
   }

   public void testSpecialControllerContextCreatorNotTriggered() throws Throwable
   {
      NoopControllerContextCreator noop1 = new NoopControllerContextCreator();
      beanMetaDataDeployer.addControllerContextCreator(noop1);
      SpecialControllerContextCreator special = new SpecialControllerContextCreator();
      beanMetaDataDeployer.addControllerContextCreator(special);
      NoopControllerContextCreator noop2 = new NoopControllerContextCreator();
      beanMetaDataDeployer.addControllerContextCreator(noop2);
      try
      {
         VFSDeployment context = createDeployment("/bean", "toplevel/my-beans.xml");
         assertDeploy(context);
         
         assertTrue(noop1.isTriggered());
         assertTrue(noop2.isTriggered());
         
         ControllerContext test = controller.getInstalledContext("Test");
         assertNotNull(test);
         assertEquals(AbstractKernelControllerContext.class, test.getClass());
         assertUndeploy(context);
         assertNull(controller.getContext("Test", null));
      }
      finally
      {
         beanMetaDataDeployer.removeControllerContextCreator(noop1);
         beanMetaDataDeployer.removeControllerContextCreator(special);
         beanMetaDataDeployer.removeControllerContextCreator(noop2);
      }
   }

   public void testSpecialControllerContextCreatorTriggered() throws Throwable
   {
      addDeployer(main, new TriggerSpecialControllerContextDeployer());
      
      NoopControllerContextCreator noop1 = new NoopControllerContextCreator();
      beanMetaDataDeployer.addControllerContextCreator(noop1);
      SpecialControllerContextCreator special = new SpecialControllerContextCreator();
      beanMetaDataDeployer.addControllerContextCreator(special);
      NoopControllerContextCreator noop2 = new NoopControllerContextCreator();
      beanMetaDataDeployer.addControllerContextCreator(noop2);
      try
      {
         VFSDeployment context = createDeployment("/bean", "toplevel/my-beans.xml");
         assertDeploy(context);
         
         assertTrue(noop1.isTriggered());
         assertFalse(noop2.isTriggered());
         
         ControllerContext test = controller.getInstalledContext("Test");
         assertNotNull(test);
         assertEquals(SpecialControllerContextCreator.SpecialControllerContext.class, test.getClass());
         assertUndeploy(context);
         assertNull(controller.getContext("Test", null));
      }
      finally
      {
         beanMetaDataDeployer.removeControllerContextCreator(noop1);
         beanMetaDataDeployer.removeControllerContextCreator(special);
         beanMetaDataDeployer.removeControllerContextCreator(noop2);
      }
   }
}
