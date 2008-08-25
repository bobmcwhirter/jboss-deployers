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
package org.jboss.test.deployers.vfs.structure.jar.test;

import org.jboss.deployers.vfs.spi.structure.VFSDeploymentContext;
import org.jboss.test.deployers.vfs.structure.AbstractStructureTest;

/**
 * AbstractJARStructureTest.
 * 
 * @author <a href="adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public abstract class AbstractJARStructureTest extends AbstractStructureTest
{
   public AbstractJARStructureTest(String name)
   {
      super(name);
   }

   public void testSimple() throws Throwable
   {
      assertDeployNoChildren("/structure/jar", "simple");
   }
   
   public void testRootNotAnArchive() throws Throwable
   {
      assertNotValid("/structure/jar/notanarchive", "NotAnArchive.jar");
      assertNotValid("/structure/jar/notanarchive", "NotAnArchive.zip");
   }
   
   public void testSubdeploymentNotAnArchive() throws Throwable
   {
      assertDeployNoChildren("/structure/jar", "notanarchive");
   }

   public void testJarAsRoot() throws Throwable
   {
      assertDeployNoChildren("/structure/jar/indirectory", "archive.jar");
      assertDeployNoChildren("/structure/jar/indirectory", "archive.zip");
   }
   
   public void testJarInDirectory() throws Throwable
   {
      VFSDeploymentContext context = assertDeploy("/structure/jar", "indirectory");
      assertChildContexts(context, "archive.jar", "archive.zip");
   }
   
   public void testSubdirectoryNotAJar() throws Throwable
   {
      assertDeployNoChildren("/structure/jar", "subdirnotajar");
   }
   
   public void testSubdirectoryIsAJar() throws Throwable
   {
      VFSDeploymentContext context = assertDeploy("/structure/jar", "subdirisajar");
      assertChildContexts(context, "sub.jar");
   }
   
   public void testDirectoryHasMetaInf() throws Throwable
   {
      assertDeployNoChildren("/structure/jar/subdirhasmetainf", "sub");
   }
   
   public void testSubdirectoryHasMetaInf() throws Throwable
   {
      VFSDeploymentContext context = assertDeploy("/structure/jar", "subdirhasmetainf");
      assertChildContexts(context, "sub");
   }
   
   public void testSubdeploymentIsKnownFile() throws Throwable
   {
      assertDeployNoChildren("/structure/file", "simple");
   }
   
   public void testSubdeploymentIsUnknownFile() throws Throwable
   {
      assertDeployNoChildren("/structure/file", "unknown");
   }
}
