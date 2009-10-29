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
import org.jboss.test.deployers.deployer.support.TestFlowDeployer;

/**
 * DeployerOrderingUnitTestCase.
 *
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class DeployerFlowUnitTestCase extends AbstractDeployerTest
{
   public static Test suite()
   {
      return new TestSuite(DeployerFlowUnitTestCase.class);
   }

   public DeployerFlowUnitTestCase(String name)
   {
      super(name);
   }

   protected void setUp() throws Exception
   {
      super.setUp();
      TestFlowDeployer.reset();
   }

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
      deployer1.setInputs("input1");
      deployer1.setOutputs("output1");
      addDeployer(main, deployer1);
      TestFlowDeployer deployer2 = new TestFlowDeployer("2");
      deployer2.setInputs("output1");
      deployer2.setOutputs("output2");
      addDeployer(main, deployer2);
      TestFlowDeployer deployer3 = new TestFlowDeployer("3");
      deployer3.setInputs("output2");
      deployer3.setOutputs("input1");
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

   public void testMultipleInput() throws Exception
   {
      DeployerClient main = createMainDeployer();
      TestFlowDeployer deployer3 = new TestFlowDeployer("3");
      deployer3.setInputs("test1", "test2");
      addDeployer(main, deployer3);
      TestFlowDeployer deployer1 = new TestFlowDeployer("1");
      deployer1.setOutputs("test1");
      addDeployer(main, deployer1);
      TestFlowDeployer deployer2 = new TestFlowDeployer("2");
      deployer2.setOutputs("test2");
      addDeployer(main, deployer2);

      Deployment deployment = createSimpleDeployment("MultipleInput");
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
      TestFlowDeployer deployer1 = new TestFlowDeployer("1");
      deployer1.setInputs("X");
      deployer1.setOutputs("B");
      addDeployer(main, deployer1);

      TestFlowDeployer deployer2 = new TestFlowDeployer("2");
      deployer2.setInputs("X");
      deployer2.setOutputs("X");
      addDeployer(main, deployer2);

      TestFlowDeployer deployer3 = new TestFlowDeployer("3");
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

      assertEquals(1, deployer3.getDeployOrder());
      assertEquals(2, deployer8.getDeployOrder());
      assertEquals(3, deployer6.getDeployOrder());
      assertEquals(4, deployer1.getDeployOrder());
      assertEquals(5, deployer4.getDeployOrder());
      assertEquals(6, deployer5.getDeployOrder());
      assertEquals(7, deployer2.getDeployOrder());
      assertEquals(8, deployer7.getDeployOrder());
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

      assertEquals(1, deployer3.getDeployOrder());
      assertEquals(2, deployer8.getDeployOrder());
      assertEquals(3, deployer6.getDeployOrder());
      assertEquals(4, deployer1.getDeployOrder());
      assertEquals(5, deployer4.getDeployOrder());
      assertEquals(6, deployer5.getDeployOrder());
      assertEquals(7, deployer2.getDeployOrder());
      assertEquals(8, deployer7.getDeployOrder());
      assertEquals(16, deployer3.getUndeployOrder());
      assertEquals(15, deployer8.getUndeployOrder());
      assertEquals(14, deployer6.getUndeployOrder());
      assertEquals(13, deployer1.getUndeployOrder());
      assertEquals(12, deployer4.getUndeployOrder());
      assertEquals(11, deployer5.getUndeployOrder());
      assertEquals(10, deployer2.getUndeployOrder());
      assertEquals(9, deployer7.getUndeployOrder());

      main.addDeployment(deployment);
      main.process();

      assertEquals(17, deployer3.getDeployOrder());
      assertEquals(18, deployer8.getDeployOrder());
      assertEquals(19, deployer6.getDeployOrder());
      assertEquals(20, deployer1.getDeployOrder());
      assertEquals(21, deployer4.getDeployOrder());
      assertEquals(22, deployer5.getDeployOrder());
      assertEquals(23, deployer2.getDeployOrder());
      assertEquals(24, deployer7.getDeployOrder());
      assertEquals(16, deployer3.getUndeployOrder());
      assertEquals(15, deployer8.getUndeployOrder());
      assertEquals(14, deployer6.getUndeployOrder());
      assertEquals(13, deployer1.getUndeployOrder());
      assertEquals(12, deployer4.getUndeployOrder());
      assertEquals(11, deployer5.getUndeployOrder());
      assertEquals(10, deployer2.getUndeployOrder());
      assertEquals(9, deployer7.getUndeployOrder());
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
      assertEquals(4, deployer7.getDeployOrder());
      assertEquals(5, deployer4.getDeployOrder());
      assertEquals(6, deployer5.getDeployOrder());
      assertEquals(7, deployer6.getDeployOrder());
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
      assertEquals(4, deployer7.getDeployOrder());
      assertEquals(5, deployer4.getDeployOrder());
      assertEquals(6, deployer5.getDeployOrder());
      assertEquals(7, deployer6.getDeployOrder());
      assertEquals(14, deployer1.getUndeployOrder());
      assertEquals(13, deployer2.getUndeployOrder());
      assertEquals(12, deployer3.getUndeployOrder());
      assertEquals(11, deployer7.getUndeployOrder());
      assertEquals(10, deployer4.getUndeployOrder());
      assertEquals(9, deployer5.getUndeployOrder());
      assertEquals(8, deployer6.getUndeployOrder());

      main.addDeployment(deployment);
      main.process();

      assertEquals(15, deployer1.getDeployOrder());
      assertEquals(16, deployer2.getDeployOrder());
      assertEquals(17, deployer3.getDeployOrder());
      assertEquals(18, deployer7.getDeployOrder());
      assertEquals(19, deployer4.getDeployOrder());
      assertEquals(20, deployer5.getDeployOrder());
      assertEquals(21, deployer6.getDeployOrder());
      assertEquals(14, deployer1.getUndeployOrder());
      assertEquals(13, deployer2.getUndeployOrder());
      assertEquals(12, deployer3.getUndeployOrder());
      assertEquals(11, deployer7.getUndeployOrder());
      assertEquals(10, deployer4.getUndeployOrder());
      assertEquals(9, deployer5.getUndeployOrder());
      assertEquals(8, deployer6.getUndeployOrder());

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
      TestFlowDeployer deployer1 = new TestFlowDeployer( "Deployer" );
      deployer1.setInputs( "1", "2" );
      deployer1.setOutputs( "3", "5", "2", "4" );
      addDeployer(main, deployer1);

      TestFlowDeployer deployer2 = new TestFlowDeployer( "Deployer" );
      deployer2.setInputs( "1", "5", "2" ); // depends on 5 (output of deployer1)
      deployer2.setOutputs( "6", "2", "4" );
      addDeployer(main, deployer2);

      TestFlowDeployer deployer3 = new TestFlowDeployer( "Deployer" );
      deployer3.setInputs( "6", "1", "5", "2" ); // depends on 6 (output of deployer2) and 5 (output of deployer1)
      deployer3.setOutputs( "7", "2", "4" );
      addDeployer( main, deployer3 );

      TestFlowDeployer deployer4 = new TestFlowDeployer( "Deployer" );
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
}
