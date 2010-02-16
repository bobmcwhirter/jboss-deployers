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
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;

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
      VirtualFile topLevel = createTopLevelWithUtil();
      VirtualFile sub = topLevel.getChild("sub.jar");
      createAssembledDirectory(sub)
         .addPackage(UseA.class)
         .addPath("/classloader/manifest/basic");
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
      VirtualFile topLevel = createTopLevelWithUtil();
      VirtualFile sub = topLevel.getChild("sub.jar");
      createAssembledDirectory(sub)
         .addPackage(UseA.class)
         .addPath("/classloader/manifest/scoped");
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
      VirtualFile topLevel = createTopLevelWithUtil();
      VirtualFile sub1 = topLevel.getChild("sub1.jar");
      createAssembledDirectory(sub1)
         .addPackage(UseA.class)
         .addPath("/classloader/manifest/scoped");
      VirtualFile sub2 = topLevel.getChild("sub2.jar");
      createAssembledDirectory(sub2)
         .addPackage(UseA.class)
         .addPath("/classloader/manifest/scoped");
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
      VirtualFile dummyParent = VFS.getChild(getName()).getChild("dummyParent.jar");
      createAssembledDirectory(dummyParent)
         .addPath("/classloader/manifest/dummyparent");
      VFSDeploymentUnit dummy = assertDeploy(dummyParent);
      try
      {
         VirtualFile topLevel = VFS.getChild(getName()).getChild("top-level.jar");
         createAssembledDirectory(topLevel)
            .addPackage("util.jar", A.class)
            .addPath("/classloader/manifest/topscoped");
         VirtualFile sub = topLevel.getChild("sub.jar");
         createAssembledDirectory(sub)
            .addPackage(UseA.class)
            .addPath("/classloader/manifest/scopednotparent");
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
   
   protected VirtualFile createTopLevelWithUtil() throws Exception
   {
      VirtualFile virtualFile = VFS.getChild(getName()).getChild("top-level.jar");
      createAssembledDirectory(virtualFile)
         .addPackage("util.jar", A.class);
      return virtualFile;
   }
}
