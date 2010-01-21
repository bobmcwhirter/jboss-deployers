/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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

package org.jboss.test.deployers.deployer.helpers.test;

import junit.framework.Test;
import org.jboss.deployers.client.spi.DeployerClient;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.spi.attachments.MutableAttachments;
import org.jboss.deployers.spi.deployer.helpers.DeploymentVisitor;
import org.jboss.deployers.spi.deployer.helpers.ExactAttachmentDeployerWithVisitor;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.test.deployers.AbstractDeployerTest;
import org.jboss.test.deployers.deployer.helpers.support.MockExtendedDeploymentVisitor;

/**
 * ExactAttachmentDeployerWithVisitorTestCase
 * 
 * Test that the {@link org.jboss.deployers.spi.deployer.helpers.ExactAttachmentDeployerWithVisitor} works as expected
 * with an {@link DeploymentVisitor}
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class ExactAttachmentDeployerWithVisitorTestCase extends AbstractDeployerTest
{
   /**
    * Constructor
    * 
    * @param name the name
    */
   public ExactAttachmentDeployerWithVisitorTestCase(String name)
   {
      super(name);
   }

   public static Test suite()
   {
      return suite(ExactAttachmentDeployerWithVisitorTestCase.class);
   }

   /**
    * Tests that the {@link org.jboss.deployers.spi.deployer.helpers.ExactAttachmentDeployerWithVisitor} works correctly with an {@link DeploymentVisitor}
    * when the unit being processed contains the expected attachment
    * 
    * @throws Exception for any error
    */
   public void testExtendedDeploymentVisitorForUnitContainingExpectedAttachment() throws Exception
   {
      // the attachment name which the visitor is interested in
      String attachmentName = "someAttachment";
      // create an mock visitor 
      MockExtendedDeploymentVisitor<String> visitor = new MockExtendedDeploymentVisitor<String>(attachmentName, String.class);
      // create the real deployer which will then use the visitor to filter out 
      // deployment units
      ExactAttachmentDeployerWithVisitor<String> deployer = new ExactAttachmentDeployerWithVisitor<String>(visitor.getVisitorTypeName(), visitor);
      // Create the main deployer which will be responsible for processing the deployment
      // unit through various stages
      DeployerClient mainDeployer = createMainDeployer(deployer);

      // some name to the deployment and create a deployment out of it
      String deploymentName = "test-deployment";
      Deployment deployment = createSimpleDeployment(deploymentName);
      MutableAttachments attachments = (MutableAttachments) deployment.getPredeterminedManagedObjects();
      
      // Jaikiran's deleted comment ;-)
      attachments.addAttachment(String.class.getName(), "IHaveNoClueWhyTheSetInputAPIWorksTheWayItDoes");
      
      // add the attachment so that this unit will be picked up by the visitor
      attachments.addAttachment(attachmentName, "Test123");

      log.debug("Deploying " + deployment);
      // deploy the deployment
      mainDeployer.deploy(deployment);

      // get the processed deployment unit
      DeploymentUnit unit = getDeploymentUnit(mainDeployer, deploymentName);

      // the processed unit is expected to contain the "processed" attachment which
      // is added by the mock visitor. This attachment indicates that the unit was
      // rightly picked up by the visitor
      assertNotNull(DeploymentVisitor.class.getName() + " did not process the unit " + unit, unit.getAttachment(MockExtendedDeploymentVisitor.PROCESSED_ATTACHMENT_NAME));
      // ensure that the unit was picked up only once by the visitor
      assertEquals("The extended deployment visitor was expected to process the unit " + unit + " exactly once", unit.getAttachment(MockExtendedDeploymentVisitor.PROCESSED_ATTACHMENT_NAME), 1);
   }

   /**
    * Tests that the {@link org.jboss.deployers.spi.deployer.helpers.ExactAttachmentDeployerWithVisitor} works correctly with an {@link DeploymentVisitor}
    * when the unit being processed does *not* contain the expected attachment
    * 
    * @throws Exception for any error
    */
   public void testExtendedDeploymentVisitorForWithoutExpectedAttachment() throws Exception
   {
      // the attachment name which the visitor is interested in
      String attachmentName = "someAttachment";
      // create an mock visitor 
      MockExtendedDeploymentVisitor<String> visitor = new MockExtendedDeploymentVisitor<String>(attachmentName, String.class);
      // create the real deployer which will then use the visitor to filter out 
      // deployment units
      ExactAttachmentDeployerWithVisitor<String> deployer = new ExactAttachmentDeployerWithVisitor<String>(visitor.getVisitorTypeName(), visitor);
      // Create the main deployer which will be responsible for processing the deployment
      // unit through various stages
      DeployerClient mainDeployer = createMainDeployer(deployer);

      // some name to the deployment and create a deployment out of it
      String deploymentName = "test-deployment";
      Deployment deployment = createSimpleDeployment(deploymentName);
      MutableAttachments attachments = (MutableAttachments) deployment.getPredeterminedManagedObjects();

      // Jaikiran's deleted comment ;-)
      attachments.addAttachment(String.class.getName(), "IHaveNoClueWhyTheSetInputAPIWorksTheWayItDoes");
      
      // add the attachment which the visitor is NOT interested in
      attachments.addAttachment("ADifferentAttachment", "Test123");

      log.debug("Deploying " + deployment);
      // deploy the deployment
      mainDeployer.deploy(deployment);

      // get the processed deployment unit
      DeploymentUnit unit = getDeploymentUnit(mainDeployer, deploymentName);

      // its expected that the visitor will NOT process this unit, since the
      // unit does not contain the expected attachment
      assertNull(DeploymentVisitor.class.getName() + " did not process the unit " + unit, unit.getAttachment(MockExtendedDeploymentVisitor.PROCESSED_ATTACHMENT_NAME));
   }
}
