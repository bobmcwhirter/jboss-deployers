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
import junit.framework.TestSuite;
import org.jboss.beans.metadata.plugins.AbstractBeanMetaData;
import org.jboss.beans.metadata.spi.BeanMetaDataFactory;
import org.jboss.dependency.spi.ControllerContext;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.spi.attachments.MutableAttachments;
import org.jboss.deployers.vfs.deployer.kernel.BeanMetaDataDeployer;
import org.jboss.deployers.vfs.deployer.kernel.KernelDeploymentDeployer;
import org.jboss.kernel.Kernel;
import org.jboss.kernel.plugins.deployment.AbstractKernelDeployment;
import org.jboss.test.deployers.vfs.deployer.AbstractDeployerUnitTest;
import org.jboss.test.deployers.vfs.deployer.bean.support.SimpleAnnotated;
import org.jboss.test.deployers.vfs.deployer.bean.support.SimpleAnnotatedAlias;
import org.jboss.test.deployers.vfs.deployer.bean.support.SimpleAnnotatedConstructor;
import org.jboss.test.deployers.vfs.deployer.bean.support.SimpleAnnotatedInjection;
import org.jboss.test.deployers.vfs.deployer.bean.support.SimpleAnnotatedStart;

/**
 * AnnotatedBeansUnitTestCase.
 *
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 */
public class AnnotatedBeansUnitTestCase extends AbstractDeployerUnitTest
{
   public static Test suite()
   {
      return new TestSuite(AnnotatedBeansUnitTestCase.class);
   }

   public AnnotatedBeansUnitTestCase(String name) throws Throwable
   {
      super(name);
   }

   @SuppressWarnings("deprecation")
   protected void addDeployers(Kernel kernel)
   {
      KernelDeploymentDeployer kernelDeploymentDeployer = new KernelDeploymentDeployer();
      BeanMetaDataDeployer beanMetaDataDeployer = new BeanMetaDataDeployer(kernel);
      addDeployer(main, kernelDeploymentDeployer);
      addDeployer(main, beanMetaDataDeployer);
   }

   public void testKernelScope() throws Exception
   {
      Deployment context = createSimpleDeployment("KernelDeployerTest");

      AbstractKernelDeployment deployment = new AbstractKernelDeployment();
      deployment.setName("KernelDeployerTest");
      BeanMetaDataFactory md1 = new AbstractBeanMetaData("Constructor", SimpleAnnotatedConstructor.class.getName());
      BeanMetaDataFactory md2 = new AbstractBeanMetaData("Injection", SimpleAnnotatedInjection.class.getName());
      BeanMetaDataFactory md3 = new AbstractBeanMetaData("Start", SimpleAnnotatedStart.class.getName());
      BeanMetaDataFactory md4 = new AbstractBeanMetaData("SomeRandomName", SimpleAnnotatedAlias.class.getName());
      deployment.setBeanFactories(Arrays.asList(md1, md2, md3, md4));
      MutableAttachments attachments = (MutableAttachments) context.getPredeterminedManagedObjects();
      attachments.addAttachment("KernelDeployerTest", deployment);

      assertDeploy(context);
      try
      {
         assertSimpleAnnotated("Constructor");
         assertSimpleAnnotated("Injection");
         assertSimpleAnnotated("Start");
         assertSimpleAnnotated("Alias");
      }
      finally
      {
         assertUndeploy(context);
      }

      assertNull(controller.getContext("Constructor", null));
      assertNull(controller.getContext("Injection", null));
      assertNull(controller.getContext("Start", null));
      assertNull(controller.getContext("SomeRandomName", null));
   }

   protected void assertSimpleAnnotated(Object name)
   {
      ControllerContext context = controller.getInstalledContext(name);
      assertNotNull(context);
      Object target = context.getTarget();
      SimpleAnnotated annotated = assertInstanceOf(target, SimpleAnnotated.class);
      assertSame(controller, annotated.getController());
   }
}