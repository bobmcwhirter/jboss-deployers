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
package org.jboss.test.deployers.structure.test;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.jboss.deployers.plugins.attachments.AttachmentsImpl;
import org.jboss.deployers.plugins.structure.ClassPathEntryImpl;
import org.jboss.deployers.plugins.structure.ContextInfoImpl;
import org.jboss.deployers.spi.structure.ClassPathEntry;
import org.jboss.deployers.spi.structure.ContextInfo;
import org.jboss.test.deployers.structure.AbstractContextInfoTest;

/**
 * ContextInfoImplUnitTestCase.
 * 
 * @author <a href="adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class ContextInfoImplUnitTestCase extends AbstractContextInfoTest
{
   public static Test suite()
   {
      return new TestSuite(ContextInfoImplUnitTestCase.class);
   }
   
   public ContextInfoImplUnitTestCase(String name)
   {
      super(name);
   }

   @Override
   protected ContextInfoImpl createDefault()
   {
      return new ContextInfoImpl();
   }

   @Override
   protected ContextInfoImpl createPath(String path)
   {
      return new ContextInfoImpl(path);
   }

   @Override
   protected ContextInfoImpl createPathAndClassPath(String path, List<ClassPathEntry> classPath)
   {
      return new ContextInfoImpl(path, classPath);
   }
   
   @Override
   protected ContextInfo createPathAndMetaDataAndClassPath(String path, String metaDataPath, List<ClassPathEntry> classPath)
   {
      return new ContextInfoImpl(path, metaDataPath, classPath);
   }

   @Override
   protected ContextInfo createPathAndMetaDataAndClassPath(String path, List<String> metaDataPath, List<ClassPathEntry> classPath)
   {
      return new ContextInfoImpl(path, metaDataPath, classPath);
   }

   @Override
   protected ClassPathEntry createClassPathEntry(String path)
   {
      return new ClassPathEntryImpl(path);
   }
   
   public void testSetPath()
   {
      ContextInfoImpl context = createDefault();
      assertEquals("", context.getPath());
      assertDefaultNonPath(context);
      
      context.setPath("path");
      assertEquals("path", context.getPath());
      assertDefaultNonPath(context);

      context.setPath("changed");
      assertEquals("changed", context.getPath());
      assertDefaultNonPath(context);
   }
   
   public void testSetPathErrors()
   {
      ContextInfoImpl context = createDefault();
      assertEquals("", context.getPath());
      assertDefaultNonPath(context);

      try
      {
         context.setPath(null);
         fail("Should not be here!");
      }
      catch (Exception e)
      {
         checkThrowable(IllegalArgumentException.class, e);
      }
   }
   
   public void testAddMetaDataPath()
   {
      ContextInfoImpl context = createDefault();
      assertEquals("", context.getPath());
      assertDefaultNonPath(context);
      
      context.addMetaDataPath("metaDataPath");
      assertDefaultMetaDataPath(context.getMetaDataPath());
      assertDefaultClassPath(context.getClassPath());

      context.addMetaDataPath("added");
      assertEquals(Arrays.asList("metaDataPath", "added"), context.getMetaDataPath());
      assertDefaultClassPath(context.getClassPath());
   }
   
   public void testAddToDefaultMetaDataPath()
   {
      List<ClassPathEntry> classPath = new ArrayList<ClassPathEntry>();
      classPath.add(new ClassPathEntryImpl());
      ContextInfo context = createPathAndMetaDataAndClassPath("", "metaDataPath", classPath);
      assertEquals("", context.getPath());
      assertDefaultMetaDataPath(context.getMetaDataPath());
      assertDefaultClassPath(context.getClassPath());

      context.addMetaDataPath("added");
      assertEquals(Arrays.asList("metaDataPath", "added"), context.getMetaDataPath());
      assertDefaultClassPath(context.getClassPath());
   }

   public void testSetMetaDataPathErrors()
   {
      ContextInfoImpl context = createDefault();
      assertEquals("", context.getPath());
      assertDefaultNonPath(context);

      try
      {
         context.addMetaDataPath(null);
         fail("Should not be here!");
      }
      catch (Exception e)
      {
         checkThrowable(IllegalArgumentException.class, e);
      }
   }
   
   public void testSetClassPath()
   {
      ContextInfoImpl context = createDefault();
      assertEquals("", context.getPath());
      assertDefaultNonPath(context);

      List<ClassPathEntry> classPath = createClassPath("classPath");
      context.setClassPath(classPath);
      assertEquals("", context.getPath());
      assertDefaultMetaDataPath(context);
      assertEquals(classPath, context.getClassPath());
      
      classPath = createClassPath("changed");
      context.setClassPath(classPath);
      assertEquals("", context.getPath());
      assertDefaultMetaDataPath(context);
      assertEquals(classPath, context.getClassPath());
      
      classPath = null;
      context.setClassPath(classPath);
      assertEquals("", context.getPath());
      assertDefaultMetaDataPath(context);
      assertEquals(classPath, context.getClassPath());
   }

   public void testPredeterminedManagedObjectAttachments()
   {
      ContextInfoImpl context = createDefault();
      assertEquals("", context.getPath());
      AttachmentsImpl ai = new AttachmentsImpl();
      ai.addAttachment("key1", "testPredeterminedManagedObjectAttachments");
      context.setPredeterminedManagedObjects(ai);
      String a1 = context.getPredeterminedManagedObjects().getAttachment("key1", String.class);
      assertEquals("key1 attachment", "testPredeterminedManagedObjectAttachments", a1);
   }

}
