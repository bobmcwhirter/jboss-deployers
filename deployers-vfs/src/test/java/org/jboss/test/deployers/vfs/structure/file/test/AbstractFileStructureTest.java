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
package org.jboss.test.deployers.vfs.structure.file.test;

import org.jboss.test.deployers.vfs.structure.AbstractStructureTest;

/**
 * AbstractFileStructureTest.
 * 
 * @author <a href="adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public abstract class AbstractFileStructureTest extends AbstractStructureTest
{
   public AbstractFileStructureTest(String name)
   {
      super(name);
   }
   
   public void testSimple() throws Throwable
   {
      assertDeployNoChildren("/structure/file/simple", "simple-service.xml");
   }
   
   public void testNotKnownButTopLevel() throws Throwable
   {
      assertDeployNoChildren("/structure/file/unknown", "test-unknown.xml");
      assertDeployNoChildren("/structure/file/unknown", "unknown.xml");
   }
   
   public void testNotKnownInSubdirectory() throws Throwable
   {
      assertNotValid("/structure/file", "unknown");
   }

   public void testDirectory() throws Throwable
   {
      assertNotValid("/structure/file", "directory");
   }
}
