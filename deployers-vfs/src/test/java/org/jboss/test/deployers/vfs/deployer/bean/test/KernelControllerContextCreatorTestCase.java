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
import org.jboss.test.deployers.vfs.deployer.bean.support.NotUndeployingSpecialControllerContextCreator;
import org.jboss.test.deployers.vfs.deployer.bean.support.SpecialControllerContextCreator;
import org.jboss.test.deployers.vfs.deployer.bean.support.TriggerSpecialControllerContextDeployer;
import org.jboss.test.deployers.vfs.deployer.bean.support.UndeployingSpecialControllerContextCreator;

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
      NoopControllerContextCreator.getTriggered().clear();
      NoopControllerContextCreator noop = new NoopControllerContextCreator(1);
      beanMetaDataDeployer.addControllerContextCreator(noop);
      try
      {

         VFSDeployment context = createDeployment("/bean", "toplevel/my-beans.xml");
         assertDeploy(context);
         
         assertEquals(1, NoopControllerContextCreator.getTriggered().size());
         assertTrue(NoopControllerContextCreator.getTriggered().contains(1));
         
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
      NoopControllerContextCreator.getTriggered().clear();
      NoopControllerContextCreator noop1 = new NoopControllerContextCreator(1);
      beanMetaDataDeployer.addControllerContextCreator(noop1);
      NotUndeployingSpecialControllerContextCreator special = new NotUndeployingSpecialControllerContextCreator(2);
      beanMetaDataDeployer.addControllerContextCreator(special);
      NoopControllerContextCreator noop2 = new NoopControllerContextCreator(3);
      beanMetaDataDeployer.addControllerContextCreator(noop2);
      try
      {
         VFSDeployment context = createDeployment("/bean", "toplevel/my-beans.xml");
         assertDeploy(context);
         
         assertEquals(2, NoopControllerContextCreator.getTriggered().size());
         assertTrue(NoopControllerContextCreator.getTriggered().contains(1));
         assertTrue(NoopControllerContextCreator.getTriggered().contains(3));
         
         ControllerContext test = controller.getInstalledContext("Test");
         assertNotNull(test);
         assertEquals(AbstractKernelControllerContext.class, test.getClass());
         assertUndeploy(context);
         assertNull(controller.getContext("Test", null));
         assertEmpty(special.getUndeployedNames());
      }
      finally
      {
         beanMetaDataDeployer.removeControllerContextCreator(noop1);
         beanMetaDataDeployer.removeControllerContextCreator(special);
         beanMetaDataDeployer.removeControllerContextCreator(noop2);
      }
   }

   public void testSpecialControllerContextCreatorTriggeredButNotHandlingUndeploy() throws Throwable
   {
      NoopControllerContextCreator.getTriggered().clear();
      addDeployer(main, new TriggerSpecialControllerContextDeployer());
      
      NoopControllerContextCreator noop1 = new NoopControllerContextCreator(1);
      beanMetaDataDeployer.addControllerContextCreator(noop1);
      NotUndeployingSpecialControllerContextCreator special = new NotUndeployingSpecialControllerContextCreator(2);
      beanMetaDataDeployer.addControllerContextCreator(special);
      NoopControllerContextCreator noop2 = new NoopControllerContextCreator(3);
      beanMetaDataDeployer.addControllerContextCreator(noop2);
      try
      {
         VFSDeployment context = createDeployment("/bean", "toplevel/my-beans.xml");
         assertDeploy(context);
         
         assertEquals(1, NoopControllerContextCreator.getTriggered().size());
         assertTrue(NoopControllerContextCreator.getTriggered().contains(1));
         assertFalse(NoopControllerContextCreator.getTriggered().contains(3));
         
         ControllerContext test = controller.getInstalledContext("Test");
         assertNotNull(test);
         assertEquals(SpecialControllerContextCreator.SpecialControllerContext.class, test.getClass());
         assertFalse(special.getUndeployedNames().contains("Test"));
         assertUndeploy(context);
         assertNull(controller.getContext("Test", null));
         assertEquals(1, special.getUndeployedNames().size());
         assertTrue(special.getUndeployedNames().contains("Test"));
      }
      finally
      {
         beanMetaDataDeployer.removeControllerContextCreator(noop1);
         beanMetaDataDeployer.removeControllerContextCreator(special);
         beanMetaDataDeployer.removeControllerContextCreator(noop2);
      }
   }
   
   public void testSpecialControllerContextCreatorTriggeredAndHandlingUndeploy() throws Throwable
   {
      NoopControllerContextCreator.getTriggered().clear();
      addDeployer(main, new TriggerSpecialControllerContextDeployer());
      
      NoopControllerContextCreator noop1 = new NoopControllerContextCreator(1);
      beanMetaDataDeployer.addControllerContextCreator(noop1);
      UndeployingSpecialControllerContextCreator special = new UndeployingSpecialControllerContextCreator(2);
      beanMetaDataDeployer.addControllerContextCreator(special);
      NoopControllerContextCreator noop2 = new NoopControllerContextCreator(3);
      beanMetaDataDeployer.addControllerContextCreator(noop2);
      try
      {
         VFSDeployment context = createDeployment("/bean", "toplevel/my-beans.xml");
         assertDeploy(context);
         
         assertEquals(1, NoopControllerContextCreator.getTriggered().size());
         assertTrue(NoopControllerContextCreator.getTriggered().contains(1));
         assertFalse(NoopControllerContextCreator.getTriggered().contains(3));
         
         ControllerContext test = controller.getInstalledContext("Test");
         assertNotNull(test);
         assertEquals(SpecialControllerContextCreator.SpecialControllerContext.class, test.getClass());
         assertFalse(special.getUndeployedNames().contains("Test"));
         assertUndeploy(context);
         assertNull(controller.getContext("Test", null));
         assertEquals(1, special.getUndeployedNames().size());
         assertTrue(special.getUndeployedNames().contains("Test"));
      }
      finally
      {
         beanMetaDataDeployer.removeControllerContextCreator(noop1);
         beanMetaDataDeployer.removeControllerContextCreator(special);
         beanMetaDataDeployer.removeControllerContextCreator(noop2);
      }
   }
   
   public void testControllerContextOrder() throws Throwable
   {
      NoopControllerContextCreator.getTriggered().clear();
      NoopControllerContextCreator noop6 = new NoopControllerContextCreator(6);
      beanMetaDataDeployer.addControllerContextCreator(noop6);
      NoopControllerContextCreator noop1 = new NoopControllerContextCreator(1);
      beanMetaDataDeployer.addControllerContextCreator(noop1);
      NoopControllerContextCreator noop3 = new NoopControllerContextCreator(3);
      beanMetaDataDeployer.addControllerContextCreator(noop3);
      NoopControllerContextCreator noop4 = new NoopControllerContextCreator(4);
      beanMetaDataDeployer.addControllerContextCreator(noop4);
      NoopControllerContextCreator noop2 = new NoopControllerContextCreator(2);
      beanMetaDataDeployer.addControllerContextCreator(noop2);
      NoopControllerContextCreator noop5 = new NoopControllerContextCreator(5);
      beanMetaDataDeployer.addControllerContextCreator(noop5);
      try
      {
         VFSDeployment context = createDeployment("/bean", "toplevel/my-beans.xml");
         assertDeploy(context);
         
         assertEquals(6, NoopControllerContextCreator.getTriggered().size());
         assertTrue(NoopControllerContextCreator.getTriggered().contains(1));
         assertTrue(NoopControllerContextCreator.getTriggered().contains(2));
         assertTrue(NoopControllerContextCreator.getTriggered().contains(3));
         assertTrue(NoopControllerContextCreator.getTriggered().contains(4));
         assertTrue(NoopControllerContextCreator.getTriggered().contains(5));
         assertTrue(NoopControllerContextCreator.getTriggered().contains(6));
         
         int last = 0;
         for (int i : NoopControllerContextCreator.getTriggered())
         {
            assertTrue(last + 1 == i);
            last = i;
         }
         
         ControllerContext test = controller.getInstalledContext("Test");
         assertNotNull(test);
         assertEquals(AbstractKernelControllerContext.class, test.getClass());
         assertUndeploy(context);
         assertNull(controller.getContext("Test", null));
      }
      finally
      {
         beanMetaDataDeployer.removeControllerContextCreator(noop1);
         beanMetaDataDeployer.removeControllerContextCreator(noop2);
         beanMetaDataDeployer.removeControllerContextCreator(noop3);
         beanMetaDataDeployer.removeControllerContextCreator(noop4);
         beanMetaDataDeployer.removeControllerContextCreator(noop5);
         beanMetaDataDeployer.removeControllerContextCreator(noop6);
      }
      
   }
}
