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
package org.jboss.deployers.spi.attachments;

import java.util.Map;

/**
 * MutableAttachments.
 * 
 * @author <a href="adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public interface MutableAttachments extends Attachments
{
   /**
    * Add attachment
    *
    * @param name the name of the attachment
    * @param attachment the attachment
    * @return any previous attachment
    * @throws IllegalArgumentException for a null name or attachment
    * @throws UnsupportedOperationException when not supported by the implementation
    */
   Object addAttachment(String name, Object attachment);

   /**
    * Add attachment
    *
    * @param <T> the expected type
    * @param name the name of the attachment
    * @param attachment the attachment
    * @param expectedType the expected type
    * @return any previous attachment
    * @throws IllegalArgumentException for a null name, attachment or expectedType
    * @throws UnsupportedOperationException when not supported by the implementation
    */
   <T> T addAttachment(String name, T attachment, Class<T> expectedType);

   /**
    * Add attachment
    *
    * @param <T> the expected type
    * @param attachment the attachment
    * @param type the type
    * @return any previous attachment
    * @throws IllegalArgumentException for a null name, attachment or type
    * @throws UnsupportedOperationException when not supported by the implementation
    */
   <T> T addAttachment(Class<T> type, T attachment);

   /**
    * Remove attachment
    * 
    * @param name the name of the attachment
    * @return the attachment or null if not present
    * @throws IllegalArgumentException for a null name
    * @throws UnsupportedOperationException when not supported by the implementation
    */
   Object removeAttachment(String name);

   /**
    * Remove attachment
    * 
    * @param <T> the expected type
    * @param name the name of the attachment
    * @return the attachment or null if not present
    * @param expectedType the expected type
    * @throws IllegalArgumentException for a null name or expectedType
    * @throws UnsupportedOperationException when not supported by the implementation
    */
   <T> T removeAttachment(String name, Class<T> expectedType);

   /**
    * Remove attachment
    * 
    * @param <T> the expected type
    * @return the attachment or null if not present
    * @param type the type
    * @throws IllegalArgumentException for a null name or type
    */
   <T> T removeAttachment(Class<T> type);

   /**
    * Set the attachments
    * 
    * @param map the new attachments a map of names to attachments
    * @throws IllegalArgumentException for a null map
    */
   void setAttachments(Map<String, Object> map);

   /**
    * Clear the attachments
    * 
    * @throws UnsupportedOperationException when not supported by the implementation
    */
   void clear();

   /**
    * Get the number of changes that have happened.
    * 
    * @return number of adds/removes that have happened since creation or clearChangeCount.
    */
   int getChangeCount();

   /**
    * Reset the change count to zero.
    */
   void clearChangeCount();
}
