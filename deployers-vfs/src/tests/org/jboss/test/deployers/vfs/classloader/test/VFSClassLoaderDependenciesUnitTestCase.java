/*
* JBoss, Home of Professional Open Source
* Copyright 2008, JBoss Inc., and individual contributors as indicated
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
package org.jboss.test.deployers.vfs.classloader.test;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.classloading.spi.metadata.ClassLoadingMetaData;
import org.jboss.deployers.client.spi.DeployerClient;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.test.deployers.vfs.classloader.support.a.A;
import org.jboss.test.deployers.vfs.classloader.support.b.B;

/**
 * VFSClassLoaderDependenciesUnitTestCase.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class VFSClassLoaderDependenciesUnitTestCase extends VFSClassLoaderDependenciesTest
{
   public static Test suite()
   {
      return new TestSuite(VFSClassLoaderDependenciesUnitTestCase.class);
   }
   
   public VFSClassLoaderDependenciesUnitTestCase(String name)
   {
      super(name);
   }

   public void testSimpleClassLoader() throws Exception
   {
      DeployerClient deployer = getMainDeployer();

      Deployment deployment = createDeployment(NameA);
      addClassLoadingMetaData(deployment, null, A.class);
      
      DeploymentUnit unit = assertDeploy(deployer, deployment);
      
      assertEquals(XA, deployer2.deployed);
      assertEquals(NONE, deployer2.undeployed);
      
      ClassLoader cl = unit.getClassLoader();
      enableTrace("org.jboss.classloader");
      assertLoadClass(cl, A.class);
      
      assertUndeploy(deployer, deployment);

      assertLoadClass(cl, A.class);

      assertEquals(XA, deployer2.deployed);
      assertEquals(XA, deployer2.undeployed);
   }

   public void testADependsUponModuleBCorrectWay() throws Exception
   {
      DeployerClient deployer = getMainDeployer();

      Deployment deploymentB = createDeployment(NameB);
      addClassLoadingMetaData(deploymentB, null, B.class);
      DeploymentUnit unitB = assertDeploy(deployer, deploymentB);
      
      ClassLoader clB = unitB.getClassLoader();
      assertLoadClass(clB, B.class);

      assertEquals(XB, deployer2.deployed);
      assertEquals(NONE, deployer2.undeployed);

      Deployment deploymentA = createDeployment(NameA);
      ClassLoadingMetaData classLoadingMetaData = addClassLoadingMetaData(deploymentA, null, A.class);
      addRequireModule(classLoadingMetaData, "B", null);
      DeploymentUnit unitA = assertDeploy(deployer, deploymentA);
      
      ClassLoader clA = unitA.getClassLoader();
      assertLoadClass(clA, B.class, clB);

      assertEquals(XBA, deployer2.deployed);
      assertEquals(NONE, deployer2.undeployed);
      
      assertUndeploy(deployer, deploymentA);
      assertLoadClassIllegal(clA, B.class);

      assertEquals(XBA, deployer2.deployed);
      assertEquals(XA, deployer2.undeployed);
      
      assertUndeploy(deployer, deploymentB);
      assertLoadClass(clB, B.class);

      assertEquals(XBA, deployer2.deployed);
      assertEquals(XAB, deployer2.undeployed);
   }

   public void testADependsUponModuleBWrongWay() throws Exception
   {
      DeployerClient deployer = getMainDeployer();

      Deployment deploymentA = createDeployment(NameA);
      ClassLoadingMetaData classLoaderMetaData = addClassLoadingMetaData(deploymentA, null, A.class);
      addRequireModule(classLoaderMetaData, "B", null);
      DeploymentUnit unitA = addDeployment(deployer, deploymentA);
      
      assertNoClassLoader(unitA);

      assertEquals(NONE, deployer2.deployed);
      assertEquals(NONE, deployer2.undeployed);

      Deployment deploymentB = createDeployment(NameB);
      addClassLoadingMetaData(deploymentB, null, B.class);
      DeploymentUnit unitB = assertDeploy(deployer, deploymentB);
      
      ClassLoader clB = unitB.getClassLoader();
      assertLoadClass(clB, B.class);

      assertEquals(XBA, deployer2.deployed);
      assertEquals(NONE, deployer2.undeployed);

      ClassLoader clA = unitA.getClassLoader();
      assertLoadClass(clA, B.class, clB);
      
      assertUndeploy(deployer, deploymentA);
      assertLoadClassIllegal(clA, B.class);

      assertEquals(XBA, deployer2.deployed);
      assertEquals(XA, deployer2.undeployed);
      
      assertUndeploy(deployer, deploymentB);
      assertLoadClass(clB, B.class);

      assertEquals(XBA, deployer2.deployed);
      assertEquals(XAB, deployer2.undeployed);
   }

   public void testADependsUponModuleBRedeployA() throws Exception
   {
      DeployerClient deployer = getMainDeployer();

      Deployment deploymentA = createDeployment(NameA);
      ClassLoadingMetaData classLoaderMetaData = addClassLoadingMetaData(deploymentA, null, A.class);
      addRequireModule(classLoaderMetaData, "B", null);
      DeploymentUnit unitA = addDeployment(deployer, deploymentA);
      
      assertNoClassLoader(unitA);

      assertEquals(NONE, deployer2.deployed);
      assertEquals(NONE, deployer2.undeployed);

      Deployment deploymentB = createDeployment(NameB);
      addClassLoadingMetaData(deploymentB, null, B.class);
      DeploymentUnit unitB = assertDeploy(deployer, deploymentB);
      
      ClassLoader clB = unitB.getClassLoader();
      assertLoadClass(clB, B.class);

      assertEquals(XBA, deployer2.deployed);
      assertEquals(NONE, deployer2.undeployed);

      ClassLoader clA = unitA.getClassLoader();
      assertLoadClass(clA, B.class, clB);
      
      assertUndeploy(deployer, deploymentA);
      assertLoadClassIllegal(clA, B.class);

      assertEquals(XBA, deployer2.deployed);
      assertEquals(XA, deployer2.undeployed);
      
      unitA = assertDeploy(deployer, deploymentA);
      clA = unitA.getClassLoader();
      assertLoadClass(clA, B.class, clB);

      assertEquals(XBAA, deployer2.deployed);
      assertEquals(XA, deployer2.undeployed);
   }

   public void testADependsUponModuleBRedeployB() throws Exception
   {
      DeployerClient deployer = getMainDeployer();

      Deployment deploymentA = createDeployment(NameA);
      ClassLoadingMetaData classLoaderMetaData = addClassLoadingMetaData(deploymentA, null, A.class);
      addRequireModule(classLoaderMetaData, "B", null);
      DeploymentUnit unitA = addDeployment(deployer, deploymentA);
      
      assertNoClassLoader(unitA);

      assertEquals(NONE, deployer2.deployed);
      assertEquals(NONE, deployer2.undeployed);

      Deployment deploymentB = createDeployment(NameB);
      addClassLoadingMetaData(deploymentB, null, B.class);
      DeploymentUnit unitB = assertDeploy(deployer, deploymentB);
      
      ClassLoader clB = unitB.getClassLoader();
      assertLoadClass(clB, B.class);

      assertEquals(XBA, deployer2.deployed);
      assertEquals(NONE, deployer2.undeployed);

      ClassLoader clA = unitA.getClassLoader();
      assertLoadClass(clA, B.class, clB);
      
      enableTrace("org.jboss.deployers");
      enableTrace("org.jboss.dependency");
      assertUndeploy(deployer, deploymentB);
      assertLoadClassIllegal(clA, B.class);

      assertEquals(XBA, deployer2.deployed);
      assertEquals(XAB, deployer2.undeployed);
      
      unitB = assertDeploy(deployer, deploymentB);
      clA = unitA.getClassLoader();
      clB = unitB.getClassLoader();
      assertLoadClass(clA, B.class, clB);

      assertEquals(XBABA, deployer2.deployed);
      assertEquals(XAB, deployer2.undeployed);
   }
}
