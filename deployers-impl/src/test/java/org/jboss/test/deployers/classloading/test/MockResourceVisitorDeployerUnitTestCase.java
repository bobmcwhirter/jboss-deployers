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

import java.util.HashSet;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.jboss.classloader.plugins.ClassLoaderUtils;
import org.jboss.classloading.spi.visitor.ResourceContext;
import org.jboss.classloading.spi.visitor.ResourceFilter;
import org.jboss.classloading.spi.visitor.ResourceVisitor;
import org.jboss.deployers.client.spi.DeployerClient;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.plugins.classloading.AbstractResourceVisitorDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.test.deployers.classloading.support.a.A;

/**
 * MockResourceVisitorDeployerUnitTestCase.
 *
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 */
public class MockResourceVisitorDeployerUnitTestCase extends ClassLoaderDependenciesTest
{
   private final Set<String> resources = new HashSet<String>();
   private ResourceVisitor visitor = new ResourceVisitor()
   {
      public ResourceFilter getFilter()
      {
         return null;
      }

      public void visit(ResourceContext resource)
      {
         resources.add(resource.getResourceName());
      }
   };

   public static Test suite()
   {
      return new TestSuite(MockResourceVisitorDeployerUnitTestCase.class);
   }

   public MockResourceVisitorDeployerUnitTestCase(String name)
   {
      super(name);
   }

   public void testSimpleResourceVisitor() throws Exception
   {
      DeployerClient deployer = getMainDeployer(new AbstractResourceVisitorDeployer(visitor));
      testResourceVisitor(deployer);
   }

   public void testCreateResourceVisitor() throws Exception
   {
      DeployerClient deployer = getMainDeployer(new AbstractResourceVisitorDeployer()
      {
         protected ResourceVisitor createVisitor(DeploymentUnit unit)
         {
            return visitor;
         }
      });
      testResourceVisitor(deployer);
   }

   public void testResourceVisitorWithFilter() throws Exception
   {
      DeployerClient deployer = getMainDeployer(new AbstractResourceVisitorDeployer()
      {
         protected ResourceVisitor createVisitor(DeploymentUnit unit)
         {
            return visitor;
         }

         protected ResourceFilter createFilter(DeploymentUnit unit)
         {
            return new ResourceFilter()
            {
               public boolean accepts(ResourceContext resource)
               {
                  return resource.getResourceName().equals(ClassLoaderUtils.packageNameToPath(A.class.getName()));
               }
            };
         }
      });
      testResourceVisitor(deployer);
   }

   protected void testResourceVisitor(DeployerClient deployer) throws Exception
   {
      Deployment deployment = createSimpleDeployment(NameA);
      addClassLoadingMetaData(deployment, deployment.getName(), null, A.class);

      DeploymentUnit unit = assertDeploy(deployer, deployment);
      try
      {
         assertEquals(A, deployer2.deployed);
         assertEquals(NONE, deployer2.undeployed);

         ClassLoader cl = unit.getClassLoader();
         assertLoadClass(cl, A.class);

         String expectedPath = ClassLoaderUtils.packageNameToPath(A.class.getName());
         assertEquals(expectedPath, resources.iterator().next());
      }
      finally
      {
         resources.clear();
         assertUndeploy(deployer, deployment);
      }
   }
}