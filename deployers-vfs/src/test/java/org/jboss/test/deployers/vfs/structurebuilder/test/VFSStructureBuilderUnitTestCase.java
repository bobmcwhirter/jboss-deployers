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
package org.jboss.test.deployers.vfs.structurebuilder.test;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.jboss.deployers.client.spi.DeploymentFactory;
import org.jboss.deployers.spi.structure.ClassPathEntry;
import org.jboss.deployers.spi.structure.ContextInfo;
import org.jboss.deployers.structure.spi.DeploymentContext;
import org.jboss.deployers.vfs.plugins.client.AbstractVFSDeployment;
import org.jboss.deployers.vfs.plugins.structure.VFSStructureBuilder;
import org.jboss.deployers.vfs.spi.client.VFSDeployment;
import org.jboss.deployers.vfs.spi.client.VFSDeploymentFactory;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentContext;
import org.jboss.test.deployers.structure.structurebuilder.StructureBuilderTest;
import org.jboss.virtual.VFS;
import org.jboss.virtual.VirtualFile;

/**
 * VFSStructureBuilderUnitTestCase.
 * 
 * @author <a href="adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class VFSStructureBuilderUnitTestCase extends StructureBuilderTest
{
   public static Test suite()
   {
      return new TestSuite(VFSStructureBuilderUnitTestCase.class);
   }
   
   public VFSStructureBuilderUnitTestCase(String name)
   {
      super(name);
   }

   protected VFSStructureBuilder getStructureBuilder()
   {
      return new VFSStructureBuilder();
   }

   protected VFSDeployment createDeployment()
   {
      URL url = getDeploymentURL();
      try
      {
         VirtualFile file = VFS.getRoot(url);
         return new AbstractVFSDeployment(file);
      }
      catch (IOException e)
      {
         throw new RuntimeException("Failed to get virtual file " + url + " for " + getName());
      }
   }

   protected DeploymentFactory getDeploymentFactory()
   {
      return VFSDeploymentFactory.getInstance();
   }

   protected String getDeploymentName()
   {
      return getDeploymentURL().toString();
   }

   protected URL getDeploymentURL()
   {
      String name = "/structurebuilder/predetermined/" + getName();
      URL url = getResource(name);
      if (url == null)
         fail("Unable to find resource: " + name);
      return url;
   }

   protected void checkContextInfo(DeploymentContext context, ContextInfo contextInfo) throws Exception
   {
      VFSDeploymentContext vfsContext = (VFSDeploymentContext) context;
      checkMetaDataLocation(vfsContext, contextInfo);
      checkClassPath(vfsContext, contextInfo);
   }
   
   protected void checkMetaDataLocation(VFSDeploymentContext context, ContextInfo contextInfo) throws Exception
   {
      List<String> metaDataPaths = contextInfo.getMetaDataPath();
      assertNotNull(metaDataPaths);
      List<VirtualFile> metaDataLocations = context.getMetaDataLocations();
      assertNotNull(metaDataLocations);

      if (metaDataPaths.isEmpty())
         assertEmpty(metaDataLocations);
      else
      {
         VirtualFile root = context.getRoot();
         VirtualFile expected = root.findChild(metaDataPaths.get(0));
         assertEquals(1, metaDataLocations.size());
         assertEquals(expected, metaDataLocations.get(0));
      }
   }
   
   protected void checkClassPath(VFSDeploymentContext context, ContextInfo contextInfo) throws Exception
   {
      List<ClassPathEntry> classPathEntries = contextInfo.getClassPath();
      List<VirtualFile> classPath = context.getClassPath();
      
      if (classPathEntries == null)
         assertNull(classPath);
      else
      {
         int cpeSize = classPathEntries.size();
         int cpSize = classPath != null ? classPath.size() : 0;
         assertTrue(cpeSize >= cpSize);
         for(int i = 0; i < cpeSize; i++)
         {
            ClassPathEntry entry = classPathEntries.get(i);
            VirtualFile file = (i < cpSize) ? classPath.get(i) : null;
            String path = entry.getPath();
            if ("".equals(path))
               assertTrue(file == null || "".equals(file.getPathName()));
            else
            {
               assertNotNull(file);
               assertEquals(path, file.getPathName());
            }
         }
      }
   }
}
