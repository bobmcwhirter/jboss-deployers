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
package org.jboss.deployers.spi.attachments.helpers;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.jboss.deployers.spi.attachments.Attachments;

/**
 * AbstractAttachments.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public abstract class AbstractAttachments implements Attachments, Externalizable
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -4084792454892396925L;

   public <T> T getAttachment(String name, Class<T> expectedType)
   {
      if (expectedType == null)
         throw new IllegalArgumentException("Null expectedType");
      Object result = getAttachment(name);
      if (result == null)
         return null;
      return expectedType.cast(result);
   }

   public <T> T getAttachment(Class<T> type)
   {
      if (type == null)
         throw new IllegalArgumentException("Null type");
      return getAttachment(type.getName(), type);
   }

   public boolean isAttachmentPresent(String name, Class<?> expectedType)
   {
      if (expectedType == null)
         throw new IllegalArgumentException("Null expectedType");
      Object result = getAttachment(name);
      if (result == null)
         return false;
      try
      {
         expectedType.cast(result);
      }
      catch (ClassCastException e)
      {
         return false;
      }
      return true;
   }

   public boolean isAttachmentPresent(Class<?> type)
   {
      if (type == null)
         throw new IllegalArgumentException("Null type");
      return isAttachmentPresent(type.getName(), type);
   }

   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
   {
   }

   public void writeExternal(ObjectOutput out) throws IOException
   {
   }
}
