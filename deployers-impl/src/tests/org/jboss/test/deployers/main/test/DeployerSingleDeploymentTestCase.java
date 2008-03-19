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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.Test;

import org.jboss.deployers.client.spi.DeployerClient;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.client.spi.main.MainDeployer;
import org.jboss.deployers.plugins.main.MainDeployerImpl;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.structure.StructureMetaDataFactory;
import org.jboss.deployers.structure.spi.DeploymentContext;
import org.jboss.deployers.structure.spi.StructuralDeployers;
import org.jboss.deployers.structure.spi.StructureBuilder;
import org.jboss.deployers.structure.spi.helpers.AbstractStructureBuilder;
import org.jboss.test.deployers.main.support.AddProcessRemoveProcessRunnable;
import org.jboss.test.deployers.main.support.DeployUndeployRunnable;
import org.jboss.test.deployers.main.support.DeployerTestRunnable;
import org.jboss.test.deployers.main.support.FailedDeployUndeployRunnable;

/**
 * Single deployment API test case.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class DeployerSingleDeploymentTestCase extends AbstractMainDeployerTest
{
   public DeployerSingleDeploymentTestCase(String name)
   {
      super(name);
   }

   public static Test suite()
   {
      return suite(DeployerSingleDeploymentTestCase.class);
   }

   protected void checkFailedDeployOnStructure(DeployerClient mainDeployer, final int failed, int size) throws Throwable
   {
      final StructureBuilder builder = new AbstractStructureBuilder();
      StructuralDeployers structuralDeployers = new StructuralDeployers()
      {
         public DeploymentContext determineStructure(Deployment deployment) throws DeploymentException
         {
            String name = deployment.getName();
            if (name.endsWith("deployment" + failed))
               throw new RuntimeException(String.valueOf(failed));
            return builder.populateContext(deployment, StructureMetaDataFactory.createStructureMetaData());
         }
      };
      ((MainDeployerImpl)mainDeployer).setStructuralDeployers(structuralDeployers);
      
      Deployment[] deployments = new Deployment[size];
      for(int i = 0; i < size; i++)
         deployments[i] = createSimpleDeployment("deployment" + i);
      try
      {
         mainDeployer.deploy(deployments);
         fail("Should not be here.");
      }
      catch (DeploymentException e)
      {
         Throwable cause = e.getCause();
         assertNotNull(cause);
         String msg = cause.getMessage();
         assertEquals(failed, Integer.parseInt(msg));
      }
      deployer.clear();
   }

   public void testFailedDeployOnStructure() throws Throwable
   {
      DeployerClient main = getMainDeployer();
      checkFailedDeployOnStructure(main, 0, 3);
      checkFailedDeployOnStructure(main, 1, 3);
      checkFailedDeployOnStructure(main, 2, 3);
   }

   protected void checkFailedDeploy(DeployerClient mainDeployer, int failed, int size) throws Throwable
   {
      Deployment[] deployments = new Deployment[size];
      for(int i = 0; i < size; i++)
      {
         deployments[i] = createSimpleDeployment("deployment" + i);
         if (i == failed)
            makeFail(deployments[i], deployer);
      }
      try
      {
         mainDeployer.deploy(deployments);
         fail("Should not be here.");
      }
      catch (DeploymentException e)
      {
         assertEquals(size, deployer.getUndeployedUnits().size() + deployer.getFailed().size());
         assertEquals(Collections.singletonList("deployment" + failed), deployer.getFailed());
      }
      deployer.clear();
   }

   public void testFailedDeploy() throws Throwable
   {
      DeployerClient main = getMainDeployer();
      checkFailedDeploy(main, 0, 3);
      checkFailedDeploy(main, 1, 3);
      checkFailedDeploy(main, 2, 3);
   }

   public void testRedeploy() throws Throwable
   {
      DeployerClient main = getMainDeployer();

      Deployment context = createSimpleDeployment("redeploy");
      main.deploy(context);
      List<String> expected = new ArrayList<String>();
      expected.add(context.getName());
      assertEquals(expected, deployer.getDeployedUnits());

      main.undeploy(context);
      assertEquals(expected, deployer.getUndeployedUnits());

      deployer.clear();
      main.deploy(context);
      expected.clear();
      expected.add(context.getName());
      assertEquals(expected, deployer.getDeployedUnits());
   }

   public void testDeployRemoveProcess() throws Throwable
   {
      DeployerClient main = getMainDeployer();

      Deployment context = createSimpleDeployment("drp");
      main.deploy(context);
      List<String> expected = new ArrayList<String>();
      expected.add(context.getName());
      assertEquals(expected, deployer.getDeployedUnits());

      main.removeDeployment(context);
      main.process();
      assertEquals(expected, deployer.getUndeployedUnits());
   }

   public void testAddProcessUndeploy() throws Throwable
   {
      DeployerClient main = getMainDeployer();

      Deployment context = createSimpleDeployment("apu");
      main.addDeployment(context);
      main.process();
      List<String> expected = new ArrayList<String>();
      expected.add(context.getName());
      assertEquals(expected, deployer.getDeployedUnits());

      main.undeploy(context);
      assertEquals(expected, deployer.getUndeployedUnits());
   }

   public void testDeployShutdown() throws Throwable
   {
      MainDeployer main = (MainDeployer)getMainDeployer();

      Deployment context = createSimpleDeployment("shutdown");
      main.deploy(context);
      List<String> expected = new ArrayList<String>();
      expected.add(context.getName());
      assertEquals(expected, deployer.getDeployedUnits());

      main.shutdown();
      assertEquals(expected, deployer.getUndeployedUnits());
   }

   public void testSingleAndMultipleMix() throws Throwable
   {
      DeployerClient main = getMainDeployer();

      Deployment single = createSimpleDeployment("single");
      main.deploy(single);
      List<String> expected = new ArrayList<String>();
      expected.add(single.getName());
      assertEquals(expected, deployer.getDeployedUnits());

      Deployment normal = createSimpleDeployment("normal");
      main.addDeployment(normal);
      main.process();
      expected.add(normal.getName());
      assertEquals(expected, deployer.getDeployedUnits());

      main.undeploy(single);
      expected.clear();
      expected.add(single.getName());
      assertEquals(expected, deployer.getUndeployedUnits());

      main.removeDeployment(normal.getName());
      main.process();
      expected.add(normal.getName());
      assertEquals(expected, deployer.getUndeployedUnits());
   }

   public void testSingleAndMultipleMix2() throws Throwable
   {
      DeployerClient main = getMainDeployer();

      Deployment single = createSimpleDeployment("single");
      main.deploy(single);
      List<String> expected = new ArrayList<String>();
      expected.add(single.getName());
      assertEquals(expected, deployer.getDeployedUnits());

      Deployment normal = createSimpleDeployment("normal");
      main.addDeployment(normal);
      main.process();
      expected.add(normal.getName());
      assertEquals(expected, deployer.getDeployedUnits());

      main.removeDeployment(normal.getName());
      main.process();
      expected.clear();
      expected.add(normal.getName());
      assertEquals(expected, deployer.getUndeployedUnits());

      main.undeploy(single);
      expected.add(single.getName());
      assertEquals(expected, deployer.getUndeployedUnits());
   }

   public void testMultiThreads() throws Exception
   {
      DeployerClient main = getMainDeployer();
      int n = 30;
      DeployerTestRunnable[] runnables = new DeployerTestRunnable[n];
      for(int i = 0; i < n; i++)
      {
         if (i % 3 == 0)
            runnables[i] = new DeployUndeployRunnable(main, createSimpleDeployment("deployundeploy" + i));
         else if (i % 3 == 1)
            runnables[i] = new AddProcessRemoveProcessRunnable(main, createSimpleDeployment("aprp" + i));
         else
            runnables[i] = new FailedDeployUndeployRunnable(main, createSimpleDeployment("failed" + i), deployer);
      }
      Thread[] threads = new Thread[n];
      for(int i = 0; i < n; i++)
      {
         threads[i] = new Thread(runnables[i]);
         threads[i].start();
      }
      for(int i = 0; i < n; i++)
      {
         threads[i].join();
         assertTrue(runnables[i].toString(), runnables[i].isValid());
      }
   }
}
