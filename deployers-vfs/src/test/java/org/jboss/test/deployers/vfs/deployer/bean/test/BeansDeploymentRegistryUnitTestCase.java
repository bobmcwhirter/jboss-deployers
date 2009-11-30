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

import java.util.HashSet;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.dependency.spi.ControllerContext;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.spi.attachments.MutableAttachments;
import org.jboss.deployers.structure.spi.DeploymentRegistry;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.structure.spi.helpers.AbstractDeploymentRegistry;
import org.jboss.deployers.vfs.deployer.kernel.BeanDeployer;
import org.jboss.deployers.vfs.deployer.kernel.BeanMetaDataDeployer;
import org.jboss.deployers.vfs.deployer.kernel.KernelDeploymentDeployer;
import org.jboss.deployers.vfs.spi.client.VFSDeployment;
import org.jboss.kernel.Kernel;
import org.jboss.test.deployers.support.TCCLClassLoaderDeployer;
import org.jboss.test.deployers.vfs.deployer.AbstractDeployerUnitTest;

/**
 * BeansDeploymentRegistryUnitTestCase.
 *
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 */
public class BeansDeploymentRegistryUnitTestCase extends AbstractDeployerUnitTest
{
   private DeploymentRegistry registry;

   public static Test suite()
   {
      return new TestSuite(BeansDeploymentRegistryUnitTestCase.class);
   }

   public BeansDeploymentRegistryUnitTestCase(String name) throws Throwable
   {
      super(name);
   }

   @Override
   protected void tearDown() throws Exception
   {
      registry = null;
      super.tearDown();
   }

   protected void addDeployers(Kernel kernel)
   {
      registry = new AbstractDeploymentRegistry();

      BeanDeployer beanDeployer = new BeanDeployer();
      KernelDeploymentDeployer kernelDeploymentDeployer = new KernelDeploymentDeployer();
      TCCLClassLoaderDeployer tcclDeployer = new TCCLClassLoaderDeployer();
      addDeployer(main, beanDeployer);
      addDeployer(main, kernelDeploymentDeployer);
      addDeployer(main, tcclDeployer);

      BeanMetaDataDeployer deployer = new BeanMetaDataDeployer(kernel.getController());
      deployer.setDeploymentRegistry(registry);
      addDeployer(main, deployer);
   }

   public void testSuccessfulDeployment() throws Throwable
   {
      ControllerContext c1 = null;
      ControllerContext c2 = null;
      DeploymentUnit unit = null;

      VFSDeployment deployment = createDeployment("/bean/multiple", "test.jar");
      assertDeploy(deployment);
      try
      {
         c1 = controller.getInstalledContext("Test1");
         c2 = controller.getInstalledContext("Test2");
         unit = assertDeploymentUnit(main, deployment.getName());

         Set<ControllerContext> contexts = new HashSet<ControllerContext>();
         contexts.add(c1);
         contexts.add(c2);

         assertEquals(contexts, registry.getContexts(unit));
         assertSame(unit, registry.getDeployment(c1));
         assertSame(unit, registry.getDeployment(c2));
      }
      finally
      {
         assertUndeploy(deployment);

         assertEmpty(registry.getContexts(unit));
         assertNull(registry.getDeployment(c1));
         assertNull(registry.getDeployment(c2));
      }
   }

   public void testFailedDeployment() throws Throwable
   {
      ControllerContext c1 = null;
      ControllerContext c2 = null;
      DeploymentUnit unit = null;

      VFSDeployment deployment = createDeployment("/bean/multiple", "test.jar");
      assertDeploy(deployment);
      try
      {
         c1 = controller.getInstalledContext("Test1");
         c2 = controller.getInstalledContext("Test2");
         unit = assertDeploymentUnit(main, deployment.getName());

         Set<ControllerContext> contexts = new HashSet<ControllerContext>();
         contexts.add(c1);
         contexts.add(c2);

         assertEquals(contexts, registry.getContexts(unit));
         assertSame(unit, registry.getDeployment(c1));
         assertSame(unit, registry.getDeployment(c2));

         Deployment failed = createSimpleDeployment("failed");
         BeanMetaDataBuilder builder = BeanMetaDataBuilder.createBuilder("Foo", Object.class.getName());
         builder.addAlias("Test1");
         ((MutableAttachments)failed.getPredeterminedManagedObjects()).addAttachment(BeanMetaData.class, builder.getBeanMetaData());

         try
         {
            DeploymentUnit du = addDeployment(main, failed);
            ControllerContext foo = controller.getInstalledContext("Foo");
            assertNull(foo);
            assertEmpty(registry.getContexts(du));
         }
         finally
         {
            assertUndeploy(failed);
         }
      }
      finally
      {
         assertUndeploy(deployment);

         assertEmpty(registry.getContexts(unit));
         assertNull(registry.getDeployment(c1));
         assertNull(registry.getDeployment(c2));
      }
   }
}