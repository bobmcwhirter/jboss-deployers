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
package org.jboss.test.deployers.vfs.classloading.test;

import java.lang.reflect.Method;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.test.deployers.BootstrapDeployersTest;
import org.jboss.test.deployers.vfs.classloading.support.MockServlet;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;

import junit.framework.Test;

/**
 * Mock different deployments to check BaseCL caching.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class ClassLoaderCachingTestCase extends BootstrapDeployersTest
{
   public ClassLoaderCachingTestCase(String name)
   {
      super(name);
   }

   public static Test suite()
   {
      return suite(ClassLoaderCachingTestCase.class);
   }

   private static void ping(Object servlet) throws Exception
   {
      Class<?> clazz = servlet.getClass();
      Method ping = clazz.getMethod("ping");
      ping.invoke(servlet);
   }

   public void testWar() throws Exception
   {
      VirtualFile war = VFS.getChild(getName()).getChild("top-level.war");
      createAssembledDirectory(war)
         .addPath("/classloading/cache/web")
         .addPackage("WEB-INF/classes", MockServlet.class);

      DeploymentUnit unit = assertDeploy(war);
      try
      {
         Object servlet = getBean("Servlet");
         assertNotNull(servlet);
         ping(servlet);
         ping(servlet);
      }
      finally
      {
         undeploy(unit);
      }
   }
}