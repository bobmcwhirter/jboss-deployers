/*
* JBoss, Home of Professional Open Source
* Copyright 2007, JBoss Inc., and individual contributors as indicated
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

import java.util.Collections;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.deployers.client.spi.DeployerClient;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.spi.attachments.MutableAttachments;
import org.jboss.test.deployers.AbstractDeployerTest;
import org.jboss.test.deployers.deployer.support.TestDeployment1;
import org.jboss.test.deployers.deployer.support.TestDeploymentDeployer1;
import org.jboss.test.deployers.deployer.support.TestMetaData1;
import org.jboss.test.deployers.deployer.support.TestRealDeployer1;

/**
 * Component tests
 * 
 * @author adrian@jboss.org
 * @version $Revision: 61417 $
 */
public class ComponentUnitTestCase extends AbstractDeployerTest
{
   
   public static Test suite()
   {
      return new TestSuite(ComponentUnitTestCase.class);
   }
   
   public ComponentUnitTestCase(String name)
   {
      super(name);
   }

   public void testDeployment() throws Exception
   {
      TestDeploymentDeployer1 componentDeployer1 = new TestDeploymentDeployer1();
      TestRealDeployer1 realDeployer1 = new TestRealDeployer1();
      DeployerClient main = createMainDeployer(componentDeployer1, realDeployer1);
      
      Deployment deployment = createSimpleDeployment("deploy");
      MutableAttachments attachments = (MutableAttachments) deployment.getPredeterminedManagedObjects();

      TestDeployment1 deployment1 = new TestDeployment1();
      TestMetaData1 component1 = new TestMetaData1("TestBean1");
      deployment1.addBean(component1);
      attachments.addAttachment(TestDeployment1.class, deployment1);

      main.addDeployment(deployment);
      main.process();

      assertEquals(Collections.singletonList(component1), realDeployer1.deployed);
      
      main.removeDeployment(deployment);
      main.process();
      assertEmpty(realDeployer1.deployed);
   }

   public void testMetaData() throws Exception
   {
      TestDeploymentDeployer1 componentDeployer1 = new TestDeploymentDeployer1();
      TestRealDeployer1 realDeployer1 = new TestRealDeployer1();
      DeployerClient main = createMainDeployer(componentDeployer1, realDeployer1);
      
      Deployment deployment = createSimpleDeployment("deploy");
      MutableAttachments attachments = (MutableAttachments) deployment.getPredeterminedManagedObjects();

      TestMetaData1 component1 = new TestMetaData1("TestBean1");
      attachments.addAttachment(TestMetaData1.class, component1);

      main.addDeployment(deployment);
      main.process();

      assertEquals(Collections.singletonList(component1), realDeployer1.deployed);
      
      main.removeDeployment(deployment);
      main.process();
      assertEmpty(realDeployer1.deployed);
   }
}
