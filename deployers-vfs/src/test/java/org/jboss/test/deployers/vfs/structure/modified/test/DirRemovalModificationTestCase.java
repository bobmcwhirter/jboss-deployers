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
package org.jboss.test.deployers.vfs.structure.modified.test;

import java.io.File;

import junit.framework.Test;
import org.jboss.deployers.structure.spi.main.MainDeployerInternals;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.deployers.vfs.spi.structure.modified.DirModificationCheckerFilter;
import org.jboss.deployers.vfs.spi.structure.modified.MetaDataStructureModificationChecker;
import org.jboss.deployers.vfs.spi.structure.modified.StructureCache;
import org.jboss.deployers.vfs.spi.structure.modified.StructureModificationChecker;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;
import org.jboss.vfs.VirtualFileFilter;

/**
 * Test JBDEPLOY-207, Rob's dir removal.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class DirRemovalModificationTestCase extends StructureModificationTest
{
   private File tmpDir;

   public DirRemovalModificationTestCase(String name)
   {
      super(name);
   }

   public static Test suite()
   {
      return suite(DirRemovalModificationTestCase.class);
   }

   @Override
   protected void tearDown() throws Exception
   {
      try
      {
         if (tmpDir != null)
            assertTrue(tmpDir.delete());
      }
      finally
      {
         super.tearDown();
      }
   }

   protected StructureModificationChecker createStructureModificationChecker(MainDeployerInternals mainDeployerInternals, VirtualFileFilter filter)
   {
      MetaDataStructureModificationChecker structureModificationChecker = new MetaDataStructureModificationChecker(mainDeployerInternals);
      structureModificationChecker.setCache(createStructureCache());
      structureModificationChecker.setFilter(filter);
      structureModificationChecker.setRootFilter(new DirModificationCheckerFilter());
      return structureModificationChecker;
   }

   protected StructureCache<Long> createStructureCache()
   {
      return null; // use default
   }

   protected VirtualFileFilter createFilter()
   {
      return null;
   }

   @Override
   protected VirtualFile makeRoot() throws Exception
   {
      File tmpFile = File.createTempFile("jbdeploy207", ".tmp");
      tmpFile.deleteOnExit();
      File tmpParent = tmpFile.getParentFile();
      tmpDir = new File(tmpParent, "tmp-dir");
      assertTrue(tmpDir.mkdir());
      return VFS.getChild(tmpDir.toURI());
   }

   protected void testStructureModified(VirtualFile root, StructureModificationChecker checker, VFSDeploymentUnit deploymentUnit) throws Exception
   {
      assertFalse("Structure should not be modified.", checker.hasStructureBeenModified(root));

      Thread.sleep(1500); // wait more than 1000ms, which is OS offset

      File parentDir = tmpDir.getParentFile();
      assertTrue(tmpDir.delete());
      tmpDir = new File(parentDir, "tmp-dir");
      assertTrue(tmpDir.mkdir());
      root = VFS.getChild(tmpDir.toURI());

      assertTrue("We created new directory, expecting modified root.", checker.hasStructureBeenModified(root));
   }
}