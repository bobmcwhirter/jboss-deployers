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
package org.jboss.test.deployers.classloading.test;

import java.util.Collections;
import java.util.List;

import org.jboss.classloading.spi.dependency.LifeCycle;
import org.jboss.classloading.spi.dependency.Module;
import org.jboss.classloading.spi.metadata.ClassLoadingMetaData;
import org.jboss.dependency.spi.Controller;
import org.jboss.deployers.client.spi.DeployerClient;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.plugins.classloading.DeploymentMetaData;
import org.jboss.deployers.plugins.classloading.DeploymentValidationDeployer;
import org.jboss.deployers.plugins.classloading.FilterMetaData;
import org.jboss.deployers.spi.deployer.Deployer;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.test.deployers.classloading.support.MockDeployer;
import org.jboss.test.deployers.classloading.support.MockDeployerImpl;
import org.jboss.test.deployers.classloading.support.a.A;
import org.jboss.test.deployers.classloading.support.b.B;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * ClassLoadingLifeCycleUnitTestCase
 *
 * TODO test multiples
 * TODO test non deployment lifecycles
 * @author adrian@jboss.org
 * @author ales.justin@jboss.org
 */
public class ClassLoadingLifeCycleUnitTestCase extends ClassLoaderDependenciesTest
{
   MockDeployerImpl deployer3 = new MockDeployerImpl("Real");

   public static Test suite()
   {
      return new TestSuite(ClassLoadingLifeCycleUnitTestCase.class);
   }
   
   public ClassLoadingLifeCycleUnitTestCase(String name)
   {
      super(name);
   }
   
   @Override
   protected DeployerClient getMainDeployer(Deployer... deployers)
   {
      Controller controller = getController();
      DeploymentValidationDeployer dvd = new DeploymentValidationDeployer(controller);
      return super.getMainDeployer(deployer3, dvd);
   }

   public void testSmoke() throws Exception
   {
      DeployerClient deployer = getMainDeployer();

      Deployment deployment = createSimpleDeployment(NameA);
      addClassLoadingMetaData(deployment, deployment.getName(), null, A.class);
      
      DeploymentUnit unit = assertDeploy(deployer, deployment);
      LifeCycle lifeCycle = assertLifeCycle(unit);

      assertDeployed(A);
      assertUndeployed(NONE);
      assertTrue(lifeCycle.isResolved());
      assertTrue(lifeCycle.isStarted());
      
      ClassLoader cl = unit.getClassLoader();
      assertLoadClass(cl, A.class);

      clear();
      assertUndeploy(deployer, deployment);
      assertDeployed(NONE);
      assertUndeployed(A);
      assertFalse(lifeCycle.isResolved());
      assertFalse(lifeCycle.isStarted());
   }

   public void testResolve() throws Exception
   {
      DeployerClient deployer = getMainDeployer();

      Deployment deployment = createSimpleDeployment(NameA);
      addClassLoadingMetaData(deployment, deployment.getName(), null, A.class);
      
      DeploymentUnit unit = assertDeploy(deployer, deployment);
      LifeCycle lifeCycle = assertLifeCycle(unit);
      assertTrue(lifeCycle.isResolved());
      assertTrue(lifeCycle.isStarted());

      deployer.change(unit.getName(), DeploymentStages.DESCRIBE);
      assertFalse(lifeCycle.isResolved());
      assertFalse(lifeCycle.isStarted());
      
      clear();
      lifeCycle.resolve();
      assertDeployed(NONE, deployer1);
      assertDeployed(A, deployer2);
      assertDeployed(NONE, deployer3);
      assertUndeployed(NONE);
      assertTrue(lifeCycle.isResolved());
      assertFalse(lifeCycle.isStarted());
      
      ClassLoader cl = unit.getClassLoader();
      assertLoadClass(cl, A.class);

      clear();
      assertUndeploy(deployer, deployment);
      assertDeployed(NONE);
      assertUndeployed(A, deployer1);
      assertUndeployed(A, deployer2);
      assertUndeployed(NONE, deployer3);
      assertFalse(lifeCycle.isResolved());
      assertFalse(lifeCycle.isStarted());
   }

   public void testResolveNoMove() throws Exception
   {
      DeployerClient deployer = getMainDeployer();

      Deployment deployment = createSimpleDeployment(NameA);
      addClassLoadingMetaData(deployment, deployment.getName(), null, A.class);
      
      DeploymentUnit unit = assertDeploy(deployer, deployment);
      LifeCycle lifeCycle = assertLifeCycle(unit);
      assertTrue(lifeCycle.isResolved());
      assertTrue(lifeCycle.isStarted());

      deployer.change(unit.getName(), DeploymentStages.CLASSLOADER);
      assertTrue(lifeCycle.isResolved());
      assertFalse(lifeCycle.isStarted());
      
      clear();
      lifeCycle.resolve();
      assertDeployed(NONE);
      assertUndeployed(NONE);
      assertTrue(lifeCycle.isResolved());
      assertFalse(lifeCycle.isStarted());
      
      ClassLoader cl = unit.getClassLoader();
      assertLoadClass(cl, A.class);

      clear();
      assertUndeploy(deployer, deployment);
      assertDeployed(NONE);
      assertUndeployed(A, deployer1);
      assertUndeployed(A, deployer2);
      assertUndeployed(NONE, deployer3);
      assertFalse(lifeCycle.isResolved());
      assertFalse(lifeCycle.isStarted());
   }

   public void testUnresolve() throws Exception
   {
      DeployerClient deployer = getMainDeployer();

      Deployment deployment = createSimpleDeployment(NameA);
      addClassLoadingMetaData(deployment, deployment.getName(), null, A.class);
      
      DeploymentUnit unit = assertDeploy(deployer, deployment);
      LifeCycle lifeCycle = assertLifeCycle(unit);
      assertTrue(lifeCycle.isResolved());
      assertTrue(lifeCycle.isStarted());

      clear();
      lifeCycle.unresolve();
      assertDeployed(NONE);
      assertUndeployed(NONE, deployer1);
      assertUndeployed(A, deployer2);
      assertUndeployed(A, deployer3);
      assertFalse(lifeCycle.isResolved());
      assertFalse(lifeCycle.isStarted());
      
      clear();
      lifeCycle.resolve();
      assertDeployed(NONE, deployer1);
      assertDeployed(A, deployer2);
      assertDeployed(NONE, deployer3);
      assertUndeployed(NONE);
      assertTrue(lifeCycle.isResolved());
      assertFalse(lifeCycle.isStarted());
      
      ClassLoader cl = unit.getClassLoader();
      assertLoadClass(cl, A.class);

      clear();
      lifeCycle.unresolve();
      assertDeployed(NONE);
      assertUndeployed(NONE, deployer1);
      assertUndeployed(A, deployer2);
      assertUndeployed(NONE, deployer3);
      assertFalse(lifeCycle.isResolved());
      assertFalse(lifeCycle.isStarted());
      
      clear();
      assertUndeploy(deployer, deployment);
      assertDeployed(NONE);
      assertUndeployed(A, deployer1);
      assertUndeployed(NONE, deployer2);
      assertUndeployed(NONE, deployer3);
      assertFalse(lifeCycle.isResolved());
      assertFalse(lifeCycle.isStarted());
   }

   public void testUnresolveNoMove() throws Exception
   {
      DeployerClient deployer = getMainDeployer();

      Deployment deployment = createSimpleDeployment(NameA);
      addClassLoadingMetaData(deployment, deployment.getName(), null, A.class);
      
      DeploymentUnit unit = assertDeploy(deployer, deployment);
      LifeCycle lifeCycle = assertLifeCycle(unit);
      assertTrue(lifeCycle.isResolved());
      assertTrue(lifeCycle.isStarted());

      clear();
      lifeCycle.unresolve();
      assertDeployed(NONE);
      assertUndeployed(NONE, deployer1);
      assertUndeployed(A, deployer2);
      assertUndeployed(A, deployer3);
      assertFalse(lifeCycle.isResolved());
      assertFalse(lifeCycle.isStarted());

      clear();
      lifeCycle.unresolve();
      assertDeployed(NONE);
      assertUndeployed(NONE);
      assertFalse(lifeCycle.isResolved());
      assertFalse(lifeCycle.isStarted());
      
      clear();
      assertUndeploy(deployer, deployment);
      assertDeployed(NONE);
      assertUndeployed(A, deployer1);
      assertUndeployed(NONE, deployer2);
      assertUndeployed(NONE, deployer3);
      assertFalse(lifeCycle.isResolved());
      assertFalse(lifeCycle.isStarted());
   }

   public void testStartFromUnResolved() throws Exception
   {
      DeployerClient deployer = getMainDeployer();

      Deployment deployment = createSimpleDeployment(NameA);
      addClassLoadingMetaData(deployment, deployment.getName(), null, A.class);
      
      DeploymentUnit unit = assertDeploy(deployer, deployment);
      LifeCycle lifeCycle = assertLifeCycle(unit);
      assertTrue(lifeCycle.isResolved());
      assertTrue(lifeCycle.isStarted());

      lifeCycle.unresolve();
      assertFalse(lifeCycle.isResolved());
      assertFalse(lifeCycle.isStarted());
      
      clear();
      lifeCycle.start();
      assertDeployed(NONE, deployer1);
      assertDeployed(A, deployer2);
      assertDeployed(A, deployer3);
      assertUndeployed(NONE);
      assertTrue(lifeCycle.isResolved());
      assertTrue(lifeCycle.isStarted());
      
      ClassLoader cl = unit.getClassLoader();
      assertLoadClass(cl, A.class);


      clear();
      assertUndeploy(deployer, deployment);
      assertDeployed(NONE);
      assertUndeployed(A);
      assertFalse(lifeCycle.isResolved());
      assertFalse(lifeCycle.isStarted());
   }

   public void testStartFromResolved() throws Exception
   {
      DeployerClient deployer = getMainDeployer();

      Deployment deployment = createSimpleDeployment(NameA);
      addClassLoadingMetaData(deployment, deployment.getName(), null, A.class);
      
      DeploymentUnit unit = assertDeploy(deployer, deployment);
      LifeCycle lifeCycle = assertLifeCycle(unit);
      assertTrue(lifeCycle.isResolved());
      assertTrue(lifeCycle.isStarted());

      deployer.change(unit.getName(), DeploymentStages.CLASSLOADER);
      assertTrue(lifeCycle.isResolved());
      assertFalse(lifeCycle.isStarted());
      
      clear();
      lifeCycle.start();
      assertDeployed(NONE, deployer1);
      assertDeployed(NONE, deployer2);
      assertDeployed(A, deployer3);
      assertUndeployed(NONE);
      assertTrue(lifeCycle.isResolved());
      assertTrue(lifeCycle.isStarted());
      
      ClassLoader cl = unit.getClassLoader();
      assertLoadClass(cl, A.class);

      clear();
      assertUndeploy(deployer, deployment);
      assertDeployed(NONE);
      assertUndeployed(A);
      assertFalse(lifeCycle.isResolved());
      assertFalse(lifeCycle.isStarted());
   }

   public void testStartNoMove() throws Exception
   {
      DeployerClient deployer = getMainDeployer();

      Deployment deployment = createSimpleDeployment(NameA);
      addClassLoadingMetaData(deployment, deployment.getName(), null, A.class);
      
      DeploymentUnit unit = assertDeploy(deployer, deployment);
      LifeCycle lifeCycle = assertLifeCycle(unit);
      assertTrue(lifeCycle.isResolved());
      assertTrue(lifeCycle.isStarted());
      
      clear();
      lifeCycle.start();
      assertDeployed(NONE);
      assertUndeployed(NONE);
      assertTrue(lifeCycle.isResolved());
      assertTrue(lifeCycle.isStarted());
      
      ClassLoader cl = unit.getClassLoader();
      assertLoadClass(cl, A.class);

      clear();
      assertUndeploy(deployer, deployment);
      assertDeployed(NONE);
      assertUndeployed(A);
      assertFalse(lifeCycle.isResolved());
      assertFalse(lifeCycle.isStarted());
   }

   public void testStop() throws Exception
   {
      DeployerClient deployer = getMainDeployer();

      Deployment deployment = createSimpleDeployment(NameA);
      addClassLoadingMetaData(deployment, deployment.getName(), null, A.class);
      
      DeploymentUnit unit = assertDeploy(deployer, deployment);
      LifeCycle lifeCycle = assertLifeCycle(unit);
      assertTrue(lifeCycle.isResolved());
      assertTrue(lifeCycle.isStarted());

      clear();
      lifeCycle.stop();
      assertDeployed(NONE);
      assertUndeployed(NONE, deployer1);
      assertUndeployed(NONE, deployer2);
      assertUndeployed(A, deployer3);
      assertTrue(lifeCycle.isResolved());
      assertFalse(lifeCycle.isStarted());
      
      ClassLoader cl = unit.getClassLoader();
      assertLoadClass(cl, A.class);

      clear();
      assertUndeploy(deployer, deployment);
      assertDeployed(NONE);
      assertUndeployed(A, deployer1);
      assertUndeployed(A, deployer2);
      assertUndeployed(NONE, deployer3);
      assertFalse(lifeCycle.isResolved());
      assertFalse(lifeCycle.isStarted());
   }

   public void testStopNoMove() throws Exception
   {
      DeployerClient deployer = getMainDeployer();

      Deployment deployment = createSimpleDeployment(NameA);
      addClassLoadingMetaData(deployment, deployment.getName(), null, A.class);
      
      DeploymentUnit unit = assertDeploy(deployer, deployment);
      LifeCycle lifeCycle = assertLifeCycle(unit);
      assertTrue(lifeCycle.isResolved());
      assertTrue(lifeCycle.isStarted());

      lifeCycle.stop();
      
      clear();
      lifeCycle.stop();
      assertDeployed(NONE);
      assertUndeployed(NONE);
      assertTrue(lifeCycle.isResolved());
      assertFalse(lifeCycle.isStarted());
      
      ClassLoader cl = unit.getClassLoader();
      assertLoadClass(cl, A.class);

      clear();
      assertUndeploy(deployer, deployment);
      assertDeployed(NONE);
      assertUndeployed(A, deployer1);
      assertUndeployed(A, deployer2);
      assertUndeployed(NONE, deployer3);
      assertFalse(lifeCycle.isResolved());
      assertFalse(lifeCycle.isStarted());
   }

   public void testStopNoMoveWhenUnresolved() throws Exception
   {
      DeployerClient deployer = getMainDeployer();

      Deployment deployment = createSimpleDeployment(NameA);
      addClassLoadingMetaData(deployment, deployment.getName(), null, A.class);
      
      DeploymentUnit unit = assertDeploy(deployer, deployment);
      LifeCycle lifeCycle = assertLifeCycle(unit);
      assertTrue(lifeCycle.isResolved());
      assertTrue(lifeCycle.isStarted());

      lifeCycle.unresolve();
      
      clear();
      lifeCycle.stop();
      assertDeployed(NONE);
      assertUndeployed(NONE);
      assertFalse(lifeCycle.isResolved());
      assertFalse(lifeCycle.isStarted());

      clear();
      assertUndeploy(deployer, deployment);
      assertDeployed(NONE);
      assertUndeployed(A, deployer1);
      assertUndeployed(NONE, deployer2);
      assertUndeployed(NONE, deployer3);
      assertFalse(lifeCycle.isResolved());
      assertFalse(lifeCycle.isStarted());
   }

   public void testBounceFromStart() throws Exception
   {
      DeployerClient deployer = getMainDeployer();

      Deployment deployment = createSimpleDeployment(NameA);
      addClassLoadingMetaData(deployment, deployment.getName(), null, A.class);
      
      DeploymentUnit unit = assertDeploy(deployer, deployment);
      LifeCycle lifeCycle = assertLifeCycle(unit);
      assertTrue(lifeCycle.isResolved());
      assertTrue(lifeCycle.isStarted());

      clear();
      lifeCycle.bounce();
      assertDeployed(NONE, deployer1);
      assertDeployed(A, deployer2);
      assertDeployed(A, deployer3);
      assertUndeployed(NONE, deployer1);
      assertUndeployed(A, deployer2);
      assertUndeployed(A, deployer3);
      assertTrue(lifeCycle.isResolved());
      assertTrue(lifeCycle.isStarted());
      
      ClassLoader cl = unit.getClassLoader();
      assertLoadClass(cl, A.class);

      clear();
      assertUndeploy(deployer, deployment);
      assertDeployed(NONE);
      assertUndeployed(A);
      assertFalse(lifeCycle.isResolved());
      assertFalse(lifeCycle.isStarted());
   }

   public void testBounceFromResolved() throws Exception
   {
      DeployerClient deployer = getMainDeployer();

      Deployment deployment = createSimpleDeployment(NameA);
      addClassLoadingMetaData(deployment, deployment.getName(), null, A.class);
      
      DeploymentUnit unit = assertDeploy(deployer, deployment);
      LifeCycle lifeCycle = assertLifeCycle(unit);
      assertTrue(lifeCycle.isResolved());
      assertTrue(lifeCycle.isStarted());

      lifeCycle.stop();
      
      clear();
      lifeCycle.bounce();
      assertDeployed(NONE, deployer1);
      assertDeployed(A, deployer2);
      assertDeployed(NONE, deployer3);
      assertUndeployed(NONE, deployer1);
      assertUndeployed(A, deployer2);
      assertUndeployed(NONE, deployer3);
      assertTrue(lifeCycle.isResolved());
      assertFalse(lifeCycle.isStarted());
      
      ClassLoader cl = unit.getClassLoader();
      assertLoadClass(cl, A.class);

      clear();
      assertUndeploy(deployer, deployment);
      assertDeployed(NONE);
      assertUndeployed(A, deployer1);
      assertUndeployed(A, deployer2);
      assertUndeployed(NONE, deployer3);
      assertFalse(lifeCycle.isResolved());
      assertFalse(lifeCycle.isStarted());
   }

   public void testBounceFromUnresolved() throws Exception
   {
      DeployerClient deployer = getMainDeployer();

      Deployment deployment = createSimpleDeployment(NameA);
      addClassLoadingMetaData(deployment, deployment.getName(), null, A.class);
      
      DeploymentUnit unit = assertDeploy(deployer, deployment);
      LifeCycle lifeCycle = assertLifeCycle(unit);
      assertTrue(lifeCycle.isResolved());
      assertTrue(lifeCycle.isStarted());

      lifeCycle.unresolve();
      
      clear();
      lifeCycle.bounce();
      assertDeployed(NONE);
      assertUndeployed(NONE);
      assertFalse(lifeCycle.isResolved());
      assertFalse(lifeCycle.isStarted());
      
      assertNoClassLoader(unit);

      clear();
      assertUndeploy(deployer, deployment);
      assertDeployed(NONE);
      assertUndeployed(A, deployer1);
      assertUndeployed(NONE, deployer2);
      assertUndeployed(NONE, deployer3);
      assertFalse(lifeCycle.isResolved());
      assertFalse(lifeCycle.isStarted());
   }

   public void testLazyResolve() throws Exception
   {
      DeployerClient deployer = getMainDeployer();

      Deployment deploymentA = createSimpleDeployment(NameA);
      addClassLoadingMetaData(deploymentA, deploymentA.getName(), null, A.class);
      
      DeploymentUnit unitA = assertDeploy(deployer, deploymentA);
      LifeCycle lifeCycleA = assertLifeCycle(unitA);
      assertTrue(lifeCycleA.isResolved());
      assertTrue(lifeCycleA.isStarted());

      Deployment deploymentB = createSimpleDeployment(NameB);
      ClassLoadingMetaData clmdB = addClassLoadingMetaData(deploymentB, deploymentB.getName(), null, B.class);
      addRequireModule(clmdB, deploymentA.getName(), null);
      
      DeploymentUnit unitB = assertDeploy(deployer, deploymentB);
      LifeCycle lifeCycleB = assertLifeCycle(unitB);
      assertTrue(lifeCycleB.isResolved());
      assertTrue(lifeCycleB.isStarted());

      ClassLoader clA = unitA.getClassLoader();
      assertLoadClass(clA, A.class);

      ClassLoader clB = unitB.getClassLoader();
      assertLoadClass(clB, A.class, clA);
      
      clear();
      lifeCycleA.unresolve();
      assertDeployed(NONE);
      assertUndeployed(NONE, deployer1);
      assertUndeployed(BA, deployer2);
      assertUndeployed(AB, deployer3); 
      assertFalse(lifeCycleA.isResolved());
      assertFalse(lifeCycleA.isStarted());
      assertFalse(lifeCycleB.isResolved());
      assertFalse(lifeCycleB.isStarted());

      clear();
      lifeCycleA.setLazyResolve(true);
      lifeCycleB.resolve();
      assertDeployed(NONE, deployer1);
      assertDeployed(AB, deployer2);
      assertDeployed(NONE, deployer3);
      assertUndeployed(NONE);
      assertTrue(lifeCycleA.isResolved());
      assertFalse(lifeCycleA.isStarted());
      assertTrue(lifeCycleB.isResolved());
      assertFalse(lifeCycleB.isStarted());

      clA = unitA.getClassLoader();
      assertLoadClass(clA, A.class);

      clB = unitB.getClassLoader();
      assertLoadClass(clB, A.class, clA);
      
      clear();
      assertUndeploy(deployer, deploymentB);
      assertUndeploy(deployer, deploymentA);
      assertDeployed(NONE);
      assertUndeployed(BA, deployer1);
      assertUndeployed(BA, deployer2);
      assertUndeployed(NONE, deployer3);
      assertFalse(lifeCycleA.isResolved());
      assertFalse(lifeCycleA.isStarted());
      assertFalse(lifeCycleB.isResolved());
      assertFalse(lifeCycleB.isStarted());
   }

   public void testLazyStart() throws Exception
   {
      DeployerClient deployer = getMainDeployer();

      Deployment deploymentA = createSimpleDeployment(NameA);
      addClassLoadingMetaData(deploymentA, deploymentA.getName(), null, A.class);
      
      DeploymentUnit unitA = assertDeploy(deployer, deploymentA);
      LifeCycle lifeCycleA = assertLifeCycle(unitA);
      assertTrue(lifeCycleA.isResolved());
      assertTrue(lifeCycleA.isStarted());

      Deployment deploymentB = createSimpleDeployment(NameB);
      ClassLoadingMetaData clmdB = addClassLoadingMetaData(deploymentB, deploymentB.getName(), null, B.class);
      addRequireModule(clmdB, deploymentA.getName(), null);
      
      DeploymentUnit unitB = assertDeploy(deployer, deploymentB);
      LifeCycle lifeCycleB = assertLifeCycle(unitB);
      assertTrue(lifeCycleB.isResolved());
      assertTrue(lifeCycleB.isStarted());

      clear();
      lifeCycleA.stop();
      assertDeployed(NONE);
      assertUndeployed(NONE, deployer1);
      assertUndeployed(NONE, deployer2);
      assertUndeployed(A, deployer3); 
      assertTrue(lifeCycleA.isResolved());
      assertFalse(lifeCycleA.isStarted());
      assertTrue(lifeCycleB.isResolved());
      assertTrue(lifeCycleB.isStarted());

      clear();
      lifeCycleA.setLazyStart(true);
      assertDeployed(NONE);
      assertUndeployed(NONE);
      assertTrue(lifeCycleA.isResolved());
      assertFalse(lifeCycleA.isStarted());
      assertTrue(lifeCycleB.isResolved());
      assertTrue(lifeCycleB.isStarted());

      clear();
      ClassLoader clA = unitA.getClassLoader();
      ClassLoader clB = unitB.getClassLoader();
      assertLoadClass(clB, A.class, clA);
      assertDeployed(NONE, deployer1);
      assertDeployed(NONE, deployer2);
      assertDeployed(A, deployer3);
      assertUndeployed(NONE);
      assertTrue(lifeCycleA.isResolved());
      assertTrue(lifeCycleA.isStarted());
      assertTrue(lifeCycleB.isResolved());
      assertTrue(lifeCycleB.isStarted());
      
      clear();
      assertUndeploy(deployer, deploymentB);
      assertUndeploy(deployer, deploymentA);
      assertDeployed(NONE);
      assertUndeployed(BA, deployer1);
      assertUndeployed(BA, deployer2);
      assertUndeployed(BA, deployer3);
      assertFalse(lifeCycleA.isResolved());
      assertFalse(lifeCycleA.isStarted());
      assertFalse(lifeCycleB.isResolved());
      assertFalse(lifeCycleB.isStarted());
   }

   public void testDeploymentMetaData() throws Exception
   {
      DeployerClient deployer = getMainDeployer();

      Deployment deploymentA = createSimpleDeployment(NameA);
      addClassLoadingMetaData(deploymentA, deploymentA.getName(), null, A.class);
      DeploymentMetaData dmd = new DeploymentMetaData();
      dmd.setLazyResolve(true);
      dmd.setLazyStart(true);
      FilterMetaData data = new FilterMetaData();
      data.setValue(A.class.getPackage().getName());
      dmd.setFilters(Collections.singleton(data));
      addMetaData(deploymentA, dmd, DeploymentMetaData.class);

      DeploymentUnit unitA = assertDeploy(deployer, deploymentA);
      LifeCycle lifeCycleA = assertLifeCycle(unitA);
      assertFalse(lifeCycleA.isResolved());
      assertFalse(lifeCycleA.isStarted());

      Deployment deploymentB = createSimpleDeployment(NameB);
      ClassLoadingMetaData clmdB = addClassLoadingMetaData(deploymentB, deploymentB.getName(), null, B.class);
      addRequireModule(clmdB, deploymentA.getName(), null);

      DeploymentUnit unitB = assertDeploy(deployer, deploymentB);
      LifeCycle lifeCycleB = assertLifeCycle(unitB);
      assertTrue(lifeCycleB.isResolved());
      assertTrue(lifeCycleA.isResolved());
      assertTrue(lifeCycleB.isStarted());
      assertFalse(lifeCycleA.isStarted());

      ClassLoader clA = unitA.getClassLoader();
      ClassLoader clB = unitB.getClassLoader();
      assertLoadClass(clB, A.class, clA);

      assertTrue(lifeCycleA.isResolved());
      assertTrue(lifeCycleA.isStarted());
      assertTrue(lifeCycleB.isResolved());
      assertTrue(lifeCycleB.isStarted());

      assertUndeploy(deployer, deploymentB);
      assertUndeploy(deployer, deploymentA);
      assertFalse(lifeCycleA.isResolved());
      assertFalse(lifeCycleA.isStarted());
      assertFalse(lifeCycleB.isResolved());
      assertFalse(lifeCycleB.isStarted());
   }

   protected LifeCycle assertLifeCycle(DeploymentUnit unit)
   {
      Module module = assertModule(unit);
      LifeCycle lifeCycle = module.getLifeCycle();
      assertNotNull(lifeCycle);
      return lifeCycle;
   }
   
   protected Module assertModule(DeploymentUnit unit)
   {
      Module module = unit.getAttachment(Module.class);
      assertNotNull(module);
      return module;
   }
   
   protected void clear()
   {
      deployer1.clear();
      deployer2.clear();
      deployer3.clear();
   }
   
   protected void assertDeployed(List<String> expected)
   {
      assertDeployed(expected, deployer1);
      assertDeployed(expected, deployer2);
      assertDeployed(expected, deployer3);
   }
   
   protected void assertDeployed(List<String> expected, MockDeployer deployer)
   {
      assertEquals(deployer.toString(), expected, deployer.getDeployed());
   }
   
   protected void assertUndeployed(List<String> expected)
   {
      assertUndeployed(expected, deployer1);
      assertUndeployed(expected, deployer2);
      assertUndeployed(expected, deployer3);
   }
   
   protected void assertUndeployed(List<String> expected, MockDeployer deployer)
   {
      assertEquals(deployer.toString(), expected, deployer.getUnDeployed());
   }
}
