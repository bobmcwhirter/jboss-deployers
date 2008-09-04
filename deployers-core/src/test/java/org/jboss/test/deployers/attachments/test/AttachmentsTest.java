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
package org.jboss.test.deployers.attachments.test;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.deployers.spi.attachments.Attachments;
import org.jboss.deployers.spi.attachments.MutableAttachments;
import org.jboss.test.BaseTestCase;

/**
 * Attachments Test.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public abstract class AttachmentsTest extends BaseTestCase
{
   public static Test suite()
   {
      return new TestSuite(AttachmentsTest.class);
   }

   public AttachmentsTest(String name)
   {
      super(name);
   }
   
   protected abstract Attachments getAttachments();
   
   protected abstract MutableAttachments getMutable();

   public void testAddAttachmentErrors() throws Exception
   {
      MutableAttachments mutable = getMutable();
      
      try
      {
         mutable.addAttachment((String) null, "attachment");
         fail("Should not be here!");
      }
      catch (Throwable t)
      {
         checkThrowable(IllegalArgumentException.class, t);
      }
      
      try
      {
         mutable.addAttachment("name", null);
         fail("Should not be here!");
      }
      catch (Throwable t)
      {
         checkThrowable(IllegalArgumentException.class, t);
      }
      
      try
      {
         mutable.addAttachment(null, "attachment", String.class);
         fail("Should not be here!");
      }
      catch (Throwable t)
      {
         checkThrowable(IllegalArgumentException.class, t);
      }
      
      try
      {
         mutable.addAttachment("name", null, String.class);
         fail("Should not be here!");
      }
      catch (Throwable t)
      {
         checkThrowable(IllegalArgumentException.class, t);
      }
      
      try
      {
         mutable.addAttachment("name", "attachment", null);
         fail("Should not be here!");
      }
      catch (Throwable t)
      {
         checkThrowable(IllegalArgumentException.class, t);
      }
      
      try
      {
         mutable.addAttachment((Class<String>) null, "attachment");
         fail("Should not be here!");
      }
      catch (Throwable t)
      {
         checkThrowable(IllegalArgumentException.class, t);
      }
      
      try
      {
         mutable.addAttachment(String.class, null);
         fail("Should not be here!");
      }
      catch (Throwable t)
      {
         checkThrowable(IllegalArgumentException.class, t);
      }
   }
   
   public void testAddAttachmentByName() throws Exception
   {
      MutableAttachments mutable = getMutable();
      ExpectedAttachments expected = new ExpectedAttachments();
      assertAddFreshAttachmentByName(mutable, expected, "name1", "attachment1");
      assertAddFreshAttachmentByName(mutable, expected, "name2", "attachment2");
      
      assertAddReplaceAttachmentByName(mutable, expected, "name1", "different1", "attachment1");
   }

   protected void assertAddFreshAttachmentByName(MutableAttachments mutable, ExpectedAttachments expected, String name, Object attachment)
   {
      assertAddReplaceAttachmentByName(mutable, expected, name, attachment, null);
   }

   protected void assertAddReplaceAttachmentByName(MutableAttachments mutable, ExpectedAttachments expected, String name, Object attachment, Object replaced)
   {
      Object result = assertAddAttachmentByName(mutable, expected, name, attachment);
      assertEquals(replaced, result);
   }

   protected Object assertAddAttachmentByName(MutableAttachments mutable, ExpectedAttachments expected, String name, Object attachment)
   {
      Object result = mutable.addAttachment(name, attachment);
      checkAdd(mutable, expected, name, attachment, result);
      return result;
   }
      
   protected Object checkAdd(Attachments attachments, ExpectedAttachments expected, String name, Object attachment, Object result)
   {
      assertEquals(attachment, attachments.getAttachment(name));
      assertTrue(attachments.isAttachmentPresent(name));
      Object expectedResult = assertExpectedAdd(attachments, expected, name, attachment);
      assertEquals(expectedResult, result);
      return result;
   }
   
   protected Object assertExpectedAdd(Attachments attachments, ExpectedAttachments expected, String name, Object attachment)
   {
      Object result = expected.add(name, attachment);
      assertEquals(expected.expected, attachments.getAttachments());
      return result;
   }
   
   public void testAddAttachmentByNameAndType() throws Exception
   {
      MutableAttachments mutable = getMutable();
      ExpectedAttachments expected = new ExpectedAttachments();
      assertAddFreshAttachmentByNameAndType(mutable, expected, "name1", "attachment1", String.class);
      assertAddFreshAttachmentByNameAndType(mutable, expected, "name2", "attachment2", String.class);
      
      assertAddReplaceAttachmentByNameAndType(mutable, expected, "name1", "different1", "attachment1", String.class);
   }

   protected <T> void assertAddFreshAttachmentByNameAndType(MutableAttachments mutable, ExpectedAttachments expected, String name, T attachment, Class<T> expectedType)
   {
      assertAddReplaceAttachmentByNameAndType(mutable, expected, name, attachment, null, expectedType);
   }

   protected <T> void assertAddReplaceAttachmentByNameAndType(MutableAttachments mutable, ExpectedAttachments expected, String name, T attachment, Object replaced, Class<T> expectedType)
   {
      Object result = assertAddAttachmentByNameAndType(mutable, expected, name, attachment, expectedType);
      assertEquals(replaced, result);
   }

   protected <T> Object assertAddAttachmentByNameAndType(MutableAttachments mutable, ExpectedAttachments expected, String name, T attachment, Class<T> expectedType)
   {
      Object result = mutable.addAttachment(name, attachment, expectedType);
      checkAdd(mutable, expected, name, attachment, result);
      assertEquals(attachment, mutable.getAttachment(name, expectedType));
      assertTrue(mutable.isAttachmentPresent(name, expectedType));
      return result;
   }
   
   public void testAddAttachmentByType() throws Exception
   {
      MutableAttachments mutable = getMutable();
      ExpectedAttachments expected = new ExpectedAttachments();
      assertAddFreshAttachmentByType(mutable, expected, Integer.class, 1);
      assertAddFreshAttachmentByType(mutable, expected, String.class, "attachment");
      
      assertAddReplaceAttachmentByType(mutable, expected, String.class, "different", "attachment");
   }

   protected <T> void assertAddFreshAttachmentByType(MutableAttachments mutable, ExpectedAttachments expected, Class<T> type, T attachment)
   {
      assertAddReplaceAttachmentByType(mutable, expected, type, attachment, null);
   }

   protected <T> void assertAddReplaceAttachmentByType(MutableAttachments mutable, ExpectedAttachments expected, Class<T> type, T attachment, Object replaced)
   {
      Object result = assertAddAttachmentByType(mutable, expected, type, attachment);
      assertEquals(replaced, result);
   }

   protected <T> Object assertAddAttachmentByType(MutableAttachments mutable, ExpectedAttachments expected, Class<T> type, T attachment)
   {
      Object result = mutable.addAttachment(type, attachment);
      String name = type.getName();
      checkAdd(mutable, expected, name, attachment, result);
      assertEquals(attachment, mutable.getAttachment(type));
      assertTrue(mutable.isAttachmentPresent(type));
      assertEquals(attachment, mutable.getAttachment(name, type));
      assertTrue(mutable.isAttachmentPresent(name, type));
      return result;
   }
   
   public void testRemoveAttachmentErrors() throws Exception
   {
      MutableAttachments mutable = getMutable();
      
      try
      {
         mutable.removeAttachment((String) null);
         fail("Should not be here!");
      }
      catch (Throwable t)
      {
         checkThrowable(IllegalArgumentException.class, t);
      }
      
      try
      {
         mutable.removeAttachment(null, String.class);
         fail("Should not be here!");
      }
      catch (Throwable t)
      {
         checkThrowable(IllegalArgumentException.class, t);
      }
      
      try
      {
         mutable.removeAttachment("name", null);
         fail("Should nt be here!");
      }
      catch (Throwable t)
      {
         checkThrowable(IllegalArgumentException.class, t);
      }
      
      try
      {
         mutable.removeAttachment((Class<String>) null);
         fail("Should not be here!");
      }
      catch (Throwable t)
      {
         checkThrowable(IllegalArgumentException.class, t);
      }
   }
   
   public void testRemoveAttachmentByName() throws Exception
   {
      MutableAttachments mutable = getMutable();
      ExpectedAttachments expected = new ExpectedAttachments();
      assertRemoveNotPresentAttachmentByName(mutable, expected, "name1");
      
      assertAddAttachmentByName(mutable, expected, "name1", "attachment1");
      assertAddAttachmentByName(mutable, expected, "name2", "different2");
      assertRemoveAttachmentByName(mutable, expected, "name1", "attachment1");
   }

   protected void assertRemoveNotPresentAttachmentByName(MutableAttachments mutable, ExpectedAttachments expected, String name)
   {
      assertRemoveAttachmentByName(mutable, expected, name, null);
   }

   protected void assertRemoveAttachmentByName(MutableAttachments mutable, ExpectedAttachments expected, String name, Object removed)
   {
      Object result = assertRemoveAttachmentByName(mutable, expected, name);
      assertEquals(removed, result);
   }

   protected Object assertRemoveAttachmentByName(MutableAttachments mutable, ExpectedAttachments expected, String name)
   {
      Object result = mutable.removeAttachment(name);
      checkRemove(mutable, expected, name, result);
      return result;
   }
      
   protected Object checkRemove(Attachments attachments, ExpectedAttachments expected, String name, Object result)
   {
      assertNull(attachments.getAttachment(name));
      assertFalse(attachments.isAttachmentPresent(name));
      Object expectedResult = assertExpectedRemove(attachments, expected, name);
      assertEquals(expectedResult, result);
      return result;
   }
   
   protected Object assertExpectedRemove(Attachments attachments, ExpectedAttachments expected, String name)
   {
      Object result = expected.remove(name);
      assertEquals(expected.expected, attachments.getAttachments());
      return result;
   }
   
   public void testRemoveAttachmentByNameAndType() throws Exception
   {
      MutableAttachments mutable = getMutable();
      ExpectedAttachments expected = new ExpectedAttachments();
      assertRemoveNotPresentAttachmentByNameAndType(mutable, expected, "name1", String.class);
      
      assertAddAttachmentByNameAndType(mutable, expected, "name1", "attachment1", String.class);
      assertAddAttachmentByNameAndType(mutable, expected, "name2", "different2", String.class);
      assertRemoveAttachmentByNameAndType(mutable, expected, "name1", "attachment1", String.class);
   }

   protected <T> void assertRemoveNotPresentAttachmentByNameAndType(MutableAttachments mutable, ExpectedAttachments expected, String name, Class<T> expectedType)
   {
      assertRemoveAttachmentByNameAndType(mutable, expected, name, null, expectedType);
   }

   protected <T> void assertRemoveAttachmentByNameAndType(MutableAttachments mutable, ExpectedAttachments expected, String name, Object removed, Class<T> expectedType)
   {
      Object result = assertRemoveAttachmentByNameAndType(mutable, expected, name, expectedType);
      assertEquals(removed, result);
   }

   protected <T> Object assertRemoveAttachmentByNameAndType(MutableAttachments mutable, ExpectedAttachments expected, String name, Class<T> expectedType)
   {
      Object result = mutable.removeAttachment(name, expectedType);
      checkRemove(mutable, expected, name, result);
      assertNull(mutable.getAttachment(name, expectedType));
      assertFalse(mutable.isAttachmentPresent(name, expectedType));
      return result;
   }
   
   public void testRemoveAttachmentByType() throws Exception
   {
      MutableAttachments mutable = getMutable();
      ExpectedAttachments expected = new ExpectedAttachments();
      assertRemoveNotPresentAttachmentByType(mutable, expected, Integer.class);
      
      assertAddAttachmentByType(mutable, expected, Integer.class, 1);
      assertAddAttachmentByType(mutable, expected, String.class, "attachment");
      assertRemoveAttachmentByType(mutable, expected, String.class, "attachment");
   }

   protected <T> void assertRemoveNotPresentAttachmentByType(MutableAttachments mutable, ExpectedAttachments expected, Class<T> type)
   {
      assertRemoveAttachmentByType(mutable, expected, type, null);
   }

   protected <T> void assertRemoveAttachmentByType(MutableAttachments mutable, ExpectedAttachments expected, Class<T> type, Object removed)
   {
      Object result = assertRemoveAttachmentByType(mutable, expected, type);
      assertEquals(removed, result);
   }

   protected <T> Object assertRemoveAttachmentByType(MutableAttachments mutable, ExpectedAttachments expected, Class<T> type)
   {
      Object result = mutable.removeAttachment(type);
      String name = type.getName();
      checkRemove(mutable, expected, name, result);
      assertNull(mutable.getAttachment(type));
      assertFalse(mutable.isAttachmentPresent(type));
      assertNull(mutable.getAttachment(name, type));
      assertFalse(mutable.isAttachmentPresent(name, type));
      return result;
   }
   
   public void testGetAttachmentErrors() throws Exception
   {
      Attachments attachments = getAttachments();
      
      try
      {
         attachments.getAttachment((String) null);
         fail("Should not be here!");
      }
      catch (Throwable t)
      {
         checkThrowable(IllegalArgumentException.class, t);
      }
      
      try
      {
         attachments.getAttachment(null, String.class);
         fail("Should not be here!");
      }
      catch (Throwable t)
      {
         checkThrowable(IllegalArgumentException.class, t);
      }
      
      try
      {
         attachments.getAttachment("name", null);
         fail("Should not be here!");
      }
      catch (Throwable t)
      {
         checkThrowable(IllegalArgumentException.class, t);
      }
      
      try
      {
         attachments.getAttachment((Class<String>) null);
         fail("Should not be here!");
      }
      catch (Throwable t)
      {
         checkThrowable(IllegalArgumentException.class, t);
      }
   }
   
   public void testIsAttachmentPresentErrors() throws Exception
   {
      Attachments attachments = getAttachments();
      
      try
      {
         attachments.isAttachmentPresent((String) null);
         fail("Should not be here!");
      }
      catch (Throwable t)
      {
         checkThrowable(IllegalArgumentException.class, t);
      }
      
      try
      {
         attachments.isAttachmentPresent(null, String.class);
         fail("Should not be here!");
      }
      catch (Throwable t)
      {
         checkThrowable(IllegalArgumentException.class, t);
      }
      
      try
      {
         attachments.isAttachmentPresent("name", null);
         fail("Should not be here!");
      }
      catch (Throwable t)
      {
         checkThrowable(IllegalArgumentException.class, t);
      }
      
      try
      {
         attachments.isAttachmentPresent((Class<String>) null);
         fail("Should not be here!");
      }
      catch (Throwable t)
      {
         checkThrowable(IllegalArgumentException.class, t);
      }
   }
   
   public void testSerialization() throws Exception
   {
      Attachments attachments = getAttachments();
      serializeDeserialize(attachments, Attachments.class);

      MutableAttachments mutable = getMutable();
      serializeDeserialize(mutable, MutableAttachments.class);
      
      mutable.addAttachment(String.class, "Hello");
      mutable = serializeDeserialize(mutable, MutableAttachments.class);
      assertEquals("Hello", mutable.getAttachment(String.class));
   }
}
