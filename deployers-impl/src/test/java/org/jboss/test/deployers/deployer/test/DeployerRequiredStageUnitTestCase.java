/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
import org.jboss.deployers.spi.attachments.MutableAttachments;
import org.jboss.deployers.spi.deployer.DeploymentStage;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.test.deployers.AbstractDeployerTest;
import org.jboss.test.deployers.deployer.support.RequiredStageDeployer;

/**
 * DeployerRequiredStageUnitTestCase.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class DeployerRequiredStageUnitTestCase extends AbstractDeployerTest
{
   public static Test suite()
   {
      return new TestSuite(DeployerRequiredStageUnitTestCase.class);
   }
   
   public DeployerRequiredStageUnitTestCase(String name)
   {
      super(name);
   }

   public void testRequiredState() throws Exception
   {
      RequiredStageDeployer deployer = new RequiredStageDeployer();
      deployer.setStage(DeploymentStages.POST_PARSE);
      DeployerClient main = createMainDeployer(deployer);
      
      String name = "simple";
      
      Deployment deployment = createSimpleDeployment(name);
      MutableAttachments attachments = (MutableAttachments) deployment.getPredeterminedManagedObjects();
      attachments.addAttachment(DeploymentStage.class, DeploymentStages.DESCRIBE);
      main.deploy(deployment);

      DeploymentUnit unit = getDeploymentUnit(main, name);
      
      assertEquals(DeploymentStages.DESCRIBE, unit.getRequiredStage());
      assertEquals(DeploymentStages.DESCRIBE, main.getDeploymentStage(name));
      
      main.change(name, DeploymentStages.INSTALLED);
      assertEquals(DeploymentStages.INSTALLED, unit.getRequiredStage());
      assertEquals(DeploymentStages.INSTALLED, main.getDeploymentStage(name));
   }
}
