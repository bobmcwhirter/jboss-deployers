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
import org.jboss.test.deployers.AbstractDeployerTest;
import org.jboss.test.deployers.deployer.support.TestSimpleDeployer;

/**
 * DeployerOrderingUnitTestCase.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class DeployerOrderingUnitTestCase extends AbstractDeployerTest
{
   public static Test suite()
   {
      return new TestSuite(DeployerOrderingUnitTestCase.class);
   }
   
   public DeployerOrderingUnitTestCase(String name)
   {
      super(name);
   }
   
   protected void setUp() throws Exception
   {
      super.setUp();
      TestSimpleDeployer.reset();
   }
   
   public void testCorrectOrder() throws Exception
   {
      DeployerClient main = createMainDeployer();
      TestSimpleDeployer deployer1 = new TestSimpleDeployer(1);
      addDeployer(main, deployer1);
      TestSimpleDeployer deployer2 = new TestSimpleDeployer(2);
      addDeployer(main, deployer2);
      
      Deployment deployment = createSimpleDeployment("correctOrder");
      main.addDeployment(deployment);
      main.process();
      
      assertEquals(1, deployer1.getDeployOrder());
      assertEquals(2, deployer2.getDeployOrder());
      assertEquals(-1, deployer1.getUndeployOrder());
      assertEquals(-1, deployer2.getUndeployOrder());

      main.removeDeployment(deployment);
      main.process();
      
      assertEquals(1, deployer1.getDeployOrder());
      assertEquals(2, deployer2.getDeployOrder());
      assertEquals(4, deployer1.getUndeployOrder());
      assertEquals(3, deployer2.getUndeployOrder());

      main.addDeployment(deployment);
      main.process();
      
      assertEquals(5, deployer1.getDeployOrder());
      assertEquals(6, deployer2.getDeployOrder());
      assertEquals(4, deployer1.getUndeployOrder());
      assertEquals(3, deployer2.getUndeployOrder());
   }
   
   public void testWrongOrder() throws Exception
   {
      DeployerClient main = createMainDeployer();
      TestSimpleDeployer deployer1 = new TestSimpleDeployer(1);
      addDeployer(main, deployer1);
      TestSimpleDeployer deployer2 = new TestSimpleDeployer(2);
      addDeployer(main, deployer2);
      
      Deployment deployment = createSimpleDeployment("wrongOrder");
      main.addDeployment(deployment);
      main.process();
      
      assertEquals(1, deployer1.getDeployOrder());
      assertEquals(2, deployer2.getDeployOrder());
      assertEquals(-1, deployer1.getUndeployOrder());
      assertEquals(-1, deployer2.getUndeployOrder());

      main.removeDeployment(deployment);
      main.process();
      
      assertEquals(1, deployer1.getDeployOrder());
      assertEquals(2, deployer2.getDeployOrder());
      assertEquals(4, deployer1.getUndeployOrder());
      assertEquals(3, deployer2.getUndeployOrder());

      main.addDeployment(deployment);
      main.process();
      
      assertEquals(5, deployer1.getDeployOrder());
      assertEquals(6, deployer2.getDeployOrder());
      assertEquals(4, deployer1.getUndeployOrder());
      assertEquals(3, deployer2.getUndeployOrder());
   }
}
