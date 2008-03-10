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
package org.jboss.test.deployers.structure;

import java.util.ArrayList;
import java.util.List;

import org.jboss.deployers.spi.structure.ClassPathEntry;
import org.jboss.deployers.spi.structure.ContextInfo;
import org.jboss.test.BaseTestCase;

/**
 * AbstractStructureTest.
 * 
 * @author <a href="adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public abstract class AbstractStructureTest extends BaseTestCase
{
   public AbstractStructureTest(String name)
   {
      super(name);
   }

   protected static void assertDefault(ContextInfo contextInfo)
   {
      assertDefaultPath(contextInfo);
      assertDefaultMetaDataPath(contextInfo);
      assertDefaultClassPath(contextInfo.getClassPath());
   }

   protected static void assertDefaultPath(ContextInfo contextInfo)
   {
      assertEquals("", contextInfo.getPath());
   }

   protected static void assertDefaultNonPath(ContextInfo contextInfo)
   {
      assertDefaultMetaDataPath(contextInfo);
      assertDefaultClassPath(contextInfo.getClassPath());
   }

   protected static void assertDefaultMetaDataPath(ContextInfo contextInfo)
   {
      assertNotNull(contextInfo);
      assertNotNull(contextInfo.getMetaDataPath());
      assertTrue(contextInfo.getMetaDataPath().isEmpty());
   }
   
   protected static void assertDefaultClassPath(List<ClassPathEntry> classPath)
   {
      assertNotNull(classPath);
      assertEquals(1, classPath.size());
      ClassPathEntry entry = classPath.get(0);
      assertNotNull(entry);
      assertEquals("", entry.getPath());
      assertNull(entry.getSuffixes());
   }
   
   protected static void assertClassPath(ContextInfo context, ClassPathEntry... paths)
   {
      List<ClassPathEntry> classPath = new ArrayList<ClassPathEntry>();
      if (paths != null)
      {
         for (ClassPathEntry entry : paths)
            classPath.add(entry);
      }
      assertEquals(classPath, context.getClassPath());
   }
   
   protected List<ClassPathEntry> createClassPath(String... paths)
   {
      List<ClassPathEntry> result = new ArrayList<ClassPathEntry>();
      if (paths != null)
      {
         for (String path : paths)
            result.add(createClassPathEntry(path));
      }
      return result;
   }

   protected ClassPathEntry createClassPathEntry(String path)
   {
      throw new UnsupportedOperationException();
   }
}
