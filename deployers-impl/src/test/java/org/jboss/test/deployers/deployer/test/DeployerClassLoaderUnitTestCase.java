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
package org.jboss.test.deployers.deployer.test;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.deployers.client.spi.DeployerClient;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.client.spi.DeploymentFactory;
import org.jboss.deployers.spi.DeploymentState;
import org.jboss.deployers.structure.spi.DeploymentContext;
import org.jboss.test.deployers.AbstractDeployerTest;
import org.jboss.test.deployers.deployer.support.TestClassLoaderDeployer;

/**
 * DeployerClassLoaderUnitTestCase.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class DeployerClassLoaderUnitTestCase extends AbstractDeployerTest
{
   public static Test suite()
   {
      return new TestSuite(DeployerClassLoaderUnitTestCase.class);
   }
   
   public DeployerClassLoaderUnitTestCase(String name)
   {
      super(name);
   }

   public void testClassLoader() throws Exception
   {
      TestClassLoaderDeployer deployer = new TestClassLoaderDeployer();
      DeployerClient main = createMainDeployer(deployer);
      
      Deployment deployment = createSimpleDeployment("single");
      main.addDeployment(deployment);
      main.process();

      DeploymentContext context = assertDeploymentContext(main, "single"); 
      assertEquals(DeploymentState.DEPLOYED, context.getState());
      assertEquals(deployer.cl, context.getClassLoader());
      
      main.removeDeployment(deployment);
      main.process();
      assertEquals(DeploymentState.UNDEPLOYED, context.getState());
      assertNull(context.getClassLoader());
   }

   public void testSubdeploymentClassLoader() throws Exception
   {
      TestClassLoaderDeployer deployer = new TestClassLoaderDeployer();
      DeployerClient main = createMainDeployer(deployer);
      
      Deployment deployment = createSimpleDeployment("top");
      DeploymentFactory factory = new DeploymentFactory();
      factory.addContext(deployment, "sub"); 
      main.addDeployment(deployment);
      main.process();
      
      DeploymentContext top = assertDeploymentContext(main, "top"); 
      DeploymentContext sub = assertDeploymentContext(main, "top/sub"); 
      assertEquals(DeploymentState.DEPLOYED, top.getState());
      assertEquals(deployer.cl, top.getClassLoader());
      assertEquals(DeploymentState.DEPLOYED, sub.getState());
      assertEquals(deployer.cl, sub.getClassLoader());
      
      main.removeDeployment(deployment);
      main.process();
      assertEquals(DeploymentState.UNDEPLOYED, top.getState());
      assertNull(top.getClassLoader());
      assertEquals(DeploymentState.UNDEPLOYED, sub.getState());
      assertNull(sub.getClassLoader());
   }
}
