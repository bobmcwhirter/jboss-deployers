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
package org.jboss.test.deployers.classloading.test;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.classloading.spi.metadata.ClassLoadingMetaData;
import org.jboss.deployers.client.spi.DeployerClient;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.test.deployers.classloading.support.a.A;
import org.jboss.test.deployers.classloading.support.b.B;

/**
 * MockClassLoaderDependenciesUnitTestCase.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class MockClassLoaderDependenciesUnitTestCase extends ClassLoaderDependenciesTest
{
   public static Test suite()
   {
      return new TestSuite(MockClassLoaderDependenciesUnitTestCase.class);
   }
   
   public MockClassLoaderDependenciesUnitTestCase(String name)
   {
      super(name);
   }

   public void testSimpleClassLoader() throws Exception
   {
      DeployerClient deployer = getMainDeployer();

      Deployment deployment = createSimpleDeployment(NameA);
      addClassLoadingMetaData(deployment, deployment.getName(), null, A.class);
      
      DeploymentUnit unit = assertDeploy(deployer, deployment);

      assertEquals(A, deployer2.deployed);
      assertEquals(NONE, deployer2.undeployed);
      
      ClassLoader cl = unit.getClassLoader();
      assertLoadClass(cl, A.class);
      
      assertUndeploy(deployer, deployment);

      assertLoadClass(cl, A.class);

      assertEquals(A, deployer2.deployed);
      assertEquals(A, deployer2.undeployed);
   }

   public void testADependsUponModuleBCorrectWay() throws Exception
   {
      DeployerClient deployer = getMainDeployer();

      Deployment deploymentB = createSimpleDeployment(NameB);
      addClassLoadingMetaData(deploymentB, deploymentB.getName(), null, B.class);
      DeploymentUnit unitB = assertDeploy(deployer, deploymentB);
      
      ClassLoader clB = unitB.getClassLoader();
      assertLoadClass(clB, B.class);

      assertEquals(B, deployer2.deployed);
      assertEquals(NONE, deployer2.undeployed);

      Deployment deploymentA = createSimpleDeployment(NameA);
      ClassLoadingMetaData classLoadingMetaData = addClassLoadingMetaData(deploymentA, deploymentA.getName(), null, A.class);
      addRequireModule(classLoadingMetaData, "B", null);
      DeploymentUnit unitA = assertDeploy(deployer, deploymentA);
      
      ClassLoader clA = unitA.getClassLoader();
      assertLoadClass(clA, B.class, clB);

      assertEquals(BA, deployer2.deployed);
      assertEquals(NONE, deployer2.undeployed);
      
      assertUndeploy(deployer, deploymentA);
      assertLoadClassIllegal(clA, B.class);

      assertEquals(BA, deployer2.deployed);
      assertEquals(A, deployer2.undeployed);
      
      assertUndeploy(deployer, deploymentB);
      assertLoadClass(clB, B.class);

      assertEquals(BA, deployer2.deployed);
      assertEquals(AB, deployer2.undeployed);
   }

   public void testADependsUponModuleBWrongWay() throws Exception
   {
      DeployerClient deployer = getMainDeployer();

      Deployment deploymentA = createSimpleDeployment(NameA);
      ClassLoadingMetaData classLoaderMetaData = addClassLoadingMetaData(deploymentA, deploymentA.getName(), null, A.class);
      addRequireModule(classLoaderMetaData, "B", null);
      DeploymentUnit unitA = addDeployment(deployer, deploymentA);
      
      assertNoClassLoader(unitA);

      assertEquals(NONE, deployer2.deployed);
      assertEquals(NONE, deployer2.undeployed);

      Deployment deploymentB = createSimpleDeployment(NameB);
      addClassLoadingMetaData(deploymentB, deploymentB.getName(), null, B.class);
      DeploymentUnit unitB = assertDeploy(deployer, deploymentB);
      
      ClassLoader clB = unitB.getClassLoader();
      assertLoadClass(clB, B.class);

      assertEquals(BA, deployer2.deployed);
      assertEquals(NONE, deployer2.undeployed);

      ClassLoader clA = unitA.getClassLoader();
      assertLoadClass(clA, B.class, clB);
      
      assertUndeploy(deployer, deploymentA);
      assertLoadClassIllegal(clA, B.class);

      assertEquals(BA, deployer2.deployed);
      assertEquals(A, deployer2.undeployed);
      
      assertUndeploy(deployer, deploymentB);
      assertLoadClass(clB, B.class);

      assertEquals(BA, deployer2.deployed);
      assertEquals(AB, deployer2.undeployed);
   }

   public void testADependsUponModuleBRedeployA() throws Exception
   {
      DeployerClient deployer = getMainDeployer();

      Deployment deploymentA = createSimpleDeployment(NameA);
      ClassLoadingMetaData classLoaderMetaData = addClassLoadingMetaData(deploymentA, deploymentA.getName(), null, A.class);
      addRequireModule(classLoaderMetaData, "B", null);
      DeploymentUnit unitA = addDeployment(deployer, deploymentA);
      
      assertNoClassLoader(unitA);

      assertEquals(NONE, deployer2.deployed);
      assertEquals(NONE, deployer2.undeployed);

      Deployment deploymentB = createSimpleDeployment(NameB);
      addClassLoadingMetaData(deploymentB, deploymentB.getName(), null, B.class);
      DeploymentUnit unitB = assertDeploy(deployer, deploymentB);
      
      ClassLoader clB = unitB.getClassLoader();
      assertLoadClass(clB, B.class);

      assertEquals(BA, deployer2.deployed);
      assertEquals(NONE, deployer2.undeployed);

      ClassLoader clA = unitA.getClassLoader();
      assertLoadClass(clA, B.class, clB);
      
      assertUndeploy(deployer, deploymentA);
      assertLoadClassIllegal(clA, B.class);

      assertEquals(BA, deployer2.deployed);
      assertEquals(A, deployer2.undeployed);
      
      unitA = assertDeploy(deployer, deploymentA);
      clA = unitA.getClassLoader();
      assertLoadClass(clA, B.class, clB);

      assertEquals(BAA, deployer2.deployed);
      assertEquals(A, deployer2.undeployed);
   }

   public void testADependsUponModuleBRedeployB() throws Exception
   {
      DeployerClient deployer = getMainDeployer();

      Deployment deploymentA = createSimpleDeployment(NameA);
      ClassLoadingMetaData classLoaderMetaData = addClassLoadingMetaData(deploymentA, deploymentA.getName(), null, A.class);
      addRequireModule(classLoaderMetaData, "B", null);
      DeploymentUnit unitA = addDeployment(deployer, deploymentA);
      
      assertNoClassLoader(unitA);

      assertEquals(NONE, deployer2.deployed);
      assertEquals(NONE, deployer2.undeployed);

      Deployment deploymentB = createSimpleDeployment(NameB);
      addClassLoadingMetaData(deploymentB, deploymentB.getName(), null, B.class);
      DeploymentUnit unitB = assertDeploy(deployer, deploymentB);
      
      ClassLoader clB = unitB.getClassLoader();
      assertLoadClass(clB, B.class);

      assertEquals(BA, deployer2.deployed);
      assertEquals(NONE, deployer2.undeployed);

      ClassLoader clA = unitA.getClassLoader();
      assertLoadClass(clA, B.class, clB);
      
      enableTrace("org.jboss.deployers");
      enableTrace("org.jboss.dependency");
      assertUndeploy(deployer, deploymentB);
      assertLoadClassIllegal(clA, B.class);

      assertEquals(BA, deployer2.deployed);
      assertEquals(AB, deployer2.undeployed);
      
      unitB = assertDeploy(deployer, deploymentB);
      clA = unitA.getClassLoader();
      clB = unitB.getClassLoader();
      assertLoadClass(clA, B.class, clB);

      assertEquals(BABA, deployer2.deployed);
      assertEquals(AB, deployer2.undeployed);
   }
   

   public void testCircular() throws Exception
   {
      DeployerClient deployer = getMainDeployer();

      Deployment deploymentB = createSimpleDeployment(NameB);
      ClassLoadingMetaData classLoadingMetaData = addClassLoadingMetaData(deploymentB, deploymentB.getName(), null, B.class);
      addRequireModule(classLoadingMetaData, "A", null);
      DeploymentUnit unitB = addDeployment(deployer, deploymentB);

      assertEquals(NONE, deployer2.deployed);
      assertEquals(NONE, deployer2.undeployed);

      Deployment deploymentA = createSimpleDeployment(NameA);
      classLoadingMetaData = addClassLoadingMetaData(deploymentA, deploymentA.getName(), null, A.class);
      addRequireModule(classLoadingMetaData, "B", null);
      DeploymentUnit unitA = assertDeploy(deployer, deploymentA);
      
      ClassLoader clA = unitA.getClassLoader();
      ClassLoader clB = unitB.getClassLoader();
      assertLoadClass(clB, B.class);
      assertLoadClass(clA, B.class, clB);
      assertLoadClass(clB, A.class, clA);
      assertLoadClass(clA, A.class);

      assertEquals(AB, deployer2.deployed);
      assertEquals(NONE, deployer2.undeployed);
      
      assertUndeploy(deployer, deploymentA);
      assertLoadClassIllegal(clA, B.class);
      assertLoadClassIllegal(clB, A.class);

      assertEquals(AB, deployer2.deployed);
      assertEquals(AB, deployer2.undeployed);
      
      assertUndeploy(deployer, deploymentB);
      assertLoadClass(clA, A.class);
      assertLoadClass(clB, B.class);
   }

}
