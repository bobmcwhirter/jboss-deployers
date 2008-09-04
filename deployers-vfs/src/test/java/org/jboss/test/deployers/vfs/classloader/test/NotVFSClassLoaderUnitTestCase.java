/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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

import org.jboss.classloading.spi.metadata.ClassLoadingMetaData;
import org.jboss.classloading.spi.metadata.ClassLoadingMetaDataFactory;
import org.jboss.deployers.client.plugins.deployment.AbstractDeployment;
import org.jboss.deployers.client.spi.DeployerClient;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.client.spi.DeploymentFactory;
import org.jboss.deployers.spi.attachments.MutableAttachments;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.structure.spi.main.MainDeployerStructure;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.test.deployers.BootstrapDeployersTest;

/**
 * NotVFSClassLoaderUnitTestCase.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class NotVFSClassLoaderUnitTestCase extends BootstrapDeployersTest
{
   public static Test suite()
   {
      return suite(NotVFSClassLoaderUnitTestCase.class);
   }

   public NotVFSClassLoaderUnitTestCase(String name)
   {
      super(name);
   }

   public void testNotVFSClassLoaderSmokeTest() throws Exception
   {
      DeploymentFactory factory = new DeploymentFactory();
      
      Deployment deployment = new AbstractDeployment("test");
      factory.addContext(deployment, "");
      
      DeployerClient main = assertBean("MainDeployer", DeployerClient.class);
      main.deploy(deployment);
      try
      {
         MainDeployerStructure mds = (MainDeployerStructure) main;
         DeploymentUnit unit = mds.getDeploymentUnit(deployment.getName());
         ClassLoader cl = unit.getClassLoader();
         assertNoResource("a/b/c/test-resource-deployment5", cl);
      }
      finally
      {
         main.undeploy(deployment);
      }
   }

   public void testVFSClassLoaderDependsOnVFS() throws Exception
   {
      DeploymentFactory factory = new DeploymentFactory();

      VFSDeploymentUnit unit5 = assertDeploy("/classloader", "deployment5");
      try
      {
         ClassLoader cl5 = getClassLoader(unit5);
         assertGetResource("a/b/c/test-resource-deployment5", cl5);
         
         Deployment deployment = new AbstractDeployment("test");
         factory.addContext(deployment, "");
         ClassLoadingMetaDataFactory clmdf = ClassLoadingMetaDataFactory.getInstance();
         ClassLoadingMetaData clmd = new ClassLoadingMetaData();
         clmd.getRequirements().addRequirement(clmdf.createRequireModule("test5"));
         ((MutableAttachments) deployment.getPredeterminedManagedObjects()).addAttachment(ClassLoadingMetaData.class, clmd);
         
         DeployerClient main = assertBean("MainDeployer", DeployerClient.class);
         main.deploy(deployment);
         try
         {
            MainDeployerStructure mds = (MainDeployerStructure) main;
            DeploymentUnit unit = mds.getDeploymentUnit(deployment.getName());
            ClassLoader cl = unit.getClassLoader();
            assertGetResource("a/b/c/test-resource-deployment5", cl);
         }
         finally
         {
            main.undeploy(deployment);
         }
      }
      finally
      {
         undeploy(unit5);
      }
   }
}
