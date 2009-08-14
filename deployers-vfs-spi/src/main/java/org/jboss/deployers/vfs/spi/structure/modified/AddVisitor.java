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

import org.jboss.virtual.VirtualFile;
import org.jboss.virtual.VirtualFileFilter;
import org.jboss.virtual.VisitorAttributes;

/**
 * Synch on add file visitor.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class AddVisitor extends SynchVisitor
{
   private VirtualFile tempRoot;
   private int initialPathLenght;

   public AddVisitor(VisitorAttributes attributes, StructureCache<Long> cache, SynchAdapter synchAdapter, VirtualFile tempRoot, int initialPathLenght)
   {
      this(null, attributes, cache, synchAdapter, tempRoot, initialPathLenght);
   }

   public AddVisitor(VirtualFileFilter filter, VisitorAttributes attributes, StructureCache<Long> cache, SynchAdapter synchAdapter, VirtualFile tempRoot, int initialPathLenght)
   {
      super(filter, attributes, cache, synchAdapter);
      if (tempRoot == null)
         throw new IllegalArgumentException("Null temp root");
      if (initialPathLenght < 0)
         throw new IllegalArgumentException("Initial path length is negative: " + initialPathLenght);

      this.tempRoot = tempRoot;
      this.initialPathLenght = initialPathLenght;
   }

   public void doVisit(VirtualFile file) throws Exception
   {
      String originalPathName = file.getPathName();
      String pathName = originalPathName.substring(initialPathLenght);
      VirtualFile child = tempRoot.getChild(pathName);
      if (child == null)
      {
         // original was added
         long timestamp = getSynchAdapter().add(file, tempRoot, pathName);
         getCache().putCacheValue(file, timestamp);
      }
   }
}