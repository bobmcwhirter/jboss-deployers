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

import java.util.List;

import junit.framework.AssertionFailedError;
import junit.framework.Test;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.test.deployers.BootstrapDeployersTest;

/**
 * DeploymentDependsOnManualClassLoaderUnitTestCase.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class SubDeploymentClassLoaderUnitTestCase extends BootstrapDeployersTest
{
   public static Test suite()
   {
      return suite(SubDeploymentClassLoaderUnitTestCase.class);
   }

   public SubDeploymentClassLoaderUnitTestCase(String name)
   {
      super(name);
   }

   public void testNoSubDeploymentClassLoader() throws Exception
   {
      VFSDeploymentUnit top = assertDeploy("/classloader", "top-sub-no-classloader");
      try
      {
         ClassLoader clTop = getClassLoader(top);
         assertGetResource("test-resource-top-no-classloader", clTop);
         assertGetResource("test-resource-sub-no-classloader", clTop);
         
         DeploymentUnit sub = assertChild(top, "sub/");
         ClassLoader clSub = getClassLoader(sub);
         assertGetResource("test-resource-top-no-classloader", clSub);
         assertGetResource("test-resource-sub-no-classloader", clSub);
         
         assertEquals(clTop, clSub);
      }
      finally
      {
         undeploy(top);
      }
   }

   public void testSubDeploymentClassLoader() throws Exception
   {
      VFSDeploymentUnit top = assertDeploy("/classloader", "top-sub-classloader");
      try
      {
         ClassLoader clTop = getClassLoader(top);
         assertGetResource("test-resource-top-classloader", clTop);
         assertNoResource("test-resource-sub-classloader", clTop);
         
         DeploymentUnit sub = assertChild(top, "sub/");
         ClassLoader clSub = getClassLoader(sub);
         assertGetResource("test-resource-top-classloader", clSub);
         assertGetResource("test-resource-sub-classloader", clSub);

         assertNotSame(clTop, clSub);
      }
      finally
      {
         undeploy(top);
      }
   }
   
   protected DeploymentUnit assertChild(DeploymentUnit parent, String name)
   {
      name = parent.getName() + name;
      List<DeploymentUnit> children = parent.getChildren();
      for (DeploymentUnit child : children)
      {
         if (name.equals(child.getName()))
            return child;
      }
      throw new AssertionFailedError("Child " + name + " not found in " + children);
   }
}
