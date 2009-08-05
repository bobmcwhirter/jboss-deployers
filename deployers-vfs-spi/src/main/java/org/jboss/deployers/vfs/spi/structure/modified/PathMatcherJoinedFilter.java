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

import org.jboss.deployers.vfs.spi.deployer.PathMatcher;
import org.jboss.virtual.VirtualFile;
import org.jboss.virtual.VirtualFileFilter;
import org.jboss.util.collection.ConcurrentSet;

/**
 * For virtual file filter we use delegate,
 * as we already have FileStructure that collects all FileMatchers
 * which is what we want to use to check if file is accepted.
 *
 * For structure cache filer we use PathMatchers.
 * They should be injected via MC's IoC incallback.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class PathMatcherJoinedFilter implements StructureCacheFilter, VirtualFileFilter
{
   /** The path matchers */
   private Set<PathMatcher> pathMatchers = new ConcurrentSet<PathMatcher>();

   /** The virtual file filter delegate */
   private VirtualFileFilter delegate;

   public PathMatcherJoinedFilter(VirtualFileFilter delegate)
   {
      if (delegate == null)
         throw new IllegalArgumentException("Null delegate");

      this.delegate = delegate;
   }

   public boolean accepts(String path)
   {
      for (PathMatcher pm : pathMatchers)
      {
         if (pm.isDeployable(path))
            return true;
      }
      return false;
   }

   public boolean accepts(VirtualFile file)
   {
      return delegate.accepts(file);
   }

   /**
    * Add path matcher.
    *
    * @param pm the path matcher
    * @return Set#add
    */
   public boolean addFileMatcher(PathMatcher pm)
   {
      return pathMatchers.add(pm);
   }

   /**
    * Remove path matcher.
    *
    * @param pm the path matcher
    * @return Set#remove
    */
   public boolean removeFileMatcher(PathMatcher pm)
   {
      return pathMatchers.remove(pm);
   }
}