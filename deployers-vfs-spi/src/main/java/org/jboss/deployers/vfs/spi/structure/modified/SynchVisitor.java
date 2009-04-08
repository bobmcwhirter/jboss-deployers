/*
 * JBoss, Home of Professional Open Source.
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
package org.jboss.deployers.vfs.spi.structure.modified;

import org.jboss.logging.Logger;
import org.jboss.virtual.VirtualFileVisitor;
import org.jboss.virtual.VisitorAttributes;
import org.jboss.virtual.VirtualFile;

/**
 * Synch file visitor.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class SynchVisitor implements VirtualFileVisitor
{
   protected final Logger log = Logger.getLogger(getClass());

   private VisitorAttributes attributes;
   private StructureCache<Long> cache;
   private SynchAdapter synchAdapter;

   protected SynchVisitor(VisitorAttributes attributes, StructureCache<Long> cache, SynchAdapter synchAdapter)
   {
      if (cache == null)
         throw new IllegalArgumentException("Null cache");
      if (synchAdapter == null)
         throw new IllegalArgumentException("Null synch adapter");

      if (attributes != null)
         this.attributes = attributes;
      else
         this.attributes = VisitorAttributes.RECURSE_LEAVES_ONLY;
      this.cache = cache;
      this.synchAdapter = synchAdapter;
   }

   public VisitorAttributes getAttributes()
   {
      return attributes;
   }

   public void visit(VirtualFile file)
   {
      try
      {
         doVisit(file);
      }
      catch (Exception e)
      {
         log.warn("Exception synching file: " + file + ", cause: " + e);
      }
   }

   /**
    * Visit a virtual file
    *
    * @param file the virtual file being visited
    * @throws Exception for any error
    */
   protected abstract void doVisit(VirtualFile file) throws Exception;

   /**
    * Get cache.
    *
    * @return the cache
    */
   protected StructureCache<Long> getCache()
   {
      return cache;
   }

   /**
    * Get synch adapter.
    *
    * @return the syncj adapter
    */
   protected SynchAdapter getSynchAdapter()
   {
      return synchAdapter;
   }
}