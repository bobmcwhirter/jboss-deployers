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

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;

import org.jboss.classloading.spi.metadata.ClassLoadingMetaData;
import org.jboss.classloading.spi.metadata.ExportAll;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.test.deployers.BootstrapDeployersTest;
import org.jboss.test.deployers.vfs.classloader.support.a.A;
import org.jboss.test.deployers.vfs.classloader.support.b.B;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;

/**
 * BootstrapDeployersSmokeTestUnitTestCase.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class BootstrapDeployersSmokeTestUnitTestCase extends BootstrapDeployersTest
{
   public static Test suite()
   {
      return suite(BootstrapDeployersSmokeTestUnitTestCase.class);
   }

   public BootstrapDeployersSmokeTestUnitTestCase(String name)
   {
      super(name);
   }
   
   public void testDeployerClient() throws Exception
   {
      assertNotNull(getDeployerClient());
   }
   
   public void testMainDeployersStructure() throws Exception
   {
      assertNotNull(getMainDeployerStructure());
   }
   
   public void testDeployBeans() throws Exception
   {
      VFSDeploymentUnit unit = assertDeploy("/bootstrap", "test");
      try
      {
         List<DeploymentUnit> components = unit.getComponents();
         assertNotNull(components);
         assertEquals(1, components.size());
         DeploymentUnit component = components.get(0);
         assertEquals("Test", component.getName());
         
         assertBean("Test", ArrayList.class);
         
         assertNoResource("META-INF/test-beans.xml", getClass().getClassLoader());
         ClassLoader unitCl = getClassLoader(unit);
         assertGetResource("META-INF/test-beans.xml", unitCl);
         ClassLoader beanCl = getClassLoader("Test");
         assertGetResource("META-INF/test-beans.xml", beanCl);
         
         ClassLoadingMetaData metaData = unit.getAttachment(ClassLoadingMetaData.class);
         assertEquals(unit.getSimpleName(), metaData.getName());
         assertEquals(ExportAll.NON_EMPTY, metaData.getExportAll());
         assertTrue(metaData.isImportAll());
      }
      finally
      {
         undeploy(unit);
      }
   }
   
   public void testClassLoadingMetaData() throws Exception
   {
      VFSDeploymentUnit unit = assertDeploy("/bootstrap", "test-classloader");
      try
      {
         ClassLoadingMetaData metaData = unit.getAttachment(ClassLoadingMetaData.class);
         assertEquals("test-classloading", metaData.getName());
         assertNull(metaData.getExportAll());
         assertFalse(metaData.isImportAll());
      }
      finally
      {
         undeploy(unit);
      }
   }
   
   public void testAssembledDirectory() throws Exception
   {
      VirtualFile dirA = VFS.getChild(getName()).getChild("/assembly");
      createAssembledDirectory(dirA)
         .addPackage(A.class)
         .addPath("/bootstrap/test-assembled");
      VFSDeploymentUnit unitA = assertDeploy(dirA);
      try
      {
         ClassLoader clA = getClassLoader(unitA);
         assertLoadClass(A.class, clA);
         assertLoadClassFail(B.class, clA);
         
         assertNoResource("META-INF/test.xml", getClass().getClassLoader());
         assertGetResource("META-INF/test.xml", clA);
      }
      finally
      {
         undeploy(unitA);
      }
   }
   
   public void testAssembledSubDirectory() throws Exception
   {
      VirtualFile dirA = VFS.getChild(getName()).getChild("/assembly");
      createAssembledDirectory(dirA)
         .addPackage(A.class)
         .addPackage("/b", B.class)
         .addPath("/b", "/bootstrap/test-assembled");
      VFSDeploymentUnit unitA = assertDeploy(dirA);
      try
      {
         ClassLoader clA = getClassLoader(unitA);
         assertLoadClass(A.class, clA);
         assertLoadClass(B.class, clA);
         
         assertNoResource("META-INF/test.xml", getClass().getClassLoader());
         assertGetResource("META-INF/test.xml", clA);
      }
      finally
      {
         undeploy(unitA);
      }
   }
}
