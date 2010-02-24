/*
* JBoss, Home of Professional Open Source
* Copyright 2010, Red Hat Inc., and individual contributors as indicated
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

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;

import org.jboss.deployers.client.spi.DeployerClient;
import org.jboss.deployers.client.spi.DeployerClientChangeExt;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.client.spi.IncompleteDeploymentException;
import org.jboss.deployers.spi.attachments.MutableAttachments;
import org.jboss.deployers.spi.attachments.PredeterminedManagedObjectAttachments;
import org.jboss.deployers.spi.deployer.Deployer;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.test.deployers.AbstractDeployerTest;
import org.jboss.test.deployers.main.support.TestSimpleDeployer;

/**
 * DeployerClient Change Extension TestCase.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class ChangeExtTestCase extends AbstractDeployerTest
{
   protected TestSimpleDeployer deployer1 = new TestSimpleDeployer("1", DeploymentStages.PARSE);
   protected TestSimpleDeployer deployer2 = new TestSimpleDeployer("2", DeploymentStages.DESCRIBE);
   protected TestSimpleDeployer deployer3 = new TestSimpleDeployer("3", DeploymentStages.CLASSLOADER);
   protected TestSimpleDeployer deployer4 = new TestSimpleDeployer("4", DeploymentStages.PRE_REAL);
   protected TestSimpleDeployer deployer5 = new TestSimpleDeployer("5", DeploymentStages.REAL);
   
   private static ArrayList<String> NOTHING = new ArrayList<String>();
   
   public ChangeExtTestCase(String name)
   {
      super(name);
   }

   public static Test suite()
   {
      return suite(ChangeExtTestCase.class);
   }

   public void testSmoke() throws Throwable
   {
      DeployerClient main = getMainDeployer();

      Deployment single = createSimpleDeployment("single");
      main.deploy(single);
      List<String> expected = new ArrayList<String>();
      expected.add(single.getName());
      assertDeployed(expected);
      assertUndeployed(NOTHING);
      
      DeploymentUnit unit = assertDeploymentUnit(main, single.getName());
      assertEquals(main, unit.getMainDeployer());
      
      clear();
      main.undeploy(single);
      assertNull(unit.getMainDeployer());
      assertDeployed(NOTHING);
      assertUndeployed(expected);
   }

   public void testSimpleChange() throws Throwable
   {
      DeployerClient main = getMainDeployer();
      DeployerClientChangeExt change = getChangeExt(main);

      Deployment single = createSimpleDeployment("single");
      main.deploy(single);
      List<String> expected = new ArrayList<String>();
      expected.add(single.getName());
      assertDeployed(expected);
      assertUndeployed(NOTHING);
      
      DeploymentUnit unit = assertDeploymentUnit(main, single.getName());
      assertEquals(main, unit.getMainDeployer());
      
      clear();
      change.change(deployer1.getStage(), true, single.getName());
      assertDeployed(NOTHING);
      assertUndeployed(NOTHING, deployer1);
      assertUndeployed(expected, deployer2);
      assertUndeployed(expected, deployer3);
      assertUndeployed(expected, deployer4);
      assertUndeployed(expected, deployer5);
      
      clear();
      change.change(deployer5.getStage(), true, single.getName());
      assertDeployed(NOTHING, deployer1);
      assertDeployed(expected, deployer2);
      assertDeployed(expected, deployer3);
      assertDeployed(expected, deployer4);
      assertDeployed(expected, deployer5);
      assertUndeployed(NOTHING);
   }

   public void testNoMoveChange() throws Throwable
   {
      DeployerClient main = getMainDeployer();
      DeployerClientChangeExt change = getChangeExt(main);

      Deployment single = createSimpleDeployment("single");
      main.deploy(single);
      List<String> expected = new ArrayList<String>();
      expected.add(single.getName());
      assertDeployed(expected);
      assertUndeployed(NOTHING);
      
      DeploymentUnit unit = assertDeploymentUnit(main, single.getName());
      assertEquals(main, unit.getMainDeployer());
      
      clear();
      change.change(deployer5.getStage(), true, single.getName());
      assertDeployed(NOTHING);
      assertUndeployed(NOTHING);
      
      clear();
      change.change(deployer1.getStage(), true, single.getName());
      clear();
      change.change(deployer1.getStage(), true, single.getName());
      assertDeployed(NOTHING);
      assertUndeployed(NOTHING);
   }

   public void testErrorChange() throws Throwable
   {
      DeployerClient main = getMainDeployer();
      DeployerClientChangeExt change = getChangeExt(main);

      Deployment single = createSimpleDeployment("single");
      main.deploy(single);
      List<String> expected = new ArrayList<String>();
      expected.add(single.getName());
      assertDeployed(expected);
      assertUndeployed(NOTHING);
      
      DeploymentUnit unit = assertDeploymentUnit(main, single.getName());
      assertEquals(main, unit.getMainDeployer());
      
      clear();
      change.change(deployer1.getStage(), true, single.getName());
      clear();
      makeFail(single, deployer2);
      change.change(deployer5.getStage(), false, single.getName());
      assertDeployed(NOTHING, deployer1);
      assertDeployed(expected, deployer2);
      assertDeployed(NOTHING, deployer3);
      assertDeployed(NOTHING, deployer4);
      assertDeployed(NOTHING, deployer5);
      assertUndeployed(expected, deployer1);
      assertUndeployed(NOTHING, deployer2);
      assertUndeployed(NOTHING, deployer3);
      assertUndeployed(NOTHING, deployer4);
      assertUndeployed(NOTHING, deployer5);
   }

   public void testCheckCompleteChange() throws Throwable
   {
      DeployerClient main = getMainDeployer();
      DeployerClientChangeExt change = getChangeExt(main);

      Deployment single = createSimpleDeployment("single");
      main.deploy(single);
      List<String> expected = new ArrayList<String>();
      expected.add(single.getName());
      assertDeployed(expected);
      assertUndeployed(NOTHING);
      
      DeploymentUnit unit = assertDeploymentUnit(main, single.getName());
      assertEquals(main, unit.getMainDeployer());
      
      clear();
      change.change(deployer1.getStage(), true, single.getName());
      clear();
      makeFail(single, deployer2);
      try
      {
         change.change(deployer5.getStage(), true, single.getName());
         fail("Should not be here!");
      }
      catch (Throwable t)
      {
         checkThrowable(IncompleteDeploymentException.class, t);
      }
   }

   public void testMultipleChange() throws Throwable
   {
      DeployerClient main = getMainDeployer();
      DeployerClientChangeExt change = getChangeExt(main);

      Deployment a = createSimpleDeployment("A");
      main.deploy(a);
      List<String> A = new ArrayList<String>();
      A.add(a.getName());
      Deployment b = createSimpleDeployment("B");
      main.deploy(b);
      List<String> B = new ArrayList<String>();
      B.add(b.getName());
      List<String> all = new ArrayList<String>();
      all.add(a.getName());
      all.add(b.getName());
      assertDeployed(all);
      assertUndeployed(NOTHING);
      
      clear();
      change.change(deployer1.getStage(), true, a.getName(), b.getName());
      assertDeployed(NOTHING);
      assertUndeployed(NOTHING, deployer1);
      assertUndeployed(all, deployer2);
      assertUndeployed(all, deployer3);
      assertUndeployed(all, deployer4);
      assertUndeployed(all, deployer5);
      
      clear();
      change.change(deployer5.getStage(), true, a.getName(), b.getName());
      assertDeployed(NOTHING, deployer1);
      assertDeployed(all, deployer2);
      assertDeployed(all, deployer3);
      assertDeployed(all, deployer4);
      assertDeployed(all, deployer5);
      assertUndeployed(NOTHING);
      
      clear();
      change.change(deployer1.getStage(), true, a.getName());
      assertDeployed(NOTHING);
      assertUndeployed(NOTHING, deployer1);
      assertUndeployed(A, deployer2);
      assertUndeployed(A, deployer3);
      assertUndeployed(A, deployer4);
      assertUndeployed(A, deployer5);
      
      clear();
      change.change(deployer3.getStage(), true, a.getName(), b.getName());
      assertDeployed(NOTHING, deployer1);
      assertDeployed(A, deployer2);
      assertDeployed(A, deployer3);
      assertDeployed(NOTHING, deployer4);
      assertDeployed(NOTHING, deployer5);
      assertUndeployed(NOTHING, deployer1);
      assertUndeployed(NOTHING, deployer2);
      assertUndeployed(NOTHING, deployer3);
      assertUndeployed(B, deployer4);
      assertUndeployed(B, deployer5);
   }

   public void testSimpleBounce() throws Throwable
   {
      DeployerClient main = getMainDeployer();
      DeployerClientChangeExt change = getChangeExt(main);

      Deployment single = createSimpleDeployment("single");
      main.deploy(single);
      List<String> expected = new ArrayList<String>();
      expected.add(single.getName());
      assertDeployed(expected);
      assertUndeployed(NOTHING);
      
      DeploymentUnit unit = assertDeploymentUnit(main, single.getName());
      assertEquals(main, unit.getMainDeployer());
      
      clear();
      change.bounce(deployer1.getStage(), true, single.getName());
      assertDeployed(NOTHING, deployer1);
      assertDeployed(expected, deployer2);
      assertDeployed(expected, deployer3);
      assertDeployed(expected, deployer4);
      assertDeployed(expected, deployer5);
      assertUndeployed(NOTHING, deployer1);
      assertUndeployed(expected, deployer2);
      assertUndeployed(expected, deployer3);
      assertUndeployed(expected, deployer4);
      assertUndeployed(expected, deployer5);
   }

   public void testNoMoveBounce() throws Throwable
   {
      DeployerClient main = getMainDeployer();
      DeployerClientChangeExt change = getChangeExt(main);

      Deployment single = createSimpleDeployment("single");
      main.deploy(single);
      List<String> expected = new ArrayList<String>();
      expected.add(single.getName());
      assertDeployed(expected);
      assertUndeployed(NOTHING);
      
      DeploymentUnit unit = assertDeploymentUnit(main, single.getName());
      assertEquals(main, unit.getMainDeployer());
      
      change.change(deployer3.getStage(), true, single.getName());
      
      clear();
      change.bounce(deployer3.getStage(), true, single.getName());
      assertDeployed(NOTHING);
      assertUndeployed(NOTHING);
   }

   public void testNoMovePreviousStateBounce() throws Throwable
   {
      DeployerClient main = getMainDeployer();
      DeployerClientChangeExt change = getChangeExt(main);

      Deployment single = createSimpleDeployment("single");
      main.deploy(single);
      List<String> expected = new ArrayList<String>();
      expected.add(single.getName());
      assertDeployed(expected);
      assertUndeployed(NOTHING);
      
      DeploymentUnit unit = assertDeploymentUnit(main, single.getName());
      assertEquals(main, unit.getMainDeployer());
      
      change.change(deployer1.getStage(), true, single.getName());
      
      clear();
      change.bounce(deployer3.getStage(), true, single.getName());
      assertDeployed(NOTHING);
      assertUndeployed(NOTHING);
   }

   public void testErrorBounce() throws Throwable
   {
      DeployerClient main = getMainDeployer();
      DeployerClientChangeExt change = getChangeExt(main);

      Deployment single = createSimpleDeployment("single");
      main.deploy(single);
      List<String> expected = new ArrayList<String>();
      expected.add(single.getName());
      assertDeployed(expected);
      assertUndeployed(NOTHING);
      
      DeploymentUnit unit = assertDeploymentUnit(main, single.getName());
      assertEquals(main, unit.getMainDeployer());
      
      makeFail(single, deployer2);
      
      clear();
      change.bounce(deployer1.getStage(), false, single.getName());
      assertDeployed(NOTHING, deployer1);
      assertDeployed(expected, deployer2);
      assertDeployed(NOTHING, deployer3);
      assertDeployed(NOTHING, deployer4);
      assertDeployed(NOTHING, deployer5);
      assertUndeployed(expected);
   }

   public void testCheckCompleteBounce() throws Throwable
   {
      DeployerClient main = getMainDeployer();
      DeployerClientChangeExt change = getChangeExt(main);

      Deployment single = createSimpleDeployment("single");
      main.deploy(single);
      List<String> expected = new ArrayList<String>();
      expected.add(single.getName());
      assertDeployed(expected);
      assertUndeployed(NOTHING);
      
      DeploymentUnit unit = assertDeploymentUnit(main, single.getName());
      assertEquals(main, unit.getMainDeployer());
      
      makeFail(single, deployer2);
      
      clear();
      try
      {
         change.bounce(deployer1.getStage(), true, single.getName());
         fail("Should not be here!");
      }
      catch (Throwable t)
      {
         checkThrowable(IncompleteDeploymentException.class, t);
      }
   }

   public void testMultipleBounce() throws Throwable
   {
      DeployerClient main = getMainDeployer();
      DeployerClientChangeExt change = getChangeExt(main);

      Deployment a = createSimpleDeployment("A");
      main.deploy(a);
      Deployment b = createSimpleDeployment("B");
      main.deploy(b);
      Deployment c = createSimpleDeployment("C");
      main.deploy(c);
      List<String> all = new ArrayList<String>();
      all.add(a.getName());
      all.add(b.getName());
      all.add(c.getName());
      List<String> BC = new ArrayList<String>();
      BC.add(b.getName());
      BC.add(c.getName());
      List<String> C = new ArrayList<String>();
      C.add(c.getName());
      assertDeployed(all);
      assertUndeployed(NOTHING);
      
      change.change(deployer1.getStage(), true, a.getName());
      change.change(deployer3.getStage(), true, b.getName());
      change.change(deployer4.getStage(), true, c.getName());
      
      clear();
      change.bounce(deployer2.getStage(), true, a.getName(), b.getName(), c.getName());
      assertDeployed(NOTHING, deployer1);
      assertDeployed(NOTHING, deployer2);
      assertDeployed(BC, deployer3);
      assertDeployed(C, deployer4);
      assertUndeployed(NOTHING, deployer5);
      assertUndeployed(NOTHING, deployer1);
      assertUndeployed(NOTHING, deployer2);
      assertUndeployed(BC, deployer3);
      assertUndeployed(C, deployer4);
      assertUndeployed(NOTHING, deployer5);
   }

   protected DeployerClient getMainDeployer()
   {
      return createMainDeployer(deployer1, deployer2, deployer3, deployer4, deployer5);
   }

   protected DeployerClientChangeExt getChangeExt(DeployerClient deployerClient)
   {
      return assertInstanceOf(deployerClient, DeployerClientChangeExt.class);
   }

   protected void clear()
   {
      deployer1.clear();
      deployer2.clear();
      deployer3.clear();
      deployer4.clear();
      deployer5.clear();
   }
   
   protected static void makeFail(PredeterminedManagedObjectAttachments attachments, Deployer deployer)
   {
      MutableAttachments mutable = (MutableAttachments) attachments.getPredeterminedManagedObjects();
      mutable.addAttachment("fail", deployer);
   }

   protected void assertDeployed(List<String> expected)
   {
      assertDeployed(expected, deployer1);
      assertDeployed(expected, deployer2);
      assertDeployed(expected, deployer3);
      assertDeployed(expected, deployer4);
      assertDeployed(expected, deployer5);
   }

   protected void assertDeployed(List<String> expected, TestSimpleDeployer deployer)
   {
      assertEquals(deployer.toString(), expected, deployer.getDeployedUnits());
   }

   protected void assertUndeployed(List<String> expected)
   {
      assertUndeployed(expected, deployer1);
      assertUndeployed(expected, deployer2);
      assertUndeployed(expected, deployer3);
      assertUndeployed(expected, deployer4);
      assertUndeployed(expected, deployer5);
   }

   protected void assertUndeployed(List<String> expected, TestSimpleDeployer deployer)
   {
      assertEquals(deployer.toString(), expected, deployer.getUndeployedUnits());
   }
}
