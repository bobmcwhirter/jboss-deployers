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
import org.jboss.virtual.VisitorAttributes;

/**
 * Synch on update and delete file visitor.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class UpdateDeleteVisitor extends SynchVisitor
{
   private VirtualFile originalRoot;
   private String initialPath;

   public UpdateDeleteVisitor(VisitorAttributes attributes, StructureCache<Long> cache, SynchAdapter synchAdapter, VirtualFile originalRoot)
   {
      super(attributes, cache, synchAdapter);
      if (originalRoot == null)
         throw new IllegalArgumentException("Null original root");

      this.originalRoot = originalRoot;
      initialPath = originalRoot.getPathName();
      if (initialPath.endsWith("/") == false)
         initialPath += "/";
   }

   protected void doVisit(VirtualFile file) throws Exception
   {
      String pathName = file.getPathName();
      String originalPathName = initialPath + pathName;
      VirtualFile child = originalRoot.getChild(pathName);
      if (child == null)
      {
         // original was deleted, try deleting the temp
         if (getSynchAdapter().delete(file))
         {
            getCache().removeCache(originalPathName);
         }
      }
      else
      {
         Long previous = getCache().getCacheValue(originalPathName);
         long lastModified = child.getLastModified();
         if (previous != null && lastModified > previous)
         {
            lastModified = getSynchAdapter().update(file, child);
         }
         getCache().putCacheValue(originalPathName, lastModified);
      }
   }
}