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
import java.util.Collections;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.deployers.client.spi.DeployerClient;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.client.spi.DeploymentFactory;
import org.jboss.deployers.spi.attachments.MutableAttachments;
import org.jboss.deployers.spi.attachments.PredeterminedManagedObjectAttachments;
import org.jboss.deployers.spi.deployer.Deployer;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.structure.ContextInfo;
import org.jboss.test.deployers.AbstractDeployerTest;
import org.jboss.test.deployers.deployer.support.TestDependencyMetaData;
import org.jboss.test.deployers.deployer.support.TestDescribeDeployer;
import org.jboss.test.deployers.deployer.support.TestSimpleDeployer3;

/**
 * DeployerDependencyUnitTestCase.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class DeployerDependencyUnitTestCase extends AbstractDeployerTest
{
   private static final DeploymentFactory factory = new DeploymentFactory();
   
   private TestDescribeDeployer deployer1 = new TestDescribeDeployer();
   private TestSimpleDeployer3 deployer2 = new TestSimpleDeployer3(DeploymentStages.CLASSLOADER);
   private TestSimpleDeployer3 deployer3 = new TestSimpleDeployer3(DeploymentStages.INSTALLED);

   public static List<String> NONE = Collections.emptyList();
   public static List<String> A = makeList("A");
   public static List<String> B = makeList("B");
   public static List<String> AB = makeList("A", "B");
   public static List<String> BA = makeList("B", "A");
   public static List<String> ABA = makeList("A", "B", "A");
   public static List<String> ABB = makeList("A", "B", "B");
   public static List<String> BAA = makeList("B", "A", "A");
   public static List<String> BABA = makeList("B", "A", "B", "A");
   public static List<String> AC = makeList("A", "A/C");
   public static List<String> ACB = makeList("A", "A/C", "B");
   public static List<String> BAC = makeList("B", "A", "A/C");
   public static List<String> CAB = makeList("A/C", "A", "B");
   public static List<String> BCA = makeList("B", "A/C", "A");
   
   private static <T> List<T> makeList(T... objects)
   {
      List<T> result = new ArrayList<T>();
      for (T object : objects)
         result.add(object);
      return result;
   }
   
   public static Test suite()
   {
      return new TestSuite(DeployerDependencyUnitTestCase.class);
   }
   
   public DeployerDependencyUnitTestCase(String name)
   {
      super(name);
   }

   public void testADependsOnBAtClassLoaderCorrectOrder() throws Exception
   {
      DeployerClient main = getMainDeployer();
      
      Deployment a = createSimpleDeployment("A");
      TestDependencyMetaData depA = new TestDependencyMetaData("A");
      depA.addDependencyItem("B", DeploymentStages.CLASSLOADER);
      addMetaData(a, depA);
      main.addDeployment(a);

      Deployment b = createSimpleDeployment("B");
      main.addDeployment(b);

      main.process();

      assertEquals(AB, deployer1.deployed);
      assertEquals(NONE, deployer1.undeployed);
      assertEquals(BA, deployer2.deployed);
      assertEquals(NONE, deployer2.undeployed);
      assertEquals(BA, deployer3.deployed);
      assertEquals(NONE, deployer3.undeployed);
   }

   public void testADependsOnBAtClassLoaderWrongOrder() throws Exception
   {
      DeployerClient main = getMainDeployer();
      
      Deployment a = createSimpleDeployment("A");
      TestDependencyMetaData depA = new TestDependencyMetaData("A");
      depA.addDependencyItem("B", DeploymentStages.CLASSLOADER);
      addMetaData(a, depA);
      main.addDeployment(a);

      main.process();

      assertEquals(A, deployer1.deployed);
      assertEquals(NONE, deployer2.deployed);
      assertEquals(NONE, deployer3.deployed);

      Deployment b = createSimpleDeployment("B");
      main.addDeployment(b);

      main.process();

      assertEquals(AB, deployer1.deployed);
      assertEquals(NONE, deployer1.undeployed);
      assertEquals(BA, deployer2.deployed);
      assertEquals(NONE, deployer2.undeployed);
      assertEquals(BA, deployer3.deployed);
      assertEquals(NONE, deployer3.undeployed);
   }

   public void testADependsOnBAtClassLoaderUndeployANoB() throws Exception
   {
      DeployerClient main = getMainDeployer();
      
      Deployment a = createSimpleDeployment("A");
      TestDependencyMetaData depA = new TestDependencyMetaData("A");
      depA.addDependencyItem("B", DeploymentStages.CLASSLOADER);
      addMetaData(a, depA);
      main.addDeployment(a);

      main.process();

      assertEquals(A, deployer1.deployed);
      assertEquals(NONE, deployer1.undeployed);
      assertEquals(NONE, deployer2.deployed);
      assertEquals(NONE, deployer2.undeployed);
      assertEquals(NONE, deployer3.deployed);
      assertEquals(NONE, deployer3.undeployed);
      
      main.removeDeployment(a);
      main.process();

      assertEquals(A, deployer1.deployed);
      assertEquals(A, deployer1.undeployed);
      assertEquals(NONE, deployer2.deployed);
      assertEquals(NONE, deployer2.undeployed);
      assertEquals(NONE, deployer3.deployed);
      assertEquals(NONE, deployer3.undeployed);
   }

   public void testADependsOnBAtClassLoaderUndeployB() throws Exception
   {
      DeployerClient main = getMainDeployer();
      
      Deployment a = createSimpleDeployment("A");
      TestDependencyMetaData depA = new TestDependencyMetaData("A");
      depA.addDependencyItem("B", DeploymentStages.CLASSLOADER);
      addMetaData(a, depA);
      main.addDeployment(a);

      Deployment b = createSimpleDeployment("B");
      main.addDeployment(b);

      main.process();

      assertEquals(AB, deployer1.deployed);
      assertEquals(NONE, deployer1.undeployed);
      assertEquals(BA, deployer2.deployed);
      assertEquals(NONE, deployer2.undeployed);
      assertEquals(BA, deployer3.deployed);
      assertEquals(NONE, deployer3.undeployed);
      
      main.removeDeployment(b);
      main.process();

      assertEquals(AB, deployer1.deployed);
      assertEquals(B, deployer1.undeployed);
      assertEquals(BA, deployer2.deployed);
      assertEquals(AB, deployer2.undeployed);
      assertEquals(BA, deployer3.deployed);
      assertEquals(AB, deployer3.undeployed);
   }

   public void testADependsOnBAtClassLoaderRedeployB() throws Exception
   {
      DeployerClient main = getMainDeployer();
      
      Deployment a = createSimpleDeployment("A");
      TestDependencyMetaData depA = new TestDependencyMetaData("A");
      depA.addDependencyItem("B", DeploymentStages.CLASSLOADER);
      addMetaData(a, depA);
      main.addDeployment(a);

      Deployment b = createSimpleDeployment("B");
      main.addDeployment(b);

      main.process();

      assertEquals(AB, deployer1.deployed);
      assertEquals(NONE, deployer1.undeployed);
      assertEquals(BA, deployer2.deployed);
      assertEquals(NONE, deployer2.undeployed);
      assertEquals(BA, deployer3.deployed);
      assertEquals(NONE, deployer3.undeployed);
      
      main.removeDeployment(b);
      main.process();
      
      main.addDeployment(b);
      main.process();

      assertEquals(ABB, deployer1.deployed);
      assertEquals(B, deployer1.undeployed);
      assertEquals(BABA, deployer2.deployed);
      assertEquals(AB, deployer2.undeployed);
      assertEquals(BABA, deployer3.deployed);
      assertEquals(AB, deployer3.undeployed);
   }

   public void testADependsOnBAtClassLoaderRedeployA() throws Exception
   {
      DeployerClient main = getMainDeployer();
      
      Deployment a = createSimpleDeployment("A");
      TestDependencyMetaData depA = new TestDependencyMetaData("A");
      depA.addDependencyItem("B", DeploymentStages.CLASSLOADER);
      addMetaData(a, depA);
      main.addDeployment(a);

      Deployment b = createSimpleDeployment("B");
      main.addDeployment(b);

      main.process();

      assertEquals(AB, deployer1.deployed);
      assertEquals(NONE, deployer1.undeployed);
      assertEquals(BA, deployer2.deployed);
      assertEquals(NONE, deployer2.undeployed);
      assertEquals(BA, deployer3.deployed);
      assertEquals(NONE, deployer3.undeployed);
      
      main.removeDeployment(a);
      main.process();
      
      main.addDeployment(a);
      main.process();

      assertEquals(ABA, deployer1.deployed);
      assertEquals(A, deployer1.undeployed);
      assertEquals(BAA, deployer2.deployed);
      assertEquals(A, deployer2.undeployed);
      assertEquals(BAA, deployer3.deployed);
      assertEquals(A, deployer3.undeployed);
   }

   public void testADependsOnBAtClassLoaderWhichFailsInInstall() throws Exception
   {
      DeployerClient main = getMainDeployer();
      
      Deployment a = createSimpleDeployment("A");
      TestDependencyMetaData depA = new TestDependencyMetaData("A");
      depA.addDependencyItem("B", DeploymentStages.CLASSLOADER);
      addMetaData(a, depA);
      main.addDeployment(a);

      Deployment b = createSimpleDeployment("B");
      makeFail(b, deployer3);
      main.addDeployment(b);

      main.process();

      assertEquals(AB, deployer1.deployed);
      assertEquals(B, deployer1.undeployed);
      assertEquals(B, deployer2.deployed);
      assertEquals(B, deployer2.undeployed);
      assertEquals(B, deployer3.deployed);
      assertEquals(NONE, deployer3.undeployed);
   }

   public void testADependsOnBAtInstallCorrectOrder() throws Exception
   {
      DeployerClient main = getMainDeployer();
      
      Deployment a = createSimpleDeployment("A");
      TestDependencyMetaData depA = new TestDependencyMetaData("A");
      depA.addDependencyItem("B", DeploymentStages.INSTALLED);
      addMetaData(a, depA);
      main.addDeployment(a);

      Deployment b = createSimpleDeployment("B");
      main.addDeployment(b);

      main.process();

      assertEquals(AB, deployer1.deployed);
      assertEquals(NONE, deployer1.undeployed);
      assertEquals(AB, deployer2.deployed);
      assertEquals(NONE, deployer2.undeployed);
      assertEquals(BA, deployer3.deployed);
      assertEquals(NONE, deployer3.undeployed);
   }

   public void testADependsOnBAtInstallWrongOrder() throws Exception
   {
      DeployerClient main = getMainDeployer();
      
      Deployment a = createSimpleDeployment("A");
      TestDependencyMetaData depA = new TestDependencyMetaData("A");
      depA.addDependencyItem("B", DeploymentStages.INSTALLED);
      addMetaData(a, depA);
      main.addDeployment(a);

      main.process();

      assertEquals(A, deployer1.deployed);
      assertEquals(A, deployer2.deployed);
      assertEquals(NONE, deployer3.deployed);

      Deployment b = createSimpleDeployment("B");
      main.addDeployment(b);

      main.process();

      assertEquals(AB, deployer1.deployed);
      assertEquals(NONE, deployer1.undeployed);
      assertEquals(AB, deployer2.deployed);
      assertEquals(NONE, deployer2.undeployed);
      assertEquals(BA, deployer3.deployed);
      assertEquals(NONE, deployer3.undeployed);
   }

   public void testADependsOnBAtInstallUndeployANoB() throws Exception
   {
      DeployerClient main = getMainDeployer();
      
      Deployment a = createSimpleDeployment("A");
      TestDependencyMetaData depA = new TestDependencyMetaData("A");
      depA.addDependencyItem("B", DeploymentStages.INSTALLED);
      addMetaData(a, depA);
      main.addDeployment(a);

      main.process();

      assertEquals(A, deployer1.deployed);
      assertEquals(NONE, deployer1.undeployed);
      assertEquals(A, deployer2.deployed);
      assertEquals(NONE, deployer2.undeployed);
      assertEquals(NONE, deployer3.deployed);
      assertEquals(NONE, deployer3.undeployed);
      
      main.removeDeployment(a);
      main.process();

      assertEquals(A, deployer1.deployed);
      assertEquals(A, deployer1.undeployed);
      assertEquals(A, deployer2.deployed);
      assertEquals(A, deployer2.undeployed);
      assertEquals(NONE, deployer3.deployed);
      assertEquals(NONE, deployer3.undeployed);
   }

   public void testADependsOnBAtInstallUndeployB() throws Exception
   {
      DeployerClient main = getMainDeployer();
      
      Deployment a = createSimpleDeployment("A");
      TestDependencyMetaData depA = new TestDependencyMetaData("A");
      depA.addDependencyItem("B", DeploymentStages.INSTALLED);
      addMetaData(a, depA);
      main.addDeployment(a);

      Deployment b = createSimpleDeployment("B");
      main.addDeployment(b);

      main.process();

      assertEquals(AB, deployer1.deployed);
      assertEquals(NONE, deployer1.undeployed);
      assertEquals(AB, deployer2.deployed);
      assertEquals(NONE, deployer2.undeployed);
      assertEquals(BA, deployer3.deployed);
      assertEquals(NONE, deployer3.undeployed);
      
      main.removeDeployment(b);
      main.process();

      assertEquals(AB, deployer1.deployed);
      assertEquals(B, deployer1.undeployed);
      assertEquals(AB, deployer2.deployed);
      assertEquals(B, deployer2.undeployed);
      assertEquals(BA, deployer3.deployed);
      assertEquals(AB, deployer3.undeployed);
   }

   public void testADependsOnBAtInstallRedeployB() throws Exception
   {
      DeployerClient main = getMainDeployer();
      
      Deployment a = createSimpleDeployment("A");
      TestDependencyMetaData depA = new TestDependencyMetaData("A");
      depA.addDependencyItem("B", DeploymentStages.INSTALLED);
      addMetaData(a, depA);
      main.addDeployment(a);

      Deployment b = createSimpleDeployment("B");
      main.addDeployment(b);

      main.process();

      assertEquals(AB, deployer1.deployed);
      assertEquals(NONE, deployer1.undeployed);
      assertEquals(AB, deployer2.deployed);
      assertEquals(NONE, deployer2.undeployed);
      assertEquals(BA, deployer3.deployed);
      assertEquals(NONE, deployer3.undeployed);
      
      main.removeDeployment(b);
      main.process();
      
      main.addDeployment(b);
      main.process();

      assertEquals(ABB, deployer1.deployed);
      assertEquals(B, deployer1.undeployed);
      assertEquals(ABB, deployer2.deployed);
      assertEquals(B, deployer2.undeployed);
      assertEquals(BABA, deployer3.deployed);
      assertEquals(AB, deployer3.undeployed);
   }

   public void testADependsOnBAtInstallRedeployA() throws Exception
   {
      DeployerClient main = getMainDeployer();
      
      Deployment a = createSimpleDeployment("A");
      TestDependencyMetaData depA = new TestDependencyMetaData("A");
      depA.addDependencyItem("B", DeploymentStages.INSTALLED);
      addMetaData(a, depA);
      main.addDeployment(a);

      Deployment b = createSimpleDeployment("B");
      main.addDeployment(b);

      main.process();

      assertEquals(AB, deployer1.deployed);
      assertEquals(NONE, deployer1.undeployed);
      assertEquals(AB, deployer2.deployed);
      assertEquals(NONE, deployer2.undeployed);
      assertEquals(BA, deployer3.deployed);
      assertEquals(NONE, deployer3.undeployed);
      
      main.removeDeployment(a);
      main.process();
      
      main.addDeployment(a);
      main.process();

      assertEquals(ABA, deployer1.deployed);
      assertEquals(A, deployer1.undeployed);
      assertEquals(ABA, deployer2.deployed);
      assertEquals(A, deployer2.undeployed);
      assertEquals(BAA, deployer3.deployed);
      assertEquals(A, deployer3.undeployed);
   }

   public void testADependsOnBAtInstallWhichFailsInInstall() throws Exception
   {
      DeployerClient main = getMainDeployer();
      
      Deployment a = createSimpleDeployment("A");
      TestDependencyMetaData depA = new TestDependencyMetaData("A");
      depA.addDependencyItem("B", DeploymentStages.INSTALLED);
      addMetaData(a, depA);
      main.addDeployment(a);

      Deployment b = createSimpleDeployment("B");
      makeFail(b, deployer3);
      main.addDeployment(b);

      main.process();

      assertEquals(AB, deployer1.deployed);
      assertEquals(B, deployer1.undeployed);
      assertEquals(AB, deployer2.deployed);
      assertEquals(B, deployer2.undeployed);
      assertEquals(B, deployer3.deployed);
      assertEquals(NONE, deployer3.undeployed);
   }

   public void testChildOfADependsOnBAtClassLoaderWrongOrder() throws Exception
   {
      DeployerClient main = getMainDeployer();
      
      Deployment a = createSimpleDeployment("A");
      ContextInfo c = factory.addContext(a, "C");
      TestDependencyMetaData depC = new TestDependencyMetaData("A");
      depC.addDependencyItem("B", DeploymentStages.CLASSLOADER);
      addMetaData(c, depC);
      main.addDeployment(a);

      main.process();

      assertEquals(AC, deployer1.deployed);
      assertEquals(NONE, deployer1.undeployed);
      assertEquals(NONE, deployer2.deployed);
      assertEquals(NONE, deployer2.undeployed);
      assertEquals(NONE, deployer3.deployed);
      assertEquals(NONE, deployer3.undeployed);

      Deployment b = createSimpleDeployment("B");
      main.addDeployment(b);

      main.process();

      assertEquals(ACB, deployer1.deployed);
      assertEquals(NONE, deployer1.undeployed);
      assertEquals(BAC, deployer2.deployed);
      assertEquals(NONE, deployer2.undeployed);
      assertEquals(BAC, deployer3.deployed);
      assertEquals(NONE, deployer3.undeployed);

      main.removeDeployment(b);
      main.process();
      
      assertEquals(ACB, deployer1.deployed);
      assertEquals(B, deployer1.undeployed);
      assertEquals(BAC, deployer2.deployed);
      assertEquals(CAB, deployer2.undeployed);
      assertEquals(BAC, deployer3.deployed);
      assertEquals(CAB, deployer3.undeployed);

      main.removeDeployment(a);
      main.process();
      
      assertEquals(ACB, deployer1.deployed);
      assertEquals(BCA, deployer1.undeployed);
      assertEquals(BAC, deployer2.deployed);
      assertEquals(CAB, deployer2.undeployed);
      assertEquals(BAC, deployer3.deployed);
      assertEquals(CAB, deployer3.undeployed);
   }

   protected static void addMetaData(PredeterminedManagedObjectAttachments attachments, TestDependencyMetaData md)
   {
      MutableAttachments mutable = (MutableAttachments) attachments.getPredeterminedManagedObjects();
      mutable.addAttachment(TestDependencyMetaData.class, md);
   }
   
   protected static void makeFail(PredeterminedManagedObjectAttachments attachments, Deployer deployer)
   {
      MutableAttachments mutable = (MutableAttachments) attachments.getPredeterminedManagedObjects();
      mutable.addAttachment("fail", deployer);
   }
   
   protected DeployerClient getMainDeployer()
   {
      return createMainDeployer(deployer1, deployer2, deployer3);
   }
}
