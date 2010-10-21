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
package org.jboss.test.deployers.vfs.classloader.test;

import java.io.IOException;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.test.deployers.BootstrapDeployersTest;
import org.jboss.test.deployers.vfs.classloader.support.CheckResource;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;

import junit.framework.Test;

/**
 * ResourcesUnitTestCase.
 *
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 */
public class ResourcesUnitTestCase extends BootstrapDeployersTest
{
   public ResourcesUnitTestCase(String name) throws IOException
   {
      super(name);
   }

   public static Test suite()
   {
      return suite(ResourcesUnitTestCase.class);
   }

   public void testDelegation() throws Exception
   {
      VirtualFile root = createDeploymentRoot("/classloader/resources", "test.jar");
      DeploymentUnit unit = assertDeploy(root);
      try
      {
         VirtualFile jarFile = VFS.getChild(getName()).getChild("check.jar");
         createAssembledDirectory(jarFile)
            .addPackage(CheckResource.class)
            .addPath("META-INF", "/classloader/resources/check/META-INF");

         DeploymentUnit check = assertDeploy(jarFile);
         undeploy(check);
      }
      finally
      {
         undeploy(unit);
      }
   }
}
