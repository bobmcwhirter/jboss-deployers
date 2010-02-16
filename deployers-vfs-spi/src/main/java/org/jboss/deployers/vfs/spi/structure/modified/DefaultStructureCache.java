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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.vfs.VirtualFile;
import org.jboss.vfs.VirtualFileFilter;

/**
 * Default structure cache.
 *
 * @param <T> exact cache value type
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class DefaultStructureCache<T> extends AbstractStructureCache<T>
{
   private Map<VirtualFile, T> map = new ConcurrentHashMap<VirtualFile, T>();

   public void initializeCache(VirtualFile root)
   {
   }

   public T putCacheValue(VirtualFile file, T value)
   {
      return map.put(file, value);
   }

   public T getCacheValue(VirtualFile file)
   {
      return map.get(file);
   }

   public List<VirtualFile> getLeaves(VirtualFile file, VirtualFileFilter filter)
   {
      List<VirtualFile> result = null;
      for (VirtualFile key : map.keySet())
      {
         VirtualFile parent = key.getParent();
         if (parent != null && parent.equals(file) && (filter == null || filter.accepts(key)))
         {
            if (result == null)
               result = new ArrayList<VirtualFile>();

            result.add(key);
         }
      }
      if (result != null)
         return result;
      else
         return (map.containsKey(file) ? Collections.<VirtualFile>emptyList() : null);
   }

   public void invalidateCache(VirtualFile file)
   {
      removeCache(file);
   }

   public void removeCache(VirtualFile file)
   {
      Iterator<Map.Entry<VirtualFile, T>> iter = map.entrySet().iterator();
      while (iter.hasNext())
      {
         Map.Entry<VirtualFile, T> entry = iter.next();
         if (isAncestorOrEquals(file, entry.getKey()))
         {
            iter.remove();
         }
      }
   }

   public void removeCache(VirtualFile root, String path)
   {
      Iterator<Map.Entry<VirtualFile, T>> iter = map.entrySet().iterator();
      while (iter.hasNext())
      {
         Map.Entry<VirtualFile, T> entry = iter.next();
         VirtualFile key = entry.getKey();
         if (isAncestorOrEquals(root, key) && key.getPathName().startsWith(path))
         {
            iter.remove();
         }
      }
   }

   /**
    * Is ref ancestor or equal to file param.
    *
    * @param ref the ref to check against
    * @param file the file to check
    * @return true if ref is ancestor or equal to file, false otherwise
    */
   protected boolean isAncestorOrEquals(VirtualFile ref, VirtualFile file)
   {
      while(file != null)
      {
         if (file.equals(ref))
            return true;

         file = file.getParent();
      }
      return false;
   }

   public void flush()
   {
      map.clear();
   }
}