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

import java.lang.reflect.Field;

import junit.framework.Test;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.test.deployers.BootstrapDeployersTest;
import org.jboss.test.deployers.vfs.classloader.support.a.A;
import org.jboss.test.deployers.vfs.classloader.support.usea.UseA;
import org.jboss.virtual.AssembledDirectory;

/**
 * ManifestClassLoaderUnitTestCase.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class ManifestClassLoaderUnitTestCase extends BootstrapDeployersTest
{
   public static Test suite()
   {
      return suite(ManifestClassLoaderUnitTestCase.class);
   }

   public ManifestClassLoaderUnitTestCase(String name)
   {
      super(name);
   }

   public void testBasicManifest() throws Exception
   {
      AssembledDirectory topLevel = createTopLevelWithUtil();
      AssembledDirectory sub = topLevel.mkdir("sub.jar");
      addPackage(sub, UseA.class);
      addPath(sub, "/classloader/manifest/basic", "META-INF");
      VFSDeploymentUnit unit = assertDeploy(topLevel);
      try
      {
         ClassLoader cl = getClassLoader(unit);
         Class<?> expected = assertLoadClass(A.class, cl);
         Class<?> actual = getUsedClass(UseA.class, cl);
         assertClassEquality(expected, actual);
      }
      finally
      {
         undeploy(unit);
      }
   }

   public void testScopedManifest() throws Exception
   {
      AssembledDirectory topLevel = createTopLevelWithUtil();
      AssembledDirectory sub = topLevel.mkdir("sub.jar");
      addPackage(sub, UseA.class);
      addPath(sub, "/classloader/manifest/scoped", "META-INF");
      enableTrace("org.jboss.deployers");
      VFSDeploymentUnit unit = assertDeploy(topLevel);
      try
      {
         ClassLoader cl = getClassLoader(unit);
         Class<?> expected = assertLoadClass(A.class, cl);
         DeploymentUnit subUnit = assertChild(unit, "sub.jar");
         ClassLoader clSub = getClassLoader(subUnit);
         Class<?> actual = getUsedClass(UseA.class, clSub);
         assertClassEquality(expected, actual);
      }
      finally
      {
         undeploy(unit);
      }
   }

   public void testScopedManifests() throws Exception
   {
      AssembledDirectory topLevel = createTopLevelWithUtil();
      AssembledDirectory sub1 = topLevel.mkdir("sub1.jar");
      addPackage(sub1, UseA.class);
      addPath(sub1, "/classloader/manifest/scoped", "META-INF");
      AssembledDirectory sub2 = topLevel.mkdir("sub2.jar");
      addPackage(sub2, UseA.class);
      addPath(sub2, "/classloader/manifest/scoped", "META-INF");
      VFSDeploymentUnit unit = assertDeploy(topLevel);
      try
      {
         ClassLoader cl = getClassLoader(unit);
         Class<?> expected = assertLoadClass(A.class, cl);
         DeploymentUnit sub1Unit = assertChild(unit, "sub1.jar");
         ClassLoader clSub1 = getClassLoader(sub1Unit);
         Class<?> actual = getUsedClass(UseA.class, clSub1);
         assertClassEquality(expected, actual);
         DeploymentUnit sub2Unit = assertChild(unit, "sub2.jar");
         ClassLoader clSub2 = getClassLoader(sub2Unit);
         actual = getUsedClass(UseA.class, clSub2);
         assertClassEquality(expected, actual);
      }
      finally
      {
         undeploy(unit);
      }
   }

   public void testScopedManifestNotParent() throws Exception
   {
      // Dummy parent to create a different parent domain
      AssembledDirectory dummyParent = createAssembledDirectory("dummyParent.jar");
      addPath(dummyParent, "/classloader/manifest/dummyparent", "META-INF");
      VFSDeploymentUnit dummy = assertDeploy(dummyParent);
      try
      {
         AssembledDirectory topLevel = createTopLevelWithUtil();
         addPath(topLevel, "/classloader/manifest/topscoped", "META-INF");
         AssembledDirectory sub = topLevel.mkdir("sub.jar");
         addPackage(sub, UseA.class);
         addPath(sub, "/classloader/manifest/scopednotparent", "META-INF");
         VFSDeploymentUnit unit = assertDeploy(topLevel);
         try
         {
            ClassLoader cl = getClassLoader(unit);
            Class<?> expected = assertLoadClass(A.class, cl);
            DeploymentUnit subUnit = assertChild(unit, "sub.jar");
            ClassLoader clSub = getClassLoader(subUnit);
            Class<?> actual = getUsedClass(UseA.class, clSub);
            assertNoClassEquality(expected, actual);
         }
         finally
         {
            undeploy(unit);
         }
      }
      finally
      {
         undeploy(dummy);
      }
   }

   @SuppressWarnings("unchecked")
   protected Class<?> getUsedClass(Class<?> reference, ClassLoader start) throws Exception
   {
      Class<?> user = assertLoadClass(reference, start);
      Field a = user.getField("used");
      return (Class) a.get(null);
   }
   
   protected AssembledDirectory createTopLevelWithUtil() throws Exception
   {
      AssembledDirectory topLevel = createAssembledDirectory("top-level.jar");
      AssembledDirectory util = topLevel.mkdir("util.jar");
      addPackage(util, A.class);
      return topLevel;
   }
}
