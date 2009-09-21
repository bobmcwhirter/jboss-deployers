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
import java.net.URL;

import junit.framework.Test;
import org.jboss.deployers.structure.spi.main.MainDeployerInternals;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.deployers.vfs.spi.structure.modified.MetaDataStructureModificationChecker;
import org.jboss.deployers.vfs.spi.structure.modified.StructureCache;
import org.jboss.deployers.vfs.spi.structure.modified.StructureModificationChecker;
import org.jboss.test.deployers.vfs.structure.modified.support.XmlIncludeVirtualFileFilter;
import org.jboss.virtual.AssembledDirectory;
import org.jboss.virtual.VFS;
import org.jboss.virtual.VirtualFile;
import org.jboss.virtual.VirtualFileFilter;

/**
 * Test StructureModificationChecker.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class MetaDataStructureModificationTestCase extends StructureModificationTest
{
   public MetaDataStructureModificationTestCase(String name)
   {
      super(name);
   }

   public static Test suite()
   {
      return suite(MetaDataStructureModificationTestCase.class);
   }

   protected StructureModificationChecker createStructureModificationChecker(MainDeployerInternals mainDeployerInternals, VirtualFileFilter filter)
   {
      MetaDataStructureModificationChecker structureModificationChecker = new MetaDataStructureModificationChecker(mainDeployerInternals);
      structureModificationChecker.setCache(createStructureCache());
      structureModificationChecker.setFilter(filter);
      return structureModificationChecker;
   }

   protected StructureCache<Long> createStructureCache()
   {
      return null; // use default
   }

   protected VirtualFileFilter createFilter()
   {
      return new XmlIncludeVirtualFileFilter();
   }

   protected void testStructureModified(VirtualFile ear, StructureModificationChecker checker, VFSDeploymentUnit deploymentUnit) throws Exception
   {
      VirtualFile root = deploymentUnit.getRoot();
      // initial run
      assertFalse(checker.hasStructureBeenModified(root));
      // already cached run 
      assertFalse(checker.hasStructureBeenModified(root));

      AssembledDirectory jar = (AssembledDirectory)ear.getChild("simple.jar");
      AssembledDirectory jarMD = (AssembledDirectory)jar.getChild("META-INF");

      // 'update' web-beans.xml
      URL url = getResource("/webbeans/simple/jar/META-INF/web-beans.xml");
      assertNotNull(url);
      File file = new File(url.toURI());
      assertTrue(file.setLastModified(System.currentTimeMillis()));
      assertTrue(checker.hasStructureBeenModified(root));
      // should be the same
      assertFalse(checker.hasStructureBeenModified(root));

      // add new xml
      url = getResource("/scanning/smoke/META-INF/jboss-scanning.xml");
      assertNotNull(url);
      jarMD.addChild(VFS.createNewRoot(url));
      assertTrue(checker.hasStructureBeenModified(root));
      // should be the same
      assertFalse(checker.hasStructureBeenModified(root));

      // 'remove' new xml
      jarMD = jar.mkdir("META-INF");
      url = getResource("/dependency/module/META-INF/jboss-dependency.xml");
      assertNotNull(url);
      jarMD.addChild(VFS.createNewRoot(url));
      url = getResource("/webbeans/simple/ejb/META-INF/web-beans.xml");
      assertNotNull(url);
      jarMD.addChild(VFS.createNewRoot(url));
      assertTrue(checker.hasStructureBeenModified(root));
      // should be the same
      assertFalse(checker.hasStructureBeenModified(root));

      // 'remove' whole metadata dir
      jar.mkdir("META-INF");
      assertTrue(checker.hasStructureBeenModified(root));
   }

   public void testInitialEmptyDir() throws Exception
   {
      AssembledDirectory top = createAssembledDirectory("top.jar", "top.jar");
      AssembledDirectory metainf = top.mkdir("META-INF");
      StructureModificationChecker checker = createStructureModificationChecker();

      VFSDeploymentUnit vdu = assertDeploy(top);
      try
      {
         VirtualFile root = vdu.getRoot();
         assertFalse(checker.hasStructureBeenModified(root));

         URL url = getResource("/scanning/smoke/META-INF/jboss-scanning.xml");
         assertNotNull(url);
         metainf.addChild(VFS.createNewRoot(url));
         assertTrue(checker.hasStructureBeenModified(root));
      }
      finally
      {
         undeploy(vdu);
      }
   }

   public void testMultipleChanges() throws Exception
   {
      AssembledDirectory top = createAssembledDirectory("top.jar", "top.jar");
      AssembledDirectory metainf = top.mkdir("META-INF");
      StructureModificationChecker checker = createStructureModificationChecker();

      VFSDeploymentUnit vdu = assertDeploy(top);
      try
      {
         VirtualFile root = vdu.getRoot();
         assertFalse(checker.hasStructureBeenModified(root));

         URL url1 = getResource("/scanning/smoke/META-INF/jboss-scanning.xml");
         assertNotNull(url1);
         metainf.addChild(VFS.createNewRoot(url1));
         URL url2 = getResource("/dependency/module/META-INF/jboss-dependency.xml");
         assertNotNull(url2);
         metainf.addChild(VFS.createNewRoot(url2));

         assertTrue(checker.hasStructureBeenModified(root));
         assertFalse(checker.hasStructureBeenModified(root));

         File f1 = new File(url1.toURI());
         assertTrue(f1.setLastModified(System.currentTimeMillis() + 1500l));
         File f2 = new File(url2.toURI());
         assertTrue(f2.setLastModified(System.currentTimeMillis() + 1500l));

         assertTrue(checker.hasStructureBeenModified(root));
         assertFalse(checker.hasStructureBeenModified(root));         
      }
      finally
      {
         undeploy(vdu);
      }
   }
}