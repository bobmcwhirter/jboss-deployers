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

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jboss.classloader.plugins.jdk.AbstractJDKChecker;
import org.jboss.classloader.plugins.system.DefaultClassLoaderSystem;
import org.jboss.classloader.spi.ClassLoaderSystem;
import org.jboss.classloader.spi.ParentPolicy;
import org.jboss.classloading.spi.dependency.ClassLoading;
import org.jboss.classloading.spi.metadata.CapabilitiesMetaData;
import org.jboss.classloading.spi.metadata.Capability;
import org.jboss.classloading.spi.metadata.ClassLoadingMetaData;
import org.jboss.classloading.spi.metadata.ClassLoadingMetaDataFactory;
import org.jboss.classloading.spi.metadata.Requirement;
import org.jboss.classloading.spi.metadata.RequirementsMetaData;
import org.jboss.classloading.spi.version.Version;
import org.jboss.classloading.spi.version.VersionRange;
import org.jboss.deployers.client.plugins.deployment.AbstractDeployment;
import org.jboss.deployers.client.spi.DeployerClient;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.client.spi.DeploymentFactory;
import org.jboss.deployers.plugins.classloading.AbstractClassLoaderDescribeDeployer;
import org.jboss.deployers.spi.attachments.MutableAttachments;
import org.jboss.deployers.spi.attachments.PredeterminedManagedObjectAttachments;
import org.jboss.deployers.spi.deployer.Deployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.plugins.classloader.VFSClassLoaderClassPathDeployer;
import org.jboss.deployers.vfs.plugins.classloader.VFSClassLoaderDescribeDeployer;
import org.jboss.deployers.vfs.spi.client.VFSDeployment;
import org.jboss.deployers.vfs.spi.client.VFSDeploymentFactory;
import org.jboss.test.deployers.BaseDeployersVFSTest;
import org.jboss.test.deployers.vfs.classloader.support.TestLevelClassLoaderSystemDeployer;
import org.jboss.virtual.VFS;
import org.jboss.virtual.VirtualFile;

/**
 * ClassLoadersDependencies test.
 *
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 */
public abstract class VFSClassLoaderDependenciesTest extends BaseDeployersVFSTest
{
   private static ClassLoadingMetaDataFactory classLoadingMetaDataFactory = ClassLoadingMetaDataFactory.getInstance();
   
   public static final String NameA = "A";
   public static final String NameB = "B";

   public static final List<String> NONE = Collections.emptyList();
   public static final List<String> XA = makeList(NameA);
   public static final List<String> XB = makeList(NameB);
   public static final List<String> XAB = makeList(NameA, NameB);
   public static final List<String> XBA = makeList(NameB, NameA);
   public static final List<String> XBAA = makeList(NameB, NameA, NameA);
   public static final List<String> XBABA = makeList(NameB, NameA, NameB, NameA);

   @SuppressWarnings("unchecked")
   protected static <T> List<T> makeList(T... objects)
   {
      List<T> result = new ArrayList<T>();
      for (T object : objects)
         result.add(object);
      return result;
   }

   protected AbstractClassLoaderDescribeDeployer deployer1;
   protected TestLevelClassLoaderSystemDeployer deployer2;

   protected VFSClassLoaderDependenciesTest(String name)
   {
      super(name);
   }
   
   /**
    * Create a deployment
    * 
    * @param name the name
    * @return the deployment
    * @throws Exception for any error
    */
   protected VFSDeployment createDeployment(String name) throws Exception
   {
      URL url = getClass().getProtectionDomain().getCodeSource().getLocation();
      VirtualFile file = VFS.getRoot(url);
      VFSDeployment deployment = VFSDeploymentFactory.getInstance().createVFSDeployment(file);
      DeploymentFactory factory = new DeploymentFactory();
      factory.addContext(deployment, "");
      ((AbstractDeployment) deployment).setName(name);
      return deployment;
   }

   protected Class<?> assertLoadClass(ClassLoader start, Class<?> reference) throws Exception
   {
      return assertLoadClass(start, reference, start);
   }

   protected Class<?> assertLoadClass(ClassLoader start, Class<?> reference, ClassLoader expected) throws Exception
   {
      Class<?> clazz = start.loadClass(reference.getName());
      if (expected != null)
         assertEquals(expected, clazz.getClassLoader());
      return clazz;
   }

   protected void assertLoadClassFail(ClassLoader start, Class<?> reference) throws Exception
   {
      try
      {
         start.loadClass(reference.getName());
         fail("Should not be here!");
      }
      catch (Exception e)
      {
         checkThrowable(ClassNotFoundException.class, e);
      }
   }

   protected void assertLoadClassIllegal(ClassLoader start, Class<?> reference) throws Exception
   {
      try
      {
         start.loadClass(reference.getName());
         fail("Should not be here!");
      }
      catch (Exception e)
      {
         checkThrowable(IllegalStateException.class, e);
      }
   }

   protected void assertNoClassLoader(DeploymentUnit unit) throws Exception
   {
      try
      {
         unit.getClassLoader();
         fail("Should not be here!");
      }
      catch (Exception e)
      {
         checkThrowable(IllegalStateException.class, e);
      }
   }

   protected static ClassLoadingMetaData addClassLoadingMetaData(Deployment deployment, Version version, Class<?>... packages)
   {
      return addClassLoadingMetaData(deployment, version, false, packages);
   }

   protected static ClassLoadingMetaData addClassLoadingMetaData(Deployment deployment, Version version, boolean useVersionOnPackages, Class<?>... packages)
   {
      ClassLoadingMetaData classLoadingMetaData = createMetaData(deployment, version, useVersionOnPackages, packages);
      addMetaData(deployment, classLoadingMetaData);
      return classLoadingMetaData;
   }

   protected static ClassLoadingMetaData createMetaData(Deployment deployment, Version version, Class<?>... packages)
   {
      return createMetaData(deployment, version, false, packages);
   }

   protected static ClassLoadingMetaData createMetaData(Deployment deployment, Version version, boolean useVersionOnPackages, Class<?>... packages)
   {
      String name = deployment.getName();
      ClassLoadingMetaData classLoadingMetaData = new ClassLoadingMetaData();
      classLoadingMetaData.setName(name);
      classLoadingMetaData.setVersion(version);

      if (packages != null)
      {
         StringBuffer included = new StringBuffer();
         boolean first = true;
         for (Class<?> pkg : packages)
         {
            if (first)
               first = false;
            else
               included.append(",");
            included.append(pkg.getPackage().getName());
         }
         classLoadingMetaData.setIncludedPackages(included.toString());
      }
      
      CapabilitiesMetaData capabilities = classLoadingMetaData.getCapabilities();
      Capability capability = classLoadingMetaDataFactory.createModule(name, version);
      capabilities.addCapability(capability);

      if (packages != null)
      {
         for (Class<?> pkg : packages)
         {
            if (useVersionOnPackages)
               capability = classLoadingMetaDataFactory.createPackage(pkg.getPackage().getName(), version);
            else
               capability = classLoadingMetaDataFactory.createPackage(pkg.getPackage().getName());
            capabilities.addCapability(capability);
         }
      }

      classLoadingMetaData.setCapabilities(capabilities);
      return classLoadingMetaData;
   }

   protected static void addRequireModule(ClassLoadingMetaData classLoadingMetaData, String moduleName, VersionRange versionRange)
   {
      RequirementsMetaData requirements = classLoadingMetaData.getRequirements();

      Requirement requirement = classLoadingMetaDataFactory.createRequireModule(moduleName, versionRange);
      requirements.addRequirement(requirement);
   }

   protected static void addRequirePackage(ClassLoadingMetaData classLoadingMetaData, Class<?> pck, VersionRange versionRange)
   {
      RequirementsMetaData requirements = classLoadingMetaData.getRequirements();

      Requirement requirement = classLoadingMetaDataFactory.createRequirePackage(pck.getPackage().getName(), versionRange);
      requirements.addRequirement(requirement);
   }

   protected static void addMetaData(PredeterminedManagedObjectAttachments attachments, ClassLoadingMetaData md)
   {
      MutableAttachments mutable = (MutableAttachments) attachments.getPredeterminedManagedObjects();
      mutable.addAttachment(ClassLoadingMetaData.class, md);
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
      
      return createMainDeployer(deployer1, deployer2, deployer3);
   }
}
