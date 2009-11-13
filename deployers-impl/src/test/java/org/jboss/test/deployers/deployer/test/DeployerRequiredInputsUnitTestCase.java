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

import java.util.Calendar;
import java.util.Collections;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.deployers.client.spi.DeployerClient;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.spi.attachments.MutableAttachments;
import org.jboss.deployers.spi.deployer.DeploymentStage;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.test.deployers.AbstractDeployerTest;
import org.jboss.test.deployers.deployer.support.TestSimpleDeployer;

/**
 * DeployerRequiredInputsUnitTestCase.
 *
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 */
public class DeployerRequiredInputsUnitTestCase extends AbstractDeployerTest
{
   public static Test suite()
   {
      return new TestSuite(DeployerRequiredInputsUnitTestCase.class);
   }

   public DeployerRequiredInputsUnitTestCase(String name)
   {
      super(name);
   }

   public void testRequiredState() throws Exception
   {
      TestSimpleDeployer missing = new TestSimpleDeployer();
      missing.addRequiredInput(Calendar.class); // I guess no calendar is present :-)
      assertTrue(missing.getInputs().contains(Calendar.class.getName()));

      TestSimpleDeployer all = new TestSimpleDeployer();
      all.addRequiredInput(DeploymentStage.class.getName());
      assertTrue(all.getInputs().contains(DeploymentStage.class.getName()));

      TestSimpleDeployer empty = new TestSimpleDeployer();

      DeployerClient main = createMainDeployer(missing, all, empty);

      String name = "simple";
      Deployment deployment = createSimpleDeployment(name);

      MutableAttachments attachments = (MutableAttachments) deployment.getPredeterminedManagedObjects();
      attachments.addAttachment(DeploymentStage.class, DeploymentStages.DESCRIBE);

      Set<String> singleton;
      main.deploy(deployment);
      try
      {
         DeploymentUnit unit = getDeploymentUnit(main, name);
         singleton = Collections.singleton(unit.getName());

         assertEmpty(missing.getDeployedUnits());
         assertEquals(singleton, all.getDeployedUnits());
         assertEquals(singleton, empty.getDeployedUnits());
      }
      finally
      {
         main.undeploy(deployment);
      }

      assertEmpty(missing.getUndeployedUnits());
      assertEquals(singleton, all.getUndeployedUnits());
      assertEquals(singleton, empty.getUndeployedUnits());        
   }
}