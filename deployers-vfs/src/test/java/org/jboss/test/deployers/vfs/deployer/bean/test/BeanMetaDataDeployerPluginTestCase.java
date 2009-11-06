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
import org.jboss.test.deployers.vfs.deployer.bean.support.NoopBeanMetaDataDeployerPlugin;
import org.jboss.test.deployers.vfs.deployer.bean.support.NotUndeployingSpecialBeanMetaDataDeployerPlugin;
import org.jboss.test.deployers.vfs.deployer.bean.support.SpecialBeanMetaDataDeployerPlugin;
import org.jboss.test.deployers.vfs.deployer.bean.support.TriggerSpecialBeanMetaDataDeployerPlugin;
import org.jboss.test.deployers.vfs.deployer.bean.support.UndeployingSpecialBeanMetaDataDeployerPlugin;

/**
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
public class BeanMetaDataDeployerPluginTestCase extends AbstractDeployerUnitTest
{
   BeanMetaDataDeployer beanMetaDataDeployer;
   
   public static Test suite()
   {
      return new TestSuite(BeanMetaDataDeployerPluginTestCase.class);
   }

   public BeanMetaDataDeployerPluginTestCase(String name) throws Throwable
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
      NoopBeanMetaDataDeployerPlugin.getTriggered().clear();
      NoopBeanMetaDataDeployerPlugin noop = new NoopBeanMetaDataDeployerPlugin(1);
      beanMetaDataDeployer.addControllerContextCreator(noop);
      try
      {

         VFSDeployment context = createDeployment("/bean", "toplevel/my-beans.xml");
         assertDeploy(context);
         
         assertEquals(1, NoopBeanMetaDataDeployerPlugin.getTriggered().size());
         assertTrue(NoopBeanMetaDataDeployerPlugin.getTriggered().contains(1));
         
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
      NoopBeanMetaDataDeployerPlugin.getTriggered().clear();
      NoopBeanMetaDataDeployerPlugin noop1 = new NoopBeanMetaDataDeployerPlugin(1);
      beanMetaDataDeployer.addControllerContextCreator(noop1);
      NotUndeployingSpecialBeanMetaDataDeployerPlugin special = new NotUndeployingSpecialBeanMetaDataDeployerPlugin(2);
      beanMetaDataDeployer.addControllerContextCreator(special);
      NoopBeanMetaDataDeployerPlugin noop2 = new NoopBeanMetaDataDeployerPlugin(3);
      beanMetaDataDeployer.addControllerContextCreator(noop2);
      try
      {
         VFSDeployment context = createDeployment("/bean", "toplevel/my-beans.xml");
         assertDeploy(context);
         
         assertEquals(2, NoopBeanMetaDataDeployerPlugin.getTriggered().size());
         assertTrue(NoopBeanMetaDataDeployerPlugin.getTriggered().contains(1));
         assertTrue(NoopBeanMetaDataDeployerPlugin.getTriggered().contains(3));
         
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
      NoopBeanMetaDataDeployerPlugin.getTriggered().clear();
      addDeployer(main, new TriggerSpecialBeanMetaDataDeployerPlugin());
      
      NoopBeanMetaDataDeployerPlugin noop1 = new NoopBeanMetaDataDeployerPlugin(1);
      beanMetaDataDeployer.addControllerContextCreator(noop1);
      NotUndeployingSpecialBeanMetaDataDeployerPlugin special = new NotUndeployingSpecialBeanMetaDataDeployerPlugin(2);
      beanMetaDataDeployer.addControllerContextCreator(special);
      NoopBeanMetaDataDeployerPlugin noop2 = new NoopBeanMetaDataDeployerPlugin(3);
      beanMetaDataDeployer.addControllerContextCreator(noop2);
      try
      {
         VFSDeployment context = createDeployment("/bean", "toplevel/my-beans.xml");
         assertDeploy(context);
         
         assertEquals(1, NoopBeanMetaDataDeployerPlugin.getTriggered().size());
         assertTrue(NoopBeanMetaDataDeployerPlugin.getTriggered().contains(1));
         assertFalse(NoopBeanMetaDataDeployerPlugin.getTriggered().contains(3));
         
         ControllerContext test = controller.getInstalledContext("Test");
         assertNotNull(test);
         assertEquals(SpecialBeanMetaDataDeployerPlugin.SpecialControllerContext.class, test.getClass());
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
      NoopBeanMetaDataDeployerPlugin.getTriggered().clear();
      addDeployer(main, new TriggerSpecialBeanMetaDataDeployerPlugin());
      
      NoopBeanMetaDataDeployerPlugin noop1 = new NoopBeanMetaDataDeployerPlugin(1);
      beanMetaDataDeployer.addControllerContextCreator(noop1);
      UndeployingSpecialBeanMetaDataDeployerPlugin special = new UndeployingSpecialBeanMetaDataDeployerPlugin(2);
      beanMetaDataDeployer.addControllerContextCreator(special);
      NoopBeanMetaDataDeployerPlugin noop2 = new NoopBeanMetaDataDeployerPlugin(3);
      beanMetaDataDeployer.addControllerContextCreator(noop2);
      try
      {
         VFSDeployment context = createDeployment("/bean", "toplevel/my-beans.xml");
         assertDeploy(context);
         
         assertEquals(1, NoopBeanMetaDataDeployerPlugin.getTriggered().size());
         assertTrue(NoopBeanMetaDataDeployerPlugin.getTriggered().contains(1));
         assertFalse(NoopBeanMetaDataDeployerPlugin.getTriggered().contains(3));
         
         ControllerContext test = controller.getInstalledContext("Test");
         assertNotNull(test);
         assertEquals(SpecialBeanMetaDataDeployerPlugin.SpecialControllerContext.class, test.getClass());
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
      NoopBeanMetaDataDeployerPlugin.getTriggered().clear();
      NoopBeanMetaDataDeployerPlugin noop6 = new NoopBeanMetaDataDeployerPlugin(6);
      beanMetaDataDeployer.addControllerContextCreator(noop6);
      NoopBeanMetaDataDeployerPlugin noop1 = new NoopBeanMetaDataDeployerPlugin(1);
      beanMetaDataDeployer.addControllerContextCreator(noop1);
      NoopBeanMetaDataDeployerPlugin noop3 = new NoopBeanMetaDataDeployerPlugin(3);
      beanMetaDataDeployer.addControllerContextCreator(noop3);
      NoopBeanMetaDataDeployerPlugin noop4 = new NoopBeanMetaDataDeployerPlugin(4);
      beanMetaDataDeployer.addControllerContextCreator(noop4);
      NoopBeanMetaDataDeployerPlugin noop2 = new NoopBeanMetaDataDeployerPlugin(2);
      beanMetaDataDeployer.addControllerContextCreator(noop2);
      NoopBeanMetaDataDeployerPlugin noop5 = new NoopBeanMetaDataDeployerPlugin(5);
      beanMetaDataDeployer.addControllerContextCreator(noop5);
      try
      {
         VFSDeployment context = createDeployment("/bean", "toplevel/my-beans.xml");
         assertDeploy(context);
         
         assertEquals(6, NoopBeanMetaDataDeployerPlugin.getTriggered().size());
         assertTrue(NoopBeanMetaDataDeployerPlugin.getTriggered().contains(1));
         assertTrue(NoopBeanMetaDataDeployerPlugin.getTriggered().contains(2));
         assertTrue(NoopBeanMetaDataDeployerPlugin.getTriggered().contains(3));
         assertTrue(NoopBeanMetaDataDeployerPlugin.getTriggered().contains(4));
         assertTrue(NoopBeanMetaDataDeployerPlugin.getTriggered().contains(5));
         assertTrue(NoopBeanMetaDataDeployerPlugin.getTriggered().contains(6));
         
         int last = 0;
         for (int i : NoopBeanMetaDataDeployerPlugin.getTriggered())
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
