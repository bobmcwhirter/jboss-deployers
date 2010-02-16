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
import org.jboss.test.deployers.support.AssembledDirectory;
import org.jboss.test.deployers.vfs.structure.modified.support.XmlIncludeVirtualFileFilter;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;
import org.jboss.vfs.VirtualFileFilter;

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

      VirtualFile jar = ear.getChild("simple.jar");
      VirtualFile jarMD = jar.getChild("META-INF");

      // 'update' web-beans.xml
      URL url = getResource("/webbeans/simple/jar/META-INF/web-beans.xml");
      assertNotNull(url);
      File file = new File(url.toURI());
      assertTrue(file.setLastModified(System.currentTimeMillis()));
      assertTrue(checker.hasStructureBeenModified(root));
      // should be the same
      assertFalse(checker.hasStructureBeenModified(root));

      AssembledDirectory jarMDAssembly = createAssembledDirectory(jarMD);
      
      // add new xml
      url = getResource("/scanning/smoke/META-INF/jboss-scanning.xml");
      assertNotNull(url);
      jarMDAssembly.add(VFS.getChild(url));
      assertTrue(checker.hasStructureBeenModified(root));
      // should be the same
      assertFalse(checker.hasStructureBeenModified(root));

      // 'remove' new xml
      
      closeAssembly(jarMD);
      jarMDAssembly = createAssembledDirectory(jarMD);
      url = getResource("/dependency/module/META-INF/jboss-dependency.xml");
      assertNotNull(url);
      jarMDAssembly.add(VFS.getChild(url));
      url = getResource("/webbeans/simple/ejb/META-INF/web-beans.xml");
      assertNotNull(url);
      jarMDAssembly.add(VFS.getChild(url));
      assertTrue(checker.hasStructureBeenModified(root));
      // should be the same
      assertFalse(checker.hasStructureBeenModified(root));

      // 'remove' whole metadata dir
      closeAssembly(jarMD);
      jarMDAssembly = createAssembledDirectory(jarMD);
      assertTrue(checker.hasStructureBeenModified(root));
   }

   public void testInitialEmptyDir() throws Exception
   {
      VirtualFile topJar = VFS.getChild(getName()).getChild("top.jar");
      createAssembledDirectory(topJar);
      VirtualFile metaInf = topJar.getChild("META-INF");
      AssembledDirectory metaInfAssembly = createAssembledDirectory(metaInf);
      
      StructureModificationChecker checker = createStructureModificationChecker();

      VFSDeploymentUnit vdu = assertDeploy(topJar);
      try
      {
         VirtualFile root = vdu.getRoot();
         assertFalse(checker.hasStructureBeenModified(root));

         URL url = getResource("/scanning/smoke/META-INF/jboss-scanning.xml");
         assertNotNull(url);
         metaInfAssembly.add(VFS.getChild(url));
         assertTrue(checker.hasStructureBeenModified(root));
      }
      finally
      {
         undeploy(vdu);
      }
   }

   public void testMultipleChanges() throws Exception
   {
      VirtualFile topJar = VFS.getChild(getName()).getChild("top.jar");
      createAssembledDirectory(topJar);
      VirtualFile metaInf = topJar.getChild("META-INF");
      AssembledDirectory metaInfAssembly = createAssembledDirectory(metaInf);
      StructureModificationChecker checker = createStructureModificationChecker();

      VFSDeploymentUnit vdu = assertDeploy(topJar);
      try
      {
         VirtualFile root = vdu.getRoot();
         assertFalse(checker.hasStructureBeenModified(root));

         URL url1 = getResource("/scanning/smoke/META-INF/jboss-scanning.xml");
         assertNotNull(url1);
         metaInfAssembly.add(VFS.getChild(url1));
         URL url2 = getResource("/dependency/module/META-INF/jboss-dependency.xml");
         assertNotNull(url2);
         metaInfAssembly.add(VFS.getChild(url2));

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