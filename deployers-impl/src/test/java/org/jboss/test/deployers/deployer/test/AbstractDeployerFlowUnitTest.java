/*
 * JBoss, Home of Professional Open Source
 * Copyright (c) 2009, JBoss Inc., and individual contributors as indicated
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

import java.util.LinkedList;
import java.util.List;

import org.jboss.deployers.client.spi.DeployerClient;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.plugins.deployers.DeployersImpl;
import org.jboss.deployers.spi.deployer.Deployers;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.test.deployers.AbstractDeployerTest;
import org.jboss.test.deployers.deployer.support.TestDeployerAdapter;
import org.jboss.test.deployers.deployer.support.TestFlowDeployer;

/**
 * DeployerOrderingUnitTestCase.
 *
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 * @author <a href="cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: 1.1 $
 */
public abstract class AbstractDeployerFlowUnitTest extends AbstractDeployerTest
{
   public AbstractDeployerFlowUnitTest(String name)
   {
      super(name);
   }

   protected void setUp() throws Exception
   {
      super.setUp();
      TestFlowDeployer.reset();
   }

   @Override
   protected Deployers createDeployers()
   {
      Deployers deployers = super.createDeployers();
      DeployersImpl impl = assertInstanceOf(deployers, DeployersImpl.class, false);
      applySortingChanges(impl);
      return impl;
   }

   protected abstract void applySortingChanges(DeployersImpl deployers);

   public void testSimpleInputOutputCorrectOrder() throws Exception
   {
      DeployerClient main = createMainDeployer();
      TestFlowDeployer deployer1 = new TestFlowDeployer("1");
      deployer1.setOutputs("test");
      addDeployer(main, deployer1);
      TestFlowDeployer deployer2 = new TestFlowDeployer("2");
      deployer2.setInputs("test");
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

   public void testSimpleInputOutputWrongOrder() throws Exception
   {
      DeployerClient main = createMainDeployer();
      TestFlowDeployer deployer2 = new TestFlowDeployer("2");
      deployer2.setInputs("test");
      addDeployer(main, deployer2);
      TestFlowDeployer deployer1 = new TestFlowDeployer("1");
      deployer1.setOutputs("test");
      addDeployer(main, deployer1);

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

   public void testInputOutputLoop() throws Exception
   {
      DeployerClient main = createMainDeployer();
      TestFlowDeployer deployer1 = new TestFlowDeployer("1");
      deployer1.setInputs("A");
      deployer1.setOutputs("B");
      addDeployer(main, deployer1);
      TestFlowDeployer deployer2 = new TestFlowDeployer("2");
      deployer2.setInputs("B");
      deployer2.setOutputs("C");
      addDeployer(main, deployer2);
      TestFlowDeployer deployer3 = new TestFlowDeployer("3");
      deployer3.setInputs("C");
      deployer3.setOutputs("A");
      try
      {
         addDeployer(main, deployer3);
         fail("Should not be here!");
      }
      catch (Throwable t)
      {
         checkThrowable(IllegalStateException.class, t);
      }
   }

   public void testInputOutputTransient() throws Exception
   {
      DeployerClient main = createMainDeployer();
      TestFlowDeployer deployer1 = new TestFlowDeployer("1");
      deployer1.setOutputs("test");
      addDeployer(main, deployer1);
      TestFlowDeployer deployer3 = new TestFlowDeployer("3");
      deployer3.setInputs("test");
      addDeployer(main, deployer3);
      TestFlowDeployer deployer2 = new TestFlowDeployer("2");
      deployer2.setInputs("test");
      deployer2.setOutputs("test");
      addDeployer(main, deployer2);

      Deployment deployment = createSimpleDeployment("transient");
      main.addDeployment(deployment);
      main.process();

      // B can appear at any position
      // BCA, CBA, CAB
      assertDeployBefore(deployer2, deployer1);
      assertTrue("C doesn't deploy", deployer1.getDeployOrder() > 0);
      assertEquals(-1, deployer1.getUndeployOrder());
      assertEquals(-1, deployer2.getUndeployOrder());
      assertEquals(-1, deployer3.getUndeployOrder());

      main.removeDeployment(deployment);
      main.process();

      assertEquals(1, deployer1.getDeployOrder());
      assertEquals(2, deployer2.getDeployOrder());
      assertEquals(3, deployer3.getDeployOrder());
      assertEquals(6, deployer1.getUndeployOrder());
      assertEquals(5, deployer2.getUndeployOrder());
      assertEquals(4, deployer3.getUndeployOrder());

      main.addDeployment(deployment);
      main.process();

      assertEquals(7, deployer1.getDeployOrder());
      assertEquals(8, deployer2.getDeployOrder());
      assertEquals(9, deployer3.getDeployOrder());
      assertEquals(6, deployer1.getUndeployOrder());
      assertEquals(5, deployer2.getUndeployOrder());
      assertEquals(4, deployer3.getUndeployOrder());
   }

   public void testInputOutputTransient2() throws Exception
   {
      DeployerClient main = createMainDeployer();
      TestFlowDeployer deployer1 = new TestFlowDeployer("1");
      deployer1.setInputs("test");
      deployer1.setOutputs("test");
      addDeployer(main, deployer1);
      TestFlowDeployer deployer2 = new TestFlowDeployer("2");
      deployer2.setInputs("test");
      addDeployer(main, deployer2);

      Deployment deployment = createSimpleDeployment("transient2");
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

   public void testInputOutputMultipleTransient() throws Exception
   {
      DeployerClient main = createMainDeployer();
      TestFlowDeployer deployer4 = new TestFlowDeployer("4");
      deployer4.setInputs("test");
      addDeployer(main, deployer4);
      TestFlowDeployer deployer2 = new TestFlowDeployer("2");
      deployer2.setInputs("test");
      deployer2.setOutputs("test");
      addDeployer(main, deployer2);
      TestFlowDeployer deployer3 = new TestFlowDeployer("3");
      deployer3.setInputs("test");
      deployer3.setOutputs("test");
      addDeployer(main, deployer3);
      TestFlowDeployer deployer1 = new TestFlowDeployer("1");
      deployer1.setOutputs("test");
      addDeployer(main, deployer1);

      Deployment deployment = createSimpleDeployment("transient");
      main.addDeployment(deployment);
      main.process();

      assertEquals(1, deployer1.getDeployOrder());
      assertEquals(2, deployer2.getDeployOrder());
      assertEquals(3, deployer3.getDeployOrder());
      assertEquals(4, deployer4.getDeployOrder());
      assertEquals(-1, deployer1.getUndeployOrder());
      assertEquals(-1, deployer2.getUndeployOrder());
      assertEquals(-1, deployer3.getUndeployOrder());
      assertEquals(-1, deployer4.getUndeployOrder());

      main.removeDeployment(deployment);
      main.process();

      assertEquals(1, deployer1.getDeployOrder());
      assertEquals(2, deployer2.getDeployOrder());
      assertEquals(3, deployer3.getDeployOrder());
      assertEquals(4, deployer4.getDeployOrder());
      assertEquals(8, deployer1.getUndeployOrder());
      assertEquals(7, deployer2.getUndeployOrder());
      assertEquals(6, deployer3.getUndeployOrder());
      assertEquals(5, deployer4.getUndeployOrder());

      main.addDeployment(deployment);
      main.process();

      assertEquals(9, deployer1.getDeployOrder());
      assertEquals(10, deployer2.getDeployOrder());
      assertEquals(11, deployer3.getDeployOrder());
      assertEquals(12, deployer4.getDeployOrder());
      assertEquals(8, deployer1.getUndeployOrder());
      assertEquals(7, deployer2.getUndeployOrder());
      assertEquals(6, deployer3.getUndeployOrder());
      assertEquals(5, deployer4.getUndeployOrder());
   }

   public void testMultipleOutput() throws Exception
   {
      DeployerClient main = createMainDeployer();
      TestFlowDeployer deployer1 = new TestFlowDeployer("1");
      deployer1.setOutputs("test1", "test2");
      addDeployer(main, deployer1);
      TestFlowDeployer deployer2 = new TestFlowDeployer("2");
      deployer2.setInputs("test1");
      addDeployer(main, deployer2);
      TestFlowDeployer deployer3 = new TestFlowDeployer("3");
      deployer3.setInputs("test2");
      addDeployer(main, deployer3);

      Deployment deployment = createSimpleDeployment("MultipleOutput");
      main.addDeployment(deployment);
      main.process();

      assertDeployBefore(deployer2, deployer1);
      assertDeployBefore(deployer3, deployer1);
      assertEquals(-1, deployer1.getUndeployOrder());
      assertEquals(-1, deployer2.getUndeployOrder());
      assertEquals(-1, deployer3.getUndeployOrder());

      main.removeDeployment(deployment);
      main.process();

      assertDeployBefore(deployer2, deployer1);
      assertDeployBefore(deployer3, deployer1);
      assertUndeployAfter(deployer2, deployer1);
      assertUndeployAfter(deployer3, deployer1);

      main.addDeployment(deployment);
      main.process();

      assertDeployBefore(deployer2, deployer1);
      assertDeployBefore(deployer3, deployer1);
      assertUndeployAfter(deployer2, deployer1);
      assertUndeployAfter(deployer3, deployer1);
   }

   public void testMultipleInput() throws Exception
   {
      DeployerClient main = createMainDeployer();
      TestFlowDeployer deployer3 = new TestFlowDeployer("in12");
      deployer3.setInputs("test1", "test2");
      addDeployer(main, deployer3);
      TestFlowDeployer deployer1 = new TestFlowDeployer("out1");
      deployer1.setOutputs("test1");
      addDeployer(main, deployer1);
      TestFlowDeployer deployer2 = new TestFlowDeployer("out2");
      deployer2.setOutputs("test2");
      addDeployer(main, deployer2);

      Deployment deployment = createSimpleDeployment("MultipleInput");
      main.addDeployment(deployment);
      main.process();

      assertDeployBefore(deployer3, deployer1);
      assertDeployBefore(deployer3, deployer2);
      assertEquals(3, deployer3.getDeployOrder());
      assertEquals(-1, deployer1.getUndeployOrder());
      assertEquals(-1, deployer2.getUndeployOrder());
      assertEquals(-1, deployer3.getUndeployOrder());

      main.removeDeployment(deployment);
      main.process();

      assertDeployBefore(deployer3, deployer1);
      assertDeployBefore(deployer3, deployer2);
      assertEquals(3, deployer3.getDeployOrder());
      assertUndeployAfter(deployer3, deployer1);
      assertUndeployAfter(deployer3, deployer2);
      assertEquals(4, deployer3.getUndeployOrder());

      main.addDeployment(deployment);
      main.process();

      assertDeployBefore(deployer3, deployer1);
      assertDeployBefore(deployer3, deployer2);
      assertEquals(9, deployer3.getDeployOrder());
      assertUndeployAfter(deployer3, deployer1);
      assertUndeployAfter(deployer3, deployer2);
      assertEquals(4, deployer3.getUndeployOrder());
   }

   public void testChain() throws Exception
   {
      DeployerClient main = createMainDeployer();
      TestFlowDeployer deployer3 = new TestFlowDeployer("3");
      deployer3.setInputs("test2");
      addDeployer(main, deployer3);
      TestFlowDeployer deployer2 = new TestFlowDeployer("2");
      deployer2.setInputs("test1");
      deployer2.setOutputs("test2");
      addDeployer(main, deployer2);
      TestFlowDeployer deployer1 = new TestFlowDeployer("1");
      deployer1.setOutputs("test1");
      addDeployer(main, deployer1);

      Deployment deployment = createSimpleDeployment("Chain");
      main.addDeployment(deployment);
      main.process();

      assertEquals(1, deployer1.getDeployOrder());
      assertEquals(2, deployer2.getDeployOrder());
      assertEquals(3, deployer3.getDeployOrder());
      assertEquals(-1, deployer1.getUndeployOrder());
      assertEquals(-1, deployer2.getUndeployOrder());
      assertEquals(-1, deployer3.getUndeployOrder());

      main.removeDeployment(deployment);
      main.process();

      assertEquals(1, deployer1.getDeployOrder());
      assertEquals(2, deployer2.getDeployOrder());
      assertEquals(3, deployer3.getDeployOrder());
      assertEquals(6, deployer1.getUndeployOrder());
      assertEquals(5, deployer2.getUndeployOrder());
      assertEquals(4, deployer3.getUndeployOrder());

      main.addDeployment(deployment);
      main.process();

      assertEquals(7, deployer1.getDeployOrder());
      assertEquals(8, deployer2.getDeployOrder());
      assertEquals(9, deployer3.getDeployOrder());
      assertEquals(6, deployer1.getUndeployOrder());
      assertEquals(5, deployer2.getUndeployOrder());
      assertEquals(4, deployer3.getUndeployOrder());
   }

   public void testComplicated() throws Exception
   {
      // (1) (2) (3)
      //  \   /   |
      //    T1    T2
      //    |     |
      //         (4)
      //          |
      //          T3
      //          |
      //         (5)
      //    |     |
      //    T1    T3
      //      \   /
      //       (6)
      DeployerClient main = createMainDeployer();
      TestFlowDeployer deployer6 = new TestFlowDeployer("6");
      deployer6.setInputs("test1", "test3");
      addDeployer(main, deployer6);
      TestFlowDeployer deployer5 = new TestFlowDeployer("5");
      deployer5.setInputs("test3");
      deployer5.setOutputs("test3");
      addDeployer(main, deployer5);
      TestFlowDeployer deployer4 = new TestFlowDeployer("4");
      deployer4.setInputs("test2");
      deployer4.setOutputs("test3");
      addDeployer(main, deployer4);
      TestFlowDeployer deployer3 = new TestFlowDeployer("3");
      deployer3.setOutputs("test2");
      addDeployer(main, deployer3);
      TestFlowDeployer deployer2 = new TestFlowDeployer("2");
      deployer2.setOutputs("test1");
      addDeployer(main, deployer2);
      TestFlowDeployer deployer1 = new TestFlowDeployer("1");
      deployer1.setOutputs("test1");
      addDeployer(main, deployer1);

      Deployment deployment = createSimpleDeployment("Complicated");
      main.addDeployment(deployment);
      main.process();

      assertDeployBefore(deployer6, deployer1);
      assertDeployBefore(deployer6, deployer2);
      assertDeployBefore(deployer4, deployer3);
      assertDeployBefore(deployer5, deployer4);
      assertDeployBefore(deployer6, deployer5);
      assertEquals(-1, deployer1.getUndeployOrder());
      assertEquals(-1, deployer2.getUndeployOrder());
      assertEquals(-1, deployer3.getUndeployOrder());
      assertEquals(-1, deployer4.getUndeployOrder());
      assertEquals(-1, deployer5.getUndeployOrder());
      assertEquals(-1, deployer6.getUndeployOrder());

      main.removeDeployment(deployment);
      main.process();

      assertDeployBefore(deployer6, deployer1);
      assertDeployBefore(deployer6, deployer2);
      assertDeployBefore(deployer4, deployer3);
      assertDeployBefore(deployer5, deployer4);
      assertDeployBefore(deployer6, deployer5);
      assertUndeployAfter(deployer6, deployer1);
      assertUndeployAfter(deployer6, deployer2);
      assertUndeployAfter(deployer4, deployer3);
      assertUndeployAfter(deployer5, deployer4);
      assertUndeployAfter(deployer6, deployer5);

      main.addDeployment(deployment);
      main.process();

      assertDeployBefore(deployer6, deployer1);
      assertDeployBefore(deployer6, deployer2);
      assertDeployBefore(deployer4, deployer3);
      assertDeployBefore(deployer5, deployer4);
      assertDeployBefore(deployer6, deployer5);
      assertUndeployAfter(deployer6, deployer1);
      assertUndeployAfter(deployer6, deployer2);
      assertUndeployAfter(deployer4, deployer3);
      assertUndeployAfter(deployer5, deployer4);
      assertUndeployAfter(deployer6, deployer5);
   }

   public void testIntermediateIsRelativelySorted() throws Exception
   {
      DeployerClient main = createMainDeployer();
      TestFlowDeployer deployer2 = new TestFlowDeployer("A");
      deployer2.setInputs("test1");
      addDeployer(main, deployer2);
      TestFlowDeployer deployer3 = new TestFlowDeployer("B");
      addDeployer(main, deployer3);
      TestFlowDeployer deployer1 = new TestFlowDeployer("C");
      deployer1.setOutputs("test1");
      addDeployer(main, deployer1);

      Deployment deployment = createSimpleDeployment("IntermediateIsRelativelySorted");
      main.addDeployment(deployment);
      main.process();

      assertDeployBefore(deployer2, deployer1);
      assertTrue("B doesn't deploy", deployer3.getDeployOrder() > 0);
      assertEquals(-1, deployer1.getUndeployOrder());
      assertEquals(-1, deployer2.getUndeployOrder());
      assertEquals(-1, deployer3.getUndeployOrder());

      main.removeDeployment(deployment);
      main.process();

      assertDeployBefore(deployer2, deployer1);
      assertTrue("B doesn't deploy", deployer3.getDeployOrder() > 0);
      assertUndeployAfter(deployer2, deployer1);
      assertTrue("B doesn't undeploy", deployer3.getUndeployOrder() > 0);

      main.addDeployment(deployment);
      main.process();

      assertDeployBefore(deployer2, deployer1);
      assertTrue("B doesn't deploy", deployer3.getDeployOrder() > 0);
      assertUndeployAfter(deployer2, deployer1);
      assertTrue("B doesn't undeploy", deployer3.getUndeployOrder() > 0);
   }

   public void testTransitionOrdering() throws Exception
   {
      DeployerClient main = createMainDeployer();
      TestFlowDeployer deployer1 = new TestFlowDeployer("A");
      deployer1.setInputs("3");
      deployer1.setOutputs("4");
      addDeployer(main, deployer1);

      TestFlowDeployer deployer2 = new TestFlowDeployer("B");
      deployer2.setInputs("1");
      deployer2.setOutputs("2");
      addDeployer(main, deployer2);

      TestFlowDeployer deployer3 = new TestFlowDeployer("C");
      deployer3.setInputs("2");
      deployer3.setOutputs("3");
      addDeployer(main, deployer3);

      Deployment deployment = createSimpleDeployment("TransitionOrdering");
      main.addDeployment(deployment);
      main.process();

      assertEquals(3, deployer1.getDeployOrder());
      assertEquals(1, deployer2.getDeployOrder());
      assertEquals(2, deployer3.getDeployOrder());
      assertEquals(-1, deployer1.getUndeployOrder());
      assertEquals(-1, deployer2.getUndeployOrder());
      assertEquals(-1, deployer3.getUndeployOrder());

      main.removeDeployment(deployment);
      main.process();

      assertEquals(3, deployer1.getDeployOrder());
      assertEquals(1, deployer2.getDeployOrder());
      assertEquals(2, deployer3.getDeployOrder());
      assertEquals(4, deployer1.getUndeployOrder());
      assertEquals(6, deployer2.getUndeployOrder());
      assertEquals(5, deployer3.getUndeployOrder());

      main.addDeployment(deployment);
      main.process();

      assertEquals(9, deployer1.getDeployOrder());
      assertEquals(7, deployer2.getDeployOrder());
      assertEquals(8, deployer3.getDeployOrder());
      assertEquals(4, deployer1.getUndeployOrder());
      assertEquals(6, deployer2.getUndeployOrder());
      assertEquals(5, deployer3.getUndeployOrder());
   }

   public void testSymetricDots() throws Exception
   {
      DeployerClient main = createMainDeployer();
      TestFlowDeployer deployer1 = new TestFlowDeployer("XB");
      deployer1.setInputs("X");
      deployer1.setOutputs("B");
      addDeployer(main, deployer1);

      TestFlowDeployer deployer2 = new TestFlowDeployer("XX");
      deployer2.setInputs("X");
      deployer2.setOutputs("X");
      addDeployer(main, deployer2);

      TestFlowDeployer deployer3 = new TestFlowDeployer("AX");
      deployer3.setInputs("A");
      deployer3.setOutputs("X");
      addDeployer(main, deployer3);

      Deployment deployment = createSimpleDeployment("SymetricDots");
      main.addDeployment(deployment);
      main.process();

      assertEquals(3, deployer1.getDeployOrder());
      assertEquals(2, deployer2.getDeployOrder());
      assertEquals(1, deployer3.getDeployOrder());
      assertEquals(-1, deployer1.getUndeployOrder());
      assertEquals(-1, deployer2.getUndeployOrder());
      assertEquals(-1, deployer3.getUndeployOrder());

      main.removeDeployment(deployment);
      main.process();

      assertEquals(3, deployer1.getDeployOrder());
      assertEquals(2, deployer2.getDeployOrder());
      assertEquals(1, deployer3.getDeployOrder());
      assertEquals(4, deployer1.getUndeployOrder());
      assertEquals(5, deployer2.getUndeployOrder());
      assertEquals(6, deployer3.getUndeployOrder());

      main.addDeployment(deployment);
      main.process();

      assertEquals(9, deployer1.getDeployOrder());
      assertEquals(8, deployer2.getDeployOrder());
      assertEquals(7, deployer3.getDeployOrder());
      assertEquals(4, deployer1.getUndeployOrder());
      assertEquals(5, deployer2.getUndeployOrder());
      assertEquals(6, deployer3.getUndeployOrder());
   }

   public void testDoubleCycle() throws Exception
   {
      // (D) (H) (B) (E) (G)
      //  |   |
      //  T1 2nd
      //  |   |
      // (F) (C)
      //  |
      //  T2
      //  |
      // (A)
      DeployerClient main = createMainDeployer();

      TestFlowDeployer deployer2 = new TestFlowDeployer("A");
      deployer2.setInputs("test2");
      addDeployer(main, deployer2);

      TestFlowDeployer deployer3 = new TestFlowDeployer("B");
      addDeployer(main, deployer3);

      TestFlowDeployer deployer6 = new TestFlowDeployer("C");
      deployer6.setInputs("2ndcycle");
      addDeployer(main, deployer6);

      TestFlowDeployer deployer1 = new TestFlowDeployer("D");
      deployer1.setOutputs("test1");
      addDeployer(main, deployer1);

      TestFlowDeployer deployer4 = new TestFlowDeployer("E");
      addDeployer(main, deployer4);

      TestFlowDeployer deployer5 = new TestFlowDeployer("F");
      deployer5.setInputs("test1");
      deployer5.setOutputs("test2");
      addDeployer(main, deployer5);

      TestFlowDeployer deployer7 = new TestFlowDeployer("G");
      addDeployer(main, deployer7);

      TestFlowDeployer deployer8 = new TestFlowDeployer("H");
      deployer8.setOutputs("2ndcycle");
      addDeployer(main, deployer8);

      Deployment deployment = createSimpleDeployment("DoubleCycle");
      main.addDeployment(deployment);
      main.process();

      assertDeploy(deployer3);
      assertDeployBefore(deployer6, deployer8);
      assertDeployBefore(deployer5, deployer1);
      assertDeploy(deployer4);
      assertDeployBefore(deployer2, deployer5);
      assertDeploy(deployer7);
      assertEquals(-1, deployer3.getUndeployOrder());
      assertEquals(-1, deployer8.getUndeployOrder());
      assertEquals(-1, deployer6.getUndeployOrder());
      assertEquals(-1, deployer1.getUndeployOrder());
      assertEquals(-1, deployer4.getUndeployOrder());
      assertEquals(-1, deployer5.getUndeployOrder());
      assertEquals(-1, deployer2.getUndeployOrder());
      assertEquals(-1, deployer7.getUndeployOrder());

      main.removeDeployment(deployment);
      main.process();

      assertDeploy(deployer3);
      assertDeployBefore(deployer6, deployer8);
      assertDeployBefore(deployer5, deployer1);
      assertDeploy(deployer4);
      assertDeployBefore(deployer2, deployer5);
      assertDeploy(deployer7);
      assertUndeploy(deployer3);
      assertUndeployAfter(deployer6, deployer8);
      assertUndeployAfter(deployer5, deployer1);
      assertUndeploy(deployer4);
      assertUndeployAfter(deployer2, deployer5);
      assertUndeploy(deployer7);

      main.addDeployment(deployment);
      main.process();

      assertDeploy(deployer3);
      assertDeployBefore(deployer6, deployer8);
      assertDeployBefore(deployer5, deployer1);
      assertDeploy(deployer4);
      assertDeployBefore(deployer2, deployer5);
      assertDeploy(deployer7);
      assertUndeploy(deployer3);
      assertUndeployAfter(deployer6, deployer8);
      assertUndeployAfter(deployer5, deployer1);
      assertUndeploy(deployer4);
      assertUndeployAfter(deployer2, deployer5);
      assertUndeploy(deployer7);
   }

   public void testOrderedThenFlowWithPassThrough() throws Exception
   {
      DeployerClient main = createMainDeployer();

      TestFlowDeployer deployer4 = new TestFlowDeployer("4");
      deployer4.setInputs("test");
      addDeployer(main, deployer4);

      TestFlowDeployer deployer3 = new TestFlowDeployer("3");
      deployer3.setRelativeOrder(3);
      deployer3.setInputs("test");
      deployer3.setOutputs("test");
      addDeployer(main, deployer3);

      TestFlowDeployer deployer2 = new TestFlowDeployer("2");
      deployer2.setRelativeOrder(2);
      addDeployer(main, deployer2);

      TestFlowDeployer deployer1 = new TestFlowDeployer("1");
      deployer1.setRelativeOrder(1);
      addDeployer(main, deployer1);

      Deployment deployment = createSimpleDeployment("orderedThenFlowWithPassThrough");
      main.addDeployment(deployment);
      main.process();

      assertEquals(1, deployer1.getDeployOrder());
      assertEquals(2, deployer2.getDeployOrder());
      assertEquals(3, deployer3.getDeployOrder());
      assertEquals(4, deployer4.getDeployOrder());
      assertEquals(-1, deployer1.getUndeployOrder());
      assertEquals(-1, deployer2.getUndeployOrder());
      assertEquals(-1, deployer3.getUndeployOrder());
      assertEquals(-1, deployer4.getUndeployOrder());

      main.removeDeployment(deployment);
      main.process();

      assertEquals(1, deployer1.getDeployOrder());
      assertEquals(2, deployer2.getDeployOrder());
      assertEquals(3, deployer3.getDeployOrder());
      assertEquals(4, deployer4.getDeployOrder());
      assertEquals(8, deployer1.getUndeployOrder());
      assertEquals(7, deployer2.getUndeployOrder());
      assertEquals(6, deployer3.getUndeployOrder());
      assertEquals(5, deployer4.getUndeployOrder());

      main.addDeployment(deployment);
      main.process();

      assertEquals(9, deployer1.getDeployOrder());
      assertEquals(10, deployer2.getDeployOrder());
      assertEquals(11, deployer3.getDeployOrder());
      assertEquals(12, deployer4.getDeployOrder());
      assertEquals(8, deployer1.getUndeployOrder());
      assertEquals(7, deployer2.getUndeployOrder());
      assertEquals(6, deployer3.getUndeployOrder());
      assertEquals(5, deployer4.getUndeployOrder());
   }

   public void testSimplePassThrough() throws Exception
   {
      DeployerClient main = createMainDeployer();

      TestFlowDeployer postJBWMD = new TestFlowDeployer("PassThrough");
      postJBWMD.setInputs("JBWMD", "CLMD");
      postJBWMD.setOutputs("JBWMD", "CLMD");
      addDeployer(main, postJBWMD);
   }

   public void testWebBeansOrder() throws Exception
   {
      DeployerClient main = createMainDeployer();

      TestFlowDeployer mcfcld = new TestFlowDeployer("ManagedConnectionFactory");
      mcfcld.setInputs("ManagedConnectionFactoryDeploymentGroup");
      mcfcld.setOutputs("CLMD");
      addDeployer(main, mcfcld);

      TestFlowDeployer postJBWMD = new TestFlowDeployer("PostJBossWebMetadataDeployer");
      postJBWMD.setInputs("JBWMD", "CLMD");
      postJBWMD.setOutputs("JBWMD", "CLMD");
      addDeployer(main, postJBWMD);

      TestFlowDeployer postEJB = new TestFlowDeployer("PostEjbJar");
      postEJB.setInputs("EJB");
      postEJB.setOutputs("EJB");
      addDeployer(main, postEJB);

      TestFlowDeployer warCL = new TestFlowDeployer("WarClassLoaderDeployer");
      warCL.setInputs("JBWMD", "CLMD");
      warCL.setOutputs("CLMD");
      addDeployer(main, warCL);

      TestFlowDeployer service = new TestFlowDeployer("ServiceCL");
      service.setInputs("ServiceDeployment");
      service.setOutputs("CLMD");
      addDeployer(main, service);

      TestFlowDeployer legacy = new TestFlowDeployer("Legacy");
      legacy.setInputs("JBWMD", "WMD");
      legacy.setOutputs("JBWMD");
      addDeployer(main, legacy);

      TestFlowDeployer cluster = new TestFlowDeployer("Cluster");
      cluster.setInputs("JBWMD");
      cluster.setOutputs("JBWMD");
      addDeployer(main, cluster);

      TestFlowDeployer postWMD = new TestFlowDeployer("PostWebMetadataDeployer");
      postWMD.setInputs("JBWMD");
      postWMD.setOutputs("JBWMD");
      addDeployer(main, postWMD);
   }

   public void testWebServicesDeployersOrder() throws Exception
   {
      DeployerClient main = createMainDeployer();

      TestFlowDeployer deployer1 = new TestFlowDeployer("FakeDeployer");
      deployer1.setOutputs("WebServicesMetaData", "WebServiceDeployment", "JBossWebMetaData" );
      addDeployer(main, deployer1);

      TestFlowDeployer deployer2 = new TestFlowDeployer("WebServicesDeploymentTypeDeployer");
      deployer2.setInputs("WebServicesMetaData", "WebServiceDeployment", "JBossWebMetaData" );
      deployer2.setOutputs("DeploymentType", "JBossWebMetaData");
      addDeployer(main, deployer2);

      TestFlowDeployer deployer4 = new TestFlowDeployer("WebServiceDeployerPreJSE");
      deployer4.setInputs("JBossWebMetaData", "DeploymentType");
      deployer4.setOutputs("JBossWebMetaData");
      addDeployer(main, deployer4);

      TestFlowDeployer deployer5 = new TestFlowDeployer("AbstractWarDeployer");
      deployer5.setInputs("JBossWebMetaData");
      deployer5.setOutputs("WarDeployment");
      addDeployer(main, deployer5);

      TestFlowDeployer deployer6 = new TestFlowDeployer("ServiceCL");
      deployer6.setInputs("DeploymentType", "WarDeployment");
      addDeployer(main, deployer6);

      // #2 duplicate
      TestFlowDeployer deployer3 = new TestFlowDeployer("WebServicesDeploymentTypeDeployer2");
      deployer3.setInputs("WebServicesMetaData", "WebServiceDeployment", "JBossWebMetaData" );
      deployer3.setOutputs("DeploymentType", "JBossWebMetaData");
      addDeployer(main, deployer3);

      // #4 duplicate
      TestFlowDeployer deployer7 = new TestFlowDeployer("WebServiceDeployerEJB");
      deployer7.setInputs("JBossWebMetaData", "DeploymentType");
      deployer7.setOutputs("JBossWebMetaData");
      addDeployer(main, deployer7);

      Deployment deployment = createSimpleDeployment("testWSDeploymentOrder");
      main.addDeployment(deployment);
      main.process();

      assertEquals(1, deployer1.getDeployOrder());
      assertEquals(2, deployer2.getDeployOrder());
      assertEquals(3, deployer3.getDeployOrder());
//      assertEquals(4, deployer7.getDeployOrder());
      assertDeployBefore(deployer2, deployer1);
      assertDeployBefore(deployer3, deployer1);
      assertDeployBefore(deployer7, deployer2);
      assertDeployBefore(deployer7, deployer3);
      assertDeployBefore(deployer4, deployer2);
      assertDeployBefore(deployer4, deployer3);
      assertDeployBefore(deployer5, deployer4);
      assertDeployBefore(deployer5, deployer7);
      assertDeployBefore(deployer6, deployer5);
      assertDeployBefore(deployer6, deployer2);
      assertDeployBefore(deployer6, deployer3);
//      assertEquals(5, deployer4.getDeployOrder());
//      assertEquals(6, deployer5.getDeployOrder());
//      assertEquals(7, deployer6.getDeployOrder());
      assertEquals(-1, deployer1.getUndeployOrder());
      assertEquals(-1, deployer2.getUndeployOrder());
      assertEquals(-1, deployer3.getUndeployOrder());
      assertEquals(-1, deployer4.getUndeployOrder());
      assertEquals(-1, deployer5.getUndeployOrder());
      assertEquals(-1, deployer6.getUndeployOrder());

      main.removeDeployment(deployment);
      main.process();

      assertEquals(1, deployer1.getDeployOrder());
      assertEquals(2, deployer2.getDeployOrder());
      assertEquals(3, deployer3.getDeployOrder());
//      assertEquals(4, deployer7.getDeployOrder());
      assertDeployBefore(deployer2, deployer1);
      assertDeployBefore(deployer3, deployer1);
      assertDeployBefore(deployer7, deployer2);
      assertDeployBefore(deployer7, deployer3);
      assertDeployBefore(deployer4, deployer2);
      assertDeployBefore(deployer4, deployer3);
      assertDeployBefore(deployer5, deployer4);
      assertDeployBefore(deployer5, deployer7);
      assertDeployBefore(deployer6, deployer5);
      assertDeployBefore(deployer6, deployer2);
      assertDeployBefore(deployer6, deployer3);
//      assertEquals(5, deployer4.getDeployOrder());
//      assertEquals(6, deployer5.getDeployOrder());
//      assertEquals(7, deployer6.getDeployOrder());
      assertEquals(14, deployer1.getUndeployOrder());
      assertEquals(13, deployer2.getUndeployOrder());
      assertEquals(12, deployer3.getUndeployOrder());
//      assertEquals(11, deployer7.getUndeployOrder());
//      assertEquals(10, deployer4.getUndeployOrder());
//      assertEquals(9, deployer5.getUndeployOrder());
//      assertEquals(8, deployer6.getUndeployOrder());
      assertUndeployAfter(deployer2, deployer1);
      assertUndeployAfter(deployer3, deployer1);
      assertUndeployAfter(deployer7, deployer2);
      assertUndeployAfter(deployer7, deployer3);
      assertUndeployAfter(deployer4, deployer2);
      assertUndeployAfter(deployer4, deployer3);
      assertUndeployAfter(deployer5, deployer4);
      assertUndeployAfter(deployer5, deployer7);
      assertUndeployAfter(deployer6, deployer5);
      assertUndeployAfter(deployer6, deployer2);
      assertUndeployAfter(deployer6, deployer3);

      main.addDeployment(deployment);
      main.process();

      assertEquals(15, deployer1.getDeployOrder());
      assertEquals(16, deployer2.getDeployOrder());
      assertEquals(17, deployer3.getDeployOrder());
//      assertEquals(18, deployer7.getDeployOrder());
      assertDeployBefore(deployer2, deployer1);
      assertDeployBefore(deployer3, deployer1);
      assertDeployBefore(deployer7, deployer2);
      assertDeployBefore(deployer7, deployer3);
      assertDeployBefore(deployer4, deployer2);
      assertDeployBefore(deployer4, deployer3);
      assertDeployBefore(deployer5, deployer4);
      assertDeployBefore(deployer5, deployer7);
      assertDeployBefore(deployer6, deployer5);
      assertDeployBefore(deployer6, deployer2);
      assertDeployBefore(deployer6, deployer3);
//      assertEquals(19, deployer4.getDeployOrder());
//      assertEquals(20, deployer5.getDeployOrder());
//      assertEquals(21, deployer6.getDeployOrder());
      assertEquals(14, deployer1.getUndeployOrder());
      assertEquals(13, deployer2.getUndeployOrder());
      assertEquals(12, deployer3.getUndeployOrder());
//      assertEquals(11, deployer7.getUndeployOrder());
//      assertEquals(10, deployer4.getUndeployOrder());
//      assertEquals(9, deployer5.getUndeployOrder());
//      assertEquals(8, deployer6.getUndeployOrder());
      assertUndeployAfter(deployer2, deployer1);
      assertUndeployAfter(deployer3, deployer1);
      assertUndeployAfter(deployer7, deployer2);
      assertUndeployAfter(deployer7, deployer3);
      assertUndeployAfter(deployer4, deployer2);
      assertUndeployAfter(deployer4, deployer3);
      assertUndeployAfter(deployer5, deployer4);
      assertUndeployAfter(deployer5, deployer7);
      assertUndeployAfter(deployer6, deployer5);
      assertUndeployAfter(deployer6, deployer2);
      assertUndeployAfter(deployer6, deployer3);

      main.removeDeployment(deployment);
      main.process();
   }

   public void testDeployersOrder1() throws Exception
   {
      DeployerClient main = createMainDeployer();

      TestFlowDeployer deployer6 = new TestFlowDeployer( "6" );
      deployer6.setInputs( "a11", "a12", "a13", "a14", "a33" );
      addDeployer(main, deployer6);

      TestFlowDeployer deployer5 = new TestFlowDeployer( "5" );
      deployer5.setInputs( "a21", "a33", "a41" );
      deployer5.setOutputs( "a33", "a51", "a52" );
      addDeployer(main, deployer5);

      TestFlowDeployer deployer4 = new TestFlowDeployer( "4" );
      deployer4.setInputs( "a14", "a33" );
      deployer4.setOutputs( "a14", "a33", "a41" );
      addDeployer( main, deployer4 );

      TestFlowDeployer deployer3 = new TestFlowDeployer( "3" );
      deployer3.setInputs( "a13", "a21" );
      deployer3.setOutputs( "a32", "a33" );
      addDeployer( main, deployer3 );

      TestFlowDeployer deployer2 = new TestFlowDeployer( "2" );
      deployer2.setInputs( "a11", "a12" );
      deployer2.setOutputs( "a12", "a21", "a22" );
      addDeployer( main, deployer2 );

      TestFlowDeployer deployer1 = new TestFlowDeployer( "1" );
      deployer1.setOutputs( "a11", "a12", "a13", "a14" );
      addDeployer( main, deployer1 );

      Deployment deployment = createSimpleDeployment( "deployersOrderTest" );
      main.addDeployment(deployment);
      main.process();

      assertEquals(1, deployer1.getDeployOrder());
      assertEquals(2, deployer2.getDeployOrder());
      assertEquals(3, deployer3.getDeployOrder());
      assertEquals(4, deployer4.getDeployOrder());
      assertEquals(5, deployer5.getDeployOrder());
      assertEquals(6, deployer6.getDeployOrder());
      assertEquals(-1, deployer1.getUndeployOrder());
      assertEquals(-1, deployer2.getUndeployOrder());
      assertEquals(-1, deployer3.getUndeployOrder());
      assertEquals(-1, deployer4.getUndeployOrder());
      assertEquals(-1, deployer5.getUndeployOrder());
      assertEquals(-1, deployer6.getUndeployOrder());

      main.removeDeployment(deployment);
      main.process();

      assertEquals(1, deployer1.getDeployOrder());
      assertEquals(2, deployer2.getDeployOrder());
      assertEquals(3, deployer3.getDeployOrder());
      assertEquals(4, deployer4.getDeployOrder());
      assertEquals(5, deployer5.getDeployOrder());
      assertEquals(6, deployer6.getDeployOrder());
      assertEquals(12, deployer1.getUndeployOrder());
      assertEquals(11, deployer2.getUndeployOrder());
      assertEquals(10, deployer3.getUndeployOrder());
      assertEquals(9, deployer4.getUndeployOrder());
      assertEquals(8, deployer5.getUndeployOrder());
      assertEquals(7, deployer6.getUndeployOrder());

      main.addDeployment(deployment);
      main.process();

      assertEquals(13, deployer1.getDeployOrder());
      assertEquals(14, deployer2.getDeployOrder());
      assertEquals(15, deployer3.getDeployOrder());
      assertEquals(16, deployer4.getDeployOrder());
      assertEquals(17, deployer5.getDeployOrder());
      assertEquals(18, deployer6.getDeployOrder());
      assertEquals(12, deployer1.getUndeployOrder());
      assertEquals(11, deployer2.getUndeployOrder());
      assertEquals(10, deployer3.getUndeployOrder());
      assertEquals(9, deployer4.getUndeployOrder());
      assertEquals(8, deployer5.getUndeployOrder());
      assertEquals(7, deployer6.getUndeployOrder());

      main.removeDeployment(deployment);
      main.process();
   }

   public void testRemovingOverlapping() throws Exception
   {
      DeployerClient main = createMainDeployer();

      // "1", "2", "3", "4" and "6" are provided by deployers in different stage
      TestFlowDeployer deployer1 = new TestFlowDeployer( "WSEJBAdapterDeployer" );
      deployer1.setInputs( "1", "2", "3", "4" );
      deployer1.setOutputs( "5" );
      addDeployer(main, deployer1);

      TestFlowDeployer deployer2 = new TestFlowDeployer( "WSTypeDeployer" );
      deployer2.setInputs( "5", "4", "6" ); // note 6 is both input and output
      deployer2.setOutputs( "6", "7" );
      addDeployer(main, deployer2);

      TestFlowDeployer deployer3 = new TestFlowDeployer( "WSDeploymentDeployer" );
      deployer3.setInputs( "6", "7" ); // note 6 is both input and output
      deployer3.setOutputs( "8", "6" );
      addDeployer( main, deployer3 );

      TestFlowDeployer deployer4 = new TestFlowDeployer( "WSDeploymentAspectDeployer" );
      deployer4.setInputs( "6", "7", "8" );  // note 6 is both input and output
      deployer4.setOutputs( "9", "6", "0" );
      addDeployer( main, deployer4 );

      Deployment deployment = createSimpleDeployment( "deployersOrderTest" );

      main.addDeployment(deployment);
      main.process();

      assertEquals(1, deployer1.getDeployOrder());
      assertEquals(2, deployer2.getDeployOrder());
      assertEquals(3, deployer3.getDeployOrder());
      assertEquals(4, deployer4.getDeployOrder());
      assertEquals(-1, deployer1.getUndeployOrder());
      assertEquals(-1, deployer2.getUndeployOrder());
      assertEquals(-1, deployer3.getUndeployOrder());
      assertEquals(-1, deployer4.getUndeployOrder());

      main.removeDeployment(deployment);
      main.process();

      assertEquals(1, deployer1.getDeployOrder());
      assertEquals(2, deployer2.getDeployOrder());
      assertEquals(3, deployer3.getDeployOrder());
      assertEquals(4, deployer4.getDeployOrder());
      assertEquals(8, deployer1.getUndeployOrder());
      assertEquals(7, deployer2.getUndeployOrder());
      assertEquals(6, deployer3.getUndeployOrder());
      assertEquals(5, deployer4.getUndeployOrder());

      main.addDeployment(deployment);
      main.process();

      assertEquals(9, deployer1.getDeployOrder());
      assertEquals(10, deployer2.getDeployOrder());
      assertEquals(11, deployer3.getDeployOrder());
      assertEquals(12, deployer4.getDeployOrder());
      assertEquals(8, deployer1.getUndeployOrder());
      assertEquals(7, deployer2.getUndeployOrder());
      assertEquals(6, deployer3.getUndeployOrder());
      assertEquals(5, deployer4.getUndeployOrder());

      main.removeDeployment(deployment);
      main.process();
   }

   public void testPartialOverlapping() throws Exception
   {
      DeployerClient main = createMainDeployer();

      // "1", "2", are provided by other preceding deployers
      TestFlowDeployer deployer1 = new TestFlowDeployer( "#1_12-2345" );
      deployer1.setInputs( "1", "2" );
      deployer1.setOutputs( "3", "5", "2", "4" );
      addDeployer(main, deployer1);

      TestFlowDeployer deployer2 = new TestFlowDeployer( "#2_125-246" );
      deployer2.setInputs( "1", "5", "2" ); // depends on 5 (output of deployer1)
      deployer2.setOutputs( "6", "2", "4" );
      addDeployer(main, deployer2);

      TestFlowDeployer deployer3 = new TestFlowDeployer( "#3_1256-247" );
      deployer3.setInputs( "6", "1", "5", "2" ); // depends on 6 (output of deployer2) and 5 (output of deployer1)
      deployer3.setOutputs( "7", "2", "4" );
      addDeployer( main, deployer3 );

      TestFlowDeployer deployer4 = new TestFlowDeployer( "#4_124-28" );
      deployer4.setInputs( "1", "2", "4" ); // depends on 4 (output of deployer1, deployer2 and deployer3)
      deployer4.setOutputs( "8", "2" );
      addDeployer( main, deployer4 );

      Deployment deployment = createSimpleDeployment( "deployersOrderTest" );

      main.addDeployment(deployment);
      main.process();

      assertEquals(1, deployer1.getDeployOrder());
      assertEquals(2, deployer2.getDeployOrder());
      assertEquals(3, deployer3.getDeployOrder());
      assertEquals(4, deployer4.getDeployOrder());
      assertEquals(-1, deployer1.getUndeployOrder());
      assertEquals(-1, deployer2.getUndeployOrder());
      assertEquals(-1, deployer3.getUndeployOrder());
      assertEquals(-1, deployer4.getUndeployOrder());

      main.removeDeployment(deployment);
      main.process();

      assertEquals(1, deployer1.getDeployOrder());
      assertEquals(2, deployer2.getDeployOrder());
      assertEquals(3, deployer3.getDeployOrder());
      assertEquals(4, deployer4.getDeployOrder());
      assertEquals(8, deployer1.getUndeployOrder());
      assertEquals(7, deployer2.getUndeployOrder());
      assertEquals(6, deployer3.getUndeployOrder());
      assertEquals(5, deployer4.getUndeployOrder());

      main.addDeployment(deployment);
      main.process();

      assertEquals(9, deployer1.getDeployOrder());
      assertEquals(10, deployer2.getDeployOrder());
      assertEquals(11, deployer3.getDeployOrder());
      assertEquals(12, deployer4.getDeployOrder());
      assertEquals(8, deployer1.getUndeployOrder());
      assertEquals(7, deployer2.getUndeployOrder());
      assertEquals(6, deployer3.getUndeployOrder());
      assertEquals(5, deployer4.getUndeployOrder());

      main.removeDeployment(deployment);
      main.process();
   }
   
   /**
    * Tests algorithm performance on complete oriented graph.
    * All dependencies are specified using inputs/outputs.
    */
   public void testAlgorithmPerformance() throws Exception
   {
       
      DeployerClient main = createMainDeployer();
      TestFlowDeployer deployer;
      final int COUNT_OF_DEPLOYERS = 500;

      List<TestFlowDeployer> deployers = new LinkedList<TestFlowDeployer>();

      for (int i = 0; i < COUNT_OF_DEPLOYERS; i++)
      {
         deployer = new TestFlowDeployer( String.valueOf(i) );
         deployer.setOutputs( String.valueOf(i) );
         for (int j = 0; j < i; j++) deployer.addInput( String.valueOf(j) );
         deployers.add(deployer);
      }
      
      long start = System.currentTimeMillis();
      for (TestFlowDeployer d : deployers)
      {
         addDeployer(main, d);
      }
      long end = System.currentTimeMillis();
      
      System.out.println("------------------------------------------------------------------------");
      System.out.println("Exhaustive deployer sorting 1 (" + getClass().getSimpleName() +  ") took: " + (end - start) + " milliseconds");
      System.out.println("------------------------------------------------------------------------");

      // test proper deployers order
      Deployment deployment = createSimpleDeployment( "exhaustiveDeployersOrderTest" );

      main.addDeployment(deployment);
      main.process();

      for (int i = 0; i < COUNT_OF_DEPLOYERS; i++)
      {
         deployer = deployers.get(i);
         assertEquals(i + 1, deployer.getDeployOrder());
         assertEquals(-1, deployer.getUndeployOrder());
      }

      main.removeDeployment(deployment);
      main.process();

      for (int i = 0; i < COUNT_OF_DEPLOYERS; i++)
      {
         deployer = deployers.get(i);
         assertEquals(i + 1, deployer.getDeployOrder());
         assertEquals(2*COUNT_OF_DEPLOYERS - i, deployer.getUndeployOrder());
      }
   }

   /**
    * Tests algorithm performance on complete oriented graph
    * where vertex in this graph is represented as set of deployers.
    * Deployers that are in specific vertex are ordered using deployer ordering feature. 
    */
   public void testAlgorithmPerformance2() throws Exception
   {
      DeployerClient main = createMainDeployer();
      TestFlowDeployer deployer;
      final int COUNT_OF_DEPLOYERS = 1000; 
      final int MODULO = 50; // count of deployers in particular vertex

      List<TestFlowDeployer> deployers = new LinkedList<TestFlowDeployer>();

      for (int i = 0; i < COUNT_OF_DEPLOYERS; i++)
      {
         deployer = new TestFlowDeployer( String.valueOf(i) );
         deployer.setOutputs( String.valueOf(i / MODULO) );
         deployer.setRelativeOrder(i % MODULO);
         for (int j = 0; j < i/MODULO; j++) 
            deployer.addInput( String.valueOf(j) );
         
         deployers.add(deployer);
      }
      
      long start = System.currentTimeMillis();
      for (TestFlowDeployer d : deployers)
      {
         addDeployer(main, d);
      }
      long end = System.currentTimeMillis();
      
      System.out.println("------------------------------------------------------------------------");
      System.out.println("Exhaustive deployer sorting 2 (" + getClass().getSimpleName() +  ") took: " + (end - start) + " milliseconds");
      System.out.println("------------------------------------------------------------------------");

      // test proper deployers order
      Deployment deployment = createSimpleDeployment( "exhaustiveDeployersOrderTest" );

      main.addDeployment(deployment);
      main.process();

      int deployerDeployOrder;
      int deployerUndeployOrder;
      int deployerDeployOrderInModulo;
      int deployerUndeployOrderInModulo;
      int level;
      for (int i = 0; i < COUNT_OF_DEPLOYERS; i++)
      {
         level = i / MODULO;
         deployer = deployers.get(i);
         deployerDeployOrder = deployer.getDeployOrder();
         deployerDeployOrderInModulo = (deployerDeployOrder - 1) / MODULO;
         deployerUndeployOrder = deployer.getUndeployOrder();
         assertTrue("Wrong deployer(" + i + ") deploy order: " + deployerDeployOrder, level <= deployerDeployOrderInModulo && deployerDeployOrderInModulo < (level + 1));
         assertEquals(i + 1, deployerDeployOrder); // remove if [JBDEPLOY-233] will be fixed
         assertEquals(-1, deployerUndeployOrder);
      }

      main.removeDeployment(deployment);
      main.process();

      for (int i = 0; i < COUNT_OF_DEPLOYERS; i++)
      {
         level = i / MODULO;
         deployer = deployers.get(i);
         deployerDeployOrder = deployer.getDeployOrder();
         deployerDeployOrderInModulo = (deployerDeployOrder - 1) / MODULO;
         deployerUndeployOrder = deployer.getUndeployOrder();
         deployerUndeployOrderInModulo = (deployerUndeployOrder - 1) / MODULO;
         assertTrue("Wrong deployer(" + i + ") deploy order: " + deployerDeployOrder, level <= deployerDeployOrderInModulo && deployerDeployOrderInModulo < (level + 1));
         assertEquals(i + 1, deployer.getDeployOrder()); // remove if [JBDEPLOY-233] will be fixed
         assertTrue("Wrong deployer(" + i + ") undeploy order: " + deployerUndeployOrder, (2 * COUNT_OF_DEPLOYERS - level) >= deployerUndeployOrderInModulo && deployerUndeployOrderInModulo < (2 * COUNT_OF_DEPLOYERS - (level + 1)));
         assertEquals(2 * COUNT_OF_DEPLOYERS - i, deployer.getUndeployOrder()); // remove if [JBDEPLOY-233] will be fixed
      }
   }

   public void testRealWorldAS6DeployersScenario() throws Exception
   {
      // THIS IS REAL WORLD SCENARIO - AS deployers with their dependencies
      DeployerClient main = createMainDeployer();
      AbstractDeployer deployer;
      long start = System.currentTimeMillis();

      // PARSE

      deployer = new TestDeployerAdapter( "org.jboss.deployers.vfs.deployer.kernel.BeanDeployer" );
      deployer.setStage(DeploymentStages.PARSE);
      deployer.setOutputs( "org.jboss.kernel.spi.deployment.KernelDeployment" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.deployers.vfs.plugins.annotations.ScanningMetaDataDeployer" );
      deployer.setStage(DeploymentStages.PARSE);
      deployer.setOutputs( "org.jboss.deployers.spi.annotations.ScanningMetaData", "org.jboss.deployers.plugins.annotations.AbstractScanningMetaData" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.deployers.vfs.plugins.dependency.AliasesParserDeployer" );
      deployer.setStage(DeploymentStages.PARSE);
      deployer.setOutputs( "org.jboss.deployers.vfs.plugins.dependency.DeploymentAliases" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.deployers.vfs.plugins.dependency.DependenciesParserDeployer" );
      deployer.setStage(DeploymentStages.PARSE);
      deployer.setOutputs( "org.jboss.deployers.vfs.plugins.dependency.DependenciesMetaData" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.deployers.vfs.spi.deployer.SchemaResolverDeployer" );
      deployer.setStage(DeploymentStages.PARSE);
      deployer.setOutputs( "org.jboss.hibernate.deployers.metadata.HibernateMetaData" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.deployers.vfs.spi.deployer.SchemaResolverDeployer" );
      deployer.setStage(DeploymentStages.PARSE);
      deployer.setOutputs( "org.jboss.xnio.metadata.XnioMetaData" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.deployers.vfs.spi.deployer.SchemaResolverDeployer" );
      deployer.setStage(DeploymentStages.PARSE);
      deployer.setOutputs( "org.jboss.security.microcontainer.beans.metadata.SecurityPolicyMetaData" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.deployers.vfs.spi.deployer.SchemaResolverDeployer" );
      deployer.setStage(DeploymentStages.PARSE);
      deployer.setOutputs( "org.jboss.logging.metadata.LoggingMetaData" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.deployers.vfs.spi.deployer.SchemaResolverDeployer" );
      deployer.setStage(DeploymentStages.PARSE);
      deployer.setOutputs( "org.jboss.classloading.spi.metadata.ClassLoadingMetaData" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.deployers.vfs.spi.deployer.SchemaResolverDeployer" );
      deployer.setStage(DeploymentStages.PARSE);
      deployer.setOutputs( "org.jboss.threads.metadata.ThreadsMetaData" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.deployers.vfs.spi.deployer.SchemaResolverDeployer" );
      deployer.setStage(DeploymentStages.PARSE);
      deployer.setOutputs( "org.jboss.aop.microcontainer.beans.metadata.AOPDeployment" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.deployment.AppParsingDeployer" );
      deployer.setStage(DeploymentStages.PARSE);
      deployer.setOutputs( "org.jboss.metadata.ear.spec.EarMetaData" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.deployment.JBossAppParsingDeployer" );
      deployer.setStage(DeploymentStages.PARSE);
      deployer.setOutputs( "org.jboss.metadata.ear.jboss.JBossAppMetaData" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.ejb3.deployers.AppClientParsingDeployer" );
      deployer.setStage(DeploymentStages.PARSE);
      deployer.setOutputs( "org.jboss.metadata.client.spec.ApplicationClientMetaData" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.ejb3.deployers.JBossClientParsingDeployer" );
      deployer.setStage(DeploymentStages.PARSE);
      deployer.setInputs( "org.jboss.metadata.client.spec.ApplicationClientMetaData" );
      deployer.setOutputs( "org.jboss.metadata.client.jboss.JBossClientMetaData" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.ejb3.deployers.PersistenceUnitParsingDeployer" );
      deployer.setStage(DeploymentStages.PARSE);
      deployer.setOutputs( "org.jboss.metadata.jpa.spec.PersistenceMetaData" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.jpa.deployers.PersistenceParsingDeployer" );
      deployer.setStage(DeploymentStages.PARSE);
      deployer.setOutputs( "org.jboss.metadata.jpa.spec.PersistenceMetaData" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.resource.deployers.ManagedConnectionFactoryParserDeployer" );
      deployer.setStage(DeploymentStages.PARSE);
      deployer.setOutputs( "org.jboss.resource.metadata.mcf.ManagedConnectionFactoryDeploymentGroup" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.resource.deployers.RARParserDeployer" );
      deployer.setStage(DeploymentStages.PARSE);
      deployer.setOutputs( "org.jboss.resource.metadata.RARDeploymentMetaData" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.security.deployers.AclConfigParsingDeployer" );
      deployer.setStage(DeploymentStages.PARSE);
      deployer.setOutputs( "org.jboss.security.acl.config.ACLConfiguration" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.security.deployers.XacmlConfigParsingDeployer" );
      deployer.setStage(DeploymentStages.PARSE);
      deployer.setOutputs( "javax.xml.bind.JAXBElement" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.system.deployers.SARDeployer" );
      deployer.setStage(DeploymentStages.PARSE);
      deployer.setOutputs( "org.jboss.system.metadata.ServiceDeployment" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.varia.deployment.LegacyBeanShellDeployer" );
      deployer.setStage(DeploymentStages.PARSE);
      deployer.setOutputs( "org.jboss.varia.deployment.BeanShellScript" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.webservices.integration.deployers.WSDescriptorDeployer" );
      deployer.setStage(DeploymentStages.PARSE);
      deployer.setOutputs( "org.jboss.wsf.spi.metadata.webservices.WebservicesMetaData" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.aop.asintegration.jboss5.AOPAnnotationMetaDataParserDeployer" );
      deployer.setStage(DeploymentStages.PARSE);
      deployer.setOutputs( "org.jboss.aop.microcontainer.beans.metadata.AOPDeployment" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.deployment.EARContentsDeployer" );
      deployer.setStage(DeploymentStages.PARSE);
      deployer.setOutputs( "org.jboss.metadata.ear.jboss.JBossAppMetaData" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.deployment.WebAppParsingDeployer" );
      deployer.setStage(DeploymentStages.PARSE);
      deployer.setOutputs( "org.jboss.metadata.web.spec.WebMetaData" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.deployment.WebAppFragmentParsingDeployer" );
      deployer.setStage(DeploymentStages.PARSE);
      deployer.setOutputs( "org.jboss.metadata.web.spec.WebFragmentMetaData" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.deployment.TldParsingDeployer" );
      deployer.setStage(DeploymentStages.PARSE);
      deployer.setOutputs( "org.jboss.metadata.web.spec.TldMetaData" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.deployment.JBossWebAppParsingDeployer" );
      deployer.setStage(DeploymentStages.PARSE);
      deployer.setInputs( "org.jboss.metadata.web.spec.WebMetaData" );
      deployer.setOutputs( "org.jboss.metadata.web.jboss.JBossWebMetaData" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.deployment.EjbParsingDeployer" );
      deployer.setStage(DeploymentStages.PARSE);
      deployer.setOutputs( "org.jboss.metadata.ejb.spec.EjbJarMetaData" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.deployment.JBossEjbParsingDeployer" );
      deployer.setStage(DeploymentStages.PARSE);
      deployer.setInputs( "org.jboss.metadata.ejb.spec.EjbJarMetaData" );
      deployer.setOutputs( "standardjboss.xml", "org.jboss.metadata.ejb.jboss.JBossMetaData", "org.jboss.metadata.ApplicationMetaData" );
      addDeployer(main, deployer);

      // POST_PARSE

      deployer = new TestDeployerAdapter( "org.jboss.deployers.vfs.plugins.dependency.DependenciesMetaDataDeployer" );
      deployer.setStage(DeploymentStages.POST_PARSE);
      deployer.setInputs( "org.jboss.deployers.vfs.plugins.dependency.DependenciesMetaData" );
      deployer.setOutputs( "org.jboss.deployers.vfs.plugins.dependency.DeploymentDependencies" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.deployers.vfs.plugins.dependency.DeploymentAliasesDeployer" );
      deployer.setStage(DeploymentStages.POST_PARSE);
      deployer.setInputs( "org.jboss.deployers.vfs.plugins.dependency.DeploymentAliases" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.deployers.vfs.plugins.dependency.DeploymentDependencyDeployer" );
      deployer.setStage(DeploymentStages.POST_PARSE);
      deployer.setInputs( "org.jboss.deployers.vfs.plugins.dependency.DeploymentDependencies" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.deployment.EarClassLoaderDeployer" );
      deployer.setStage(DeploymentStages.POST_PARSE);
      deployer.setInputs( "org.jboss.metadata.ear.jboss.JBossAppMetaData" );
      deployer.setOutputs( "org.jboss.classloading.spi.metadata.ClassLoadingMetaData" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.deployment.EjbClassLoaderDeployer" );
      deployer.setStage(DeploymentStages.POST_PARSE);
      deployer.setInputs( "org.jboss.metadata.ejb.jboss.JBossMetaData" );
      deployer.setOutputs( "org.jboss.classloading.spi.metadata.ClassLoadingMetaData" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.deployment.LegacyWebXmlLessDeployer" );
      deployer.setStage(DeploymentStages.POST_PARSE);
      deployer.setInputs( "org.jboss.metadata.web.spec.WebMetaData", "org.jboss.metadata.web.jboss.JBossWebMetaData" );
      deployer.setOutputs( "org.jboss.metadata.web.jboss.JBossWebMetaData" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.resource.deployers.ManagedConnectionFactoryClassLoaderDeployer" );
      deployer.setStage(DeploymentStages.POST_PARSE);
      deployer.setInputs( "org.jboss.resource.metadata.mcf.ManagedConnectionFactoryDeploymentGroup" );
      deployer.setOutputs( "org.jboss.classloading.spi.metadata.ClassLoadingMetaData" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.system.deployers.ServiceClassLoaderDeployer" );
      deployer.setStage(DeploymentStages.POST_PARSE);
      deployer.setInputs( "org.jboss.system.metadata.ServiceDeployment" );
      deployer.setOutputs( "org.jboss.classloading.spi.metadata.ClassLoadingMetaData" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.web.tomcat.service.deployers.ClusteringDefaultsDeployer" );
      deployer.setStage(DeploymentStages.POST_PARSE);
      deployer.setInputs( "org.jboss.metadata.web.jboss.JBossWebMetaData" );
      deployer.setOutputs( "org.jboss.metadata.web.jboss.JBossWebMetaData" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.web.tomcat.service.deployers.WarClassLoaderDeployer" );
      deployer.setStage(DeploymentStages.POST_PARSE);
      deployer.setInputs( "org.jboss.classloading.spi.metadata.ClassLoadingMetaData", "org.jboss.metadata.web.jboss.JBossWebMetaData" );
      deployer.setOutputs( "org.jboss.classloading.spi.metadata.ClassLoadingMetaData" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.weld.integration.deployer.metadata.PostJBossMetadataDeployer" );
      deployer.setStage(DeploymentStages.POST_PARSE);
      deployer.setInputs( "org.jboss.metadata.ejb.jboss.JBossMetaData", "org.jboss.classloading.spi.metadata.ClassLoadingMetaData" );
      deployer.setOutputs( "org.jboss.classloading.spi.metadata.ClassLoadingMetaData" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.weld.integration.deployer.metadata.PostJBossWebMetadataDeployer" );
      deployer.setStage(DeploymentStages.POST_PARSE);
      deployer.setInputs( "org.jboss.classloading.spi.metadata.ClassLoadingMetaData", "org.jboss.metadata.web.jboss.JBossWebMetaData" );
      deployer.setOutputs( "org.jboss.classloading.spi.metadata.ClassLoadingMetaData" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.weld.integration.deployer.metadata.WeldFilesDeployer" );
      deployer.setStage(DeploymentStages.POST_PARSE);
      deployer.setInputs( "org.jboss.weld.integration.deployer.ext.JBossWeldMetaData" );
      deployer.setOutputs( "WELD_FILES", "WELD_CLASSPATH" );
      addDeployer(main, deployer);

      // PRE_DESCRIBE

      deployer = new TestDeployerAdapter( "org.jboss.deployers.plugins.classloading.ClassLoadingDefaultDeployer" );
      deployer.setStage(DeploymentStages.PRE_DESCRIBE);
      deployer.setInputs( "org.jboss.classloading.spi.metadata.ClassLoadingMetaData" );
      deployer.setOutputs( "org.jboss.classloading.spi.metadata.ClassLoadingMetaData" );
      addDeployer(main, deployer);

      // DESCRIBE

      deployer = new TestDeployerAdapter( "org.jboss.deployers.vfs.plugins.classloader.InMemoryClassesDeployer" );
      deployer.setStage(DeploymentStages.DESCRIBE);
      deployer.setOutputs( "org.jboss.classloading.spi.metadata.ClassLoadingMetaData" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.seam.integration.microcontainer.deployers.SeamWebUrlIntegrationDeployer" );
      deployer.setStage(DeploymentStages.DESCRIBE);
      deployer.setInputs( "org.jboss.metadata.web.jboss.JBossWebMetaData" );
      deployer.setOutputs( "org.jboss.classloading.spi.metadata.ClassLoadingMetaData" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.weld.integration.deployer.cl.WeldFacesIntegrationDeployer" );
      deployer.setStage(DeploymentStages.DESCRIBE);
      deployer.setInputs( "WELD_FILES" );
      deployer.setOutputs( "org.jboss.classloading.spi.metadata.ClassLoadingMetaData" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.deployers.vfs.plugins.classloader.VFSClassLoaderClassPathDeployer" );
      deployer.setStage(DeploymentStages.DESCRIBE);
      deployer.setInputs( "org.jboss.classloading.spi.metadata.ClassLoadingMetaData" );
      deployer.setOutputs( "org.jboss.classloading.spi.metadata.ClassLoadingMetaData" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.deployers.vfs.plugins.classloader.VFSClassLoaderDescribeDeployer" );
      deployer.setStage(DeploymentStages.DESCRIBE);
      deployer.setInputs( "org.jboss.classloading.spi.metadata.ClassLoadingMetaData" );
      deployer.setOutputs( "org.jboss.classloading.spi.dependency.Module" );
      addDeployer(main, deployer);

      // CLASSLOADER

      deployer = new TestDeployerAdapter( "org.jboss.deployers.plugins.classloading.AbstractLevelClassLoaderSystemDeployer" );
      deployer.setStage(DeploymentStages.CLASSLOADER);
      deployer.setInputs( "org.jboss.deployers.structure.spi.ClassLoaderFactory" );
      deployer.setOutputs( "java.lang.ClassLoader" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.aop.asintegration.jboss5.AOPClassLoaderDeployer" );
      deployer.setStage(DeploymentStages.CLASSLOADER);
      deployer.setInputs( "java.lang.ClassLoader" );
      addDeployer(main, deployer);

      // POST_CLASSLOADER

      deployer = new TestDeployerAdapter( "org.jboss.aop.asintegration.jboss5.AOPDeploymentAopMetaDataDeployer" );
      deployer.setStage(DeploymentStages.POST_CLASSLOADER);
      deployer.setInputs( "org.jboss.aop.microcontainer.beans.metadata.AOPDeployment" );
      deployer.setOutputs( "org.jboss.aop.asintegration.jboss5.AopMetaDataDeployerOutput" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.aop.asintegration.jboss5.BeansDeploymentAopMetaDataDeployer" );
      deployer.setStage(DeploymentStages.POST_CLASSLOADER);
      deployer.setInputs( "org.jboss.kernel.spi.deployment.KernelDeployment" );
      deployer.setOutputs( "org.jboss.aop.asintegration.jboss5.AopMetaDataDeployerOutput" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.deployment.EarLibExcludeDeployer" );
      deployer.setStage(DeploymentStages.POST_CLASSLOADER);
      deployer.setInputs( "org.jboss.metadata.ear.jboss.JBossAppMetaData" );
      deployer.setOutputs( "org.jboss.classloading.spi.visitor.ResourceFilter.recurse" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.deployers.vfs.plugins.annotations.FilteredAnnotationEnvironmentDeployer" );
      deployer.setStage(DeploymentStages.POST_CLASSLOADER);
      deployer.setInputs( "org.jboss.deployers.spi.annotations.ScanningMetaData", "org.jboss.classloading.spi.visitor.ResourceFilter.resource", "org.jboss.classloading.spi.dependency.Module", "org.jboss.classloading.spi.visitor.ResourceFilter.recurse" );
      deployer.setOutputs( "org.jboss.deployers.spi.annotations.AnnotationEnvironment" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.deployment.EarSecurityDeployer" );
      deployer.setStage(DeploymentStages.POST_CLASSLOADER);
      deployer.setInputs( "org.jboss.metadata.ear.jboss.JBossAppMetaData" );
      deployer.setOutputs( "jboss.jacc", "org.jboss.system.metadata.ServiceMetaData" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.deployment.OptAnnotationMetaDataDeployer" );
      deployer.setStage(DeploymentStages.POST_CLASSLOADER);
      deployer.setInputs( "org.jboss.metadata.web.spec.WebMetaData", "org.jboss.metadata.client.spec.ApplicationClientMetaData", "org.jboss.deployers.spi.annotations.AnnotationEnvironment", "org.jboss.metadata.ejb.spec.EjbJarMetaData" );
      deployer.setOutputs( "annotated.org.jboss.metadata.web.spec.WebMetaData", "annotated.org.jboss.metadata.ejb.spec.EjbJarMetaData", "annotated.org.jboss.metadata.client.spec.ApplicationClientMetaData" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.ejb.deployers.MergedJBossMetaDataDeployer" );
      deployer.setStage(DeploymentStages.POST_CLASSLOADER);
      deployer.setInputs( "annotated.org.jboss.metadata.ejb.spec.EjbJarMetaData", "org.jboss.metadata.ejb.jboss.JBossMetaData", "org.jboss.metadata.ejb.spec.EjbJarMetaData" );
      deployer.setOutputs( "merged.org.jboss.metadata.ejb.jboss.JBossMetaData", "org.jboss.metadata.ejb.jboss.JBossMetaData" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.ejb3.deployers.MergedJBossClientMetaDataDeployer" );
      deployer.setStage(DeploymentStages.POST_CLASSLOADER);
      deployer.setInputs( "org.jboss.metadata.client.jboss.JBossClientMetaData", "annotated.org.jboss.metadata.client.spec.ApplicationClientMetaData", "org.jboss.metadata.client.spec.ApplicationClientMetaData" );
      deployer.setOutputs( "org.jboss.metadata.client.jboss.JBossClientMetaData", "merged.org.jboss.metadata.client.jboss.JBossClientMetaData" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.ha.framework.server.deployers.Ejb2HAPartitionDependencyDeployer" );
      deployer.setStage(DeploymentStages.POST_CLASSLOADER);
      deployer.setInputs( "merged.org.jboss.metadata.ejb.jboss.JBossMetaData" );
      deployer.setOutputs( "merged.org.jboss.metadata.ejb.jboss.JBossMetaData" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.ha.framework.server.deployers.Ejb3HAPartitionDependencyDeployer" );
      deployer.setStage(DeploymentStages.POST_CLASSLOADER);
      deployer.setInputs( "merged.org.jboss.metadata.ejb.jboss.JBossMetaData" );
      deployer.setOutputs( "merged.org.jboss.metadata.ejb.jboss.JBossMetaData" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.ejb.deployers.StandardJBossMetaDataDeployer" );
      deployer.setStage(DeploymentStages.POST_CLASSLOADER);
      deployer.setInputs( "standardjboss.xml", "merged.org.jboss.metadata.ejb.jboss.JBossMetaData", "org.jboss.metadata.ejb.jboss.JBossMetaData" );
      deployer.setOutputs( "raw.org.jboss.metadata.ejb.jboss.JBossMetaData", "org.jboss.metadata.ejb.jboss.JBossMetaData" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.ejb3.deployers.Ejb3MetadataProcessingDeployer" );
      deployer.setStage(DeploymentStages.POST_CLASSLOADER);
      deployer.setInputs( "merged.org.jboss.metadata.ejb.jboss.JBossMetaData" );
      deployer.setOutputs( "processed.org.jboss.metadata.ejb.jboss.JBossMetaData" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.ejb3.deployers.EjbMetadataJndiPolicyDecoratorDeployer" );
      deployer.setStage(DeploymentStages.POST_CLASSLOADER);
      deployer.setInputs( "processed.org.jboss.metadata.ejb.jboss.JBossMetaData" );
      deployer.setOutputs( "EjbMetadataJndiPolicyDecoratorDeployer" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.weld.integration.deployer.metadata.PostWebMetadataDeployer" );
      deployer.setStage(DeploymentStages.POST_CLASSLOADER);
      deployer.setInputs( "merged.org.jboss.metadata.web.jboss.JBossWebMetaData", "WELD_FILES", "org.jboss.metadata.web.jboss.JBossWebMetaData" );
      deployer.setOutputs( "org.jboss.metadata.web.jboss.JBossWebMetaData" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.weld.integration.deployer.metadata.WeldEjbInterceptorMetadataDeployer" );
      deployer.setStage(DeploymentStages.POST_CLASSLOADER);
      deployer.setInputs( "WELD_FILES", "merged.org.jboss.metadata.ejb.jboss.JBossMetaData", "org.jboss.metadata.ejb.jboss.JBossMetaData" );
      deployer.setOutputs( "org.jboss.metadata.ejb.jboss.JBossMetaData" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.ejb.deployers.EjbSecurityDeployer" );
      deployer.setStage(DeploymentStages.POST_CLASSLOADER);
      deployer.setInputs( "merged.org.jboss.metadata.ejb.jboss.JBossMetaData", "org.jboss.metadata.ejb.jboss.JBossMetaData" );
      deployer.setOutputs( "jboss.jacc", "org.jboss.system.metadata.ServiceMetaData" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.web.deployers.WarAnnotationMetaDataDeployer" );
      deployer.setStage(DeploymentStages.POST_CLASSLOADER);
      deployer.setInputs( "org.jboss.metadata.web.spec.WebMetaData" );
      deployer.setOutputs( "annotated.org.jboss.metadata.web.spec.WebMetaData" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.web.deployers.MergedJBossWebMetaDataDeployer" );
      deployer.setStage(DeploymentStages.POST_CLASSLOADER);
      deployer.setInputs( "annotated.org.jboss.metadata.web.spec.WebMetaData", "org.jboss.metadata.web.spec.WebMetaData", "org.jboss.metadata.web.jboss.JBossWebMetaData" );
      deployer.setOutputs( "overlays.org.jboss.metadata.web.spec.WebMetaData", "org.jboss.metadata.web.jboss.JBossWebMetaData", "order.org.jboss.metadata.web.spec.WebMetaData", "localscis.org.jboss.metadata.web.spec.WebMetaData" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.deployment.MappedReferenceMetaDataResolverDeployer" );
      deployer.setStage(DeploymentStages.POST_CLASSLOADER);
      deployer.setInputs( "org.jboss.metadata.client.jboss.JBossClientMetaData", "org.jboss.metadata.ejb.jboss.JBossMetaData", "org.jboss.metadata.web.jboss.JBossWebMetaData" );
      deployer.setOutputs( "org.jboss.deployment.spi.DeploymentEndpointResolver" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.web.deployers.ServletContainerInitializerDeployer" );
      deployer.setStage(DeploymentStages.POST_CLASSLOADER);
      deployer.setInputs( "org.jboss.metadata.web.jboss.JBossWebMetaData", "order.org.jboss.metadata.web.spec.WebMetaData", "localscis.org.jboss.metadata.web.spec.WebMetaData" );
      deployer.setOutputs( "sci.org.jboss.metadata.web.spec.WebMetaData", "sci.handlestypes.org.jboss.metadata.web.spec.WebMetaData" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.web.deployers.WarSecurityDeployer" );
      deployer.setStage(DeploymentStages.POST_CLASSLOADER);
      deployer.setInputs( "org.jboss.metadata.web.jboss.JBossWebMetaData" );
      deployer.setOutputs( "jboss.jacc", "org.jboss.system.metadata.ServiceMetaData" );
      addDeployer(main, deployer);

      // PRE_REAL

      deployer = new TestDeployerAdapter( "org.jboss.beanvalidation.deployers.ValidatorFactoryDeployer" );
      deployer.setStage(DeploymentStages.PRE_REAL);
      deployer.setOutputs( "javax.validation.ValidatorFactory" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.ejb3.deployers.Ejb3DependenciesDeployer" );
      deployer.setStage(DeploymentStages.PRE_REAL);
      deployer.setInputs( "org.jboss.metadata.ejb.jboss.JBossMetaData" );
      deployer.setOutputs( "org.jboss.deployers.vfs.plugins.dependency.DependenciesMetaData" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.system.server.profileservice.persistence.deployer.ProfileServicePersistenceDeployer" );
      deployer.setStage(DeploymentStages.PRE_REAL);
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.weld.integration.deployer.env.EjbServicesDeployer" );
      deployer.setStage(DeploymentStages.PRE_REAL);
      deployer.setInputs( "WELD_FILES", "org.jboss.weld.integration.deployer.env.BootstrapInfo" );
      deployer.setOutputs( "BootstrapInfoEJB_SERVICES", "org.jboss.weld.integration.deployer.env.BootstrapInfo" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.weld.integration.deployer.env.WeldDiscoveryDeployer" );
      deployer.setStage(DeploymentStages.PRE_REAL);
      deployer.setInputs( "org.jboss.weld.integration.deployer.ext.JBossWeldMetaData", "WELD_FILES", "WELD_CLASSPATH" );
      deployer.setOutputs( "org.jboss.weld.integration.deployer.env.WeldDiscoveryEnvironment" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.weld.integration.deployer.env.FlatDeploymentDeployer" );
      deployer.setStage(DeploymentStages.PRE_REAL);
      deployer.setInputs( "WELD_FILES", "org.jboss.weld.integration.deployer.env.BootstrapInfo", "org.jboss.weld.integration.deployer.env.WeldDiscoveryEnvironment", "BootstrapInfoEJB_SERVICES" );
      deployer.setOutputs( "org.jboss.beans.metadata.spi.BeanMetaData", "org.jboss.weld.integration.deployer.env.BootstrapInfo", "BootstrapInfoDEPLOYMENT" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.weld.integration.deployer.env.WeldBootstrapDeployer" );
      deployer.setStage(DeploymentStages.PRE_REAL);
      deployer.setInputs( "javax.validation.ValidatorFactory", "WELD_FILES", "org.jboss.weld.integration.deployer.env.BootstrapInfo" );
      deployer.setOutputs( "org.jboss.beans.metadata.spi.BeanMetaData" );
      addDeployer(main, deployer);

      // REAL

      deployer = new TestDeployerAdapter( "org.jboss.deployers.vfs.deployer.kernel.BeanMetaDataFactoryDeployer" );
      deployer.setStage(DeploymentStages.REAL);
      deployer.setInputs( "org.jboss.threads.metadata.ThreadsMetaData" );
      deployer.setOutputs( "org.jboss.beans.metadata.spi.BeanMetaData" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.deployers.vfs.deployer.kernel.BeanMetaDataFactoryDeployer" );
      deployer.setStage(DeploymentStages.REAL);
      deployer.setInputs( "org.jboss.logging.metadata.LoggingMetaData" );
      deployer.setOutputs( "org.jboss.beans.metadata.spi.BeanMetaData" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.deployers.vfs.deployer.kernel.BeanMetaDataFactoryDeployer" );
      deployer.setStage(DeploymentStages.REAL);
      deployer.setInputs( "org.jboss.xnio.metadata.XnioMetaData" );
      deployer.setOutputs( "org.jboss.beans.metadata.spi.BeanMetaData" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.ejb.deployers.CreateDestinationDeployer" );
      deployer.setStage(DeploymentStages.REAL);
      deployer.setInputs( "org.jboss.metadata.ejb.jboss.JBossMetaData" );
      deployer.setOutputs( "org.jboss.metadata.ejb.jboss.JBossMetaData", "org.jboss.system.metadata.ServiceMetaData", "org.jboss.kernel.spi.deployment.KernelDeployment" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.ejb.deployers.EjbDeployer" );
      deployer.setStage(DeploymentStages.REAL);
      deployer.setInputs( "org.jboss.metadata.ejb.jboss.JBossMetaData" );
      deployer.setOutputs( "org.jboss.ejb.deployers.EjbDeployment", "org.jboss.system.metadata.ServiceMetaData", "org.jboss.kernel.spi.deployment.KernelDeployment" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.ejb3.deployers.Ejb3ClientDeployer" );
      deployer.setStage(DeploymentStages.REAL);
      deployer.setInputs( "org.jboss.metadata.client.jboss.JBossClientMetaData" );
      deployer.setOutputs( "org.jboss.ejb3.clientmodule.ClientENCInjectionContainer", "org.jboss.kernel.spi.deployment.KernelDeployment" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.ejb3.deployers.Ejb3Deployer" );
      deployer.setStage(DeploymentStages.REAL);
      deployer.setInputs( "org.jboss.metadata.ejb.jboss.JBossMetaData", "processed.org.jboss.metadata.ejb.jboss.JBossMetaData" );
      deployer.setOutputs( "org.jboss.ejb3.Ejb3Deployment", "org.jboss.kernel.spi.deployment.KernelDeployment" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.deployers.vfs.deployer.kernel.AliasDeploymentDeployer" );
      deployer.setStage(DeploymentStages.REAL);
      deployer.setInputs( "org.jboss.kernel.spi.deployment.KernelDeployment", "org.jboss.beans.metadata.spi.NamedAliasMetaData" );
      deployer.setOutputs( "org.jboss.beans.metadata.spi.NamedAliasMetaData" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.ejb3.endpoint.deployers.EJB3EndpointDeployer" );
      deployer.setStage(DeploymentStages.REAL);
      deployer.setInputs( "org.jboss.metadata.ejb.jboss.JBossMetaData" );
      deployer.setOutputs( "org.jboss.beans.metadata.spi.BeanMetaData" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.ejb3.metrics.deployer.Ejb3MetricsDeployer" );
      deployer.setStage(DeploymentStages.REAL);
      deployer.setInputs( "org.jboss.ejb3.Ejb3Deployment" );
      deployer.setOutputs( "org.jboss.ejb3.metrics.deployer.Ejb3MetricsDeployer" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.hibernate.deployers.HibernateDeployer" );
      deployer.setStage(DeploymentStages.REAL);
      deployer.setInputs( "org.jboss.hibernate.deployers.metadata.HibernateMetaData" );
      deployer.setOutputs( "org.jboss.beans.metadata.spi.BeanMetaData" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.jpa.deployers.PersistenceDeployer" );
      deployer.setStage(DeploymentStages.REAL);
      deployer.setInputs( "org.jboss.metadata.jpa.spec.PersistenceMetaData" );
      deployer.setOutputs( "org.jboss.metadata.jpa.spec.PersistenceUnitMetaData" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.jpa.deployers.PersistenceUnitDeployer" );
      deployer.setStage(DeploymentStages.REAL);
      deployer.setInputs( "org.jboss.metadata.jpa.spec.PersistenceUnitMetaData" );
      deployer.setOutputs( "org.jboss.beans.metadata.spi.BeanMetaData" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.deployers.vfs.deployer.kernel.KernelDeploymentDeployer" );
      deployer.setStage(DeploymentStages.REAL);
      deployer.setInputs( "org.jboss.beans.metadata.spi.BeanMetaData", "org.jboss.kernel.spi.deployment.KernelDeployment" );
      deployer.setOutputs( "org.jboss.beans.metadata.spi.BeanMetaData" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.deployers.vfs.deployer.kernel.BeanMetaDataDeployer" );
      deployer.setStage(DeploymentStages.REAL);
      deployer.setInputs( "org.jboss.beans.metadata.spi.BeanMetaData" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.deployers.vfs.deployer.kernel.DeploymentAliasMetaDataDeployer" );
      deployer.setStage(DeploymentStages.REAL);
      deployer.setInputs( "org.jboss.beans.metadata.spi.BeanMetaData", "org.jboss.beans.metadata.spi.NamedAliasMetaData" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.management.j2ee.deployers.EarModuleJSR77Deployer" );
      deployer.setStage(DeploymentStages.REAL);
      deployer.setInputs( "org.jboss.metadata.ear.jboss.JBossAppMetaData" );
      deployer.setOutputs( "javax.management.ObjectName" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.management.j2ee.deployers.EjbModuleJSR77Deployer" );
      deployer.setStage(DeploymentStages.REAL);
      deployer.setInputs( "org.jboss.metadata.ejb.jboss.JBossMetaData" );
      deployer.setOutputs( "javax.management.ObjectName" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.management.j2ee.deployers.JCAResourceJSR77Deployer" );
      deployer.setStage(DeploymentStages.REAL);
      deployer.setInputs( "org.jboss.resource.metadata.mcf.ManagedConnectionFactoryDeploymentGroup" );
      deployer.setOutputs( "javax.management.ObjectName" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.management.j2ee.deployers.RarModuleJSR77Deployer" );
      deployer.setStage(DeploymentStages.REAL);
      deployer.setInputs( "org.jboss.resource.metadata.RARDeploymentMetaData" );
      deployer.setOutputs( "javax.management.ObjectName" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.resource.deployers.ManagedConnectionFactoryDeployer" );
      deployer.setStage(DeploymentStages.REAL);
      deployer.setInputs( "org.jboss.resource.metadata.mcf.ManagedConnectionFactoryDeploymentGroup" );
      deployer.setOutputs( "org.jboss.system.metadata.ServiceDeployment" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.management.j2ee.deployers.ServiceModuleJSR77Deployer" );
      deployer.setStage(DeploymentStages.REAL);
      deployer.setInputs( "org.jboss.system.metadata.ServiceDeployment" );
      deployer.setOutputs( "javax.management.ObjectName" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.resource.deployers.RARDeployer" );
      deployer.setStage(DeploymentStages.REAL);
      deployer.setInputs( "org.jboss.resource.metadata.RARDeploymentMetaData" );
      deployer.setOutputs( "org.jboss.system.metadata.ServiceMetaData" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.varia.deployment.LegacyBeanShellScriptDeployer" );
      deployer.setStage(DeploymentStages.REAL);
      deployer.setInputs( "org.jboss.varia.deployment.BeanShellScript" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.webservices.integration.deployers.WSEJBAdapterDeployer" );
      deployer.setStage(DeploymentStages.REAL);
      deployer.setInputs( "org.jboss.ejb3.Ejb3Deployment", "merged.org.jboss.metadata.ejb.jboss.JBossMetaData", "org.jboss.ejb.deployers.EjbDeployment", "org.jboss.wsf.spi.metadata.webservices.WebservicesMetaData" );
      deployer.setOutputs( "org.jboss.wsf.spi.deployment.integration.WebServiceDeployment" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.webservices.integration.deployers.WSTypeDeployer" );
      deployer.setStage(DeploymentStages.REAL);
      deployer.setInputs( "org.jboss.wsf.spi.deployment.integration.WebServiceDeployment", "org.jboss.wsf.spi.metadata.webservices.WebservicesMetaData", "org.jboss.metadata.web.jboss.JBossWebMetaData" );
      deployer.setOutputs( "org.jboss.metadata.web.jboss.JBossWebMetaData", "org.jboss.wsf.spi.deployment.Deployment$DeploymentType" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.webservices.integration.deployers.WSDeploymentDeployer" );
      deployer.setStage(DeploymentStages.REAL);
      deployer.setInputs( "org.jboss.metadata.web.jboss.JBossWebMetaData", "org.jboss.wsf.spi.deployment.Deployment$DeploymentType" );
      deployer.setOutputs( "org.jboss.wsf.spi.deployment.Deployment", "org.jboss.metadata.web.jboss.JBossWebMetaData" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.webservices.integration.deployers.WSDeploymentAspectDeployer" );
      deployer.setStage(DeploymentStages.REAL);
      deployer.setInputs( "org.jboss.wsf.spi.deployment.Deployment", "org.jboss.metadata.web.jboss.JBossWebMetaData" );
      deployer.setOutputs( "jbossws.EndpointMetrics", "org.jboss.metadata.web.jboss.JBossWebMetaData", "jbossws.metadata" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.webservices.integration.deployers.WSDeploymentAspectDeployer" );
      deployer.setStage(DeploymentStages.REAL);
      deployer.setInputs( "org.jboss.wsf.spi.deployment.Deployment", "org.jboss.metadata.web.jboss.JBossWebMetaData" );
      deployer.setOutputs( "jbossws.VFSRoot", "jbossws.ContainerMetaData", "org.jboss.metadata.web.jboss.JBossWebMetaData", "jbossws.metadata" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.webservices.integration.deployers.WSDeploymentAspectDeployer" );
      deployer.setStage(DeploymentStages.REAL);
      deployer.setInputs( "org.jboss.wsf.spi.deployment.Deployment", "jbossws.ContainerMetaData", "org.jboss.metadata.web.jboss.JBossWebMetaData" );
      deployer.setOutputs( "org.jboss.metadata.web.jboss.JBossWebMetaData", "jbossws.metadata", "jbossws.VirtualHosts" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.webservices.integration.deployers.WSDeploymentAspectDeployer" );
      deployer.setStage(DeploymentStages.REAL);
      deployer.setInputs( "org.jboss.wsf.spi.deployment.Deployment", "jbossws.ContainerMetaData", "org.jboss.metadata.web.jboss.JBossWebMetaData" );
      deployer.setOutputs( "jbossws.ContextRoot", "org.jboss.metadata.web.jboss.JBossWebMetaData", "jbossws.metadata" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.webservices.integration.deployers.WSDeploymentAspectDeployer" );
      deployer.setStage(DeploymentStages.REAL);
      deployer.setInputs( "jbossws.ContextRoot", "org.jboss.wsf.spi.deployment.Deployment", "jbossws.ContainerMetaData", "org.jboss.metadata.web.jboss.JBossWebMetaData" );
      deployer.setOutputs( "jbossws.URLPattern", "org.jboss.metadata.web.jboss.JBossWebMetaData", "jbossws.metadata" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.webservices.integration.deployers.WSDeploymentAspectDeployer" );
      deployer.setStage(DeploymentStages.REAL);
      deployer.setInputs( "jbossws.URLPattern", "org.jboss.wsf.spi.deployment.Deployment", "org.jboss.metadata.web.jboss.JBossWebMetaData" );
      deployer.setOutputs( "org.jboss.metadata.web.jboss.JBossWebMetaData", "jbossws.EndpointAddress", "jbossws.metadata" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.webservices.integration.deployers.WSDeploymentAspectDeployer" );
      deployer.setStage(DeploymentStages.REAL);
      deployer.setInputs( "jbossws.URLPattern", "org.jboss.wsf.spi.deployment.Deployment", "org.jboss.metadata.web.jboss.JBossWebMetaData" );
      deployer.setOutputs( "jbossws.EndpointName", "org.jboss.metadata.web.jboss.JBossWebMetaData", "jbossws.metadata" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.webservices.integration.deployers.WSDeploymentAspectDeployer" );
      deployer.setStage(DeploymentStages.REAL);
      deployer.setInputs( "jbossws.EndpointName", "org.jboss.wsf.spi.deployment.Deployment", "org.jboss.metadata.web.jboss.JBossWebMetaData" );
      deployer.setOutputs( "jbossws.RegisteredEndpoint", "org.jboss.metadata.web.jboss.JBossWebMetaData", "jbossws.metadata" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.webservices.integration.deployers.WSDeploymentAspectDeployer" );
      deployer.setStage(DeploymentStages.REAL);
      deployer.setInputs( "jbossws.URLPattern", "org.jboss.wsf.spi.deployment.Deployment", "org.jboss.metadata.web.jboss.JBossWebMetaData", "jbossws.VirtualHosts" );
      deployer.setOutputs( "jbossws.WebMetaData", "org.jboss.metadata.web.jboss.JBossWebMetaData", "jbossws.metadata" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.webservices.integration.deployers.WSDeploymentAspectDeployer" );
      deployer.setStage(DeploymentStages.REAL);
      deployer.setInputs( "org.jboss.wsf.spi.deployment.Deployment", "jbossws.ContainerMetaData", "org.jboss.metadata.web.jboss.JBossWebMetaData" );
      deployer.setOutputs( "jbossws.StackEndpointHandler", "org.jboss.metadata.web.jboss.JBossWebMetaData", "jbossws.metadata" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.webservices.integration.deployers.WSDeploymentAspectDeployer" );
      deployer.setStage(DeploymentStages.REAL);
      deployer.setInputs( "org.jboss.wsf.spi.deployment.Deployment", "org.jboss.metadata.web.jboss.JBossWebMetaData" );
      deployer.setOutputs( "jbossws.JAXBIntros", "org.jboss.metadata.web.jboss.JBossWebMetaData", "jbossws.metadata" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.webservices.integration.deployers.WSDeploymentAspectDeployer" );
      deployer.setStage(DeploymentStages.REAL);
      deployer.setInputs( "jbossws.JAXBIntros", "jbossws.VFSRoot", "jbossws.URLPattern", "org.jboss.wsf.spi.deployment.Deployment", "jbossws.ContainerMetaData", "org.jboss.metadata.web.jboss.JBossWebMetaData" );
      deployer.setOutputs( "jbossws.UnifiedMetaDataModel", "org.jboss.metadata.web.jboss.JBossWebMetaData", "jbossws.metadata" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.webservices.integration.deployers.WSDeploymentAspectDeployer" );
      deployer.setStage(DeploymentStages.REAL);
      deployer.setInputs( "jbossws.UnifiedMetaDataModel", "org.jboss.wsf.spi.deployment.Deployment", "org.jboss.metadata.web.jboss.JBossWebMetaData" );
      deployer.setOutputs( "org.jboss.metadata.web.jboss.JBossWebMetaData", "jbossws.metadata" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.webservices.integration.deployers.WSDeploymentAspectDeployer" );
      deployer.setStage(DeploymentStages.REAL);
      deployer.setInputs( "jbossws.JAXBIntros", "jbossws.UnifiedMetaDataModel", "org.jboss.wsf.spi.deployment.Deployment", "org.jboss.metadata.web.jboss.JBossWebMetaData" );
      deployer.setOutputs( "jbossws.PublishedContract", "org.jboss.metadata.web.jboss.JBossWebMetaData", "jbossws.metadata" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.webservices.integration.deployers.WSDeploymentAspectDeployer" );
      deployer.setStage(DeploymentStages.REAL);
      deployer.setInputs( "jbossws.UnifiedMetaDataModel", "org.jboss.wsf.spi.deployment.Deployment", "org.jboss.metadata.web.jboss.JBossWebMetaData" );
      deployer.setOutputs( "jbossws.InitializedMetaDataModel", "org.jboss.metadata.web.jboss.JBossWebMetaData", "jbossws.metadata" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.webservices.integration.deployers.WSDeploymentAspectDeployer" );
      deployer.setStage(DeploymentStages.REAL);
      deployer.setInputs( "jbossws.UnifiedMetaDataModel", "org.jboss.wsf.spi.deployment.Deployment", "org.jboss.metadata.web.jboss.JBossWebMetaData" );
      deployer.setOutputs( "org.jboss.metadata.web.jboss.JBossWebMetaData", "jbossws.metadata" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.webservices.integration.deployers.WSDeploymentAspectDeployer" );
      deployer.setStage(DeploymentStages.REAL);
      deployer.setInputs( "jbossws.StackEndpointHandler", "jbossws.UnifiedMetaDataModel", "org.jboss.wsf.spi.deployment.Deployment", "org.jboss.metadata.web.jboss.JBossWebMetaData" );
      deployer.setOutputs( "org.jboss.metadata.web.jboss.JBossWebMetaData", "jbossws.metadata" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.webservices.integration.deployers.WSDeploymentAspectDeployer" );
      deployer.setStage(DeploymentStages.REAL);
      deployer.setInputs( "org.jboss.wsf.spi.deployment.Deployment", "org.jboss.metadata.web.jboss.JBossWebMetaData" );
      deployer.setOutputs( "jbossws.StackDescriptor", "org.jboss.metadata.web.jboss.JBossWebMetaData", "jbossws.ContextProperties", "jbossws.metadata" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.webservices.integration.deployers.WSDeploymentAspectDeployer" );
      deployer.setStage(DeploymentStages.REAL);
      deployer.setInputs( "jbossws.WebMetaData", "org.jboss.wsf.spi.deployment.Deployment", "jbossws.StackDescriptor", "org.jboss.metadata.web.jboss.JBossWebMetaData", "jbossws.ContextProperties" );
      deployer.setOutputs( "jbossws.WebMetaData", "org.jboss.metadata.web.jboss.JBossWebMetaData", "jbossws.metadata" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.webservices.integration.deployers.WSDeploymentAspectDeployer" );
      deployer.setStage(DeploymentStages.REAL);
      deployer.setInputs( "jbossws.WebMetaData", "org.jboss.wsf.spi.deployment.Deployment", "org.jboss.metadata.web.jboss.JBossWebMetaData" );
      deployer.setOutputs( "jbossws.JACCPermisions", "org.jboss.metadata.web.jboss.JBossWebMetaData", "jbossws.metadata" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.webservices.integration.deployers.WSDeploymentAspectDeployer" );
      deployer.setStage(DeploymentStages.REAL);
      deployer.setInputs( "jbossws.WebMetaData", "org.jboss.wsf.spi.deployment.Deployment", "org.jboss.metadata.web.jboss.JBossWebMetaData" );
      deployer.setOutputs( "jbossws.InjectionMetaData", "org.jboss.metadata.web.jboss.JBossWebMetaData", "jbossws.metadata" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.webservices.integration.deployers.WSDeploymentAspectDeployer" );
      deployer.setStage(DeploymentStages.REAL);
      deployer.setInputs( "jbossws.RegisteredEndpoint", "org.jboss.wsf.spi.deployment.Deployment", "org.jboss.metadata.web.jboss.JBossWebMetaData" );
      deployer.setOutputs( "jbossws.EndpointRecordProcessors", "org.jboss.metadata.web.jboss.JBossWebMetaData", "jbossws.metadata" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.webservices.integration.deployers.WSDeploymentAspectDeployer" );
      deployer.setStage(DeploymentStages.REAL);
      deployer.setInputs( "org.jboss.wsf.spi.deployment.Deployment", "org.jboss.metadata.web.jboss.JBossWebMetaData", "jbossws.metadata" );
      deployer.setOutputs( "jbossws.LifecycleHandler", "org.jboss.metadata.web.jboss.JBossWebMetaData" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.management.j2ee.deployers.WebModuleJSR77Deployer" );
      deployer.setStage(DeploymentStages.REAL);
      deployer.setInputs( "org.jboss.metadata.web.jboss.JBossWebMetaData" );
      deployer.setOutputs( "javax.management.ObjectName" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.web.tomcat.service.deployers.TomcatDeployer" );
      deployer.setStage(DeploymentStages.REAL);
      deployer.setInputs( "org.jboss.metadata.web.jboss.JBossWebMetaData" );
      deployer.setOutputs( "org.jboss.web.deployers.WarDeployment", "org.jboss.system.metadata.ServiceMetaData" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.system.deployers.ServiceDeploymentDeployer" );
      deployer.setStage(DeploymentStages.REAL);
      deployer.setInputs( "org.jboss.system.metadata.ServiceDeployment", "org.jboss.system.metadata.ServiceMetaData" );
      deployer.setOutputs( "org.jboss.system.metadata.ServiceMetaData" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.management.j2ee.deployers.JMSResourceJSR77Deployer" );
      deployer.setStage(DeploymentStages.REAL);
      deployer.setInputs( "org.jboss.system.metadata.ServiceMetaData" );
      deployer.setOutputs( "javax.management.ObjectName" );
      addDeployer(main, deployer);

      deployer = new TestDeployerAdapter( "org.jboss.system.deployers.ServiceDeployer" );
      deployer.setStage(DeploymentStages.REAL);
      deployer.setInputs( "org.jboss.system.metadata.ServiceMetaData" );
      addDeployer(main, deployer);

      long end = System.currentTimeMillis();
      System.out.println("------------------------------------------------------------------------");
      System.out.println("Deployer sorting (" + getClass().getSimpleName() +  ") took: " + (end - start) + " milliseconds");
      System.out.println("------------------------------------------------------------------------");
   }

   private static void assertDeploy(TestFlowDeployer deployer)
   {
      assertTrue(deployer + " must deploy", deployer.getDeployOrder() > 0);
   }

   private static void assertDeployBefore(TestFlowDeployer after, TestFlowDeployer before)
   {
      assertTrue(before + " must deploy before " + after, after.getDeployOrder() > before.getDeployOrder());
   }

   private static void assertUndeploy(TestFlowDeployer deployer)
   {
      assertTrue(deployer + " must undeploy", deployer.getUndeployOrder() > 0);
   }

   private static void assertUndeployAfter(TestFlowDeployer after, TestFlowDeployer before)
   {
      assertTrue(before + " must undeploy after " + after, after.getUndeployOrder() < before.getUndeployOrder());
   }
}