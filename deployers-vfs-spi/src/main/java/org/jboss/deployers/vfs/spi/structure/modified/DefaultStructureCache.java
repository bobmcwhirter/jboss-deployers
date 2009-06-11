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

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Default structure cache.
 *
 * @param <T> exact cache value type
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class DefaultStructureCache<T> extends AbstractStructureCache<T>
{
   private Map<String, T> map = new ConcurrentHashMap<String, T>();

   public void initializeCache(String pathName)
   {
   }

   public T putCacheValue(String pathName, T value)
   {
      return map.put(pathName, value);
   }

   public T getCacheValue(String pathName)
   {
      return map.get(pathName);
   }

   public Set<String> getLeaves(String pathName, StructureCacheFilter filter)
   {
      Set<String> result = null;
      Pattern pattern = Pattern.compile(pathName + "/[^/]+");
      for (String key : map.keySet())
      {
         // first the pattern should match, only then we check the filter
         if (pattern.matcher(key).matches() && (filter == null || filter.accepts(key)))
         {
            if (result == null)
               result = new HashSet<String>();

            result.add(key);
         }
      }
      if (result != null)
         return result;
      else
         return (map.containsKey(pathName) ? Collections.<String>emptySet() : null);
   }

   public void invalidateCache(String pathName)
   {
      removeCache(pathName);
   }

   public void removeCache(String pathName)
   {
      Iterator<Map.Entry<String, T>> iter = map.entrySet().iterator();
      while (iter.hasNext())
      {
         Map.Entry<String, T> entry = iter.next();
         if (entry.getKey().startsWith(pathName))
         {
            iter.remove();
         }
      }
   }

   public void flush()
   {
      map.clear();
   }
}