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

import java.net.URL;

import junit.framework.Test;

import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.test.deployers.BootstrapDeployersTest;

/**
 * DeploymentDependsOnManualClassLoaderUnitTestCase.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class DeploymentDependsOnManualClassLoaderUnitTestCase extends BootstrapDeployersTest
{
   public static Test suite()
   {
      return suite(DeploymentDependsOnManualClassLoaderUnitTestCase.class);
   }

   public DeploymentDependsOnManualClassLoaderUnitTestCase(String name)
   {
      super(name);
   }
   
   protected void setUp() throws Exception
   {
      super.setUp();
      URL classLoaderResource = getResource("/classloader");
      System.setProperty("test.classloader.url", classLoaderResource.toString());
   }

   public void testDependencyCorrectWay() throws Exception
   {
      VFSDeploymentUnit unit2 = assertDeploy("/classloader", "deployment2");
      try
      {
         ClassLoader cl2 = assertBean("test2:0.0.0", ClassLoader.class);
         assertGetResource("a/b/c/test-resource-external-deployment2", cl2);
         VFSDeploymentUnit unit1 = assertDeploy("/classloader", "deployment1");
         try
         {
            ClassLoader cl1 = getClassLoader(unit1);
            assertGetResource("a/b/c/test-resource-external-deployment2", cl1);
         }
         finally
         {
            undeploy(unit1);
         }
      }
      finally
      {
         undeploy(unit2);
      }
   }

   public void testDependencyWrongWay() throws Exception
   {
      VFSDeploymentUnit unit1 = addDeployment("/classloader", "deployment1");
      try
      {
         assertNoClassLoader(unit1);
         VFSDeploymentUnit unit2 = assertDeploy("/classloader", "deployment2");
         try
         {
            ClassLoader cl2 = assertBean("test2:0.0.0", ClassLoader.class);
            assertGetResource("a/b/c/test-resource-external-deployment2", cl2);
         }
         finally
         {
            undeploy(unit2);
         }
      }
      finally
      {
         undeploy(unit1);
      }
   }

   public void testDependencyRedeploy1() throws Exception
   {
      VFSDeploymentUnit unit2 = assertDeploy("/classloader", "deployment2");
      try
      {
         ClassLoader cl2 = assertBean("test2:0.0.0", ClassLoader.class);
         assertGetResource("a/b/c/test-resource-external-deployment2", cl2);
         VFSDeploymentUnit unit1 = assertDeploy("/classloader", "deployment1");
         try
         {
            ClassLoader cl1 = getClassLoader(unit1);
            assertGetResource("a/b/c/test-resource-external-deployment2", cl1);
            undeploy(unit1);
            try
            {
               assertNoClassLoader(unit1);
            }
            finally
            {
               unit1 = assertDeploy("/classloader", "deployment1");
            }
            cl1 = getClassLoader(unit1);
            assertGetResource("a/b/c/test-resource-external-deployment2", cl1);
         }
         finally
         {
            undeploy(unit1);
         }
      }
      finally
      {
         undeploy(unit2);
      }
   }

   public void testDependencyRedeploy2() throws Exception
   {
      VFSDeploymentUnit unit2 = assertDeploy("/classloader", "deployment2");
      try
      {
         ClassLoader cl2 = assertBean("test2:0.0.0", ClassLoader.class);
         assertGetResource("a/b/c/test-resource-external-deployment2", cl2);
         VFSDeploymentUnit unit1 = assertDeploy("/classloader", "deployment1");
         try
         {
            ClassLoader cl1 = getClassLoader(unit1);
            assertGetResource("a/b/c/test-resource-external-deployment2", cl1);
            undeploy(unit2);
            try
            {
               assertNoClassLoader(unit1);
            }
            finally
            {
               unit2 = assertDeploy("/classloader", "deployment2");
            }
            cl1 = getClassLoader(unit1);
            assertGetResource("a/b/c/test-resource-external-deployment2", cl1);
            cl2 = assertBean("test2:0.0.0", ClassLoader.class);
            assertGetResource("a/b/c/test-resource-external-deployment2", cl2);
         }
         finally
         {
            undeploy(unit1);
         }
      }
      finally
      {
         undeploy(unit2);
      }
   }
}
