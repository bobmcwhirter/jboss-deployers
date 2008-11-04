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
package org.jboss.test.deployers.main.test;

import junit.framework.Test;
import org.jboss.dependency.plugins.AbstractController;
import org.jboss.deployers.client.spi.DeployerClient;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.client.spi.IncompleteDeploymentException;
import org.jboss.deployers.plugins.deployers.DeployersImpl;
import org.jboss.deployers.plugins.main.MainDeployerImpl;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.attachments.MutableAttachments;
import org.jboss.deployers.spi.deployer.helpers.DefaultManagedObjectCreator;
import org.jboss.deployers.structure.spi.DeploymentContext;
import org.jboss.deployers.structure.spi.StructuralDeployers;
import org.jboss.test.deployers.main.support.DependencyDeployer;
import org.jboss.test.deployers.main.support.TestAttachment;
import org.jboss.test.deployers.main.support.TestAttachmentDeployer;
import org.jboss.test.deployers.main.support.TestAttachments;
import org.jboss.test.deployers.main.support.TestAttachmentsDeployer;

/**
 * Check complete deployment test case.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class DeployerCheckCompleteTestCase extends AbstractMainDeployerTest
{
   public DeployerCheckCompleteTestCase(String name)
   {
      super(name);
   }

   public static Test suite()
   {
      return suite(DeployerCheckCompleteTestCase.class);
   }

   public void testAllFailure() throws Exception
   {
      DeployerClient main = getMainDeployer();
      Deployment deployment = createSimpleDeployment("failure");
      makeFail(deployment, deployer);
      main.addDeployment(deployment);
      main.process();
      try
      {
         main.checkComplete(deployment);
         fail("Should not be here.");
      }
      catch (DeploymentException e)
      {
         assertInstanceOf(e, IncompleteDeploymentException.class);
      }
   }

   public void testAllSuccess() throws Exception
   {
      DeployerClient main = getMainDeployer();
      Deployment deployment = createSimpleDeployment("failure");
      main.addDeployment(deployment);
      main.process();
      main.checkComplete(deployment);
   }

   public void testStructureFailure() throws Exception
   {
      DeployerClient main = getMainDeployer();
      MainDeployerImpl mainImpl = (MainDeployerImpl)main;
      StructuralDeployers sd = new StructuralDeployers()
      {
         public DeploymentContext determineStructure(Deployment deployment) throws DeploymentException
         {
            throw new DeploymentException("No such structure deployers.");
         }
      };
      mainImpl.setStructuralDeployers(sd);
      Deployment deployment = createSimpleDeployment("failure");
      makeFail(deployment, deployer);
      try
      {
         main.addDeployment(deployment);
      }
      catch (DeploymentException ignored)
      {
      }
      main.process();
      try
      {
         main.checkComplete();
         fail("Should not be here.");
      }
      catch (DeploymentException e)
      {
         assertInstanceOf(e, IncompleteDeploymentException.class);
      }
   }

   public void testStructureSuccess() throws Exception
   {
      DeployerClient main = getMainDeployer();
      Deployment deployment = createSimpleDeployment("failure");
      main.addDeployment(deployment);
      main.process();
      main.checkStructureComplete(deployment);
   }

   public void testHalfThenAll() throws Exception
   {
      DeployerClient main = getDependencyMainDeployer();

      Deployment dA = createSimpleDeployment("A");
      addAttachment(dA, "xB");

      main.addDeployment(dA);
      main.process();
      try
      {
         main.checkComplete(dA);
         fail("Should not be here.");
      }
      catch (DeploymentException e)
      {
         assertInstanceOf(e, IncompleteDeploymentException.class);
      }

      Deployment dB = createSimpleDeployment("B");
      addAttachment(dB, null);
      main.deploy(dB);

      main.checkComplete(dA);
   }

   public void testAllThenHalf() throws Exception
   {
      DeployerClient main = getDependencyMainDeployer();

      Deployment dA = createSimpleDeployment("A");
      addAttachment(dA, "xB");

      main.addDeployment(dA);
      main.process();

      Deployment dB = createSimpleDeployment("B");
      addAttachment(dB, null);
      main.deploy(dB);

      main.checkComplete(dA);

      main.undeploy(dB);

      try
      {
         main.checkComplete(dA);
         fail("Should not be here.");
      }
      catch (DeploymentException e)
      {
         assertInstanceOf(e, IncompleteDeploymentException.class);
      }
   }

   public void testComponentAllThenHalf() throws Exception
   {
      DeployerClient main = getComponentMainDeployer();

      Deployment dA = createSimpleDeployment("A");
      addComponentAttachment(dA, "xB");

      main.addDeployment(dA);
      main.process();

      Deployment dB = createSimpleDeployment("B");
      addComponentAttachment(dB, null);
      main.deploy(dB);

      main.checkComplete(dA);

      main.undeploy(dB);

      try
      {
         main.checkComplete(dA);
         fail("Should not be here.");
      }
      catch (DeploymentException e)
      {
         assertInstanceOf(e, IncompleteDeploymentException.class);
      }
   }

   protected DeployerClient getDependencyMainDeployer()
   {
      MainDeployerImpl main = new MainDeployerImpl();
      main.setStructuralDeployers(createStructuralDeployers());
      AbstractController controller = new AbstractController();
      DeployersImpl deployers = new DeployersImpl(controller);
      deployers.setMgtObjectCreator(new DefaultManagedObjectCreator());
      deployers.addDeployer(new DependencyDeployer(controller));
      main.setDeployers(deployers);
      return main;
   }

   protected DeployerClient getComponentMainDeployer()
   {
      MainDeployerImpl main = new MainDeployerImpl();
      main.setStructuralDeployers(createStructuralDeployers());
      AbstractController controller = new AbstractController();
      DeployersImpl deployers = new DeployersImpl(controller);
      deployers.setMgtObjectCreator(new DefaultManagedObjectCreator());
      deployers.addDeployer(new TestAttachmentsDeployer());
      deployers.addDeployer(new TestAttachmentDeployer(controller));
      main.setDeployers(deployers);
      return main;
   }

   protected void addAttachment(Deployment deployment, Object dependency)
   {
      MutableAttachments mutableAttachments = (MutableAttachments)deployment.getPredeterminedManagedObjects();
      mutableAttachments.addAttachment(TestAttachment.class, new TestAttachment("x" + deployment.getName(), dependency));
   }

   protected void addComponentAttachment(Deployment deployment, Object dependency)
   {
      MutableAttachments mutableAttachments = (MutableAttachments)deployment.getPredeterminedManagedObjects();
      TestAttachment testAttachment = new TestAttachment("x" + deployment.getName(), dependency);
      TestAttachments testAttachments = new TestAttachments();
      testAttachments.addAttachment(testAttachment);
      mutableAttachments.addAttachment(TestAttachments.class, testAttachments);
   }
}
