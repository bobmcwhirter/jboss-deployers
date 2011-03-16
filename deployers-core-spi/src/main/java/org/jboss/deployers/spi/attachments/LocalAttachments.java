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
package org.jboss.deployers.spi.attachments;

import java.io.Serializable;
import java.util.Map;

/**
 * Local attachments
 * 
 * Represents a set of local attachments
 * 
 * @author <a href="ales.justin@jboss.org">Ales Justin</a>
 */
public interface LocalAttachments extends Serializable
{
   /**
    * Get all the local attachments
    * 
    * @return the unmodifiable attachments
    */
   Map<String, Object> getLocalAttachments();

   /**
    * Get local attachment
    * 
    * @param name the name of the attachment
    * @return the attachment or null if not present
    * @throws IllegalArgumentException for a null name
    */
   Object getLocalAttachment(String name);

   /**
    * Get local attachment
    * 
    * @param <T> the expected type
    * @param name the name of the attachment
    * @param expectedType the expected type
    * @return the attachment or null if not present
    * @throws IllegalArgumentException for a null name or expectedType
    */
   <T> T getLocalAttachment(String name, Class<T> expectedType);

   /**
    * Get local attachment
    * 
    * @param <T> the expected type
    * @param type the type
    * @return the attachment or null if not present
    * @throws IllegalArgumentException for a null name or type
    */
   <T> T getLocalAttachment(Class<T> type);
   
   /**
    * Is the local attachment present
    * 
    * @param name the name of the attachment
    * @return true when the attachment is present
    * @throws IllegalArgumentException for a null name
    */
   boolean isLocalAttachmentPresent(String name);
   
   /**
    * Is the local attachment present
    * 
    * @param name the name of the attachment
    * @param expectedType the expected type
    * @return true when the attachment is present
    * @throws IllegalArgumentException for a null name or expectedType
    */
   boolean isLocalAttachmentPresent(String name, Class<?> expectedType);
   
   /**
    * Is the local attachment present
    * 
    * @param type the type
    * @return true when the attachment is present
    * @throws IllegalArgumentException for a null name or type
    */
   boolean isLocalAttachmentPresent(Class<?> type);

   /**
    * Are there any local attachments
    * 
    * @return true if there are any attachments, false otherwise.
    */
   boolean hasLocalAttachments();
}
