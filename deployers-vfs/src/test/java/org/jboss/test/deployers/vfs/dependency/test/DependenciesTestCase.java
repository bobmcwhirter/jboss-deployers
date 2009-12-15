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
package org.jboss.test.deployers.vfs.dependency.test;

import junit.framework.Test;
import org.jboss.dependency.spi.ControllerContext;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.test.deployers.BootstrapDeployersTest;

/**
 * DependenciesTestCase.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class DependenciesTestCase extends BootstrapDeployersTest
{
   public DependenciesTestCase(String name)
   {
      super(name);
   }

   public static Test suite()
   {
      return suite(DependenciesTestCase.class);
   }

   protected void assertDeployment(DeploymentUnit unit, ControllerState state)
   {
      ControllerContext context = unit.getAttachment(ControllerContext.class);
      assertNotNull(context);
      assertEquals(state, context.getState());
   }

   public void testBasicDependency() throws Throwable
   {
      DeploymentUnit du = addDeployment("/dependency", "basic");
      try
      {
         assertDeployment(du, ControllerState.PRE_INSTALL);
         DeploymentUnit tmDU = assertDeploy("/dependency", "support");
         try
         {
            assertDeployment(du, ControllerState.INSTALLED);
         }
         finally
         {
            undeploy(tmDU);
         }
      }
      finally
      {
         undeploy(du);
      }
   }

   public void testBeanDependency() throws Throwable
   {
      DeploymentUnit du = addDeployment("/dependency", "bean");
      try
      {
         assertDeployment(du, new ControllerState("PreReal"));
         DeploymentUnit tmDU = assertDeploy("/dependency", "support");
         try
         {
            assertDeployment(du, ControllerState.INSTALLED);
         }
         finally
         {
            undeploy(tmDU);
         }
      }
      finally
      {
         undeploy(du);
      }
   }

   public void testModuleAndAliasDependency() throws Throwable
   {
      DeploymentUnit du = addDeployment("/dependency", "module");
      try
      {
         assertDeployment(du, ControllerState.PRE_INSTALL);
         DeploymentUnit aliasDU = assertDeploy("/dependency", "alias");
         try
         {
            assertDeployment(aliasDU, ControllerState.INSTALLED);
            assertDeployment(du, ControllerState.INSTALLED);
         }
         finally
         {
            undeploy(aliasDU);
         }
      }
      finally
      {
         undeploy(du);
      }
   }

   public void testNestedDependency() throws Throwable
   {
      DeploymentUnit du = addDeployment("/dependency", "nested");
      try
      {
         assertDeployment(du, new ControllerState("PreReal"));
         DeploymentUnit tmDU = assertDeploy("/dependency", "support");
         try
         {
            assertDeployment(du, ControllerState.INSTALLED);
         }
         finally
         {
            undeploy(tmDU);
         }
      }
      finally
      {
         undeploy(du);
      }
   }

   public void testBeanRedeploy() throws Throwable
   {
      DeploymentUnit du = addDeployment("/dependency", "bean");
      try
      {
         assertDeployment(du, new ControllerState("PreReal"));
         DeploymentUnit tmDU = assertDeploy("/dependency", "support");
         try
         {
            assertDeployment(du, ControllerState.INSTALLED);

            undeploy(tmDU);

            assertDeployment(du, new ControllerState("PreReal"));

            tmDU = assertDeploy("/dependency", "support");

            assertDeployment(du, ControllerState.INSTALLED);
         }
         finally
         {
            undeploy(tmDU);
         }
      }
      finally
      {
         undeploy(du);
      }
   }

   public void testModuleAndAliasRedeploy() throws Throwable
   {
      DeploymentUnit du = addDeployment("/dependency", "module");
      try
      {
         assertDeployment(du, ControllerState.PRE_INSTALL);
         DeploymentUnit aliasDU = assertDeploy("/dependency", "alias");
         try
         {
            assertDeployment(aliasDU, ControllerState.INSTALLED);
            assertDeployment(du, ControllerState.INSTALLED);

            undeploy(aliasDU);
            assertDeployment(du, ControllerState.PRE_INSTALL);

            aliasDU = assertDeploy("/dependency", "alias");
            assertDeployment(du, ControllerState.INSTALLED);            
         }
         finally
         {
            undeploy(aliasDU);
         }
      }
      finally
      {
         undeploy(du);
      }
   }
}
