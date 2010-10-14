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

import org.jboss.classloading.spi.metadata.ClassLoadingDomainMetaData;
import org.jboss.classloading.spi.metadata.ClassLoadingMetaData;
import org.jboss.classloading.spi.metadata.FilterMetaData;
import org.jboss.classloading.spi.metadata.ParentPolicyMetaData;
import org.jboss.deployers.client.spi.DeployerClient;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.plugins.classloading.AbstractClassLoadingDomainDeployer;
import org.jboss.deployers.spi.deployer.Deployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.test.deployers.classloading.support.a.A;
import org.jboss.test.deployers.classloading.support.b.B;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * ClassLoadingDomainUnitTestCase
 *
 * @author ales.justin@jboss.org
 */
public class ClassLoadingDomainUnitTestCase extends ClassLoaderDependenciesTest
{
   public static Test suite()
   {
      return new TestSuite(ClassLoadingDomainUnitTestCase.class);
   }

   public ClassLoadingDomainUnitTestCase(String name)
   {
      super(name);
   }
   
   @Override
   protected DeployerClient getMainDeployer(Deployer... deployers)
   {
      AbstractClassLoadingDomainDeployer deployer = new AbstractClassLoadingDomainDeployer();
      DeployerClient mainDeployer = super.getMainDeployer(deployer);
      deployer.setSystem(getSystem());
      return mainDeployer;
   }

   public void testSmoke() throws Exception
   {
      DeployerClient mainDeployer = getMainDeployer();

      Deployment deployment = createSimpleDeployment(NameA);
      addClassLoadingMetaData(deployment, deployment.getName(), null);

      ClassLoadingDomainMetaData cldmd = new ClassLoadingDomainMetaData();
      addMetaData(deployment, cldmd, ClassLoadingDomainMetaData.class);

      DeploymentUnit unit = assertDeploy(mainDeployer, deployment);
      try
      {
         assertDomain(unit.getName());
      }
      finally
      {
         mainDeployer.undeploy(deployment);
      }
   }

   public void testParentPolicy() throws Exception
   {
      DeployerClient mainDeployer = getMainDeployer();

      Deployment deploymentA = createSimpleDeployment(NameA);
      addClassLoadingMetaData(deploymentA, deploymentA.getName(), null, A.class, B.class);
      DeploymentUnit unitA = assertDeploy(mainDeployer, deploymentA);
      try
      {
         ClassLoader clA = unitA.getClassLoader();
         assertLoadClass(clA, A.class);
         assertLoadClass(clA, B.class);

         Deployment deploymentB = createSimpleDeployment(NameB);
         ClassLoadingMetaData clmd = addClassLoadingMetaData(deploymentB, deploymentB.getName(), null);
         addRequirePackage(clmd, A.class, null);
         ClassLoadingDomainMetaData cldmd = new ClassLoadingDomainMetaData();
         ParentPolicyMetaData ppmd = new ParentPolicyMetaData();
         FilterMetaData bfmd = new FilterMetaData();
         bfmd.setValueString(A.class.getPackage().getName());
         ppmd.setBeforeFilter(bfmd);
         cldmd.setParentPolicy(ppmd);
         addMetaData(deploymentB, cldmd, ClassLoadingDomainMetaData.class);

         DeploymentUnit unitB = assertDeploy(mainDeployer, deploymentB);
         try
         {
            ClassLoader clB = unitB.getClassLoader();
            assertLoadClass(clB, A.class, clA);
            assertLoadClassFail(clB, B.class);
         }
         finally
         {
            mainDeployer.undeploy(deploymentB);
         }
      }
      finally
      {
         mainDeployer.undeploy(deploymentA);
      }


   }
}
