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

import java.util.HashSet;
import java.util.Set;

import junit.framework.Test;

import org.jboss.classloading.spi.dependency.Module;
import org.jboss.classloading.spi.visitor.ResourceContext;
import org.jboss.classloading.spi.visitor.ResourceFilter;
import org.jboss.classloading.spi.visitor.ResourceVisitor;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.test.deployers.BootstrapDeployersTest;

/**
 * DeploymentDependsOnManualClassLoaderUnitTestCase.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class SubDeploymentClassLoaderVisitorUnitTestCase extends BootstrapDeployersTest
{
   public static Test suite()
   {
      return suite(SubDeploymentClassLoaderVisitorUnitTestCase.class);
   }

   public SubDeploymentClassLoaderVisitorUnitTestCase(String name)
   {
      super(name);
   }

   public void testNoSubDeploymentClassLoaderVisit() throws Exception
   {
      VFSDeploymentUnit top = assertDeploy("/classloader", "top-sub-no-classloader");
      try
      {
         TestResourceVisitor test = visit(top);
         test.assertContains("test-resource-top-no-classloader");
         test.assertContains("test-resource-sub-no-classloader");
      }
      finally
      {
         undeploy(top);
      }
   }

   public void testSubDeploymentClassLoaderVisit() throws Exception
   {
      VFSDeploymentUnit top = assertDeploy("/classloader", "top-sub-classloader");
      try
      {
         TestResourceVisitor test = visit(top);
         test.assertContains("test-resource-top-classloader");
         test.assertNotContains("sub/test-resource-sub-classloader");

         DeploymentUnit sub = assertChild(top, "sub/");
         test = visit(sub);
         test.assertNotContains("test-resource-top-classloader");
         test.assertContains("test-resource-sub-classloader");
      }
      finally
      {
         undeploy(top);
      }
   }

   protected TestResourceVisitor visit(DeploymentUnit unit) throws Exception
   {
      Module module = unit.getAttachment(Module.class);
      if (module == null)
         fail("Expected " + unit + " to have a module");
      
      TestResourceVisitor visitor = new TestResourceVisitor();
      module.visit(visitor);
      
      getLog().debug(unit.getName() + " found: " + visitor.resources);
      
      return visitor;
   }
   
   public class TestResourceVisitor implements ResourceVisitor
   {
      private Set<String> resources = new HashSet<String>();
      
      public void assertContains(String resourceName)
      {
         if (resources.contains(resourceName) == false)
            fail(resourceName + " not found in " + resources);
      }
      
      public void assertNotContains(String resourceName)
      {
         if (resources.contains(resourceName))
            fail(resourceName + " unexpectedly found in " + resources);
      }
      
      public ResourceFilter getFilter()
      {
         return null;
      }

      public void visit(ResourceContext resource)
      {
         resources.add(resource.getResourceName());
      }
   }
}
