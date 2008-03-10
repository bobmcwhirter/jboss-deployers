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
 * DeploymentDependsOnManualClassLoaderUnitTestCase.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class DeploymentDependsOnDeploymentClassLoaderUnitTestCase extends BootstrapDeployersTest
{
   public static Test suite()
   {
      return suite(DeploymentDependsOnDeploymentClassLoaderUnitTestCase.class);
   }

   public DeploymentDependsOnDeploymentClassLoaderUnitTestCase(String name)
   {
      super(name);
   }

   public void testDependencyCorrectWay() throws Exception
   {
      VFSDeploymentUnit unit5 = assertDeploy("/classloader", "deployment5");
      try
      {
         ClassLoader cl5 = getClassLoader(unit5);
         assertGetResource("a/b/c/test-resource-deployment5", cl5);
         VFSDeploymentUnit unit6 = assertDeploy("/classloader", "deployment6");
         try
         {
            ClassLoader cl6 = getClassLoader(unit6);
            assertGetResource("a/b/c/test-resource-deployment5", cl6);
         }
         finally
         {
            undeploy(unit6);
         }
      }
      finally
      {
         undeploy(unit5);
      }
   }

   public void testDependencyWrongWay() throws Exception
   {
      VFSDeploymentUnit unit6 = addDeployment("/classloader", "deployment6");
      try
      {
         assertNoClassLoader(unit6);
         VFSDeploymentUnit unit5 = assertDeploy("/classloader", "deployment5");
         try
         {
            ClassLoader cl5 = getClassLoader(unit5);
            assertGetResource("a/b/c/test-resource-deployment5", cl5);
            ClassLoader cl6 = getClassLoader(unit6);
            assertGetResource("a/b/c/test-resource-deployment5", cl6);
         }
         finally
         {
            undeploy(unit5);
         }
      }
      finally
      {
         undeploy(unit6);
      }
   }

   public void testDependencyRedeploy5() throws Exception
   {
      VFSDeploymentUnit unit5 = assertDeploy("/classloader", "deployment5");
      try
      {
         ClassLoader cl5 = getClassLoader(unit5);
         assertGetResource("a/b/c/test-resource-deployment5", cl5);
         VFSDeploymentUnit unit6 = assertDeploy("/classloader", "deployment6");
         try
         {
            ClassLoader cl6 = getClassLoader(unit6);
            assertGetResource("a/b/c/test-resource-deployment5", cl6);
            undeploy(unit5);
            try
            {
               assertNoClassLoader(unit5);
               assertNoClassLoader(unit6);
            }
            finally
            {
               unit5 = assertDeploy("/classloader", "deployment5");
            }
            cl5 = getClassLoader(unit5);
            assertGetResource("a/b/c/test-resource-deployment5", cl5);
            cl6 = getClassLoader(unit6);
            assertGetResource("a/b/c/test-resource-deployment5", cl6);
         }
         finally
         {
            undeploy(unit6);
         }
      }
      finally
      {
         undeploy(unit5);
      }
   }

   public void testDependencyRedeploy6() throws Exception
   {
      VFSDeploymentUnit unit5 = assertDeploy("/classloader", "deployment5");
      try
      {
         ClassLoader cl5 = getClassLoader(unit5);
         assertGetResource("a/b/c/test-resource-deployment5", cl5);
         VFSDeploymentUnit unit6 = assertDeploy("/classloader", "deployment6");
         try
         {
            ClassLoader cl6 = getClassLoader(unit6);
            assertGetResource("a/b/c/test-resource-deployment5", cl6);
            undeploy(unit6);
            try
            {
               cl5 = getClassLoader(unit5);
               assertGetResource("a/b/c/test-resource-deployment5", cl5);
               assertNoClassLoader(unit6);
            }
            finally
            {
               unit6 = assertDeploy("/classloader", "deployment6");
            }
            cl5 = getClassLoader(unit5);
            assertGetResource("a/b/c/test-resource-deployment5", cl5);
            cl6 = getClassLoader(unit6);
            assertGetResource("a/b/c/test-resource-deployment5", cl6);
         }
         finally
         {
            undeploy(unit6);
         }
      }
      finally
      {
         undeploy(unit5);
      }
   }
}
