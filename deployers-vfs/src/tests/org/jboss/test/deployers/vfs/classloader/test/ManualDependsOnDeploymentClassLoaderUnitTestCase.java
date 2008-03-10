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

import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.test.deployers.BootstrapDeployersTest;

/**
 * ManualDependsOnDeploymentClassLoaderUnitTestCase.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class ManualDependsOnDeploymentClassLoaderUnitTestCase extends BootstrapDeployersTest
{
   public static Test suite()
   {
      return suite(ManualDependsOnDeploymentClassLoaderUnitTestCase.class);
   }

   public ManualDependsOnDeploymentClassLoaderUnitTestCase(String name)
   {
      super(name);
   }

   public void testDependencyCorrectWay() throws Exception
   {
      VFSDeploymentUnit unit3 = assertDeploy("/classloader", "deployment3");
      try
      {
         ClassLoader cl3 = getClassLoader(unit3);
         assertGetResource("a/b/c/test-resource-deployment3", cl3);
         VFSDeploymentUnit unit4 = assertDeploy("/classloader", "deployment4");
         try
         {
            ClassLoader cl4 = assertBean("test4:0.0.0", ClassLoader.class);
            assertGetResource("a/b/c/test-resource-deployment3", cl4);
         }
         finally
         {
            undeploy(unit4);
         }
      }
      finally
      {
         undeploy(unit3);
      }
   }

   public void testDependencyWrongWay() throws Exception
   {
      VFSDeploymentUnit unit4 = addDeployment("/classloader", "deployment4");
      try
      {
         assertNoBean("test4:0:0:0");
         VFSDeploymentUnit unit3 = assertDeploy("/classloader", "deployment3");
         try
         {
            ClassLoader cl3 = getClassLoader(unit3);
            assertGetResource("a/b/c/test-resource-deployment3", cl3);
            ClassLoader cl4 = assertBean("test4:0.0.0", ClassLoader.class);
            assertGetResource("a/b/c/test-resource-deployment3", cl4);
         }
         finally
         {
            undeploy(unit3);
         }
      }
      finally
      {
         undeploy(unit4);
      }
   }

   public void testDependencyRedeploy3() throws Exception
   {
      VFSDeploymentUnit unit3 = assertDeploy("/classloader", "deployment3");
      try
      {
         ClassLoader cl3 = getClassLoader(unit3);
         assertGetResource("a/b/c/test-resource-deployment3", cl3);
         VFSDeploymentUnit unit4 = assertDeploy("/classloader", "deployment4");
         try
         {
            ClassLoader cl4 = assertBean("test4:0.0.0", ClassLoader.class);
            assertGetResource("a/b/c/test-resource-deployment3", cl4);
            undeploy(unit3);
            try
            {
               assertNoBean("test4:0.0.0");
            }
            finally
            {
               unit3 = assertDeploy("/classloader", "deployment3");
            }
            cl3 = getClassLoader(unit3);
            assertGetResource("a/b/c/test-resource-deployment3", cl3);
            cl4 = assertBean("test4:0.0.0", ClassLoader.class);
            assertGetResource("a/b/c/test-resource-deployment3", cl4);
         }
         finally
         {
            undeploy(unit4);
         }
      }
      finally
      {
         undeploy(unit3);
      }
   }

   public void testDependencyRedeploy4() throws Exception
   {
      VFSDeploymentUnit unit3 = assertDeploy("/classloader", "deployment3");
      try
      {
         ClassLoader cl3 = getClassLoader(unit3);
         assertGetResource("a/b/c/test-resource-deployment3", cl3);
         VFSDeploymentUnit unit4 = assertDeploy("/classloader", "deployment4");
         try
         {
            ClassLoader cl4 = assertBean("test4:0.0.0", ClassLoader.class);
            assertGetResource("a/b/c/test-resource-deployment3", cl4);
            undeploy(unit4);
            try
            {
               assertNoBean("test4:0.0.0");
               assertGetResource("a/b/c/test-resource-deployment3", cl3);
            }
            finally
            {
               unit4 = assertDeploy("/classloader", "deployment4");
            }
            cl4 = assertBean("test4:0.0.0", ClassLoader.class);
            assertGetResource("a/b/c/test-resource-deployment3", cl4);
         }
         finally
         {
            undeploy(unit4);
         }
      }
      finally
      {
         undeploy(unit3);
      }
   }
}
