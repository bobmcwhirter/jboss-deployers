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

import java.util.Arrays;

import junit.framework.Test;
import org.jboss.beans.metadata.plugins.AbstractBeanMetaData;
import org.jboss.beans.metadata.plugins.AbstractConstructorMetaData;
import org.jboss.beans.metadata.spi.BeanMetaDataFactory;
import org.jboss.dependency.spi.ControllerContext;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.spi.attachments.MutableAttachments;
import org.jboss.deployers.vfs.deployer.kernel.BeanMetaDataDeployer;
import org.jboss.deployers.vfs.deployer.kernel.KernelDeploymentDeployer;
import org.jboss.kernel.Kernel;
import org.jboss.kernel.plugins.deployment.AbstractKernelDeployment;
import org.jboss.test.deployers.support.TCCLClassLoaderDeployer;
import org.jboss.test.deployers.vfs.deployer.AbstractDeployerUnitTest;
import org.jboss.test.deployers.vfs.deployer.bean.support.DefaultXPCResolver;
import org.jboss.test.deployers.vfs.deployer.bean.support.PUDeployment;

/**
 * AutowireAnnotationBeansTestCase.
 *
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 */
public class AutowireAnnotationBeansTestCase extends AbstractDeployerUnitTest
{
   public AutowireAnnotationBeansTestCase(String name) throws Throwable
   {
      super(name);
   }

   public static Test suite()
   {
      return suite(AutowireAnnotationBeansTestCase.class);
   }

   protected void addDeployers(Kernel kernel)
   {
      TCCLClassLoaderDeployer tcclDeployer = new TCCLClassLoaderDeployer();
      KernelDeploymentDeployer kernelDeploymentDeployer = new KernelDeploymentDeployer();
      BeanMetaDataDeployer beanMetaDataDeployer = new BeanMetaDataDeployer(kernel.getController());
      addDeployer(main, tcclDeployer);
      addDeployer(main, kernelDeploymentDeployer);
      addDeployer(main, beanMetaDataDeployer);
   }

   public void testAutowiredBeans() throws Exception
   {
      Deployment context = createSimpleDeployment("KernelDeployerTest1");

      AbstractKernelDeployment deployment = new AbstractKernelDeployment();
      deployment.setName("KernelDeployerTest1");
      BeanMetaDataFactory md1 = new AbstractBeanMetaData("PUD", PUDeployment.class.getName());
      BeanMetaDataFactory md2 = new AbstractBeanMetaData("XPC", DefaultXPCResolver.class.getName());
      deployment.setBeanFactories(Arrays.asList(md1, md2));
      MutableAttachments attachments = (MutableAttachments) context.getPredeterminedManagedObjects();
      attachments.addAttachment("KernelDeployerTest1", deployment);

      assertDeploy(context);
      try
      {
         ControllerContext pud = controller.getInstalledContext("PUD");
         assertNotNull(pud);
      }
      finally
      {
         assertUndeploy(context);
      }

      assertNull(controller.getContext("PUD", null));
      assertNull(controller.getContext("XPC", null));
   }

   public void testMissingAutowireDependency() throws Exception
   {
      Deployment context = createSimpleDeployment("KernelDeployerTest2");

      AbstractKernelDeployment deployment = new AbstractKernelDeployment();
      deployment.setName("KernelDeployerTest2");
      BeanMetaDataFactory md1 = new AbstractBeanMetaData("PUD", PUDeployment.class.getName());
      deployment.setBeanFactories(Arrays.asList(md1));
      MutableAttachments attachments = (MutableAttachments) context.getPredeterminedManagedObjects();
      attachments.addAttachment("KernelDeployerTest2", deployment);

      assertDeploy(context);
      try
      {
         ControllerContext pud = controller.getContext("PUD", null);
         assertNotNull(pud);
         assertEquals(ControllerState.INSTANTIATED, pud.getState());
      }
      finally
      {
         assertUndeploy(context);
      }

      assertNull(controller.getContext("PUD", null));
   }

   public void testDependencyOnExistingTarget() throws Exception
   {
      Deployment context = createSimpleDeployment("KernelDeployerTest3");

      AbstractKernelDeployment deployment = new AbstractKernelDeployment();
      deployment.setName("KernelDeployerTest3");
      PUDeployment bean = new PUDeployment();
      AbstractBeanMetaData md1 = new AbstractBeanMetaData("PUD", PUDeployment.class.getName());
      AbstractConstructorMetaData acmd = new AbstractConstructorMetaData();
      acmd.setValueObject(bean);
      md1.setConstructor(acmd);
      BeanMetaDataFactory md2 = new AbstractBeanMetaData("XPC", DefaultXPCResolver.class.getName());
      deployment.setBeanFactories(Arrays.asList(md1, md2));
      MutableAttachments attachments = (MutableAttachments) context.getPredeterminedManagedObjects();
      attachments.addAttachment("KernelDeployerTest3", deployment);

      assertDeploy(context);
      try
      {
         ControllerContext pud = controller.getInstalledContext("PUD");
         assertNotNull(pud);
         ControllerContext xpc = controller.getInstalledContext("XPC");
         assertNotNull(xpc);
         assertSame(bean.getResolver(), xpc.getTarget());
      }
      finally
      {
         assertUndeploy(context);
      }

      assertNull(controller.getContext("PUD", null));
      assertNull(controller.getContext("XPC", null));
   }

   public void testExistingTargetWithMissingDependency() throws Exception
   {
      Deployment context = createSimpleDeployment("KernelDeployerTest4");

      AbstractKernelDeployment deployment = new AbstractKernelDeployment();
      deployment.setName("KernelDeployerTest4");
      PUDeployment bean = new PUDeployment();
      AbstractBeanMetaData md1 = new AbstractBeanMetaData("PUD", PUDeployment.class.getName());
      AbstractConstructorMetaData acmd = new AbstractConstructorMetaData();
      acmd.setValueObject(bean);
      md1.setConstructor(acmd);
      deployment.setBeanFactories(Arrays.asList((BeanMetaDataFactory)md1));
      MutableAttachments attachments = (MutableAttachments) context.getPredeterminedManagedObjects();
      attachments.addAttachment("KernelDeployerTest4", deployment);

      assertDeploy(context);
      try
      {
         ControllerContext pud = controller.getContext("PUD", null);
         assertNotNull(pud);
         assertSame(bean, pud.getTarget());
         assertNull(bean.getResolver());
         assertEquals(ControllerState.INSTANTIATED, pud.getState());
      }
      finally
      {
         assertUndeploy(context);
      }

      assertNull(controller.getContext("PUD", null));
   }
}