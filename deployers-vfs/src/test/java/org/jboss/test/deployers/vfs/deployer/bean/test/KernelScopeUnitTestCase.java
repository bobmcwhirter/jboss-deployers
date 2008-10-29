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

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;

import junit.framework.Test;
import org.jboss.beans.metadata.plugins.AbstractBeanMetaData;
import org.jboss.beans.metadata.spi.BeanMetaDataFactory;
import org.jboss.dependency.spi.ControllerContext;
import org.jboss.dependency.spi.ScopeInfo;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.spi.attachments.MutableAttachments;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.structure.spi.main.MainDeployerStructure;
import org.jboss.deployers.vfs.deployer.kernel.BeanMetaDataDeployer;
import org.jboss.deployers.vfs.deployer.kernel.KernelDeploymentDeployer;
import org.jboss.kernel.Kernel;
import org.jboss.kernel.plugins.deployment.AbstractKernelDeployment;
import org.jboss.metadata.spi.MetaData;
import org.jboss.metadata.spi.scope.Scope;
import org.jboss.metadata.spi.scope.ScopeKey;
import org.jboss.test.deployers.vfs.deployer.AbstractDeployerUnitTest;
import org.jboss.test.deployers.vfs.deployer.bean.support.Simple;
import org.jboss.test.deployers.vfs.deployer.bean.support.TestInstanceMetaDataBeanDeployer;
import org.jboss.test.deployers.vfs.deployer.bean.support.TestMetaDataBeanDeployer;
import org.jboss.test.deployers.support.TCCLClassLoaderDeployer;

/**
 * BeanDeployerUnitTestCase.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class KernelScopeUnitTestCase extends AbstractDeployerUnitTest
{
   public static Test suite()
   {
      return suite(KernelScopeUnitTestCase.class);
   }

   protected TestMetaDataBeanDeployer testMetaDataDeployer;

   protected TestInstanceMetaDataBeanDeployer testInstanceMetaDataDeployer;

   public KernelScopeUnitTestCase(String name) throws Throwable
   {
      super(name);
   }

   @SuppressWarnings("deprecation")
   protected void addDeployers(Kernel kernel)
   {
      testMetaDataDeployer = new TestMetaDataBeanDeployer();
      testInstanceMetaDataDeployer = new TestInstanceMetaDataBeanDeployer();
      KernelDeploymentDeployer kernelDeploymentDeployer = new KernelDeploymentDeployer();
      BeanMetaDataDeployer beanMetaDataDeployer = new BeanMetaDataDeployer(kernel);
      addDeployer(main, new TCCLClassLoaderDeployer());
      addDeployer(main, testMetaDataDeployer);
      addDeployer(main, testInstanceMetaDataDeployer);
      addDeployer(main, kernelDeploymentDeployer);
      addDeployer(main, beanMetaDataDeployer);
   }

   public void testKernelScope() throws Exception
   {
      Deployment context = createSimpleDeployment("KernelDeployerTest");

      AbstractKernelDeployment deployment = new AbstractKernelDeployment();
      deployment.setName("KernelDeployerTest");
      
      BeanMetaDataFactory metaData = new AbstractBeanMetaData("Test", Simple.class.getName());
      deployment.setBeanFactories(Collections.singletonList(metaData));

      MutableAttachments attachments = (MutableAttachments) context.getPredeterminedManagedObjects();
      attachments.addAttachment("KernelDeployerTest", deployment);
      
      assertDeploy(context);
      ControllerContext ctx = controller.getInstalledContext("Test");

      ScopeInfo scopeInfo = ctx.getScopeInfo();
      
      MainDeployerStructure mds = (MainDeployerStructure) main;
      DeploymentUnit unit = mds.getDeploymentUnit("KernelDeployerTest", true);
      DeploymentUnit component = unit.getComponent("Test");
      assertScopeKeys(component.getScope(), scopeInfo.getScope());
      assertScopeKeys(component.getMutableScope(), scopeInfo.getMutableScope());
      
      MetaData md = ctx.getScopeInfo().getMetaData();
      assertEquals(testMetaDataDeployer, md.getMetaData("test"));
      assertEquals(testInstanceMetaDataDeployer, md.getMetaData("instance"));
      
      assertUndeploy(context);
      assertNull(controller.getContext("Test", null));
   }

   /**
    * Component scopes should be subset of context scopes.
    *
    * @param component the component scope key
    * @param context the context scope key 
    */
   protected void assertScopeKeys(ScopeKey component, ScopeKey context)
   {
      Collection<Scope> componentScopes = component.getScopes();
      Collection<Scope> contextScopes = context.getScopes();

      Set<Scope> first = new HashSet<Scope>(componentScopes);
      Set<Scope> second = new HashSet<Scope>(contextScopes);
      second.retainAll(first);
      assertEquals(first, second);
   }
}
