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
package org.jboss.test.deployers.vfs.metadata.test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.deployers.vfs.spi.structure.VFSDeploymentContext;
import org.jboss.test.deployers.BaseDeployersVFSTest;
import org.jboss.test.deployers.vfs.metadata.support.ServicesFilter;
import org.jboss.vfs.VirtualFile;
import org.jboss.vfs.VirtualFileFilter;

/**
 * MetaDataUnitTestCase.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 * @version $Revision: 1.1 $
 */
public class MetaDataUnitTestCase extends BaseDeployersVFSTest
{
   public static Test suite()
   {
      return new TestSuite(MetaDataUnitTestCase.class);
   }
   
   public MetaDataUnitTestCase(String name)
   {
      super(name);
   }

   public void testExactMatchTopLevel() throws Exception
   {
      VFSDeploymentContext context = createDeploymentContext("/metadata", "toplevel/exact/jboss-service.xml");
      String expected = getVfsURL("/metadata/toplevel/exact/jboss-service.xml");
      assertMetaDataMatch(context, expected, "jboss-service.xml");
   }

   public void testNotExactMatchTopLevel() throws Exception
   {
      VFSDeploymentContext context = createDeploymentContext("/metadata", "toplevel/exact/jboss-service.xml");
      assertNoMetaDataMatch(context, "not-correct.xml");
   }

   public void testPartialMatchTopLevel() throws Exception
   {
      VFSDeploymentContext context = createDeploymentContext("/metadata", "toplevel/partial/mydb-ds.xml");
      Set<String> expected = new HashSet<String>();
      expected.add(getVfsURL("/metadata/toplevel/partial/mydb-ds.xml"));
      assertMetaDataMatch(context, expected, null, "-ds.xml");
   }

   public void testNotPartialMatchTopLevel() throws Exception
   {
      VFSDeploymentContext context = createDeploymentContext("/metadata", "toplevel/partial/mydb-ds.xml");
      assertNoMetaDataMatch(context, null, "-not.xml");
   }

   public void testExactMatchMetaInf() throws Exception
   {
      VFSDeploymentContext context = createDeploymentContext("/metadata", "toplevel/metadata.jar");
      context.setMetaDataPath(createMetaDataEntries("META-INF"));
      String expected = getVfsURL("/metadata/toplevel/metadata.jar/META-INF/jboss-service.xml");
      assertMetaDataMatch(context, expected, "jboss-service.xml");
   }

   public void testNotExactMatchMetaInf() throws Exception
   {
      VFSDeploymentContext context = createDeploymentContext("/metadata", "toplevel/metadata.jar");
      context.setMetaDataPath(createMetaDataEntries("META-INF"));
      assertNoMetaDataMatch(context, "not-correct.xml");
   }

   public void testExactMatchNoMetaInf() throws Exception
   {
      VFSDeploymentContext context = createDeploymentContext("/metadata", "toplevel/metadata-nometainf.jar");
      context.setMetaDataPath(createMetaDataEntries("META-INF"));
      assertNoMetaDataMatch(context, "jboss-service.xml");
   }

   public void testNotExactMatchNoMetaInf() throws Exception
   {
      VFSDeploymentContext context = createDeploymentContext("/metadata", "toplevel/metadata-nometainf.jar");
      context.setMetaDataPath(createMetaDataEntries("META-INF"));
      assertNoMetaDataMatch(context, "not-correct.xml");
   }

   public void testPartialMatchMetaInf() throws Exception
   {
      VFSDeploymentContext context = createDeploymentContext("/metadata", "toplevel/metadata.jar");
      context.setMetaDataPath(createMetaDataEntries("META-INF"));
      Set<String> expected = new HashSet<String>();
      expected.add(getVfsURL("/metadata/toplevel/metadata.jar/META-INF/1-ds.xml"));
      expected.add(getVfsURL("/metadata/toplevel/metadata.jar/META-INF/2-ds.xml"));
      assertMetaDataMatch(context, expected, null, "-ds.xml");
   }

   public void testNotPartialMatchMetaInf() throws Exception
   {
      VFSDeploymentContext context = createDeploymentContext("/metadata", "toplevel/metadata.jar");
      context.setMetaDataPath(createMetaDataEntries("META-INF"));
      assertNoMetaDataMatch(context, null, "-not.xml");
   }

   public void testPartialMatchNoMetaInf() throws Exception
   {
      VFSDeploymentContext context = createDeploymentContext("/metadata", "toplevel/metadata-nometainf.jar");
      context.setMetaDataPath(createMetaDataEntries("META-INF"));
      assertNoMetaDataMatch(context, null, "-ds.xml");
   }

   public void testNotPartialMatchNoMetaInf() throws Exception
   {
      VFSDeploymentContext context = createDeploymentContext("/metadata", "toplevel/metadata-nometainf.jar");
      context.setMetaDataPath(createMetaDataEntries("META-INF"));
      assertNoMetaDataMatch(context, null, "-not.xml");
   }

   public void testExactAndPartialMatchMetaInf() throws Exception
   {
      VFSDeploymentContext context = createDeploymentContext("/metadata", "toplevel/metadata.jar");
      context.setMetaDataPath(createMetaDataEntries("META-INF"));
      Set<String> expected = new HashSet<String>();
      expected.add(getVfsURL("/metadata/toplevel/metadata.jar/META-INF/jboss-service.xml"));
      expected.add(getVfsURL("/metadata/toplevel/metadata.jar/META-INF/1-ds.xml"));
      expected.add(getVfsURL("/metadata/toplevel/metadata.jar/META-INF/2-ds.xml"));
      assertMetaDataMatch(context, expected, "jboss-service.xml", "-ds.xml");
   }

   public void testNotExactAndPartialMatchMetaInf() throws Exception
   {
      VFSDeploymentContext context = createDeploymentContext("/metadata", "toplevel/metadata.jar");
      context.setMetaDataPath(createMetaDataEntries("META-INF"));
      assertNoMetaDataMatch(context, "not-correct.xml", "-not.xml");
   }

   public void testExactAndPartialMatchNoMetaInf() throws Exception
   {
      VFSDeploymentContext context = createDeploymentContext("/metadata", "toplevel/metadata-nometainf.jar");
      context.setMetaDataPath(createMetaDataEntries("META-INF"));
      assertNoMetaDataMatch(context, "jboss-service.xml", "-ds.xml");
   }

   public void testNotExactAndPartialMatchNoMetaInf() throws Exception
   {
      VFSDeploymentContext context = createDeploymentContext("/metadata", "toplevel/metadata-nometainf.jar");
      context.setMetaDataPath(createMetaDataEntries("META-INF"));
      assertNoMetaDataMatch(context, "jboss-service.xml", "-not.xml");
   }
   
   public void testRecurseInMetaInf() throws Exception
   {
      VFSDeploymentContext context = createDeploymentContext("/metadata", "toplevel/metadata-recurseinmetainf.jar");
      context.setMetaDataPath(createMetaDataEntries("META-INF"));
      VirtualFileFilter filter = new ServicesFilter();
      Set<String> expected = new HashSet<String>();
      expected.add(getVfsURL("/metadata/toplevel/metadata-recurseinmetainf.jar/META-INF/jboss-service.xml"));
      expected.add(getVfsURL("/metadata/toplevel/metadata-recurseinmetainf.jar/META-INF/services/wsdl.xml"));
      assertMetaDataMatch(context, expected, filter);
   }

   protected void assertMetaDataMatch(VFSDeploymentContext context, String expected, String name) throws Exception
   {
      VirtualFile file = context.getMetaDataFile(name);
      assertNotNull(file);
      assertEquals(expected, file.toURL().toString());
   }
   
   protected void assertNoMetaDataMatch(VFSDeploymentContext context, String name) throws Exception
   {
      VirtualFile file = context.getMetaDataFile(name);
      assertNull(file);
   }
   
   protected void assertMetaDataMatch(VFSDeploymentContext context, Set<String> expected, String name, String suffix) throws Exception
   {
      List<VirtualFile> files = context.getMetaDataFiles(name, suffix);
      assertMetaDataMatch(expected, files);
   }
   
   protected void assertMetaDataMatch(VFSDeploymentContext context, Set<String> expected, VirtualFileFilter filter) throws Exception
   {
      List<VirtualFile> files = context.getMetaDataFiles(filter);
      assertMetaDataMatch(expected, files);
   }

   protected void assertMetaDataMatch(Set<String> expected, List<VirtualFile> files) throws Exception
   {
      Set<String> actual = new HashSet<String>(files.size());
      for (VirtualFile file : files)
         actual.add(file.toURL().toString());
      assertEquals(expected, actual);
   }

   protected void assertNoMetaDataMatch(VFSDeploymentContext context, String name, String suffix) throws Exception
   {
      List<VirtualFile> files = context.getMetaDataFiles(name, suffix);
      assertEmpty(files);
   }

   protected void assertNoMetaDataMatch(VFSDeploymentContext context, VirtualFileFilter filter) throws Exception
   {
      List<VirtualFile> files = context.getMetaDataFiles(filter);
      assertEmpty(files);
   }
}
