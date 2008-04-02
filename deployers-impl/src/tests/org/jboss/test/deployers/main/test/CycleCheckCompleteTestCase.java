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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;
import org.jboss.dependency.plugins.AbstractController;
import org.jboss.deployers.client.spi.DeployerClient;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.client.spi.IncompleteDeploymentException;
import org.jboss.deployers.client.spi.IncompleteDeployments;
import org.jboss.deployers.client.spi.MissingDependency;
import org.jboss.deployers.plugins.deployers.DeployersImpl;
import org.jboss.deployers.plugins.main.MainDeployerImpl;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.attachments.MutableAttachments;
import org.jboss.test.deployers.main.support.OrderedTestAttachmentDeployer;
import org.jboss.test.deployers.main.support.TestAttachment;
import org.jboss.test.deployers.main.support.TestAttachments;
import org.jboss.test.deployers.main.support.TestAttachmentsDeployer;

/**
 * Check for cycles.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class CycleCheckCompleteTestCase extends AbstractMainDeployerTest
{
   public CycleCheckCompleteTestCase(String name)
   {
      super(name);
   }

   public static Test suite()
   {
      return suite(CycleCheckCompleteTestCase.class);
   }

   public void testOtherAsCause() throws Exception
   {
      DeployerClient main = getMainDeployer();

      Deployment dA = createSimpleDeployment("A");
      addAttachment(dA, "xB", true);
      addAttachment(dA, "xC", false);

      Deployment dB = createSimpleDeployment("B");
      addAttachment(dB, "xA", true);
      addAttachment(dB, "xC", false);
      addAttachment(dB, "xD", false);

      Deployment dC = createSimpleDeployment("C");
      addAttachment(dC, null, true);

      Deployment dD = createSimpleDeployment("D");
      addAttachment(dD, null, true);

      try
      {
         main.deploy(dA, dB, dC, dD);
         fail("Should not be here.");
      }
      catch (DeploymentException e)
      {
         IncompleteDeploymentException ide = assertInstanceOf(e, IncompleteDeploymentException.class);
         ide.printStackTrace();
         IncompleteDeployments id = ide.getIncompleteDeployments();
         assertNotNull(id);
         assertEmpty(id.getDeploymentsInError());
         assertEmpty(id.getDeploymentsMissingDeployer());
         assertEmpty(id.getContextsInError());
         Map<String,Set<MissingDependency>> map = id.getContextsMissingDependencies();
         assertNotNull(map);
         assertEquals(new HashSet<String>(Arrays.asList("xA", "xB")), map.keySet());
      }
   }

   protected void assertEmpty(Map<?, ?> map)
   {
      assertNotNull(map);
      assertEmpty(map.keySet());
   }

   protected DeployerClient getMainDeployer()
   {
      MainDeployerImpl main = new MainDeployerImpl();
      main.setStructuralDeployers(createStructuralDeployers());
      AbstractController controller = new AbstractController();
      DeployersImpl deployers = new DeployersImpl(controller);
      deployers.addDeployer(new TestAttachmentsDeployer());
      deployers.addDeployer(new OrderedTestAttachmentDeployer(controller));
      main.setDeployers(deployers);
      return main;
   }

   protected void addAttachment(Deployment deployment, Object dependency, boolean install)
   {
      MutableAttachments mutableAttachments = (MutableAttachments)deployment.getPredeterminedManagedObjects();
      TestAttachments testAttachments = mutableAttachments.getAttachment(TestAttachments.class);
      if (testAttachments == null)
      {
         testAttachments = new TestAttachments();
         mutableAttachments.addAttachment(TestAttachments.class, testAttachments);
      }
      TestAttachment testAttachment = new TestAttachment("x" + deployment.getName(), dependency, install);
      testAttachments.addAttachment(testAttachment);
   }
}