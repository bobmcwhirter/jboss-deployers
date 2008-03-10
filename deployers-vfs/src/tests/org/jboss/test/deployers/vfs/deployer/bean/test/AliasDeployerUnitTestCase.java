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
import org.jboss.dependency.spi.ControllerState;
import org.jboss.deployers.vfs.deployer.kernel.AliasDeploymentDeployer;
import org.jboss.deployers.vfs.deployer.kernel.BeanDeployer;
import org.jboss.deployers.vfs.deployer.kernel.BeanMetaDataDeployer;
import org.jboss.deployers.vfs.deployer.kernel.DeploymentAliasMetaDataDeployer;
import org.jboss.deployers.vfs.deployer.kernel.KernelDeploymentDeployer;
import org.jboss.deployers.vfs.spi.client.VFSDeployment;
import org.jboss.kernel.Kernel;

/**
 * AliasDeployerUnitTestCase.
 *
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 */
public class AliasDeployerUnitTestCase extends AbstractDeployerUnitTestCase
{
   public static Test suite()
   {
      return new TestSuite(AliasDeployerUnitTestCase.class);
   }

   public AliasDeployerUnitTestCase(String name) throws Throwable
   {
      super(name);
   }

   protected void addDeployers(Kernel kernel)
   {
      BeanDeployer beanDeployer = new BeanDeployer();
      KernelDeploymentDeployer kernelDeploymentDeployer = new KernelDeploymentDeployer();
      AliasDeploymentDeployer aliasDeploymentDeployer = new AliasDeploymentDeployer();
      BeanMetaDataDeployer beanMetaDataDeployer = new BeanMetaDataDeployer(kernel);
      DeploymentAliasMetaDataDeployer aliasMetaDataDeployer = new DeploymentAliasMetaDataDeployer(kernel);
      addDeployer(main, beanDeployer);
      addDeployer(main, kernelDeploymentDeployer);
      addDeployer(main, aliasDeploymentDeployer);
      addDeployer(main, beanMetaDataDeployer);
      addDeployer(main, aliasMetaDataDeployer);
   }

   public void testAliasSuccessful() throws Exception
   {
      VFSDeployment context = createDeployment("/alias", "toplevel/my-beans.xml");
      assertDeploy(context);
      assertNotNull(controller.getInstalledContext("Test"));

      VFSDeployment alias = createDeployment("/alias", "toplevel/aliases-beans.xml");
      assertDeploy(alias);
      assertNotNull("Missing Test bean.", controller.getInstalledContext("MyAlias"));
      assertEquals(controller.getInstalledContext("MyAlias"), controller.getInstalledContext("Test"));
      assertNotNull("Missing Injectee bean.", controller.getInstalledContext("Injectee"));

      assertUndeploy(alias);
      assertNull(controller.getContext("Injectee", null));

      assertUndeploy(context);
      assertNull(controller.getContext("Test", null));
      assertNull(controller.getContext("MyAlias", null));
   }

   public void testJoinedSuccessful() throws Exception
   {
      VFSDeployment context = createDeployment("/alias", "toplevel/joined-beans.xml");
      assertDeploy(context);
      assertNotNull(controller.getInstalledContext("Test"));
      assertNotNull(controller.getInstalledContext("MyAlias"));     
      assertEquals(controller.getInstalledContext("MyAlias"), controller.getInstalledContext("Test"));

      assertUndeploy(context);
      assertNull(controller.getContext("MyAlias", null));
      assertNull(controller.getContext("Test", null));
   }

   public void testAliasMissing() throws Exception
   {
      VFSDeployment context = createDeployment("/alias", "toplevel/aliases-beans.xml");
      assertDeploy(context);
      assertNotNull(controller.getContext("Injectee", ControllerState.INSTANTIATED));
      assertUndeploy(context);
      assertNull(controller.getContext("Test", null));
   }

   public void testJMXAlias() throws Exception
   {
      VFSDeployment context = createDeployment("/alias", "toplevel/mbean-beans.xml");
      assertDeploy(context);
      assertNotNull(controller.getInstalledContext("Test"));

      VFSDeployment alias = createDeployment("/alias", "toplevel/jmx-beans.xml");
      assertDeploy(alias);
      assertNotNull("Missing Injectee bean.", controller.getInstalledContext("Injectee"));

      assertUndeploy(alias);
      assertNull(controller.getContext("Injectee", null));

      assertUndeploy(context);
      assertNull(controller.getContext("Test", null));
   }

   public void testAliasDemand() throws Exception
   {
      VFSDeployment context = createDeployment("/alias", "toplevel/tomcat-beans.xml");
      assertDeploy(context);
      assertNotNull(controller.getInstalledContext("Tomcat"));

      VFSDeployment alias = createDeployment("/alias", "toplevel/servicex-beans.xml");
      assertDeploy(alias);
      assertNotNull(controller.getInstalledContext("ServiceX"));

      assertUndeploy(context);
      assertNull(controller.getContext("Tomcat", null));
      assertNull(controller.getContext("ServiceX", ControllerState.CREATE));
      assertNotNull(controller.getContext("ServiceX", ControllerState.CONFIGURED));

      assertUndeploy(alias);
      assertNull(controller.getContext("JBossWeb", null));
      assertNull(controller.getContext("ServiceX", null));
   }

   public void testAliasDependency() throws Exception
   {
      VFSDeployment alias = createDeployment("/alias", "toplevel/servicex-beans.xml");
      assertDeploy(alias);
      assertNotNull(controller.getContext("ServiceX", ControllerState.CONFIGURED));

      VFSDeployment context = createDeployment("/alias", "toplevel/tomcat-beans.xml");
      assertDeploy(context);
      assertNotNull(controller.getInstalledContext("Tomcat"));
      assertNotNull(controller.getInstalledContext("JBossWeb"));
      assertNotNull(controller.getInstalledContext("ServiceX"));

      assertUndeploy(context);
      assertNull(controller.getContext("Tomcat", null));
      assertNull(controller.getContext("JBossWeb", null));
      assertNull(controller.getContext("ServiceX", ControllerState.CREATE));
      assertNotNull(controller.getContext("ServiceX", ControllerState.CONFIGURED));

      assertUndeploy(alias);
      assertNull(controller.getContext("ServiceX", null));
   }

   public void testRemovingAlias() throws Exception
   {
      VFSDeployment justx = createDeployment("/alias", "toplevel/justx-beans.xml");
      assertDeploy(justx);
      assertNotNull(controller.getContext("ServiceX", ControllerState.CONFIGURED));

      VFSDeployment alias = createDeployment("/alias", "toplevel/tcalias-beans.xml");
      assertDeploy(alias);

      VFSDeployment context = createDeployment("/alias", "toplevel/tomcat-beans.xml");
      assertDeploy(context);
      assertNotNull(controller.getInstalledContext("Tomcat"));
      assertNotNull(controller.getInstalledContext("JBossWeb"));
      assertNotNull(controller.getInstalledContext("ServiceX"));

      assertUndeploy(alias);
      assertNull(controller.getInstalledContext("JBossWeb"));
      assertNotNull(controller.getInstalledContext("Tomcat"));
      assertNotNull(controller.getInstalledContext("ServiceX"));

      assertUndeploy(context);
      assertNull(controller.getContext("Tomcat", null));
      assertNull(controller.getContext("ServiceX", ControllerState.CREATE));
      assertNotNull(controller.getContext("ServiceX", ControllerState.CONFIGURED));

      assertUndeploy(justx);
      assertNull(controller.getContext("JBossWeb", null));
      assertNull(controller.getContext("ServiceX", null));
   }

}
