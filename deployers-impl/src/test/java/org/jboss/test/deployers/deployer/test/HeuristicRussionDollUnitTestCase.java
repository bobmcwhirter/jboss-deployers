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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.deployers.client.spi.DeployerClient;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.spi.DeploymentState;
import org.jboss.deployers.spi.attachments.MutableAttachments;
import org.jboss.deployers.spi.attachments.PredeterminedManagedObjectAttachments;
import org.jboss.deployers.spi.deployer.Deployer;
import org.jboss.deployers.spi.structure.ContextInfo;
import org.jboss.test.deployers.AbstractDeployerTest;
import org.jboss.test.deployers.deployer.support.TestComponentDeployer;
import org.jboss.test.deployers.deployer.support.TestComponentMetaData;
import org.jboss.test.deployers.deployer.support.TestComponentMetaDataContainer;
import org.jboss.test.deployers.deployer.support.TestSimpleDeployer2;

/**
 * HeuristicRussionDollUnitTestCase.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class HeuristicRussionDollUnitTestCase extends AbstractDeployerTest
{
   private TestComponentDeployer deployer0 = new TestComponentDeployer(1);
   private TestSimpleDeployer2 deployer1 = new TestSimpleDeployer2(true, 2);
   private TestSimpleDeployer2 deployer2 = new TestSimpleDeployer2(false, 3);
   
   private static String parentName = "parent";
   private static String childPath = "child";
   private static String childName = parentName + "/" + childPath;
   private static String child1Path = "child1";
   private static String child1Name = parentName + "/" + child1Path;
   private static String child2Path = "child2";
   private static String child2Name = parentName + "/" + child2Path;

   private static TestComponentMetaData parentmd1 = new TestComponentMetaData(parentName + ".1", false);
   private static TestComponentMetaData parentmd2 = new TestComponentMetaData(parentName + ".2", false);
   private static TestComponentMetaData childmd1 = new TestComponentMetaData(childName + ".1", false);
   private static TestComponentMetaData childmd2 = new TestComponentMetaData(childName + ".2", false);
   private static TestComponentMetaData child1md1 = new TestComponentMetaData(child1Name + ".1", false);
   private static TestComponentMetaData child1md2 = new TestComponentMetaData(child1Name + ".2", false);
   private static TestComponentMetaData child2md1 = new TestComponentMetaData(child2Name + ".1", false);
   private static TestComponentMetaData child2md2 = new TestComponentMetaData(child2Name + ".2", false);

   private static List<String> expectedNothing = Collections.emptyList();
   private static List<String> expectedParent = makeList(parentName);
   private static List<String> expectedParentMD12 = makeList(parentName, parentmd1.name, parentmd2.name);
   private static List<String> expectedPMD21 = makeList(parentmd2.name, parentmd1.name);
   private static List<String> expectedMD12Parent = makeList(parentmd1.name, parentmd2.name, parentName);
   private static List<String> expectedMD21Parent = makeList(parentmd2.name, parentmd1.name, parentName);
   private static List<String> expectedChild = makeList(childName);
   private static List<String> expectedMD12Child = makeList(childmd1.name, childmd2.name, childName);
   private static List<String> expectedMD21C = makeList(childmd2.name, childmd1.name);
   private static List<String> expectedPMD21ChildMD21 = makeList(parentmd2.name, parentmd1.name, childName, childmd2.name, childmd1.name);
   private static List<String> expectedChild1 = makeList(child1Name);
   private static List<String> expectedMD12Child1 = makeList(child1md1.name, child1md2.name, child1Name);
   private static List<String> expectedMD21C1 = makeList(child1md2.name, child1md1.name);
   private static List<String> expectedParentChild = makeList(parentName, childName);
   private static List<String> expectedParentMD12Child = makeList(parentName, parentmd1.name, parentmd2.name, childName);
   private static List<String> expectedParentMD12ChildMD12 = makeList(parentName, parentmd1.name, parentmd2.name, childName, childmd1.name, childmd2.name);
   private static List<String> expectedChildParent = makeList(childName, parentName);
   private static List<String> expectedMD12ChildMD12Parent = makeList(childmd1.name, childmd2.name, childName, parentmd1.name, parentmd2.name, parentName);
   private static List<String> expectedMD21ChildMD21Parent = makeList(childmd2.name, childmd1.name, childName, parentmd2.name, parentmd1.name, parentName);
   private static List<String> expectedChild1Parent = makeList(child1Name, parentName);
   private static List<String> expectedMD21Child1MD21Parent = makeList(child1md2.name, child1md1.name, child1Name, parentmd2.name, parentmd1.name, parentName);
   private static List<String> expectedParentChild1 = makeList(parentName, child1Name);
   private static List<String> expectedParentMD12Child1 = makeList(parentName, parentmd1.name, parentmd2.name, child1Name);
   private static List<String> expectedParentChild1Child2 = makeList(parentName, child1Name, child2Name);
   private static List<String> expectedParentMD12Child1MD12Child2 = makeList(parentName, parentmd1.name, parentmd2.name, child1Name, child1md1.name, child1md2.name, child2Name);
   private static List<String> expectedParentMD12Child1MD12Child2MD12 = makeList(parentName, parentmd1.name, parentmd2.name, child1Name, child1md1.name, child1md2.name, child2Name, child2md1.name, child2md2.name);
   private static List<String> expectedChild1Child2Parent = makeList(child1Name, child2Name, parentName);
   private static List<String> expectedMD12Child1MD12Child2MD12Parent = makeList(child1md1.name, child1md2.name, child1Name, child2md1.name, child2md2.name, child2Name, parentmd1.name, parentmd2.name, parentName);
   private static List<String> expectedChild2Child1Parent = makeList(child2Name, child1Name, parentName);
   private static List<String> expectedMD21Child2MD21Child1MD21Parent = makeList(child2md2.name, child2md1.name, child2Name, child1md2.name, child1md1.name, child1Name, parentmd2.name, parentmd1.name, parentName);
   private static List<String> expectedChild1Child2 = makeList(child1Name, child2Name);
   private static List<String> expectedMD12Child1MD12Child2 = makeList(child1md1.name, child1md2.name, child1Name, child2md1.name, child2md2.name, child2Name);
   private static List<String> expectedChild2Child1 = makeList(child2Name, child1Name);
   private static List<String> expectedPMD21Child2MD21Child1MD21 = makeList(parentmd2.name, parentmd1.name, child2Name, child2md2.name, child2md1.name, child1Name, child1md2.name, child1md1.name);
   private static List<String> expectedMD21C2Child1MD21 = makeList(child2md2.name, child2md1.name, child1Name, child1md2.name, child1md1.name);
   
   private static TestComponentMetaDataContainer parentmd = new TestComponentMetaDataContainer(parentmd1, parentmd2);
   private static TestComponentMetaDataContainer childmd = new TestComponentMetaDataContainer(childmd1, childmd2);
   private static TestComponentMetaDataContainer child1md = new TestComponentMetaDataContainer(child1md1, child1md2);
   private static TestComponentMetaDataContainer child2md = new TestComponentMetaDataContainer(child2md1, child2md2);
   
   private static <T> List<T> makeList(T... objects)
   {
      List<T> result = new ArrayList<T>();
      for (T object : objects)
         result.add(object);
      return result;
   }
   
   public static Test suite()
   {
      return new TestSuite(HeuristicRussionDollUnitTestCase.class);
   }
   
   public HeuristicRussionDollUnitTestCase(String name)
   {
      super(name);
   }

   public void testDeployParentNoErrors() throws Exception
   {
      DeployerClient main = getMainDeployer();
      
      Deployment deployment = createSimpleDeployment(parentName);
      main.addDeployment(deployment);
      main.process();

      assertEquals(expectedParent, deployer1.getDeployedUnits());
      assertEquals(expectedNothing, deployer1.getUndeployedUnits());
      assertEquals(expectedParent, deployer2.getDeployedUnits());
      assertEquals(expectedNothing, deployer2.getUndeployedUnits());
      assertEquals(DeploymentState.DEPLOYED, main.getDeploymentState(deployment.getName()));
   }

   public void testDeployParentFailInParentDeployer1() throws Exception
   {
      DeployerClient main = getMainDeployer();
      
      Deployment deployment = createSimpleDeployment(parentName);
      makeFail(deployment, deployer1);
      main.addDeployment(deployment);
      main.process();

      assertEquals(expectedParent, deployer1.getDeployedUnits());
      assertEquals(expectedNothing, deployer1.getUndeployedUnits());
      assertEquals(expectedNothing, deployer2.getDeployedUnits());
      assertEquals(expectedNothing, deployer2.getUndeployedUnits());
      assertEquals(DeploymentState.ERROR, main.getDeploymentState(deployment.getName()));
   }

   public void testDeployParentFailInParentDeployer2() throws Exception
   {
      DeployerClient main = getMainDeployer();
      
      Deployment deployment = createSimpleDeployment(parentName);
      makeFail(deployment, deployer2);
      main.addDeployment(deployment);
      main.process();

      Set<String> expected = new HashSet<String>();
      expected.add(deployment.getName());
      assertEquals(expectedParent, deployer1.getDeployedUnits());
      assertEquals(expectedParent, deployer1.getUndeployedUnits());
      assertEquals(expectedParent, deployer2.getDeployedUnits());
      assertEquals(expectedNothing, deployer2.getUndeployedUnits());
      assertEquals(DeploymentState.ERROR, main.getDeploymentState(deployment.getName()));
   }

   public void testDeployParentOneChildNoErrors() throws Exception
   {
      DeployerClient main = getMainDeployer();
      
      Deployment deployment = createSimpleDeployment(parentName);
      addChild(deployment, childPath); 
      main.addDeployment(deployment);
      main.process();

      assertEquals(expectedParentChild, deployer1.getDeployedUnits());
      assertEquals(expectedNothing, deployer1.getUndeployedUnits());
      assertEquals(expectedChildParent, deployer2.getDeployedUnits());
      assertEquals(expectedNothing, deployer2.getUndeployedUnits());
      assertEquals(DeploymentState.DEPLOYED, main.getDeploymentState(deployment.getName()));
      assertEquals(DeploymentState.DEPLOYED, main.getDeploymentState(childName));
   }
   
   public void testDeployParentOneChildFailInParentDeployer1() throws Exception
   {
      DeployerClient main = getMainDeployer();
      
      Deployment deployment = createSimpleDeployment(parentName);
      makeFail(deployment, deployer1);
      addChild(deployment, childPath); 
      main.addDeployment(deployment);
      main.process();

      assertEquals(expectedParent, deployer1.getDeployedUnits());
      assertEquals(expectedNothing, deployer1.getUndeployedUnits());
      assertEquals(expectedNothing, deployer2.getDeployedUnits());
      assertEquals(expectedNothing, deployer2.getUndeployedUnits());
      assertEquals(DeploymentState.ERROR, main.getDeploymentState(deployment.getName()));
      assertEquals(DeploymentState.UNDEPLOYED, main.getDeploymentState(childName));
   }
   
   public void testDeployParentOneChildFailInParentDeployer2() throws Exception
   {
      DeployerClient main = getMainDeployer();
      
      Deployment deployment = createSimpleDeployment(parentName);
      makeFail(deployment, deployer2);
      addChild(deployment, "child"); 
      main.addDeployment(deployment);
      main.process();

      assertEquals(expectedParentChild, deployer1.getDeployedUnits());
      assertEquals(expectedChildParent, deployer1.getUndeployedUnits());
      assertEquals(expectedChildParent, deployer2.getDeployedUnits());
      assertEquals(expectedChild, deployer2.getUndeployedUnits());
      assertEquals(DeploymentState.ERROR, main.getDeploymentState(deployment.getName()));
      assertEquals(DeploymentState.UNDEPLOYED, main.getDeploymentState(childName));
   }
   
   public void testDeployParentOneChildFailInChildDeployer1() throws Exception
   {
      DeployerClient main = getMainDeployer();
      
      Deployment deployment = createSimpleDeployment(parentName);
      ContextInfo child = addChild(deployment, childPath); 
      makeFail(child, deployer1);
      main.addDeployment(deployment);
      main.process();

      assertEquals(expectedParentChild, deployer1.getDeployedUnits());
      assertEquals(expectedParent, deployer1.getUndeployedUnits());
      assertEquals(expectedNothing, deployer2.getDeployedUnits());
      assertEquals(expectedNothing, deployer2.getUndeployedUnits());
      assertEquals(DeploymentState.ERROR, main.getDeploymentState(deployment.getName()));
      assertEquals(DeploymentState.ERROR, main.getDeploymentState(childName));
   }
   
   public void testDeployParentOneChildFailInChildDeployer2() throws Exception
   {
      DeployerClient main = getMainDeployer();
      
      Deployment deployment = createSimpleDeployment(parentName);
      ContextInfo child = addChild(deployment, childPath); 
      makeFail(child, deployer2);
      main.addDeployment(deployment);
      main.process();

      assertEquals(expectedParentChild, deployer1.getDeployedUnits());
      assertEquals(expectedChildParent, deployer1.getUndeployedUnits());
      assertEquals(expectedChild, deployer2.getDeployedUnits());
      assertEquals(expectedNothing, deployer2.getUndeployedUnits());
      assertEquals(DeploymentState.ERROR, main.getDeploymentState(deployment.getName()));
      assertEquals(DeploymentState.ERROR, main.getDeploymentState(childName));
   }

   public void testDeployParentMultipleChildrenNoErrors() throws Exception
   {
      DeployerClient main = getMainDeployer();
      
      Deployment deployment = createSimpleDeployment(parentName);
      addChild(deployment, child1Path); 
      addChild(deployment, child2Path); 
      main.addDeployment(deployment);
      main.process();

      assertEquals(expectedParentChild1Child2, deployer1.getDeployedUnits());
      assertEquals(expectedNothing, deployer1.getUndeployedUnits());
      assertEquals(expectedChild1Child2Parent, deployer2.getDeployedUnits());
      assertEquals(expectedNothing, deployer2.getUndeployedUnits());
      assertEquals(DeploymentState.DEPLOYED, main.getDeploymentState(deployment.getName()));
      assertEquals(DeploymentState.DEPLOYED, main.getDeploymentState(child1Name));
      assertEquals(DeploymentState.DEPLOYED, main.getDeploymentState(child2Name));
   }

   public void testDeployParentMultipleChildrenFailInParentDeployer1() throws Exception
   {
      DeployerClient main = getMainDeployer();
      
      Deployment deployment = createSimpleDeployment(parentName);
      makeFail(deployment, deployer1);
      addChild(deployment, child1Path); 
      addChild(deployment, child2Path); 
      main.addDeployment(deployment);
      main.process();

      assertEquals(expectedParent, deployer1.getDeployedUnits());
      assertEquals(expectedNothing, deployer1.getUndeployedUnits());
      assertEquals(expectedNothing, deployer2.getDeployedUnits());
      assertEquals(expectedNothing, deployer2.getUndeployedUnits());
      assertEquals(DeploymentState.ERROR, main.getDeploymentState(deployment.getName()));
      assertEquals(DeploymentState.UNDEPLOYED, main.getDeploymentState(child1Name));
      assertEquals(DeploymentState.UNDEPLOYED, main.getDeploymentState(child2Name));
   }

   public void testDeployParentMultipleChildrenFailInParentDeployer2() throws Exception
   {
      DeployerClient main = getMainDeployer();
      
      Deployment deployment = createSimpleDeployment(parentName);
      makeFail(deployment, deployer2);
      addChild(deployment, child1Path); 
      addChild(deployment, child2Path); 
      main.addDeployment(deployment);
      main.process();

      assertEquals(expectedParentChild1Child2, deployer1.getDeployedUnits());
      assertEquals(expectedChild2Child1Parent, deployer1.getUndeployedUnits());
      assertEquals(expectedChild1Child2Parent, deployer2.getDeployedUnits());
      assertEquals(expectedChild2Child1, deployer2.getUndeployedUnits());
      assertEquals(DeploymentState.ERROR, main.getDeploymentState(deployment.getName()));
      assertEquals(DeploymentState.UNDEPLOYED, main.getDeploymentState(child1Name));
      assertEquals(DeploymentState.UNDEPLOYED, main.getDeploymentState(child2Name));
   }

   public void testDeployParentMultipleChildrenFailInChild1Deployer1() throws Exception
   {
      DeployerClient main = getMainDeployer();
      
      Deployment deployment = createSimpleDeployment(parentName);
      ContextInfo child1 = addChild(deployment, child1Path); 
      makeFail(child1, deployer1);
      addChild(deployment, child2Path); 
      main.addDeployment(deployment);
      main.process();

      assertEquals(expectedParentChild1, deployer1.getDeployedUnits());
      assertEquals(expectedParent, deployer1.getUndeployedUnits());
      assertEquals(expectedNothing, deployer2.getDeployedUnits());
      assertEquals(expectedNothing, deployer2.getUndeployedUnits());
      assertEquals(DeploymentState.ERROR, main.getDeploymentState(deployment.getName()));
      assertEquals(DeploymentState.ERROR, main.getDeploymentState(child1Name));
      assertEquals(DeploymentState.UNDEPLOYED, main.getDeploymentState(child2Name));
   }

   public void testDeployParentMultipleChildrenFailInChild1Deployer2() throws Exception
   {
      DeployerClient main = getMainDeployer();
      
      Deployment deployment = createSimpleDeployment(parentName);
      ContextInfo child1 = addChild(deployment, child1Path); 
      makeFail(child1, deployer2);
      addChild(deployment, child2Path); 
      main.addDeployment(deployment);
      main.process();

      assertEquals(expectedParentChild1Child2, deployer1.getDeployedUnits());
      assertEquals(expectedChild2Child1Parent, deployer1.getUndeployedUnits());
      assertEquals(expectedChild1, deployer2.getDeployedUnits());
      assertEquals(expectedNothing, deployer2.getUndeployedUnits());
      assertEquals(DeploymentState.ERROR, main.getDeploymentState(deployment.getName()));
      assertEquals(DeploymentState.ERROR, main.getDeploymentState(child1Name));
      assertEquals(DeploymentState.UNDEPLOYED, main.getDeploymentState(child2Name));
   }

   public void testDeployParentMultipleChildrenFailInChild2Deployer1() throws Exception
   {
      DeployerClient main = getMainDeployer();
      
      Deployment deployment = createSimpleDeployment(parentName);
      addChild(deployment, child1Path); 
      ContextInfo child2 = addChild(deployment, child2Path); 
      makeFail(child2, deployer1);
      main.addDeployment(deployment);
      main.process();

      assertEquals(expectedParentChild1Child2, deployer1.getDeployedUnits());
      assertEquals(expectedChild1Parent, deployer1.getUndeployedUnits());
      assertEquals(expectedNothing, deployer2.getDeployedUnits());
      assertEquals(expectedNothing, deployer2.getUndeployedUnits());
      assertEquals(DeploymentState.ERROR, main.getDeploymentState(deployment.getName()));
      assertEquals(DeploymentState.UNDEPLOYED, main.getDeploymentState(child1Name));
      assertEquals(DeploymentState.ERROR, main.getDeploymentState(child2Name));
   }

   public void testDeployParentMultipleChildrenFailInChild2Deployer2() throws Exception
   {
      DeployerClient main = getMainDeployer();
      
      Deployment deployment = createSimpleDeployment(parentName);
      addChild(deployment, child1Path); 
      ContextInfo child2 = addChild(deployment, child2Path); 
      makeFail(child2, deployer2);
      main.addDeployment(deployment);
      main.process();

      assertEquals(expectedParentChild1Child2, deployer1.getDeployedUnits());
      assertEquals(expectedChild2Child1Parent, deployer1.getUndeployedUnits());
      assertEquals(expectedChild1Child2, deployer2.getDeployedUnits());
      assertEquals(expectedChild1, deployer2.getUndeployedUnits());
      assertEquals(DeploymentState.ERROR, main.getDeploymentState(deployment.getName()));
      assertEquals(DeploymentState.UNDEPLOYED, main.getDeploymentState(child1Name));
      assertEquals(DeploymentState.ERROR, main.getDeploymentState(child2Name));
   }

   public void testDeployParentWithComponentsNoErrors() throws Exception
   {
      DeployerClient main = getMainDeployerWithComponentDeployers();
      
      Deployment deployment = createSimpleDeployment(parentName);
      addMetaData(deployment, parentmd);
      main.addDeployment(deployment);
      main.process();

      assertEquals(expectedParentMD12, deployer1.getDeployedUnits());
      assertEquals(expectedNothing, deployer1.getUndeployedUnits());
      assertEquals(expectedMD12Parent, deployer2.getDeployedUnits());
      assertEquals(expectedNothing, deployer2.getUndeployedUnits());
      assertEquals(DeploymentState.DEPLOYED, main.getDeploymentState(deployment.getName()));
   }

   public void testDeployParentWithComponentsFailInParentDeployer1() throws Exception
   {
      DeployerClient main = getMainDeployerWithComponentDeployers();
      
      Deployment deployment = createSimpleDeployment(parentName);
      addMetaData(deployment, parentmd);
      makeFail(deployment, deployer1);
      main.addDeployment(deployment);
      main.process();

      assertEquals(expectedParent, deployer1.getDeployedUnits());
      assertEquals(expectedNothing, deployer1.getUndeployedUnits());
      assertEquals(expectedNothing, deployer2.getDeployedUnits());
      assertEquals(expectedNothing, deployer2.getUndeployedUnits());
      assertEquals(DeploymentState.ERROR, main.getDeploymentState(deployment.getName()));
   }

   public void testDeployParentWithComponentsFailInParentDeployer2() throws Exception
   {
      DeployerClient main = getMainDeployerWithComponentDeployers();
      
      Deployment deployment = createSimpleDeployment(parentName);
      addMetaData(deployment, parentmd);
      makeFail(deployment, deployer2);
      main.addDeployment(deployment);
      main.process();

      Set<String> expected = new HashSet<String>();
      expected.add(deployment.getName());
      assertEquals(expectedParentMD12, deployer1.getDeployedUnits());
      assertEquals(expectedMD21Parent, deployer1.getUndeployedUnits());
      assertEquals(expectedMD12Parent, deployer2.getDeployedUnits());
      assertEquals(expectedPMD21, deployer2.getUndeployedUnits());
      assertEquals(DeploymentState.ERROR, main.getDeploymentState(deployment.getName()));
   }

   public void testDeployParentWithComponentsOneChildNoErrors() throws Exception
   {
      DeployerClient main = getMainDeployerWithComponentDeployers();
      
      Deployment deployment = createSimpleDeployment(parentName);
      addMetaData(deployment, parentmd);
      ContextInfo child = addChild(deployment, childPath);
      addMetaData(child, childmd);
      main.addDeployment(deployment);
      main.process();

      assertEquals(expectedParentMD12ChildMD12, deployer1.getDeployedUnits());
      assertEquals(expectedNothing, deployer1.getUndeployedUnits());
      assertEquals(expectedMD12ChildMD12Parent, deployer2.getDeployedUnits());
      assertEquals(expectedNothing, deployer2.getUndeployedUnits());
      assertEquals(DeploymentState.DEPLOYED, main.getDeploymentState(deployment.getName()));
      assertEquals(DeploymentState.DEPLOYED, main.getDeploymentState(childName));
   }
   
   public void testDeployParentWithComponentsOneChildFailInParentDeployer1() throws Exception
   {
      DeployerClient main = getMainDeployerWithComponentDeployers();
      
      Deployment deployment = createSimpleDeployment(parentName);
      addMetaData(deployment, parentmd);
      makeFail(deployment, deployer1);
      ContextInfo child = addChild(deployment, childPath);
      addMetaData(child, childmd);
      main.addDeployment(deployment);
      main.process();

      assertEquals(expectedParent, deployer1.getDeployedUnits());
      assertEquals(expectedNothing, deployer1.getUndeployedUnits());
      assertEquals(expectedNothing, deployer2.getDeployedUnits());
      assertEquals(expectedNothing, deployer2.getUndeployedUnits());
      assertEquals(DeploymentState.ERROR, main.getDeploymentState(deployment.getName()));
      assertEquals(DeploymentState.UNDEPLOYED, main.getDeploymentState(childName));
   }
   
   public void testDeployParentWithComponentsOneChildFailInParentDeployer2() throws Exception
   {
      DeployerClient main = getMainDeployerWithComponentDeployers();
      
      Deployment deployment = createSimpleDeployment(parentName);
      addMetaData(deployment, parentmd);
      makeFail(deployment, deployer2);
      ContextInfo child = addChild(deployment, childPath);
      addMetaData(child, childmd);
      main.addDeployment(deployment);
      main.process();

      assertEquals(expectedParentMD12ChildMD12, deployer1.getDeployedUnits());
      assertEquals(expectedMD21ChildMD21Parent, deployer1.getUndeployedUnits());
      assertEquals(expectedMD12ChildMD12Parent, deployer2.getDeployedUnits());
      assertEquals(expectedPMD21ChildMD21, deployer2.getUndeployedUnits());
      assertEquals(DeploymentState.ERROR, main.getDeploymentState(deployment.getName()));
      assertEquals(DeploymentState.UNDEPLOYED, main.getDeploymentState(childName));
   }
   
   public void testDeployParentWithComponentsOneChildFailInChildDeployer1() throws Exception
   {
      DeployerClient main = getMainDeployerWithComponentDeployers();
      
      Deployment deployment = createSimpleDeployment(parentName);
      addMetaData(deployment, parentmd);
      ContextInfo child = addChild(deployment, childPath);
      addMetaData(child, childmd);
      makeFail(child, deployer1);
      main.addDeployment(deployment);
      main.process();

      assertEquals(expectedParentMD12Child, deployer1.getDeployedUnits());
      assertEquals(expectedMD21Parent, deployer1.getUndeployedUnits());
      assertEquals(expectedNothing, deployer2.getDeployedUnits());
      assertEquals(expectedNothing, deployer2.getUndeployedUnits());
      assertEquals(DeploymentState.ERROR, main.getDeploymentState(deployment.getName()));
      assertEquals(DeploymentState.ERROR, main.getDeploymentState(childName));
   }
   
   public void testDeployParentWithComponentsOneChildFailInChildDeployer2() throws Exception
   {
      DeployerClient main = getMainDeployerWithComponentDeployers();
      
      Deployment deployment = createSimpleDeployment(parentName);
      addMetaData(deployment, parentmd);
      ContextInfo child = addChild(deployment, childPath);
      addMetaData(child, childmd);
      makeFail(child, deployer2);
      main.addDeployment(deployment);
      main.process();

      assertEquals(expectedParentMD12ChildMD12, deployer1.getDeployedUnits());
      assertEquals(expectedMD21ChildMD21Parent, deployer1.getUndeployedUnits());
      assertEquals(expectedMD12Child, deployer2.getDeployedUnits());
      assertEquals(expectedMD21C, deployer2.getUndeployedUnits());
      assertEquals(DeploymentState.ERROR, main.getDeploymentState(deployment.getName()));
      assertEquals(DeploymentState.ERROR, main.getDeploymentState(childName));
   }

   public void testDeployParentWithComponentsMultipleChildrenNoErrors() throws Exception
   {
      DeployerClient main = getMainDeployerWithComponentDeployers();
      
      Deployment deployment = createSimpleDeployment(parentName);
      addMetaData(deployment, parentmd);
      ContextInfo child1 = addChild(deployment, child1Path);
      addMetaData(child1, child1md);
      ContextInfo child2 = addChild(deployment, child2Path);
      addMetaData(child2, child2md);
      main.addDeployment(deployment);
      main.process();

      assertEquals(expectedParentMD12Child1MD12Child2MD12, deployer1.getDeployedUnits());
      assertEquals(expectedNothing, deployer1.getUndeployedUnits());
      assertEquals(expectedMD12Child1MD12Child2MD12Parent, deployer2.getDeployedUnits());
      assertEquals(expectedNothing, deployer2.getUndeployedUnits());
      assertEquals(DeploymentState.DEPLOYED, main.getDeploymentState(deployment.getName()));
      assertEquals(DeploymentState.DEPLOYED, main.getDeploymentState(child1Name));
      assertEquals(DeploymentState.DEPLOYED, main.getDeploymentState(child2Name));
   }

   public void testDeployParentWithComponentsMultipleChildrenFailInParentDeployer1() throws Exception
   {
      DeployerClient main = getMainDeployerWithComponentDeployers();
      
      Deployment deployment = createSimpleDeployment(parentName);
      addMetaData(deployment, parentmd);
      makeFail(deployment, deployer1);
      ContextInfo child1 = addChild(deployment, child1Path);
      addMetaData(child1, child1md);
      ContextInfo child2 = addChild(deployment, child2Path);
      addMetaData(child2, child2md);
      main.addDeployment(deployment);
      main.process();

      assertEquals(expectedParent, deployer1.getDeployedUnits());
      assertEquals(expectedNothing, deployer1.getUndeployedUnits());
      assertEquals(expectedNothing, deployer2.getDeployedUnits());
      assertEquals(expectedNothing, deployer2.getUndeployedUnits());
      assertEquals(DeploymentState.ERROR, main.getDeploymentState(deployment.getName()));
      assertEquals(DeploymentState.UNDEPLOYED, main.getDeploymentState(child1Name));
      assertEquals(DeploymentState.UNDEPLOYED, main.getDeploymentState(child2Name));
   }

   public void testDeployParentWithComponentsMultipleChildrenFailInParentDeployer2() throws Exception
   {
      DeployerClient main = getMainDeployerWithComponentDeployers();
      
      Deployment deployment = createSimpleDeployment(parentName);
      addMetaData(deployment, parentmd);
      makeFail(deployment, deployer2);
      ContextInfo child1 = addChild(deployment, child1Path);
      addMetaData(child1, child1md);
      ContextInfo child2 = addChild(deployment, child2Path);
      addMetaData(child2, child2md);
      main.addDeployment(deployment);
      main.process();

      assertEquals(expectedParentMD12Child1MD12Child2MD12, deployer1.getDeployedUnits());
      assertEquals(expectedMD21Child2MD21Child1MD21Parent, deployer1.getUndeployedUnits());
      assertEquals(expectedMD12Child1MD12Child2MD12Parent, deployer2.getDeployedUnits());
      assertEquals(expectedPMD21Child2MD21Child1MD21, deployer2.getUndeployedUnits());
      assertEquals(DeploymentState.ERROR, main.getDeploymentState(deployment.getName()));
      assertEquals(DeploymentState.UNDEPLOYED, main.getDeploymentState(child1Name));
      assertEquals(DeploymentState.UNDEPLOYED, main.getDeploymentState(child2Name));
   }

   public void testDeployParentWithComponentsMultipleChildrenFailInChild1Deployer1() throws Exception
   {
      DeployerClient main = getMainDeployerWithComponentDeployers();
      
      Deployment deployment = createSimpleDeployment(parentName);
      addMetaData(deployment, parentmd);
      ContextInfo child1 = addChild(deployment, child1Path);
      addMetaData(child1, child1md);
      makeFail(child1, deployer1);
      ContextInfo child2 = addChild(deployment, child2Path);
      addMetaData(child2, child2md);
      main.addDeployment(deployment);
      main.process();

      assertEquals(expectedParentMD12Child1, deployer1.getDeployedUnits());
      assertEquals(expectedMD21Parent, deployer1.getUndeployedUnits());
      assertEquals(expectedNothing, deployer2.getDeployedUnits());
      assertEquals(expectedNothing, deployer2.getUndeployedUnits());
      assertEquals(DeploymentState.ERROR, main.getDeploymentState(deployment.getName()));
      assertEquals(DeploymentState.ERROR, main.getDeploymentState(child1Name));
      assertEquals(DeploymentState.UNDEPLOYED, main.getDeploymentState(child2Name));
   }

   public void testDeployParentWithComponentsMultipleChildrenFailInChild1Deployer2() throws Exception
   {
      DeployerClient main = getMainDeployerWithComponentDeployers();
      
      Deployment deployment = createSimpleDeployment(parentName);
      addMetaData(deployment, parentmd);
      ContextInfo child1 = addChild(deployment, child1Path);
      addMetaData(child1, child1md);
      makeFail(child1, deployer2);
      ContextInfo child2 = addChild(deployment, child2Path);
      addMetaData(child2, child2md);
      main.addDeployment(deployment);
      main.process();

      assertEquals(expectedParentMD12Child1MD12Child2MD12, deployer1.getDeployedUnits());
      assertEquals(expectedMD21Child2MD21Child1MD21Parent, deployer1.getUndeployedUnits());
      assertEquals(expectedMD12Child1, deployer2.getDeployedUnits());
      assertEquals(expectedMD21C1, deployer2.getUndeployedUnits());
      assertEquals(DeploymentState.ERROR, main.getDeploymentState(deployment.getName()));
      assertEquals(DeploymentState.ERROR, main.getDeploymentState(child1Name));
      assertEquals(DeploymentState.UNDEPLOYED, main.getDeploymentState(child2Name));
   }

   public void testDeployParentWithComponentsMultipleChildrenFailInChild2Deployer1() throws Exception
   {
      DeployerClient main = getMainDeployerWithComponentDeployers();
      
      Deployment deployment = createSimpleDeployment(parentName);
      addMetaData(deployment, parentmd);
      ContextInfo child1 = addChild(deployment, child1Path);
      addMetaData(child1, child1md);
      ContextInfo child2 = addChild(deployment, child2Path);
      addMetaData(child2, child2md);
      makeFail(child2, deployer1);
      main.addDeployment(deployment);
      main.process();

      assertEquals(expectedParentMD12Child1MD12Child2, deployer1.getDeployedUnits());
      assertEquals(expectedMD21Child1MD21Parent, deployer1.getUndeployedUnits());
      assertEquals(expectedNothing, deployer2.getDeployedUnits());
      assertEquals(expectedNothing, deployer2.getUndeployedUnits());
      assertEquals(DeploymentState.ERROR, main.getDeploymentState(deployment.getName()));
      assertEquals(DeploymentState.UNDEPLOYED, main.getDeploymentState(child1Name));
      assertEquals(DeploymentState.ERROR, main.getDeploymentState(child2Name));
   }

   public void testDeployParentWithComponentsMultipleChildrenFailInChild2Deployer2() throws Exception
   {
      DeployerClient main = getMainDeployerWithComponentDeployers();
      
      Deployment deployment = createSimpleDeployment(parentName);
      addMetaData(deployment, parentmd);
      ContextInfo child1 = addChild(deployment, child1Path);
      addMetaData(child1, child1md);
      ContextInfo child2 = addChild(deployment, child2Path);
      addMetaData(child2, child2md);
      makeFail(child2, deployer2);
      main.addDeployment(deployment);
      main.process();

      assertEquals(expectedParentMD12Child1MD12Child2MD12, deployer1.getDeployedUnits());
      assertEquals(expectedMD21Child2MD21Child1MD21Parent, deployer1.getUndeployedUnits());
      assertEquals(expectedMD12Child1MD12Child2, deployer2.getDeployedUnits());
      assertEquals(expectedMD21C2Child1MD21, deployer2.getUndeployedUnits());
      assertEquals(DeploymentState.ERROR, main.getDeploymentState(deployment.getName()));
      assertEquals(DeploymentState.UNDEPLOYED, main.getDeploymentState(child1Name));
      assertEquals(DeploymentState.ERROR, main.getDeploymentState(child2Name));
   }

   protected static void addMetaData(PredeterminedManagedObjectAttachments attachments, TestComponentMetaDataContainer md)
   {
      MutableAttachments mutable = (MutableAttachments) attachments.getPredeterminedManagedObjects();
      mutable.addAttachment(TestComponentMetaDataContainer.class, md);
   }
   
   protected static void makeFail(PredeterminedManagedObjectAttachments attachments, Deployer deployer)
   {
      MutableAttachments mutable = (MutableAttachments) attachments.getPredeterminedManagedObjects();
      mutable.addAttachment("fail", deployer);
   }
   
   protected DeployerClient getMainDeployer()
   {
      return createMainDeployer(deployer1, deployer2);
   }
   
   protected DeployerClient getMainDeployerWithComponentDeployers()
   {
      return createMainDeployer(deployer0, deployer1, deployer2);
   }
}
