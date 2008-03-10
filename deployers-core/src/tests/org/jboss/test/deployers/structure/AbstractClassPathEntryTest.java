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

import org.jboss.deployers.spi.structure.ClassPathEntry;
import org.jboss.test.BaseTestCase;

/**
 * AbstractClassPathEntryTest.
 * 
 * @author <a href="adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public abstract class AbstractClassPathEntryTest extends BaseTestCase
{
   public AbstractClassPathEntryTest(String name)
   {
      super(name);
   }
   
   protected abstract ClassPathEntry createDefault();
   
   public void testConstructorDefault()
   {
      ClassPathEntry entry = createDefault();
      assertEquals("", entry.getPath());
      assertNull(entry.getSuffixes());
   }
   
   protected abstract ClassPathEntry createPath(String path);
   
   public void testConstructorPath()
   {
      ClassPathEntry entry = createPath("path");
      assertEquals("path", entry.getPath());
      assertNull(entry.getSuffixes());
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
   
   protected abstract ClassPathEntry createPathAndSuffixes(String path, String suffixes);

   public void testConstructorPathAndSuffixes()
   {
      ClassPathEntry entry = createPathAndSuffixes("path", "suffixes");
      assertEquals("path", entry.getPath());
      assertEquals("suffixes", entry.getSuffixes());

      entry = createPathAndSuffixes("path", null);
      assertEquals("path", entry.getPath());
      assertNull(entry.getSuffixes());
   }
   
   public void testConstructorErrorsPathAndSuffixes()
   {
      try
      {
         createPathAndSuffixes(null, "suffixes");
         fail("Should not be here");
      }
      catch (Exception e)
      {
         checkThrowable(IllegalArgumentException.class, e);
      }
   }
   
   public void testEqualsAndHashCode()
   {
      ClassPathEntry one = createDefault();
      ClassPathEntry two = createDefault();
      assertEquals(one, two);
      assertEquals(two, one);
      assertEquals(one.hashCode(), two.hashCode());
      
      two = createPathAndSuffixes("", null);
      assertEquals(one, two);
      assertEquals(two, one);
      assertEquals(one.hashCode(), two.hashCode());
      
      one = createPathAndSuffixes("path", "suffixes");
      two = createPathAndSuffixes("path", "suffixes");
      assertEquals(one, two);
      assertEquals(two, one);
      assertEquals(one.hashCode(), two.hashCode());
      
      one = createPathAndSuffixes("path", "suffixes");
      two = createPathAndSuffixes("not-path", "suffixes");
      assertNotSame(one, two);
      assertNotSame(two, one);
      assertNotSame(one.hashCode(), two.hashCode());
      
      one = createPathAndSuffixes("path", "suffixes");
      two = createPathAndSuffixes("Path", "not-suffixes");
      assertNotSame(one, two);
      assertNotSame(two, one);
      assertNotSame(one.hashCode(), two.hashCode());
      
      one = createPathAndSuffixes("path", "suffixes");
      two = createPathAndSuffixes("Path", null);
      assertNotSame(one, two);
      assertNotSame(two, one);
      assertNotSame(one.hashCode(), two.hashCode());
   }
   
   public void testSerialization() throws Exception
   {
      ClassPathEntry entry = createDefault();
      assertEquals("", entry.getPath());
      assertNull(entry.getSuffixes());

      entry = serializeDeserialize(entry, ClassPathEntry.class);
      assertEquals("", entry.getPath());
      assertNull(entry.getSuffixes());

      entry = createPathAndSuffixes("path", "suffixes");
      assertEquals("path", entry.getPath());
      assertEquals("suffixes", entry.getSuffixes());

      entry = serializeDeserialize(entry, ClassPathEntry.class);
      assertEquals("path", entry.getPath());
      assertEquals("suffixes", entry.getSuffixes());
   }
}
