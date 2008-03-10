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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jboss.classloader.plugins.system.DefaultClassLoaderSystem;
import org.jboss.classloader.spi.ClassLoaderSystem;
import org.jboss.classloader.spi.ParentPolicy;
import org.jboss.classloading.spi.dependency.ClassLoading;
import org.jboss.classloading.spi.dependency.policy.mock.MockClassLoadingMetaData;
import org.jboss.classloading.spi.metadata.CapabilitiesMetaData;
import org.jboss.classloading.spi.metadata.Capability;
import org.jboss.classloading.spi.metadata.ClassLoadingMetaData;
import org.jboss.classloading.spi.metadata.ClassLoadingMetaDataFactory;
import org.jboss.classloading.spi.metadata.Requirement;
import org.jboss.classloading.spi.metadata.RequirementsMetaData;
import org.jboss.classloading.spi.version.Version;
import org.jboss.classloading.spi.version.VersionRange;
import org.jboss.deployers.client.spi.DeployerClient;
import org.jboss.deployers.plugins.classloading.AbstractClassLoaderDescribeDeployer;
import org.jboss.deployers.spi.attachments.MutableAttachments;
import org.jboss.deployers.spi.attachments.PredeterminedManagedObjectAttachments;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.test.deployers.AbstractDeployerTest;
import org.jboss.test.deployers.classloading.support.MockClassLoaderDescribeDeployer;
import org.jboss.test.deployers.classloading.support.MockLevelClassLoaderSystemDeployer;

/**
 * ClassLoaderDependenciesTest.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public abstract class ClassLoaderDependenciesTest extends AbstractDeployerTest
{
   private static ClassLoadingMetaDataFactory classLoadingMetaDataFactory = ClassLoadingMetaDataFactory.getInstance();
   
   public static final String NameA = "A";
   public static final String NameB = "B";

   public static final List<String> NONE = Collections.emptyList();
   public static final List<String> A = makeList(NameA);
   public static final List<String> B = makeList(NameB);
   public static final List<String> AB = makeList(NameA, NameB);
   public static final List<String> BA = makeList(NameB, NameA);
   public static final List<String> BAA = makeList(NameB, NameA, NameA);
   public static final List<String> BABA = makeList(NameB, NameA, NameB, NameA);

   @SuppressWarnings("unchecked")
   protected static <T> List<T> makeList(T... objects)
   {
      List<T> result = new ArrayList<T>();
      for (T object : objects)
         result.add(object);
      return result;
   }

   protected AbstractClassLoaderDescribeDeployer deployer1;
   protected MockLevelClassLoaderSystemDeployer deployer2;

   protected ClassLoaderDependenciesTest(String name)
   {
      super(name);
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

   protected static ClassLoadingMetaData addClassLoadingMetaData(PredeterminedManagedObjectAttachments deployment, String name, Version version, Class<?>... packages)
   {
      return addClassLoadingMetaData(deployment, name, version, false, packages);
   }

   protected static ClassLoadingMetaData addClassLoadingMetaData(PredeterminedManagedObjectAttachments deployment, String name, Version version, boolean useVersionOnPackages, Class<?>... packages)
   {
      ClassLoadingMetaData classLoadingMetaData = createMetaData(deployment, name, version, useVersionOnPackages, packages);
      addMetaData(deployment, classLoadingMetaData);
      return classLoadingMetaData;
   }

   protected static ClassLoadingMetaData createMetaData(PredeterminedManagedObjectAttachments deployment, String name, Version version, Class<?>... packages)
   {
      return createMetaData(deployment, name, version, false, packages);
   }

   protected static ClassLoadingMetaData createMetaData(PredeterminedManagedObjectAttachments deployment, String name, Version version, boolean useVersionOnPackages, Class<?>... packages)
   {
      MockClassLoadingMetaData classLoadingMetaData = new MockClassLoadingMetaData(name, version);

      classLoadingMetaData.setPaths(packages);
      
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
      ClassLoading classLoading = new ClassLoading();
      ClassLoaderSystem system = new DefaultClassLoaderSystem();
      system.getDefaultDomain().setParentPolicy(ParentPolicy.BEFORE_BUT_JAVA_ONLY);

      deployer1 = new MockClassLoaderDescribeDeployer();
      deployer1.setClassLoading(classLoading);

      deployer2 = new MockLevelClassLoaderSystemDeployer();
      deployer2.setClassLoading(classLoading);
      deployer2.setSystem(system);

      return createMainDeployer(deployer1, deployer2);
   }
}
