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
package org.jboss.test.deployers.vfs.classloader.test;

import java.net.URL;

import junit.framework.Test;
import org.jboss.classloader.plugins.jdk.AbstractJDKChecker;
import org.jboss.classloader.plugins.system.DefaultClassLoaderSystem;
import org.jboss.classloader.spi.ClassLoaderSystem;
import org.jboss.classloader.spi.ParentPolicy;
import org.jboss.classloading.spi.dependency.ClassLoading;
import org.jboss.classloading.spi.metadata.ClassLoadingMetaData;
import org.jboss.classloading.spi.version.Version;
import org.jboss.deployers.client.spi.DeployerClient;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.spi.deployer.Deployer;
import org.jboss.deployers.structure.spi.main.MainDeployerStructure;
import org.jboss.deployers.vfs.plugins.classloader.InMemoryClassesDeployer;
import org.jboss.deployers.vfs.plugins.classloader.VFSClassLoaderClassPathDeployer;
import org.jboss.deployers.vfs.plugins.classloader.VFSClassLoaderDescribeDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.test.deployers.vfs.classloader.support.TestLevelClassLoaderSystemDeployer;
import org.jboss.test.deployers.vfs.classloader.support.a.A;
import org.jboss.virtual.MemoryFileFactory;
import org.jboss.virtual.VirtualFile;

/**
 * InMemoryClasesUnitTestCase.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class InMemoryClasesUnitTestCase extends VFSClassLoaderDependenciesTest
{
   public InMemoryClasesUnitTestCase(String name)
   {
      super(name);
   }

   public static Test suite()
   {
      return suite(InMemoryClasesUnitTestCase.class);
   }

   public void testInMemory() throws Exception
   {
      DeployerClient mainDeployer = getMainDeployer();
      MainDeployerStructure main = (MainDeployerStructure) mainDeployer; 

      Version v1 = Version.parseVersion("1");

      Deployment ad = createDeployment("A");
      ClassLoadingMetaData clmd = addClassLoadingMetaData(ad, v1, true, A.class);
      clmd.setBlackListable(false);
      assertDeploy(mainDeployer, ad);

      VFSDeploymentUnit unit = (VFSDeploymentUnit) main.getDeploymentUnit("A");
      VirtualFile file = unit.getAttachment(InMemoryClassesDeployer.DYNAMIC_CLASS_KEY, VirtualFile.class);
      assertNotNull(file);
      assertTrue("dynamic classes should be in the classpath", unit.getClassPath().contains(file));

      URL root = unit.getAttachment(InMemoryClassesDeployer.DYNAMIC_CLASS_URL_KEY, URL.class);
      assertNotNull(root);
      URL classes = new URL(root, "classes");
      
      String aPackage = A.class.getPackage().getName();
      aPackage = aPackage.replace(".", "/");
      String resourceName = aPackage + "/TestInMemory";
      URL testResource = new URL(classes + "/" + resourceName);
      ClassLoader cl = unit.getClassLoader();
      assertNull(cl.getResource(resourceName));
      
      byte[] bytes = new byte[0];
      MemoryFileFactory.putFile(testResource, bytes);
      assertEquals(testResource, cl.getResource(resourceName));
      
      mainDeployer.undeploy(ad);
      mainDeployer.checkComplete();
      assertFalse("dynamic classes should NOT be in the classpath", unit.getClassPath().contains(file));
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
      
      Deployer deployer3 = new InMemoryClassesDeployer();

      Deployer deployer4 = new VFSClassLoaderClassPathDeployer();
      
      return createMainDeployer(deployer1, deployer2, deployer3, deployer4);
   }
}
