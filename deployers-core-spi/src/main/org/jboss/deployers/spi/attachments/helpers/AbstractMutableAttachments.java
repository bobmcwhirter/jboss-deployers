/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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

import java.util.Map;
import java.util.Map.Entry;

import org.jboss.deployers.spi.attachments.MutableAttachments;

/**
 * AbstractMutableAttachments.
 * 
 * @author <a href="adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public abstract class AbstractMutableAttachments extends AbstractAttachments implements MutableAttachments
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -1692116584854666016L;

   public <T> T addAttachment(String name, T attachment, Class<T> expectedType)
   {
      if (expectedType == null)
         throw new IllegalArgumentException("Null expectedType");
      Object result = addAttachment(name, attachment);
      if (result == null)
         return null;
      if (expectedType.isInstance(result) == false)
         throw new IllegalArgumentException("Previous attachment not of the same type: expected=" + expectedType + ", previous=" + result);
      return expectedType.cast(result);
   }

   public void setAttachments(Map<String, Object> map)
   {
      if (map == null)
         throw new IllegalArgumentException("Null map");

      clear();
      
      for (Entry<String, Object> entry : map.entrySet())
         addAttachment(entry.getKey(), entry.getValue());
   }

   public <T> T addAttachment(Class<T> type, T attachment)
   {
      if (type == null)
         throw new IllegalArgumentException("Null type");
      return addAttachment(type.getName(), attachment, type);
   }

   public <T> T removeAttachment(String name, Class<T> expectedType)
   {
      if (expectedType == null)
         throw new IllegalArgumentException("Null expectedType");
      Object result = removeAttachment(name);
      if (result == null)
         return null;
      if (expectedType.isInstance(result) == false)
         throw new IllegalArgumentException("Removed attachment not of the same type: expected=" + expectedType + ", previous=" + result);      
      return expectedType.cast(result);
   }

   public <T> T removeAttachment(Class<T> type)
   {
      if (type == null)
         throw new IllegalArgumentException("Null type");
      return removeAttachment(type.getName(), type);
   }
}
