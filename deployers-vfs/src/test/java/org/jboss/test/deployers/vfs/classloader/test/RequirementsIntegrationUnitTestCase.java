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

import java.util.Collections;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.jboss.classloader.plugins.ClassLoaderUtils;
import org.jboss.classloader.plugins.jdk.AbstractJDKChecker;
import org.jboss.classloader.plugins.system.DefaultClassLoaderSystem;
import org.jboss.classloader.spi.ClassLoaderSystem;
import org.jboss.classloader.spi.ParentPolicy;
import org.jboss.classloading.spi.dependency.ClassLoading;
import org.jboss.classloading.spi.metadata.ClassLoadingMetaData;
import org.jboss.deployers.client.spi.DeployerClient;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.spi.deployer.Deployer;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.plugins.classloader.ModuleRequirementIntegrationDeployer;
import org.jboss.deployers.vfs.plugins.classloader.PackageRequirementIntegrationDeployer;
import org.jboss.deployers.vfs.plugins.classloader.VFSClassLoaderClassPathDeployer;
import org.jboss.deployers.vfs.plugins.classloader.VFSClassLoaderDescribeDeployer;
import org.jboss.test.deployers.vfs.classloader.support.TestLevelClassLoaderSystemDeployer;
import org.jboss.test.deployers.vfs.classloader.support.a.A;

/**
 * RequirementsIntegrationUnitTestCase.
 *
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 */
public class RequirementsIntegrationUnitTestCase extends VFSClassLoaderDependenciesTest
{
   public static Test suite()
   {
      return new TestSuite(RequirementsIntegrationUnitTestCase.class);
   }

   public RequirementsIntegrationUnitTestCase(String name)
   {
      super(name);
   }

   protected DeployerClient getMainDeployer()
   {
      AbstractJDKChecker.getExcluded().add(VFSClassLoaderDependenciesTest.class);

      ClassLoading classLoading = new ClassLoading();
      ClassLoaderSystem system = new DefaultClassLoaderSystem();
      system.getDefaultDomain().setParentPolicy(ParentPolicy.BEFORE_BUT_JAVA_ONLY);

      deployer1 = new VFSClassLoaderDescribeDeployer();
      deployer1.setClassLoading(classLoading);

      deployer2 = new TestLevelClassLoaderSystemDeployer();
      deployer2.setClassLoading(classLoading);
      deployer2.setSystem(system);

      Deployer deployer3 = new VFSClassLoaderClassPathDeployer();

      ModuleRequirementIntegrationDeployer<Object> moduleRequirementDeployer = new ModuleRequirementIntegrationDeployer<Object>(Object.class);
      moduleRequirementDeployer.setModule("seam");
      moduleRequirementDeployer.setIntegrationModuleName("jboss-seam-int");

      PackageRequirementIntegrationDeployer<Object> packageRequirementDeployer = new PackageRequirementIntegrationDeployer<Object>(Object.class);
      String pck = ClassLoaderUtils.getClassPackageName(A.class.getName());
      packageRequirementDeployer.setPackages(Collections.singleton(pck));
      packageRequirementDeployer.setIntegrationModuleName("jboss-seam-int");

      return createMainDeployer(deployer1, deployer2, deployer3, moduleRequirementDeployer, packageRequirementDeployer);
   }

   public void testModules() throws Exception
   {
      DeployerClient mainDeployer = getMainDeployer();

      Deployment app = createDeployment("app");
      ClassLoadingMetaData appCLMD = addClassLoadingMetaData(app, null);
      addRequireModule(appCLMD, "seam", null);
      DeploymentUnit appUnit = addDeployment(mainDeployer, app);
      try
      {
         assertDeploymentStage(appUnit, DeploymentStages.DESCRIBE);

         Deployment seam = createDeployment("seam");
         try
         {
            addClassLoadingMetaData(seam, null);
            assertDeploy(mainDeployer, seam);
            // still in describe, since we depend on integration
            assertDeploymentStage(appUnit, DeploymentStages.DESCRIBE);

            Deployment integration = createDeployment("jboss-seam-int");
            try
            {
               addClassLoadingMetaData(integration, null);
               assertDeploy(mainDeployer, integration);
               // should be installed now
               assertDeploymentStage(appUnit, DeploymentStages.INSTALLED);
            }
            finally
            {
               assertUndeploy(mainDeployer, integration);
            }
         }
         finally
         {
            assertUndeploy(mainDeployer, seam);
         }
      }
      finally
      {
         assertUndeploy(mainDeployer, app);
      }
   }

   public void testPackages() throws Exception
   {
      DeployerClient mainDeployer = getMainDeployer();

      Deployment app = createDeployment("app");
      ClassLoadingMetaData appCLMD = addClassLoadingMetaData(app, null);
      addRequirePackage(appCLMD, A.class, null);
      DeploymentUnit appUnit = addDeployment(mainDeployer, app);
      try
      {
         assertDeploymentStage(appUnit, DeploymentStages.DESCRIBE);

         Deployment pckgA = createDeployment("pckgA");
         try
         {
            addClassLoadingMetaData(pckgA, null, A.class);
            assertDeploy(mainDeployer, pckgA);
            // still in describe, since we depend on integration
            assertDeploymentStage(appUnit, DeploymentStages.DESCRIBE);

            Deployment integration = createDeployment("jboss-seam-int");
            try
            {
               addClassLoadingMetaData(integration, null);
               assertDeploy(mainDeployer, integration);
               // should be installed now
               assertDeploymentStage(appUnit, DeploymentStages.INSTALLED);
            }
            finally
            {
               assertUndeploy(mainDeployer, integration);
            }
         }
         finally
         {
            assertUndeploy(mainDeployer, pckgA);
         }
      }
      finally
      {
         assertUndeploy(mainDeployer, app);
      }
   }
}