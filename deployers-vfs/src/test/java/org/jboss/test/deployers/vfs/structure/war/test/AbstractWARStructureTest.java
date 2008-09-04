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
package org.jboss.test.deployers.vfs.structure.war.test;

import java.util.List;

import org.jboss.deployers.vfs.spi.structure.VFSDeploymentContext;
import org.jboss.test.deployers.vfs.structure.AbstractStructureTest;
import org.jboss.virtual.VirtualFile;

/**
 * AbstractWARStructureTest.
 * 
 * @author <a href="adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public abstract class AbstractWARStructureTest extends AbstractStructureTest
{
   public AbstractWARStructureTest(String name)
   {
      super(name);
   }

   public void testSimple() throws Throwable
   {
      assertDeployNoChildren("/structure/war/simple", "simple.war");
   }

   public void testNotAnArchive() throws Throwable
   {
      assertNotValid("/structure/war/notanarchive", "notanarchive.war");
   }
   
   public void testWarDirectory() throws Throwable
   {
      assertDeployNoChildren("/structure/war", "directory.war");
   }
   
   public void testDirectoryNotAWar() throws Throwable
   {
      assertNotValid("/structure/war", "directorynotawar");
   }

   public void testMetainfMetadata() throws Throwable
   {
      VFSDeploymentContext war = assertDeploy("/structure/war", "metainf.war");
      VirtualFile persistence = war.getMetaDataFile("persistence.xml");
      assertNotNull(persistence);
   }

   public void testDirectoryWithWebInf() throws Throwable
   {
      VFSDeploymentContext war = assertDeployNoChildren("/structure/war", "directorywithwebinf");
      List<VirtualFile> classpath = war.getClassPath();
      assertNotNull("classpath", classpath);
      assertEquals("classpath.size = 3", 3, classpath.size());
      VirtualFile warFile = war.getRoot();
      VirtualFile classes = warFile.findChild("WEB-INF/classes");
      assertTrue("WEB-INF/classes in classpath", classpath.contains(classes));
      VirtualFile j0 = warFile.findChild("WEB-INF/lib/j0.jar");
      assertTrue("WEB-INF/lib/j0.jar in classpath", classpath.contains(j0));
      VirtualFile j1 = warFile.findChild("WEB-INF/lib/j1.jar");
      assertTrue("WEB-INF/lib/j1.jar in classpath", classpath.contains(j1));
   }
}
