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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.deployers.client.spi.DeployerClient;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.client.spi.DeploymentFactory;
import org.jboss.deployers.spi.attachments.Attachments;
import org.jboss.deployers.spi.attachments.MutableAttachments;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.structure.ContextInfo;
import org.jboss.test.deployers.AbstractDeployerTest;
import org.jboss.test.deployers.deployer.support.ModifyComponentRelativeOrderDeployer;
import org.jboss.test.deployers.deployer.support.ModifyRelativeOrderDeployer;
import org.jboss.test.deployers.deployer.support.TestAttachmentDeployer;
import org.jboss.test.deployers.deployer.support.TestSimpleDeployer3;
import org.jboss.test.deployers.deployer.support.TestSimpleDeployer4;

/**
 * Dynamic relative order.
 *
 * @author <a href="ales.justin@jboss.org">Ales Justin</a>
 */
public class DynamicRelativeOrderUnitTestCase extends AbstractDeployerTest
{
   private static final DeploymentFactory factory = new DeploymentFactory();

   private TestSimpleDeployer3 deployer1 = new TestSimpleDeployer3(DeploymentStages.REAL);
   private TestSimpleDeployer4 deployer2 = new TestSimpleDeployer4(DeploymentStages.PRE_REAL);

   private static String P = "Parent";
   private static String C1 = P + "/" + "C1";
   private static String C2 = P + "/" + "C2";
   private static List<String> NONE = Collections.emptyList();
   private static List<String> PC2C1 = makeList(P, C2, C1);
   private static List<String> C1C2P = makeList(C1, C2, P);
   private static List<String> COMPS = makeList("4", "3", "2", "1");

   private static <T> List<T> makeList(T... objects)
   {
      List<T> result = new ArrayList<T>();
      result.addAll(Arrays.asList(objects));
      return result;
   }

   public static Test suite()
   {
      return new TestSuite(DynamicRelativeOrderUnitTestCase.class);
   }

   public DynamicRelativeOrderUnitTestCase(String name)
   {
      super(name);
   }

   public void testSubDeployments() throws Exception
   {
      DeployerClient main = getMainDeployer();

      Deployment parent = createSimpleDeployment("Parent");
      factory.addContext(parent, "C1");
      factory.addContext(parent, "C2");
      main.addDeployment(parent);

      main.process();

      assertEquals(PC2C1, deployer1.deployed);
      assertEquals(NONE, deployer1.undeployed);

      main.removeDeployment(parent);
      main.process();

      assertEquals(PC2C1, deployer1.deployed);
      assertEquals(C1C2P, deployer1.undeployed);
   }

   public void testComponents() throws Exception
   {
      DeployerClient main = getMainDeployer();

      Deployment parent = createSimpleDeployment("Parent");

      ContextInfo ci1 = factory.addContext(parent, "C1");
      Attachments attachments1 = ci1.getPredeterminedManagedObjects();
      MutableAttachments ma1 = (MutableAttachments) attachments1;
      ma1.addAttachment("1.1", "1", String.class);
      ma1.addAttachment("1.2", "2", String.class);

      ContextInfo ci2 = factory.addContext(parent, "C2");
      Attachments attachments2 = ci2.getPredeterminedManagedObjects();
      MutableAttachments ma2 = (MutableAttachments) attachments2;
      ma2.addAttachment("2.1", "3", String.class);
      ma2.addAttachment("2.2", "4", String.class);

      main.addDeployment(parent);
      main.process();

      assertEquals(PC2C1, deployer1.deployed);
      assertEquals(NONE, deployer1.undeployed);
      assertEquals(COMPS, deployer2.deployed);
      assertEquals(NONE, deployer2.undeployed);

      main.removeDeployment(parent);
      main.process();

      assertEquals(PC2C1, deployer1.deployed);
      assertEquals(C1C2P, deployer1.undeployed);
      assertEquals(COMPS, deployer2.deployed);
      Collections.reverse(COMPS);
      assertEquals(COMPS, deployer2.undeployed);
   }

   protected DeployerClient getMainDeployer()
   {
      TestAttachmentDeployer tad = new TestAttachmentDeployer(DeploymentStages.DESCRIBE);
      ModifyRelativeOrderDeployer mrod = new ModifyRelativeOrderDeployer(DeploymentStages.CLASSLOADER);
      ModifyComponentRelativeOrderDeployer mcrod = new ModifyComponentRelativeOrderDeployer(DeploymentStages.POST_CLASSLOADER);
      return createMainDeployer(mrod, mcrod, tad, deployer1, deployer2);
   }
}