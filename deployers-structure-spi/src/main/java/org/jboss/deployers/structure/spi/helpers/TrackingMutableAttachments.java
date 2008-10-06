/*
* JBoss, Home of Professional Open Source
* Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.deployers.structure.spi.helpers;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.jboss.deployers.spi.attachments.MutableAttachments;

/**
 * TrackingMutableAttachments.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class TrackingMutableAttachments implements MutableAttachments
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;

   /** The delegate */
   private MutableAttachments delegate;

   /** The attachment creator */ 
   private Map<String, String> createdDeployer = new ConcurrentHashMap<String, String>();

   /** The attachment referencer */ 
   private Map<String, Set<String>> referencedDeployer = new ConcurrentHashMap<String, Set<String>>();
   
   /**
    * Create a new TrackingMutableAttachments.
    * 
    * @param delegate the delegate
    */
   public  TrackingMutableAttachments(MutableAttachments delegate)
   {
      if (delegate == null)
         throw new IllegalArgumentException("Null delegate");
      this.delegate = delegate;
   }

   /**
    * Get the deployer that created an attachment
    * 
    * @param name the attachment name
    * @return the name or null if was predetermined
    */
   public String getCreated(String name)
   {
      if (name == null)
         throw new IllegalArgumentException("Null name");
      return createdDeployer.get(name);
   }

   /**
    * Get the deployers that referenced an attachment
    * 
    * @param name the attachment name
    * @return the name or null if nothing referenced it
    */
   public Set<String> getReferenced(String name)
   {
      if (name == null)
         throw new IllegalArgumentException("Null name");
      Set<String> result = referencedDeployer.get(name);
      if (result == null)
         return null;
      return Collections.unmodifiableSet(result);
   }
   
   /**
    * An attachment is being created
    * 
    * @param name the name
    */
   void created(String name)
   {
      createdDeployer.put(name, DeployerTracking.getCurrentDeployer());
   }

   /**
    * An attachment is being referenced
    * 
    * @param name the name
    */
   void referenced(String name)
   {
      Set<String> deployers = referencedDeployer.get(name);
      if (deployers == null)
      {
         deployers = new CopyOnWriteArraySet<String>();
         referencedDeployer.put(name, deployers);
      }
      deployers.add(DeployerTracking.getCurrentDeployer());
   }
   
   public <T> T addAttachment(Class<T> type, T attachment)
   {
      T result = delegate.addAttachment(type, attachment);
      if (result == null)
         created(type.getName());
      else
         referenced(type.getName());
      return result;
   }

   public Object addAttachment(String name, Object attachment)
   {
      Object result = delegate.addAttachment(name, attachment);
      if (result == null)
         created(name);
      else
         referenced(name);
      return result;
   }

   public <T> T addAttachment(String name, T attachment, Class<T> expectedType)
   {
      T result = delegate.addAttachment(name, attachment, expectedType);
      if (result == null)
         created(name);
      else
         referenced(name);
      return result;
   }

   public void clear()
   {
      createdDeployer.clear();
      referencedDeployer.clear();
      delegate.clear();
   }

   public void clearChangeCount()
   {
      delegate.clearChangeCount();
   }

   public <T> T getAttachment(Class<T> type)
   {
      T result = delegate.getAttachment(type);
      if (result != null)
         referenced(type.getName());
      return result;
   }

   public <T> T getAttachment(String name, Class<T> expectedType)
   {
      T result = delegate.getAttachment(name, expectedType);
      if (result != null)
         referenced(name);
      return result;
   }

   public Object getAttachment(String name)
   {
      Object result = delegate.getAttachment(name);
      if (result != null)
         referenced(name);
      return result;
   }

   public Map<String, Object> getAttachments()
   {
      return delegate.getAttachments();
   }

   public int getChangeCount()
   {
      return delegate.getChangeCount();
   }

   public boolean hasAttachments()
   {
      return delegate.hasAttachments();
   }

   public boolean isAttachmentPresent(Class<?> type)
   {
      boolean result = delegate.isAttachmentPresent(type);
      if (result)
         referenced(type.getName());
      return result;
   }

   public boolean isAttachmentPresent(String name, Class<?> expectedType)
   {
      boolean result = delegate.isAttachmentPresent(name, expectedType);
      if (result)
         referenced(name);
      return result;
   }

   public boolean isAttachmentPresent(String name)
   {
      boolean result = delegate.isAttachmentPresent(name);
      if (result)
         referenced(name);
      return result;
   }

   public <T> T removeAttachment(Class<T> type)
   {
      T result = delegate.removeAttachment(type);
      if (result != null)
         referenced(type.getName());
      return result;
   }

   public <T> T removeAttachment(String name, Class<T> expectedType)
   {
      T result = delegate.removeAttachment(name, expectedType);
      if (result != null)
         referenced(name);
      return result;
   }

   public Object removeAttachment(String name)
   {
      Object result = delegate.removeAttachment(name);
      if (result != null)
         referenced(name);
      return result;
   }

   public void setAttachments(Map<String, Object> map)
   {
      delegate.setAttachments(map);
      createdDeployer.clear();
      referencedDeployer.clear();
   }
}
