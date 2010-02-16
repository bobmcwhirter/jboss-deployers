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

import java.util.List;

import org.jboss.vfs.VirtualFile;
import org.jboss.vfs.VirtualFileFilter;

/**
 * Simple structure cache.
 *
 * @param <T> exact cache value type
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public interface StructureCache<T>
{
   /**
    * Initialize cache.
    *
    * @param root the root to initialize
    */
   void initializeCache(VirtualFile root);

   /**
    * Put cache value.
    *
    * @param file the file key
    * @param value the value
    * @return previous value
    */
   T putCacheValue(VirtualFile file, T value);

   /**
    * Get cache value.
    *
    * @param file the file key
    * @return the cache value
    */
   T getCacheValue(VirtualFile file);

   /**
    * Get leaves for this file parameter.
    * Only first level children count in.
    *
    * This method should return a mutable Set copy
    * as we intend to modify it in checker processing.
    *
    * @param file the file key
    * @return first level children or null if no such match yet
    */
   List<VirtualFile> getLeaves(VirtualFile file);

   /**
    * Get leaves for this file parameter.
    * Only first level children count in.
    *
    * This method should return a mutable Set copy
    * as we intend to modify it in checker processing.
    *
    * @param file the file key
    * @param filter the file filter
    * @return first level children or null if no such match yet
    */
   List<VirtualFile> getLeaves(VirtualFile file, VirtualFileFilter filter);

   /**
    * Invalidate cache for file and all of its subs.
    *
    * @param file the file key
    */
   void invalidateCache(VirtualFile file);

   /**
    * Remove cache for file and all of its subs.
    *
    * @param file the file key
    */
   void removeCache(VirtualFile file);

   /**
    * Remove cache for file and all of its subs.
    *
    * @param root the root
    * @param path the path of non-existing file
    */
   void removeCache(VirtualFile root, String path);

   /**
    * Flush the cache.
    */
   void flush();
}
