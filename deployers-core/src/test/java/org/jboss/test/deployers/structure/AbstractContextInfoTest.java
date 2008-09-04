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

import java.util.List;

import org.jboss.deployers.spi.structure.ClassPathEntry;
import org.jboss.deployers.spi.structure.ContextInfo;

/**
 * AbstractContextInfoTest.
 * 
 * @author <a href="adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public abstract class AbstractContextInfoTest extends AbstractStructureTest
{
   public AbstractContextInfoTest(String name)
   {
      super(name);
   }

   protected abstract ContextInfo createDefault();
   
   public void testConstructorDefault()
   {
      ContextInfo context = createDefault();
      assertDefault(context);
   }
   
   protected abstract ContextInfo createPath(String path);
   
   public void testConstructorPath()
   {
      ContextInfo context = createPath("path");
      assertEquals("path", context.getPath());
      assertDefaultNonPath(context);
   }
   
   public void testConstructorErrorsPath()
   {
      try
      {
         createPath(null);
         fail("Should not be here");
      }
      catch (Exception e)
      {
         checkThrowable(IllegalArgumentException.class, e);
      }
   }
   
   protected abstract ContextInfo createPathAndClassPath(String path, List<ClassPathEntry> classPath);

   @Override
   protected abstract ClassPathEntry createClassPathEntry(String path);

   public void testConstructorPathAndClassPath()
   {
      List<ClassPathEntry> classPath = createClassPath("ClassPath");
      ContextInfo context = createPathAndClassPath("path", classPath);
      assertEquals("path", context.getPath());
      assertDefaultMetaDataPath(context);
      assertEquals(classPath, context.getClassPath());

      classPath = null;
      context = createPathAndClassPath("path", classPath);
      assertEquals("path", context.getPath());
      assertDefaultMetaDataPath(context);
      assertNull(context.getClassPath());
   }
   
   public void testConstructorErrorsPathAndClassPath()
   {
      List<ClassPathEntry> classPath = createClassPath("ClassPath");
      try
      {
         createPathAndClassPath(null, classPath);
         fail("Should not be here");
      }
      catch (Exception e)
      {
         checkThrowable(IllegalArgumentException.class, e);
      }
   }
   
   protected abstract ContextInfo createPathAndMetaDataAndClassPath(String path, String metaDataPath, List<ClassPathEntry> classPath);

   protected abstract ContextInfo createPathAndMetaDataAndClassPath(String path, List<String> metaDataPath, List<ClassPathEntry> classPath);

   protected static void assertDefaultMetaDataPath(List<String> metaDataPath)
   {
      assertEquals(1, metaDataPath.size());
      assertEquals("metaDataPath", metaDataPath.get(0));
   }

   public void testConstructorPathAndMetaDataAndClassPath()
   {
      List<ClassPathEntry> classPath = createClassPath("ClassPath");
      ContextInfo context = createPathAndMetaDataAndClassPath("path", "metaDataPath", classPath);
      assertEquals("path", context.getPath());
      assertDefaultMetaDataPath(context.getMetaDataPath());
      assertEquals(classPath, context.getClassPath());

      classPath = null;
      context = createPathAndMetaDataAndClassPath("path", "metaDataPath", classPath);
      assertEquals("path", context.getPath());
      assertDefaultMetaDataPath(context.getMetaDataPath());
      assertNull(context.getClassPath());
   }
   
   public void testConstructorErrorsPathAndMetaDataPathAndAndClassPath()
   {
      List<ClassPathEntry> classPath = createClassPath("ClassPath");
      try
      {
         createPathAndMetaDataAndClassPath(null, "metaData", classPath);
         fail("Should not be here");
      }
      catch (Exception e)
      {
         checkThrowable(IllegalArgumentException.class, e);
      }

      try
      {
         createPathAndMetaDataAndClassPath("path", (String)null, classPath);
         fail("Should not be here");
      }
      catch (Exception e)
      {
         checkThrowable(IllegalArgumentException.class, e);
      }
   }
   
   public void testAddClassPath()
   {
      ContextInfo context = createPathAndClassPath("", null);
      assertDefaultPath(context);
      assertDefaultMetaDataPath(context);
      assertNull(context.getClassPath());

      ClassPathEntry entry1 = createClassPathEntry("path1");
      context.addClassPathEntry(entry1);
      assertDefaultPath(context);
      assertDefaultMetaDataPath(context);
      assertClassPath(context, entry1);

      ClassPathEntry entry2 = createClassPathEntry("path2");
      context.addClassPathEntry(entry2);
      assertDefaultPath(context);
      assertDefaultMetaDataPath(context);
      assertClassPath(context, entry1, entry2);
   }

   public void testEqualsAndHashCode()
   {
      ContextInfo one = createDefault();
      ContextInfo two = createDefault();
      assertEquals(one, two);
      assertEquals(two, one);
      assertEquals(one.hashCode(), two.hashCode());
      
      List<ClassPathEntry> classPath1 = createClassPath("");
      two = createPathAndClassPath("", classPath1);
      assertEquals(one, two);
      assertEquals(two, one);
      assertEquals(one.hashCode(), two.hashCode());
      
      classPath1 = createClassPath("classPath");
      one = createPathAndMetaDataAndClassPath("path", "metaDataPath", classPath1);
      two = createPathAndMetaDataAndClassPath("path", "metaDataPath", classPath1);
      assertEquals(one, two);
      assertEquals(two, one);
      assertEquals(one.hashCode(), two.hashCode());

      List<ClassPathEntry> classPath2 = createClassPath("classPath");
      one = createPathAndMetaDataAndClassPath("path", "metaDataPath", classPath1);
      two = createPathAndMetaDataAndClassPath("path", "metaDataPath", classPath2);
      assertEquals(one, two);
      assertEquals(two, one);
      assertEquals(one.hashCode(), two.hashCode());
      
      one = createPathAndMetaDataAndClassPath("path", "metaDataPath", classPath1);
      two = createPathAndMetaDataAndClassPath("not-path", "metaDataPath", classPath1);
      assertNotSame(one, two);
      assertNotSame(two, one);
      assertNotSame(one.hashCode(), two.hashCode());
      
      one = createPathAndMetaDataAndClassPath("path", "metaDataPath", classPath1);
      two = createPathAndMetaDataAndClassPath("path", "not-metaDataPath", classPath1);
      assertNotSame(one, two);
      assertNotSame(two, one);
      assertNotSame(one.hashCode(), two.hashCode());
      
      classPath2 = createClassPath("not-classPath");
      one = createPathAndMetaDataAndClassPath("path", "metaDataPath", classPath1);
      two = createPathAndMetaDataAndClassPath("Path", "metaDataPath", classPath2);
      assertNotSame(one, two);
      assertNotSame(two, one);
      assertNotSame(one.hashCode(), two.hashCode());
      
      one = createPathAndMetaDataAndClassPath("path", "metaDataPath", classPath1);
      two = createPathAndMetaDataAndClassPath("Path", "metaDataPath", null);
      assertNotSame(one, two);
      assertNotSame(two, one);
      assertNotSame(one.hashCode(), two.hashCode());

      classPath2 = createClassPath("classPath", "notClassPath");
      one = createPathAndMetaDataAndClassPath("path", "metaDataPath", classPath1);
      two = createPathAndMetaDataAndClassPath("Path", "metaDataPath", classPath2);
      assertNotSame(one, two);
      assertNotSame(two, one);
      assertNotSame(one.hashCode(), two.hashCode());
   }
   
   public void testSerialization() throws Exception
   {
      ContextInfo context = createDefault();
      assertDefault(context);

      context = serializeDeserialize(context, ContextInfo.class);
      assertDefault(context);

      List<ClassPathEntry> classPath = createClassPath("ClassPath");
      context = createPathAndMetaDataAndClassPath("path", "metaDataPath", classPath);
      assertEquals("path", context.getPath());
      assertDefaultMetaDataPath(context.getMetaDataPath());
      assertEquals(classPath, context.getClassPath());

      context = serializeDeserialize(context, ContextInfo.class);
      assertEquals("path", context.getPath());
      assertDefaultMetaDataPath(context.getMetaDataPath());
      assertEquals(classPath, context.getClassPath());
   }
}
