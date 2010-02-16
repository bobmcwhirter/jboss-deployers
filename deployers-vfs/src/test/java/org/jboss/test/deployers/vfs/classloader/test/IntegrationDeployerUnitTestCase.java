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

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.Executors;

import junit.framework.Test;
import org.jboss.deployers.vfs.spi.client.VFSDeployment;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.test.deployers.BootstrapDeployersTest;
import org.jboss.vfs.TempFileProvider;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VFSUtils;
import org.jboss.vfs.VirtualFile;

/**
 * IntegrationDeployerUnitTestCase.
 *
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 */
public class IntegrationDeployerUnitTestCase extends BootstrapDeployersTest
{
   private Closeable tmpDirHandle;
   
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
      VirtualFile dynamicClassRoot = VFS.getChild("/integration-test");
      tmpDirHandle = VFS.mountTemp(dynamicClassRoot, TempFileProvider.create("test", Executors.newSingleThreadScheduledExecutor()));
      System.setProperty("integration.test.url", dynamicClassRoot.toURL().toExternalForm());
      
      URL file = getResource("/org/jboss/test/deployers/vfs/classloader/test/Touch.class");
      assertNotNull(file);

      File tempFile = dynamicClassRoot.getChild("Touch.class").getPhysicalFile();
      
      FileOutputStream os = new FileOutputStream(tempFile);
      InputStream is = file.openStream();
      try
      {
         int read = is.read();
         while(read >= 0)
         {
            os.write(read);
            read = is.read();
         }
      }
      finally
      {
         VFSUtils.safeClose(is);
         VFSUtils.safeClose(os);
      }
      super.setUp();
   }
   
   protected void tearDown() throws Exception 
   {
      VFSUtils.safeClose(tmpDirHandle);
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