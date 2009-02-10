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

import java.util.Set;

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
    * @param pathName the path name
    */
   void initializeCache(String pathName);

   /**
    * Put cache value.
    *
    * @param pathName the path name
    * @param value the value
    * @return previous value
    */
   T putCacheValue(String pathName, T value);

   /**
    * Get cache value.
    *
    * @param pathName the path name
    * @return the cache value
    */
   T getCacheValue(String pathName);

   /**
    * Get leaves for this path name parameter.
    * Only exact sub path nodes count in.
    *
    * This method should return a mutable Set copy
    * as we intend to modify it in checker processing.
    *
    * @param pathName the path name
    * @return sub-paths nodes or null if no such match yet
    */
   Set<String> getLeaves(String pathName);

   /**
    * Invalidate cache for path name.
    *
    * @param pathName the path name
    */
   void invalidateCache(String pathName);

   /**
    * Remove cache for path name.
    *
    * @param pathName the path name
    */
   void removeCache(String pathName);

   /**
    * Flush the cache.
    */
   void flush();
}
