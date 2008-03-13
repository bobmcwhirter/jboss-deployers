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

import java.util.HashSet;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.deployers.client.spi.DeployerClient;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.test.deployers.AbstractDeployerTest;
import org.jboss.test.deployers.deployer.support.TestSimpleDeployer;

/**
 * DeployerProtocolUnitTestCase.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class DeployerProtocolUnitTestCase extends AbstractDeployerTest
{
   private TestSimpleDeployer deployer = new TestSimpleDeployer();
   
   public static Test suite()
   {
      return new TestSuite(DeployerProtocolUnitTestCase.class);
   }
   
   public DeployerProtocolUnitTestCase(String name)
   {
      super(name);
   }

   public void testDeploy() throws Exception
   {
      DeployerClient main = getMainDeployer();
      
      Deployment context = createSimpleDeployment("deploy");
      main.addDeployment(context);
      main.process();
      Set<String> expected = new HashSet<String>();
      expected.add(context.getName());
      assertEquals(expected, deployer.getDeployedUnits());
   }

   public void testUndeploy() throws Exception
   {
      DeployerClient main = getMainDeployer();

      Deployment context = createSimpleDeployment("undeploy");
      main.addDeployment(context);
      main.process();
      Set<String> expected = new HashSet<String>();
      expected.add(context.getName());
      assertEquals(expected, deployer.getDeployedUnits());
      
      main.removeDeployment(context.getName());
      main.process();
      assertEquals(expected, deployer.getUndeployedUnits());
   }

   public void testRedeploy() throws Exception
   {
      DeployerClient main = getMainDeployer();

      Deployment context = createSimpleDeployment("redeploy");
      main.addDeployment(context);
      main.process();
      Set<String> expected = new HashSet<String>();
      expected.add(context.getName());
      assertEquals(expected, deployer.getDeployedUnits());
      
      main.removeDeployment(context.getName());
      main.process();
      assertEquals(expected, deployer.getUndeployedUnits());
      
      deployer.clear();
      main.addDeployment(context);
      main.process();
      expected.clear();
      expected.add(context.getName());
      assertEquals(expected, deployer.getDeployedUnits());
   }
   
   protected DeployerClient getMainDeployer()
   {
      return createMainDeployer(deployer);
   }
}
