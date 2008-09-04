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
package org.jboss.deployers.plugins.attachments;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.deployers.spi.attachments.helpers.AbstractMutableAttachments;

/**
 * Attachments implementation
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class AttachmentsImpl extends AbstractMutableAttachments
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 8078990023620470935L;

   /** The attachments */
   private Map<String, Object> attachments = new ConcurrentHashMap<String, Object>();
   
   /** The number of changes since construction or clearing of the change count */
   private AtomicInteger changeCount = new AtomicInteger();

   public Map<String, Object> getAttachments()
   {
      return Collections.unmodifiableMap(attachments);
   }

   @Override
   public void setAttachments(Map<String, Object> map)
   {
      if (map == null)
         throw new IllegalArgumentException("Null map");
      
      attachments.clear();
      attachments.putAll(map);
      changeCount.addAndGet(map.size());
   }

   public Object addAttachment(String name, Object attachment)
   {
      if (name == null)
         throw new IllegalArgumentException("Null name");
      if (attachment == null)
         throw new IllegalArgumentException("Null attachment");
      Object value = attachments.put(name, attachment);
      changeCount.incrementAndGet();
      return value;
   }

   public Object getAttachment(String name)
   {
      if (name == null)
         throw new IllegalArgumentException("Null name");
      return attachments.get(name);
   }

   public boolean isAttachmentPresent(String name)
   {
      if (name == null)
         throw new IllegalArgumentException("Null name");
      return attachments.containsKey(name);
   }

   public Object removeAttachment(String name)
   {
      if (name == null)
         throw new IllegalArgumentException("Null name");
      Object value = attachments.remove(name);
      changeCount.incrementAndGet();
      return value;
   }

   public void clear()
   {
      attachments.clear();
      changeCount.incrementAndGet();
   }

   public boolean hasAttachments()
   {
      return attachments.isEmpty() == false;
   }

   public int getChangeCount()
   {
      return changeCount.intValue();
   }

   public void clearChangeCount()
   {
      changeCount.set(0);
   }

   @Override
   public String toString()
   {
      return attachments.toString();
   }
   
   @SuppressWarnings("unchecked")
   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
   {
      super.readExternal(in);
      setAttachments((Map) in.readObject());
      changeCount.set(in.read());
   }

   /**
    * @serialData attachments from {@link #getAttachments()}
    * @serialData changeCount from {@link #getChangeCount()}
    * @param out the output
    * @throws IOException for any error
    */
   public void writeExternal(ObjectOutput out) throws IOException
   {
      super.writeExternal(out);
      out.writeObject(getAttachments());
      out.write(changeCount.get());
   }
}
