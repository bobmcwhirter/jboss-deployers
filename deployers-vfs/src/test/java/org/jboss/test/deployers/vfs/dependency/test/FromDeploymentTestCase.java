/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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

import java.lang.reflect.Method;

import org.jboss.deployers.plugins.metadata.FromDeploymentValueMetaData;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.test.deployers.BootstrapDeployersTest;
import org.jboss.test.deployers.support.AssembledDirectory;
import org.jboss.test.deployers.vfs.dependency.support.FDTest;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;
import org.jboss.xb.util.JBossXBHelper;

import junit.framework.Test;

/**
 * FromDeploymentTestCase.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class FromDeploymentTestCase extends BootstrapDeployersTest
{
   public FromDeploymentTestCase(String name)
   {
      super(name);
   }

   public static Test suite()
   {
      return suite(FromDeploymentTestCase.class);
   }

   @Override
   @SuppressWarnings("deprecation")
   protected void setUp() throws Exception
   {
      super.setUp();
      JBossXBHelper.addClassBinding("urn:jboss:bean-deployer:deployment:2.0", FromDeploymentValueMetaData.class);
   }

   @Override
   @SuppressWarnings("deprecation")
   protected void tearDown() throws Exception
   {
      JBossXBHelper.removeClassBinding("urn:jboss:bean-deployer:deployment:2.0");
      super.tearDown();
   }

   public void testBasic() throws Throwable
   {
      String appName = "fdAppName.jar";
      VirtualFile file = VFS.getChild(appName);
      AssembledDirectory root = createAssembledDirectory(file);
      root.addPackage(FDTest.class).addPath("/dependency/fd");

      DeploymentUnit unit = assertDeploy(file);
      try
      {
         Object test1 = assertBean("Test1", Object.class);
         assertFromDeployment(test1, unit, appName);

         Object test2 = assertBean("Test2", Object.class);
         assertFromDeployment(test2, unit, appName);
      }
      finally
      {
         undeploy(unit);
      }
   }

   protected static void assertFromDeployment(Object test, DeploymentUnit unit, String appName) throws Exception
   {
      Class<?> clazz = test.getClass();
      Method u = clazz.getMethod("getUnit");
      assertSame(unit, u.invoke(test));
      Method m = clazz.getMethod("getAppName");
      assertEquals(appName, m.invoke(test));
   }
}
