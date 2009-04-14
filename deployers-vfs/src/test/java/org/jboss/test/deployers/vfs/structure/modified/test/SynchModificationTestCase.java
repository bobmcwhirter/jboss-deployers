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
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.URI;

import junit.framework.Test;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.deployers.vfs.spi.structure.modified.OverrideSynchAdapter;
import org.jboss.deployers.vfs.spi.structure.modified.StructureModificationChecker;
import org.jboss.deployers.vfs.spi.structure.modified.SynchAdapter;
import org.jboss.test.deployers.vfs.structure.modified.support.XmlIncludeVirtualFileFilter;
import org.jboss.virtual.VirtualFile;
import org.jboss.virtual.VirtualFileFilter;
import org.jboss.virtual.VFSUtils;

/**
 * Test file synch.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class SynchModificationTestCase extends AbstractSynchTest
{
   public SynchModificationTestCase(String name)
   {
      super(name);
   }

   public static Test suite()
   {
      return suite(SynchModificationTestCase.class);
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
            String path = file.getPathName();
            // only wars, but not its classes
            return (path.contains(".war") && path.contains("/WEB-INF") == false);
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
      VFSDeploymentUnit deploymentUnit = assertDeploy(originalRoot);
      try
      {
         VirtualFile tempRoot = deploymentUnit.getRoot();
         StructureModificationChecker checker = createStructureModificationChecker();
         assertFalse(checker.hasStructureBeenModified(originalRoot));

         // add new file
         URI rootURI = VFSUtils.getRealURL(originalRoot).toURI();
         File rootFile = new File(rootURI);
         File newFile = newFile(rootFile, "newfile.txt");
         try
         {
            assertNull(tempRoot.getChild("newfile.txt"));                        
            assertFalse(checker.hasStructureBeenModified(originalRoot));
            assertNotNull(tempRoot.getChild("newfile.txt"));

            // try deleting this one now
            assertTrue(newFile.delete());
            assertFalse(checker.hasStructureBeenModified(originalRoot));
            assertNull(tempRoot.getChild("newfile.txt"));
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
         @SuppressWarnings("deprecation")
         VirtualFile testJsp = tempRoot.findChild("test.jsp");
         long tempTimestamp = testJsp.getLastModified();
         // Platform dependent precision for last modified, let's wait a minimum of 1 sec
         Thread.sleep(1500l);
         assertFalse(checker.hasStructureBeenModified(originalRoot));
         long lastModified = testJsp.getLastModified();
         long diff = lastModified - tempTimestamp;
         assertTrue("Last modified diff is not bigger then 0, diff: " + diff, diff > 0);

         // update something outside recurse filter
         VirtualFile someProps = originalRoot.getChild("WEB-INF/classes/some.properties");
         assertNotNull(someProps);
         updateFile = new File(VFSUtils.getRealURL(someProps).toURI());
         assertTrue(updateFile.exists());
         assertTrue(updateFile.setLastModified(System.currentTimeMillis() + 1500l));
         @SuppressWarnings("deprecation")
         VirtualFile tempProps = tempRoot.findChild("WEB-INF/classes/some.properties");
         tempTimestamp = tempProps.getLastModified();
         // Platform dependent precision for last modified, let's wait a minimum of 1 sec
         Thread.sleep(1500l);
         assertFalse(checker.hasStructureBeenModified(originalRoot));
         assertEquals(tempTimestamp, tempProps.getLastModified());

         // check we don't update for nothing
         @SuppressWarnings("deprecation")
         VirtualFile xhtml = tempRoot.findChild("test.xhtml");
         long xhtmlTimestamp = xhtml.getLastModified();
         assertFalse(checker.hasStructureBeenModified(originalRoot));
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
}