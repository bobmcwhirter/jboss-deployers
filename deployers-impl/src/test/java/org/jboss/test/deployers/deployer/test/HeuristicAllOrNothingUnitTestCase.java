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

import java.util.Collections;
import java.util.HashSet;
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
import org.jboss.test.deployers.deployer.support.TestComponentRealDeployer;
import org.jboss.test.deployers.deployer.support.TestSimpleDeployer;

/**
 * HeuristicAllOrNothingUnitTestCase.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class HeuristicAllOrNothingUnitTestCase extends AbstractDeployerTest
{
   private TestSimpleDeployer deployer1 = new TestSimpleDeployer(1);
   private TestSimpleDeployer deployer2 = new TestSimpleDeployer(2);
   private TestComponentDeployer deployer3 = new TestComponentDeployer(3);
   private TestComponentRealDeployer deployer4 = new TestComponentRealDeployer(4);
   
   private static String parentName = "parent";
   private static String childPath = "child";
   private static String childName = parentName + "/" + childPath;
   private static String child1Path = "child1";
   private static String child1Name = parentName + "/" + child1Path;
   private static String child2Path = "child2";
   private static String child2Name = parentName + "/" + child2Path;

   private static Set<String> expectedNothing = Collections.emptySet();
   private static Set<String> expectedParent = makeSet(parentName);
   private static Set<String> expectedParentChild = makeSet(parentName, childName);
   private static Set<String> expectedParentChild1 = makeSet(parentName, child1Name);
   private static Set<String> expectedParentChild1Child2 = makeSet(parentName, child1Name, child2Name);

   private static TestComponentMetaData parentmd1 = new TestComponentMetaData(parentName + ".1", false);
   private static TestComponentMetaData parentmd1fail = new TestComponentMetaData(parentName + ".1", true);
   private static TestComponentMetaData parentmd2 = new TestComponentMetaData(parentName + ".2", false);
   private static TestComponentMetaData parentmd2fail = new TestComponentMetaData(parentName + ".2", true);
   private static TestComponentMetaData child1md1 = new TestComponentMetaData(child1Name + ".1", false);
   private static TestComponentMetaData child1md1fail = new TestComponentMetaData(child1Name + ".1", true);
   private static TestComponentMetaData child1md2 = new TestComponentMetaData(child1Name + ".2", false);
   private static TestComponentMetaData child1md2fail = new TestComponentMetaData(child1Name + ".2", true);
   private static TestComponentMetaData child2md1 = new TestComponentMetaData(child2Name + ".1", false);
   private static TestComponentMetaData child2md1fail = new TestComponentMetaData(child2Name + ".1", true);
   private static TestComponentMetaData child2md2 = new TestComponentMetaData(child2Name + ".2", false);
   private static TestComponentMetaData child2md2fail = new TestComponentMetaData(child2Name + ".2", true);
   
   private static TestComponentMetaDataContainer parentmd = new TestComponentMetaDataContainer(parentmd1, parentmd2);
   private static TestComponentMetaDataContainer parentmdfail1 = new TestComponentMetaDataContainer(parentmd1fail, parentmd2);
   private static TestComponentMetaDataContainer parentmdfail2 = new TestComponentMetaDataContainer(parentmd1, parentmd2fail);
   private static TestComponentMetaDataContainer child1md = new TestComponentMetaDataContainer(child1md1, child1md2);
   private static TestComponentMetaDataContainer child1mdfail1 = new TestComponentMetaDataContainer(child1md1fail, child1md2);
   private static TestComponentMetaDataContainer child1mdfail2 = new TestComponentMetaDataContainer(child1md1, child1md2fail);
   private static TestComponentMetaDataContainer child2md = new TestComponentMetaDataContainer(child2md1, child2md2);
   private static TestComponentMetaDataContainer child2mdfail1 = new TestComponentMetaDataContainer(child2md1fail, child2md2);
   private static TestComponentMetaDataContainer child2mdfail2 = new TestComponentMetaDataContainer(child2md1, child2md2fail);
   
   private static Set<TestComponentMetaData> expectedNone = Collections.emptySet();
   private static Set<TestComponentMetaData> expectedPmd1Fail = makeSet(parentmd1fail);
   private static Set<TestComponentMetaData> expectedPmd1 = makeSet(parentmd1);
   private static Set<TestComponentMetaData> expectedPmd1Pmd2Fail = makeSet(parentmd1, parentmd2fail);
   private static Set<TestComponentMetaData> expectedPmd1Pmd2 = makeSet(parentmd1, parentmd2);
   private static Set<TestComponentMetaData> expectedPmd1Pmd2C1md1Fail = makeSet(parentmd1, parentmd2, child1md1fail);
   private static Set<TestComponentMetaData> expectedPmd1Pmd2C1md1 = makeSet(parentmd1, parentmd2, child1md1);
   private static Set<TestComponentMetaData> expectedPmd1Pmd2C1md1C1md2Fail = makeSet(parentmd1, parentmd2, child1md1, child1md2fail);
   private static Set<TestComponentMetaData> expectedPmd1Pmd2C1md1C1md2 = makeSet(parentmd1, parentmd2, child1md1, child1md2);
   private static Set<TestComponentMetaData> expectedPmd1Pmd2C1md1C1md2C2md1Fail = makeSet(parentmd1, parentmd2, child1md1, child1md2, child2md1fail);
   private static Set<TestComponentMetaData> expectedPmd1Pmd2C1md1C1md2C2md1 = makeSet(parentmd1, parentmd2, child1md1, child1md2, child2md1);
   private static Set<TestComponentMetaData> expectedPmd1Pmd2C1md1C1md2C2md1C2md2Fail = makeSet(parentmd1, parentmd2, child1md1, child1md2, child2md1, child2md2fail);
   private static Set<TestComponentMetaData> expectedPmd1Pmd2C1md1C1md2C2md1C2md2 = makeSet(parentmd1, parentmd2, child1md1, child1md2, child2md1, child2md2);
   
   @SuppressWarnings("unchecked")
   private static <T> Set<T> makeSet(T... objects)
   {
      Set<T> result = new HashSet();
      for (T object : objects)
         result.add(object);
      return result;
   }
   
   public static Test suite()
   {
      return new TestSuite(HeuristicAllOrNothingUnitTestCase.class);
   }
   
   public HeuristicAllOrNothingUnitTestCase(String name)
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
      
      main.removeDeployment(deployment);
      main.process();
      main.checkComplete();
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
      
      main.removeDeployment(deployment);
      main.process();
      main.checkComplete();
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
      
      main.removeDeployment(deployment);
      main.process();
      main.checkComplete();
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
      assertEquals(expectedParentChild, deployer2.getDeployedUnits());
      assertEquals(expectedNothing, deployer2.getUndeployedUnits());
      assertEquals(DeploymentState.DEPLOYED, main.getDeploymentState(deployment.getName()));
      assertEquals(DeploymentState.DEPLOYED, main.getDeploymentState(childName));
      
      main.removeDeployment(deployment);
      main.process();
      main.checkComplete();
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
      
      main.removeDeployment(deployment);
      main.process();
      main.checkComplete();
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
      assertEquals(expectedParentChild, deployer1.getUndeployedUnits());
      assertEquals(expectedParent, deployer2.getDeployedUnits());
      assertEquals(expectedNothing, deployer2.getUndeployedUnits());
      assertEquals(DeploymentState.ERROR, main.getDeploymentState(deployment.getName()));
      assertEquals(DeploymentState.UNDEPLOYED, main.getDeploymentState(childName));
      
      main.removeDeployment(deployment);
      main.process();
      main.checkComplete();
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
      
      main.removeDeployment(deployment);
      main.process();
      main.checkComplete();
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
      assertEquals(expectedParentChild, deployer1.getUndeployedUnits());
      assertEquals(expectedParentChild, deployer2.getDeployedUnits());
      assertEquals(expectedParent, deployer2.getUndeployedUnits());
      assertEquals(DeploymentState.ERROR, main.getDeploymentState(deployment.getName()));
      assertEquals(DeploymentState.ERROR, main.getDeploymentState(childName));
      
      main.removeDeployment(deployment);
      main.process();
      main.checkComplete();
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
      assertEquals(expectedParentChild1Child2, deployer2.getDeployedUnits());
      assertEquals(expectedNothing, deployer2.getUndeployedUnits());
      assertEquals(DeploymentState.DEPLOYED, main.getDeploymentState(deployment.getName()));
      assertEquals(DeploymentState.DEPLOYED, main.getDeploymentState(child1Name));
      assertEquals(DeploymentState.DEPLOYED, main.getDeploymentState(child2Name));
      
      main.removeDeployment(deployment);
      main.process();
      main.checkComplete();
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
      
      main.removeDeployment(deployment);
      main.process();
      main.checkComplete();
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
      assertEquals(expectedParentChild1Child2, deployer1.getUndeployedUnits());
      assertEquals(expectedParent, deployer2.getDeployedUnits());
      assertEquals(expectedNothing, deployer2.getUndeployedUnits());
      assertEquals(DeploymentState.ERROR, main.getDeploymentState(deployment.getName()));
      assertEquals(DeploymentState.UNDEPLOYED, main.getDeploymentState(child1Name));
      assertEquals(DeploymentState.UNDEPLOYED, main.getDeploymentState(child2Name));
      
      main.removeDeployment(deployment);
      main.process();
      main.checkComplete();
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
      
      main.removeDeployment(deployment);
      main.process();
      main.checkComplete();
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
      assertEquals(expectedParentChild1Child2, deployer1.getUndeployedUnits());
      assertEquals(expectedParentChild1, deployer2.getDeployedUnits());
      assertEquals(expectedParent, deployer2.getUndeployedUnits());
      assertEquals(DeploymentState.ERROR, main.getDeploymentState(deployment.getName()));
      assertEquals(DeploymentState.ERROR, main.getDeploymentState(child1Name));
      assertEquals(DeploymentState.UNDEPLOYED, main.getDeploymentState(child2Name));
      
      main.removeDeployment(deployment);
      main.process();
      main.checkComplete();
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
      assertEquals(expectedParentChild1, deployer1.getUndeployedUnits());
      assertEquals(expectedNothing, deployer2.getDeployedUnits());
      assertEquals(expectedNothing, deployer2.getUndeployedUnits());
      assertEquals(DeploymentState.ERROR, main.getDeploymentState(deployment.getName()));
      assertEquals(DeploymentState.UNDEPLOYED, main.getDeploymentState(child1Name));
      assertEquals(DeploymentState.ERROR, main.getDeploymentState(child2Name));
      
      main.removeDeployment(deployment);
      main.process();
      main.checkComplete();
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
      assertEquals(expectedParentChild1Child2, deployer1.getUndeployedUnits());
      assertEquals(expectedParentChild1Child2, deployer2.getDeployedUnits());
      assertEquals(expectedParentChild1, deployer2.getUndeployedUnits());
      assertEquals(DeploymentState.ERROR, main.getDeploymentState(deployment.getName()));
      assertEquals(DeploymentState.UNDEPLOYED, main.getDeploymentState(child1Name));
      assertEquals(DeploymentState.ERROR, main.getDeploymentState(child2Name));
      
      main.removeDeployment(deployment);
      main.process();
      main.checkComplete();
   }

   public void testDeployParentMultipleChildrenAndComponentsNoErrors() throws Exception
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

      assertEquals(expectedParentChild1Child2, deployer1.getDeployedUnits());
      assertEquals(expectedNothing, deployer1.getUndeployedUnits());
      assertEquals(expectedParentChild1Child2, deployer2.getDeployedUnits());
      assertEquals(expectedNothing, deployer2.getUndeployedUnits());
      assertEquals(expectedPmd1Pmd2C1md1C1md2C2md1C2md2, deployer4.deployed);
      assertEquals(expectedNone, deployer4.undeployed);
      assertEquals(DeploymentState.DEPLOYED, main.getDeploymentState(deployment.getName()));
      assertEquals(DeploymentState.DEPLOYED, main.getDeploymentState(child1Name));
      assertEquals(DeploymentState.DEPLOYED, main.getDeploymentState(child2Name));
      
      main.removeDeployment(deployment);
      main.process();
      main.checkComplete();
   }

   public void testDeployParentMultipleChildrenAndComponentsParentComponent1Fails() throws Exception
   {
      DeployerClient main = getMainDeployerWithComponentDeployers();
      
      Deployment deployment = createSimpleDeployment(parentName);
      addMetaData(deployment, parentmdfail1);
      ContextInfo child1 = addChild(deployment, child1Path);
      addMetaData(child1, child1md);
      ContextInfo child2 = addChild(deployment, child2Path);
      addMetaData(child2, child2md);
      main.addDeployment(deployment);
      main.process();

      assertEquals(expectedParentChild1Child2, deployer1.getDeployedUnits());
      assertEquals(expectedParentChild1Child2, deployer1.getUndeployedUnits());
      assertEquals(expectedParentChild1Child2, deployer2.getDeployedUnits());
      assertEquals(expectedParentChild1Child2, deployer2.getUndeployedUnits());
      assertEquals(expectedPmd1Fail, deployer4.deployed);
      assertEquals(expectedNone, deployer4.undeployed);
      assertEquals(DeploymentState.ERROR, main.getDeploymentState(deployment.getName()));
      assertEquals(DeploymentState.UNDEPLOYED, main.getDeploymentState(child1Name));
      assertEquals(DeploymentState.UNDEPLOYED, main.getDeploymentState(child2Name));
      
      main.removeDeployment(deployment);
      main.process();
      main.checkComplete();
   }

   public void testDeployParentMultipleChildrenAndComponentsParentComponent2Fails() throws Exception
   {
      DeployerClient main = getMainDeployerWithComponentDeployers();
      
      Deployment deployment = createSimpleDeployment(parentName);
      addMetaData(deployment, parentmdfail2);
      ContextInfo child1 = addChild(deployment, child1Path);
      addMetaData(child1, child1md);
      ContextInfo child2 = addChild(deployment, child2Path);
      addMetaData(child2, child2md);
      main.addDeployment(deployment);
      main.process();

      assertEquals(expectedParentChild1Child2, deployer1.getDeployedUnits());
      assertEquals(expectedParentChild1Child2, deployer1.getUndeployedUnits());
      assertEquals(expectedParentChild1Child2, deployer2.getDeployedUnits());
      assertEquals(expectedParentChild1Child2, deployer2.getUndeployedUnits());
      assertEquals(expectedPmd1Pmd2Fail, deployer4.deployed);
      assertEquals(expectedPmd1, deployer4.undeployed);
      assertEquals(DeploymentState.ERROR, main.getDeploymentState(deployment.getName()));
      assertEquals(DeploymentState.UNDEPLOYED, main.getDeploymentState(child1Name));
      assertEquals(DeploymentState.UNDEPLOYED, main.getDeploymentState(child2Name));
      
      main.removeDeployment(deployment);
      main.process();
      main.checkComplete();
   }

   public void testDeployParentMultipleChildrenAndComponentsChild1Component1Fails() throws Exception
   {
      DeployerClient main = getMainDeployerWithComponentDeployers();
      
      Deployment deployment = createSimpleDeployment(parentName);
      addMetaData(deployment, parentmd);
      ContextInfo child1 = addChild(deployment, child1Path);
      addMetaData(child1, child1mdfail1);
      ContextInfo child2 = addChild(deployment, child2Path);
      addMetaData(child2, child2md);
      main.addDeployment(deployment);
      main.process();

      assertEquals(expectedParentChild1Child2, deployer1.getDeployedUnits());
      assertEquals(expectedParentChild1Child2, deployer1.getUndeployedUnits());
      assertEquals(expectedParentChild1Child2, deployer2.getDeployedUnits());
      assertEquals(expectedParentChild1Child2, deployer2.getUndeployedUnits());
      assertEquals(expectedPmd1Pmd2C1md1Fail, deployer4.deployed);
      assertEquals(expectedPmd1Pmd2, deployer4.undeployed);
      assertEquals(DeploymentState.ERROR, main.getDeploymentState(deployment.getName()));
      assertEquals(DeploymentState.ERROR, main.getDeploymentState(child1Name));
      assertEquals(DeploymentState.UNDEPLOYED, main.getDeploymentState(child2Name));
      
      main.removeDeployment(deployment);
      main.process();
      main.checkComplete();
   }

   public void testDeployParentMultipleChildrenAndComponentsChild1Component2Fails() throws Exception
   {
      DeployerClient main = getMainDeployerWithComponentDeployers();
      
      Deployment deployment = createSimpleDeployment(parentName);
      addMetaData(deployment, parentmd);
      ContextInfo child1 = addChild(deployment, child1Path);
      addMetaData(child1, child1mdfail2);
      ContextInfo child2 = addChild(deployment, child2Path);
      addMetaData(child2, child2md);
      main.addDeployment(deployment);
      main.process();

      assertEquals(expectedParentChild1Child2, deployer1.getDeployedUnits());
      assertEquals(expectedParentChild1Child2, deployer1.getUndeployedUnits());
      assertEquals(expectedParentChild1Child2, deployer2.getDeployedUnits());
      assertEquals(expectedParentChild1Child2, deployer2.getUndeployedUnits());
      assertEquals(expectedPmd1Pmd2C1md1C1md2Fail, deployer4.deployed);
      assertEquals(expectedPmd1Pmd2C1md1, deployer4.undeployed);
      assertEquals(DeploymentState.ERROR, main.getDeploymentState(deployment.getName()));
      assertEquals(DeploymentState.ERROR, main.getDeploymentState(child1Name));
      assertEquals(DeploymentState.UNDEPLOYED, main.getDeploymentState(child2Name));
      
      main.removeDeployment(deployment);
      main.process();
      main.checkComplete();
   }

   public void testDeployParentMultipleChildrenAndComponentsChild2Component1Fails() throws Exception
   {
      DeployerClient main = getMainDeployerWithComponentDeployers();
      
      Deployment deployment = createSimpleDeployment(parentName);
      addMetaData(deployment, parentmd);
      ContextInfo child1 = addChild(deployment, child1Path);
      addMetaData(child1, child1md);
      ContextInfo child2 = addChild(deployment, child2Path);
      addMetaData(child2, child2mdfail1);
      main.addDeployment(deployment);
      main.process();

      assertEquals(expectedParentChild1Child2, deployer1.getDeployedUnits());
      assertEquals(expectedParentChild1Child2, deployer1.getUndeployedUnits());
      assertEquals(expectedParentChild1Child2, deployer2.getDeployedUnits());
      assertEquals(expectedParentChild1Child2, deployer2.getUndeployedUnits());
      assertEquals(expectedPmd1Pmd2C1md1C1md2C2md1Fail, deployer4.deployed);
      assertEquals(expectedPmd1Pmd2C1md1C1md2, deployer4.undeployed);
      assertEquals(DeploymentState.ERROR, main.getDeploymentState(deployment.getName()));
      assertEquals(DeploymentState.UNDEPLOYED, main.getDeploymentState(child1Name));
      assertEquals(DeploymentState.ERROR, main.getDeploymentState(child2Name));
      
      main.removeDeployment(deployment);
      main.process();
      main.checkComplete();
   }

   public void testDeployParentMultipleChildrenAndComponentsChild2Component2Fails() throws Exception
   {
      DeployerClient main = getMainDeployerWithComponentDeployers();
      
      Deployment deployment = createSimpleDeployment(parentName);
      addMetaData(deployment, parentmd);
      ContextInfo child1 = addChild(deployment, child1Path);
      addMetaData(child1, child1md);
      ContextInfo child2 = addChild(deployment, child2Path);
      addMetaData(child2, child2mdfail2);
      main.addDeployment(deployment);
      main.process();

      assertEquals(expectedParentChild1Child2, deployer1.getDeployedUnits());
      assertEquals(expectedParentChild1Child2, deployer1.getUndeployedUnits());
      assertEquals(expectedParentChild1Child2, deployer2.getDeployedUnits());
      assertEquals(expectedParentChild1Child2, deployer2.getUndeployedUnits());
      assertEquals(expectedPmd1Pmd2C1md1C1md2C2md1C2md2Fail, deployer4.deployed);
      assertEquals(expectedPmd1Pmd2C1md1C1md2C2md1, deployer4.undeployed);
      assertEquals(DeploymentState.ERROR, main.getDeploymentState(deployment.getName()));
      assertEquals(DeploymentState.UNDEPLOYED, main.getDeploymentState(child1Name));
      assertEquals(DeploymentState.ERROR, main.getDeploymentState(child2Name));
      
      main.removeDeployment(deployment);
      main.process();
      main.checkComplete();
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
      return createMainDeployer(deployer1, deployer2, deployer3, deployer4);
   }
}
