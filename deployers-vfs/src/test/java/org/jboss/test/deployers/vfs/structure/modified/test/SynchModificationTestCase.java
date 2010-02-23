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

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.concurrent.Executors;

import junit.framework.Test;

import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.deployers.vfs.spi.structure.modified.OverrideSynchAdapter;
import org.jboss.deployers.vfs.spi.structure.modified.StructureModificationChecker;
import org.jboss.deployers.vfs.spi.structure.modified.SynchAdapter;
import org.jboss.test.deployers.vfs.structure.modified.support.XmlIncludeVirtualFileFilter;
import org.jboss.vfs.TempFileProvider;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VFSUtils;
import org.jboss.vfs.VirtualFile;
import org.jboss.vfs.VirtualFileFilter;

/**
 * Test file synch.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class SynchModificationTestCase extends AbstractSynchTest
{
   private TempFileProvider tempFileProvider;
   private VirtualFile copiesDir;
   private Closeable copiesDirHandle;
   
   public SynchModificationTestCase(String name)
   {
      super(name);
   }

   public static Test suite()
   {
      return suite(SynchModificationTestCase.class);
   }
   
   

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      tempFileProvider = TempFileProvider.create("temp", Executors.newScheduledThreadPool(2));
   }

   @Override
   protected void tearDown() throws Exception
   {
      VFSUtils.safeClose(copiesDirHandle, tempFileProvider);
      super.tearDown();
   }

   protected VirtualFileFilter createFilter()
   {
      return new XmlIncludeVirtualFileFilter();
   }

   protected VirtualFileFilter createRecurseFilter()
   {
      return new VirtualFileFilter()
      {
         public boolean accepts(VirtualFile file)
         {
            try
            {
               URL url = file.toURL();
               String path = url.toExternalForm();
               // only wars, but not its classes
               return (path.contains(".war") && path.contains("/WEB-INF/classes") == false);
            }
            catch (Exception e)
            {
               throw new RuntimeException(e);
            }
         }
      };
   }

   protected SynchAdapter createSynchAdapter()
   {
      return new OverrideSynchAdapter();
   }

   public void testWAR() throws Exception
   {
      VirtualFile originalRoot = createDeploymentRoot("/synch/war", "simple.war");
      File rootFile = originalRoot.getPhysicalFile();
      
      VirtualFile deploymentCopy = createCopy(originalRoot);
      
      VFSDeploymentUnit deploymentUnit = assertDeploy(deploymentCopy);
      try
      {
         String deploymentName = deploymentUnit.getName();
         
         VirtualFile tempRoot = deploymentUnit.getRoot();
         StructureModificationChecker checker = createStructureModificationChecker();
         assertFalse(checker.hasStructureBeenModified(deploymentName,originalRoot));

         // add new file

         File newFile = newFile(rootFile, "newfile.txt");
         try
         {
            assertFalse(tempRoot.getChild("newfile.txt").exists());                        
            assertFalse(checker.hasStructureBeenModified(deploymentName,originalRoot));
            assertNotNull(tempRoot.getChild("newfile.txt"));

            // try deleting this one now
            assertTrue(newFile.delete());
            assertFalse(checker.hasStructureBeenModified(deploymentName,originalRoot));
            assertFalse(tempRoot.getChild("newfile.txt").exists());
         }
         finally
         {
            if (newFile.exists())
               assertTrue(newFile.delete());
         }

         // update some file
         File updateFile = new File(rootFile, "test.jsp");
         assertTrue(updateFile.exists());
         assertTrue(updateFile.setLastModified(System.currentTimeMillis() + 1500l));
         VirtualFile testJsp = tempRoot.getChild("test.jsp");
         long tempTimestamp = testJsp.getLastModified();
         // Platform dependent precision for last modified, let's wait a minimum of 1 sec
         Thread.sleep(1500l);
         assertFalse(checker.hasStructureBeenModified(deploymentName, originalRoot));
         long lastModified = testJsp.getLastModified();
         long diff = lastModified - tempTimestamp;
         assertTrue("Last modified diff is not bigger then 0, diff: " + diff, diff > 0);

         // update something outside recurse filter
         VirtualFile someProps = originalRoot.getChild("WEB-INF/classes/some.properties");
         assertTrue(someProps.exists());
         updateFile = someProps.getPhysicalFile();
         assertTrue(updateFile.exists());
         assertTrue(updateFile.setLastModified(System.currentTimeMillis() + 1500l));
         VirtualFile tempProps = tempRoot.getChild("WEB-INF/classes/some.properties");
         tempTimestamp = tempProps.getLastModified();
         // Platform dependent precision for last modified, let's wait a minimum of 1 sec
         Thread.sleep(1500l);
         assertFalse(checker.hasStructureBeenModified(deploymentName, originalRoot));
         assertEquals(tempTimestamp, tempProps.getLastModified());

         // add new file into WEB-INF
         VirtualFile webInfo = originalRoot.getChild("WEB-INF");
         File webInfFile = webInfo.getPhysicalFile();
         File newWebInfFile = newFile(webInfFile, "newfile.txt");
         try
         {
            assertFalse(tempRoot.getChild("WEB-INF/newfile.txt").exists());
            assertFalse(checker.hasStructureBeenModified(deploymentName, originalRoot));
            assertTrue(tempRoot.getChild("WEB-INF/newfile.txt").exists());
            assertFalse(checker.hasStructureBeenModified(deploymentName, originalRoot));

            // try deleting this one now
            assertTrue(newWebInfFile.delete());
            assertFalse(checker.hasStructureBeenModified(deploymentName, originalRoot));
            assertFalse(tempRoot.getChild("WEB-INF/newfile.txt").exists());
         }
         finally
         {
            if (newWebInfFile.exists())
               assertTrue(newWebInfFile.delete());
         }

         // check we don't update for nothing
         VirtualFile xhtml = tempRoot.getChild("test.xhtml");
         long xhtmlTimestamp = xhtml.getLastModified();
         // Platform dependent precision for last modified, let's wait a minimum of 1 sec
         Thread.sleep(1500l);
         assertFalse(checker.hasStructureBeenModified(deploymentName, originalRoot));
         assertEquals(xhtmlTimestamp, xhtml.getLastModified());
      }
      finally
      {
         undeploy(deploymentUnit);
      }
   }

   public void testEAR() throws Exception
   {
      VFSDeploymentUnit deploymentUnit = assertDeploy("/synch/ear", "simple.ear");
      try
      {
         VirtualFile root = deploymentUnit.getRoot();
         StructureModificationChecker checker = createStructureModificationChecker();
         assertFalse(checker.hasStructureBeenModified(root));
      }
      finally
      {
         undeploy(deploymentUnit);
      }
   }

   protected File newFile(File parent, String name) throws IOException
   {
      File newFile = new File(parent, name);
      FileOutputStream fos = new FileOutputStream(newFile);
      BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
      try
      {
         writer.write("sometext");
         return newFile;
      }
      finally
      {
         writer.close();
      }
   }
   
   protected VirtualFile getCopiesDir() throws IOException 
   {
      if(copiesDir == null) {
         copiesDir = VFS.getChild("copies");
         copiesDirHandle = VFS.mountTemp(copiesDir, tempFileProvider);
      }
      return copiesDir;
   }
   
   protected VirtualFile createCopy(VirtualFile original) throws IOException 
   {
      VirtualFile copy = getCopiesDir().getChild(original.getName());
      File copyFile = copy.getPhysicalFile();
      copyFile.mkdir();
      VFSUtils.copyChildrenRecursive(original, copy);
      return copy;
   }
}