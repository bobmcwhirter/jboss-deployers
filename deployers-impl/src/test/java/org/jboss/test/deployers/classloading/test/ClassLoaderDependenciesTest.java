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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.jboss.classloader.plugins.system.DefaultClassLoaderSystem;
import org.jboss.classloader.spi.ClassLoaderDomain;
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
import org.jboss.deployers.spi.attachments.MutableAttachments;
import org.jboss.deployers.spi.attachments.PredeterminedManagedObjectAttachments;
import org.jboss.deployers.spi.deployer.Deployer;
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
   
   private ClassLoaderSystem system;
   
   protected ClassLoading classLoading;
   
   public static final String NameA = "A";
   public static final String NameB = "B";

   public static final Set<String> NONE = Collections.emptySet();
   public static final Set<String> A = makeSet(NameA);
   public static final Set<String> B = makeSet(NameB);
   public static final Set<String> AB = makeSet(NameA, NameB);
   public static final Set<String> BA = makeSet(NameB, NameA);
   public static final Set<String> BAA = makeSet(NameB, NameA, NameA);
   public static final Set<String> BABA = makeSet(NameB, NameA, NameB, NameA);

   protected static <T> Set<T> makeSet(T... objects)
   {
      Set<T> result = new HashSet<T>();
      for (T object : objects)
         result.add(object);
      return result;
   }

   protected MockClassLoaderDescribeDeployer deployer1;
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
         checkThrowable(ClassNotFoundException.class, e);
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
      addMetaData(attachments, md,  ClassLoadingMetaData.class);
   }
   
   protected static <T> void addMetaData(PredeterminedManagedObjectAttachments attachments, T md, Class<T> expectedType)
   {
      MutableAttachments mutable = (MutableAttachments) attachments.getPredeterminedManagedObjects();
      mutable.addAttachment(expectedType, md);
   }

   protected ClassLoaderDomain assertDomain(String name) throws Exception
   {
      ClassLoaderDomain result = system.getDomain(name);
      assertNotNull("Expected domain " + name, result);
      return result;
   }
   
   protected void assertNoDomain(String name) throws Exception
   {
      assertNull("Did not expect domain " + name, system.getDomain(name));
   }

   protected DeployerClient getMainDeployer(Deployer... deployers)
   {
      classLoading = new ClassLoading();
      system = new DefaultClassLoaderSystem();
      system.getDefaultDomain().setParentPolicy(ParentPolicy.BEFORE_BUT_JAVA_ONLY);

      deployer1 = new MockClassLoaderDescribeDeployer();
      deployer1.setClassLoading(classLoading);

      deployer2 = new MockLevelClassLoaderSystemDeployer();
      deployer2.setClassLoading(classLoading);
      deployer2.setSystem(system);

      if (deployers != null && deployers.length > 0)
      {
         Deployer[] allDeployers = new Deployer[deployers.length + 2];
         allDeployers[0] = deployer1;
         allDeployers[1] = deployer2;
         System.arraycopy(deployers, 0, allDeployers, 2, deployers.length);
         return createMainDeployer(allDeployers);
      }
      
      return createMainDeployer(deployer1, deployer2);
   }
}
