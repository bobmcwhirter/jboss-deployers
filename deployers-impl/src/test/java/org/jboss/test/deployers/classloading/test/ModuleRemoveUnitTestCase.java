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

import junit.framework.Test;
import junit.framework.TestSuite;
import org.jboss.classloading.spi.version.Version;
import org.jboss.dependency.spi.Controller;
import org.jboss.dependency.spi.ControllerContext;
import org.jboss.deployers.client.spi.DeployerClient;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.plugins.main.MainDeployerImpl;
import org.jboss.deployers.spi.structure.ContextInfo;
import org.jboss.deployers.structure.spi.DeploymentContext;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.structure.spi.helpers.AbstractDeploymentContext;
import org.jboss.deployers.structure.spi.helpers.AbstractDeploymentUnit;
import org.jboss.deployers.structure.spi.helpers.AbstractStructuralDeployers;
import org.jboss.deployers.structure.spi.helpers.AbstractStructureBuilder;
import org.jboss.test.deployers.classloading.support.a.A;

/**
 * ModuleRemoveUnitTestCase.
 *
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 */
public class ModuleRemoveUnitTestCase extends ClassLoaderDependenciesTest
{
   private Controller controller;

   public static Test suite()
   {
      return new TestSuite(ModuleRemoveUnitTestCase.class);
   }

   public ModuleRemoveUnitTestCase(String name)
   {
      super(name);
   }

   public void testAliasRemove() throws Exception
   {
      DeployerClient mainDeployer = getMainDeployer();
      // change structure builder
      MainDeployerImpl mdi = assertInstanceOf(mainDeployer, MainDeployerImpl.class);
      AbstractStructuralDeployers ads = assertInstanceOf(mdi.getStructuralDeployers(), AbstractStructuralDeployers.class);
      ads.setStructureBuilder(new RenamingStructureBuilder());

      Version v1 = Version.parseVersion("1");
      Deployment ad = createSimpleDeployment("A");
      addClassLoadingMetaData(ad, ad.getName(), v1, true, A.class);

      mainDeployer.deploy(ad);
      try
      {
         assertAlias(true, "A");
      }
      finally
      {
         mainDeployer.undeploy(ad);        
         assertAlias(false, "A");
      }
   }

   public void testAliasRemoveOnChild() throws Exception
   {
      DeployerClient mainDeployer = getMainDeployer();

      Version v1 = Version.parseVersion("1");
      Deployment ad = createSimpleDeployment("A");
      addClassLoadingMetaData(ad, ad.getName(), v1, true, A.class);

      Version v2 = Version.parseVersion("2");
      ContextInfo childContextInfo = addChild(ad, "B");
      addClassLoadingMetaData(childContextInfo, "B", v2, true, A.class);

      mainDeployer.deploy(ad);
      try
      {
         assertAlias(true, "A/B");
      }
      finally
      {
         mainDeployer.undeploy(ad);
         assertAlias(false, "A/B");
      }
   }

   protected void assertAlias(boolean exists, String name) throws Exception
   {
      // this is ugly impl detail
      String controllerId = controller.getClass().getSimpleName() + "[" + System.identityHashCode(controller) + "]";
      ControllerContext alias = controller.getContext(name + "_Alias_" + controllerId, null);
      assertEquals(exists, alias != null);
   }

   private class RenamingStructureBuilder extends AbstractStructureBuilder
   {
      @Override
      protected DeploymentContext createRootDeploymentContext(Deployment deployment) throws Exception
      {
         return new RenamingDeploymentContext("NotA", "");
      }
   }

   private class RenamingDeploymentContext extends AbstractDeploymentContext
   {
      @SuppressWarnings("unused")
      public RenamingDeploymentContext()
      {
      }

      public RenamingDeploymentContext(String name, String relativePath)
      {
         super(name, relativePath);
      }

      @Override
      protected DeploymentUnit createDeploymentUnit()
      {
         return new RenamingDeploymentUnit(this);
      }
   }

   private class RenamingDeploymentUnit extends AbstractDeploymentUnit
   {
      @SuppressWarnings("unused")
      public RenamingDeploymentUnit()
      {
      }

      private RenamingDeploymentUnit(DeploymentContext deploymentContext)
      {
         super(deploymentContext);
      }

      @Override
      public String getName()
      {
         return "A";
      }
   }

   @Override
   protected Controller getController()
   {
      controller = super.getController();
      return controller;
   }
}