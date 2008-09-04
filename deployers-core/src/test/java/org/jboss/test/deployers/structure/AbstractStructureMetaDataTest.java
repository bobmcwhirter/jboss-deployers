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

import org.jboss.deployers.spi.structure.ContextInfo;
import org.jboss.deployers.spi.structure.StructureMetaData;

/**
 * AbstractStructureMetaDataTest.
 * 
 * @author <a href="adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public abstract class AbstractStructureMetaDataTest extends AbstractStructureTest
{
   public AbstractStructureMetaDataTest(String name)
   {
      super(name);
   }

   protected abstract StructureMetaData createDefault();

   protected abstract ContextInfo createContext(String path);

   protected List<ContextInfo> getExpected(ContextInfo... expected)
   {
      List<ContextInfo> result = new ArrayList<ContextInfo>();
      if (expected != null)
      {
         for (ContextInfo context : expected)
            result.add(context);
      }
      return result;
   }
   
   public void testConstructorDefault()
   {
      StructureMetaData structure = createDefault();
      assertEmpty(structure.getContexts());
   }
   
   public void testAddContext()
   {
      StructureMetaData structure = createDefault();
      assertEmpty(structure.getContexts());
      
      ContextInfo one = createContext("one");
      structure.addContext(one);
      assertEquals(getExpected(one), structure.getContexts());
      assertEquals(one, structure.getContext("one"));
   }
   
   public void testAddContexts()
   {
      StructureMetaData structure = createDefault();
      assertEmpty(structure.getContexts());
      
      ContextInfo one = createContext("one");
      ContextInfo two = createContext("two");
      ContextInfo three = createContext("three");
      structure.addContext(one);
      structure.addContext(two);
      structure.addContext(three);
      assertEquals(getExpected(one, two, three), structure.getContexts());
      assertEquals(one, structure.getContext("one"));
      assertEquals(two, structure.getContext("two"));
      assertEquals(three, structure.getContext("three"));
   }
   
   public void testAddContextsErrors()
   {
      StructureMetaData structure = createDefault();
      assertEmpty(structure.getContexts());
      
      try
      {
         structure.addContext(null);
         fail("Should not be here!");
      }
      catch (Exception e)
      {
         checkThrowable(IllegalArgumentException.class, e);
      }
      
      ContextInfo one = createContext("one");
      structure.addContext(one);
      try
      {
         structure.addContext(one);
         fail("Should not be here");
      }
      catch (Exception e)
      {
         checkThrowable(IllegalStateException.class, e);
      }

      ContextInfo another = createContext("one");
      try
      {
         structure.addContext(another);
         fail("Should not be here");
      }
      catch (Exception e)
      {
         checkThrowable(IllegalStateException.class, e);
      }
   }
   
   public void testRemoveContext()
   {
      StructureMetaData structure = createDefault();
      assertEmpty(structure.getContexts());
      
      ContextInfo one = createContext("one");
      structure.addContext(one);
      assertEquals(getExpected(one), structure.getContexts());
      assertEquals(one, structure.getContext("one"));
      
      structure.removeContext(one);
      assertEquals(getExpected(), structure.getContexts());
      assertNull(structure.getContext("one"));

      structure.addContext(one);
      assertEquals(getExpected(one), structure.getContexts());
      assertEquals(one, structure.getContext("one"));
   }
   
   public void testRemoveContexts()
   {
      StructureMetaData structure = createDefault();
      assertEmpty(structure.getContexts());
      
      ContextInfo one = createContext("one");
      ContextInfo two = createContext("two");
      ContextInfo three = createContext("three");
      structure.addContext(one);
      structure.addContext(two);
      structure.addContext(three);
      assertEquals(getExpected(one, two, three), structure.getContexts());
      assertEquals(one, structure.getContext("one"));
      assertEquals(two, structure.getContext("two"));
      assertEquals(three, structure.getContext("three"));
      
      structure.removeContext(one);
      assertEquals(getExpected(two, three), structure.getContexts());
      assertNull(structure.getContext("one"));
      assertEquals(two, structure.getContext("two"));
      assertEquals(three, structure.getContext("three"));
      
      structure.removeContext(two);
      assertEquals(getExpected(three), structure.getContexts());
      assertNull(structure.getContext("one"));
      assertNull(structure.getContext("two"));
      assertEquals(three, structure.getContext("three"));
      
      structure.removeContext(three);
      assertEquals(getExpected(), structure.getContexts());
      assertNull(structure.getContext("one"));
      assertNull(structure.getContext("two"));
      assertNull(structure.getContext("three"));

      structure.addContext(one);
      assertEquals(getExpected(one), structure.getContexts());
      assertEquals(one, structure.getContext("one"));
      
      // Removing a context not present is ok
      structure.removeContext(two);
   }
   
   public void testRemoveContextErrors()
   {
      StructureMetaData structure = createDefault();
      assertEmpty(structure.getContexts());
      
      try
      {
         ContextInfo context = null;
         structure.removeContext(context);
         fail("Should not be here!");
      }
      catch (Exception e)
      {
         checkThrowable(IllegalArgumentException.class, e);
      }
   }
   
   public void testRemoveContextByName()
   {
      StructureMetaData structure = createDefault();
      assertEmpty(structure.getContexts());
      
      ContextInfo one = createContext("one");
      structure.addContext(one);
      assertEquals(getExpected(one), structure.getContexts());
      assertEquals(one, structure.getContext("one"));
      
      structure.removeContext("one");
      assertEquals(getExpected(), structure.getContexts());
      assertNull(structure.getContext("one"));

      structure.addContext(one);
      assertEquals(getExpected(one), structure.getContexts());
      assertEquals(one, structure.getContext("one"));
   }
   
   public void testRemoveContextsByName()
   {
      StructureMetaData structure = createDefault();
      assertEmpty(structure.getContexts());
      
      ContextInfo one = createContext("one");
      ContextInfo two = createContext("two");
      ContextInfo three = createContext("three");
      structure.addContext(one);
      structure.addContext(two);
      structure.addContext(three);
      assertEquals(getExpected(one, two, three), structure.getContexts());
      assertEquals(one, structure.getContext("one"));
      assertEquals(two, structure.getContext("two"));
      assertEquals(three, structure.getContext("three"));
      
      structure.removeContext("one");
      assertEquals(getExpected(two, three), structure.getContexts());
      assertNull(structure.getContext("one"));
      assertEquals(two, structure.getContext("two"));
      assertEquals(three, structure.getContext("three"));
      
      structure.removeContext("two");
      assertEquals(getExpected(three), structure.getContexts());
      assertNull(structure.getContext("one"));
      assertNull(structure.getContext("two"));
      assertEquals(three, structure.getContext("three"));
      
      structure.removeContext("three");
      assertEquals(getExpected(), structure.getContexts());
      assertNull(structure.getContext("one"));
      assertNull(structure.getContext("two"));
      assertNull(structure.getContext("three"));

      structure.addContext(one);
      assertEquals(getExpected(one), structure.getContexts());
      assertEquals(one, structure.getContext("one"));
      
      // Removing a context not present is ok
      structure.removeContext("two");
   }
   
   public void testRemoveContextByNameErrors()
   {
      StructureMetaData structure = createDefault();
      assertEmpty(structure.getContexts());
      
      try
      {
         String name = null;
         structure.removeContext(name);
         fail("Should not be here!");
      }
      catch (Exception e)
      {
         checkThrowable(IllegalArgumentException.class, e);
      }
   }
   
   public void testGetContext()
   {
      StructureMetaData structure = createDefault();
      assertEmpty(structure.getContexts());
      
      assertNull(structure.getContext("one"));
      assertNull(structure.getContext("two"));
      
      ContextInfo one = createContext("one");
      ContextInfo two = createContext("two");
      structure.addContext(one);
      structure.addContext(two);

      assertEquals(one, structure.getContext("one"));
      assertEquals(two, structure.getContext("two"));
      
      structure.removeContext("one");
      assertNull(structure.getContext("one"));
      assertEquals(two, structure.getContext("two"));
      
      structure.removeContext("two");
      assertNull(structure.getContext("one"));
      assertNull(structure.getContext("two"));

      structure.addContext(one);
      assertEquals(one, structure.getContext("one"));
      assertNull(structure.getContext("two"));
   }
   
   public void testEqualsAndHashCode()
   {
      StructureMetaData one = createDefault();
      StructureMetaData two = createDefault();
      assertEquals(one, two);
      assertEquals(two, one);
      assertEquals(one.hashCode(), two.hashCode());

      ContextInfo context1 = createContext("one");
      ContextInfo context2 = createContext("two");
      
      one.addContext(context1);
      two.addContext(context1);
      assertEquals(one, two);
      assertEquals(two, one);
      assertEquals(one.hashCode(), two.hashCode());

      one.addContext(context2);
      two.addContext(context2);
      assertEquals(one, two);
      assertEquals(two, one);
      assertEquals(one.hashCode(), two.hashCode());
      one.removeContext(context2);
      two.removeContext(context2);

      ContextInfo another1 = createContext("one");
      two.removeContext(context1);
      two.addContext(another1);
      assertEquals(one, two);
      assertEquals(two, one);
      assertEquals(one.hashCode(), two.hashCode());

      two.removeContext(context1);
      assertNotSame(one, two);
      assertNotSame(two, one);
      assertNotSame(one.hashCode(), two.hashCode());

      two.addContext(context2);
      assertNotSame(one, two);
      assertNotSame(two, one);
      assertNotSame(one.hashCode(), two.hashCode());
      
      one = createDefault();
      two = createDefault();
      one.addContext(context1);
      one.addContext(context2);
      two.addContext(context2);
      two.addContext(context1);
      assertNotSame(one, two);
      assertNotSame(two, one);
      assertNotSame(one.hashCode(), two.hashCode());
   }
   
   public void testSerialization() throws Exception
   {
      StructureMetaData structure = createDefault();
      assertEmpty(structure.getContexts());

      structure = serializeDeserialize(structure, StructureMetaData.class);
      assertEmpty(structure.getContexts());

      ContextInfo one = createContext("one");
      ContextInfo two = createContext("two");
      structure.addContext(one);
      structure.addContext(two);
      assertEquals(getExpected(one, two), structure.getContexts());
      assertEquals(one, structure.getContext("one"));
      assertEquals(two, structure.getContext("two"));
      
      structure = serializeDeserialize(structure, StructureMetaData.class);
      assertEquals(getExpected(one, two), structure.getContexts());
      assertEquals(one, structure.getContext("one"));
      assertEquals(two, structure.getContext("two"));
   }
}
