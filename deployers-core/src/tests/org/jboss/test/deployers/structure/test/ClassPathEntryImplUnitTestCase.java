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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.deployers.plugins.structure.ClassPathEntryImpl;
import org.jboss.test.deployers.structure.AbstractClassPathEntryTest;

/**
 * ClassPathEntryImplUnitTestCase.
 * 
 * @author <a href="adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class ClassPathEntryImplUnitTestCase extends AbstractClassPathEntryTest
{
   public static Test suite()
   {
      return new TestSuite(ClassPathEntryImplUnitTestCase.class);
   }
   
   public ClassPathEntryImplUnitTestCase(String name)
   {
      super(name);
   }

   @Override
   protected ClassPathEntryImpl createDefault()
   {
      return new ClassPathEntryImpl();
   }

   @Override
   protected ClassPathEntryImpl createPath(String path)
   {
      return new ClassPathEntryImpl(path);
   }

   @Override
   protected ClassPathEntryImpl createPathAndSuffixes(String path, String suffixes)
   {
      return new ClassPathEntryImpl(path, suffixes);
   }
   
   public void testSetPath()
   {
      ClassPathEntryImpl entry = createDefault();
      assertEquals("", entry.getPath());
      assertNull(entry.getSuffixes());
      
      entry.setPath("path");
      assertEquals("path", entry.getPath());
      assertNull(entry.getSuffixes());

      entry.setPath("changed");
      assertEquals("changed", entry.getPath());
      assertNull(entry.getSuffixes());
   }
   
   public void testSetPathErrors()
   {
      ClassPathEntryImpl entry = createDefault();
      assertEquals("", entry.getPath());
      assertNull(entry.getSuffixes());

      try
      {
         entry.setPath(null);
         fail("Should not be here!");
      }
      catch (Exception e)
      {
         checkThrowable(IllegalArgumentException.class, e);
      }
   }
   
   public void testSetSuffixes()
   {
      ClassPathEntryImpl entry = createDefault();
      assertEquals("", entry.getPath());
      assertNull(entry.getSuffixes());
      
      entry.setSuffixes("suffixes");
      assertEquals("", entry.getPath());
      assertEquals("suffixes", entry.getSuffixes());
      
      entry.setSuffixes("changed");
      assertEquals("", entry.getPath());
      assertEquals("changed", entry.getSuffixes());
      
      entry.setSuffixes(null);
      assertEquals("", entry.getPath());
      assertNull(entry.getSuffixes());
   }
}
