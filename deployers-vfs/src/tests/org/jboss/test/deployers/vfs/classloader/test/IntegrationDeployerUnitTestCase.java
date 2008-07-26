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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;

import junit.framework.Test;
import org.jboss.deployers.vfs.spi.client.VFSDeployment;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.test.deployers.BootstrapDeployersTest;
import org.jboss.virtual.MemoryFileFactory;
import org.jboss.virtual.VFS;

/**
 * IntegrationDeployerUnitTestCase.
 *
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 */
public class IntegrationDeployerUnitTestCase extends BootstrapDeployersTest
{
   public static Test suite()
   {
      return suite(IntegrationDeployerUnitTestCase.class);
   }

   public IntegrationDeployerUnitTestCase(String name)
   {
      super(name);
   }

   protected void setUp() throws Exception
   {
      VFS.init();

      URL dynamicClassRoot = new URL("vfsmemory", "integration-test", "");
      VFS vfs = MemoryFileFactory.createRoot(dynamicClassRoot);
      System.setProperty("integration.test.url", vfs.getRoot().toURL().toExternalForm());

      URL file = getResource("/org/jboss/test/deployers/vfs/classloader/test/Touch.class");
      assertNotNull(file);
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      InputStream is = file.openStream();
      try
      {
         int read = is.read();
         while(read >= 0)
         {
            baos.write(read);
            read = is.read();
         }
      }
      finally
      {
         is.close();
      }
      MemoryFileFactory.putFile(new URL(dynamicClassRoot.toExternalForm() + "/Touch.class"), baos.toByteArray());

      super.setUp();
   }

   public void testPath() throws Exception
   {
      VFSDeployment deployment = createVFSDeployment("/classloader/integration", "path");
      VFSDeploymentUnit unit = assertDeploy(deployment.getRoot());
      undeploy(unit);
   }

   public void testMetaData() throws Exception
   {
      VFSDeployment deployment = createVFSDeployment("/classloader/integration", "metadata");
      VFSDeploymentUnit unit = assertDeploy(deployment.getRoot());
      undeploy(unit);
   }
}