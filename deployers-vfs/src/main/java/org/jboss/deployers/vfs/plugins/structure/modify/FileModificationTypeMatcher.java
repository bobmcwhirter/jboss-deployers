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
package org.jboss.deployers.vfs.plugins.structure.modify;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

import org.jboss.deployers.spi.structure.ContextInfo;
import org.jboss.virtual.VirtualFile;

/**
 * File modification type matcher.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class FileModificationTypeMatcher extends AbstractModificationTypeMatcher
{
   private String[] paths;
   private boolean metadataOnly;

   public FileModificationTypeMatcher(String... paths)
   {
      if (paths == null || paths.length == 0)
         throw new IllegalArgumentException("Null or empty paths");

      this.paths = paths;
   }

   protected boolean isModificationDetermined(VirtualFile root, ContextInfo contextInfo)
   {
      for (String path : paths)
      {
         for (VirtualFile file : getStartingFiles(root, contextInfo))
         {
            try
            {
               if (file.getChild(path) != null)
                  return true;
            }
            catch (Exception e)
            {
               log.debug("Cannot determine modification type, cause: " + e);
            }
         }
      }
      return false;
   }

   /**
    * Get starting files for path check.
    *
    * @param file the current file
    * @param contextInfo the context info
    * @return list of starting files
    */
   protected List<VirtualFile> getStartingFiles(VirtualFile file, ContextInfo contextInfo)
   {
      if (metadataOnly)
      {
         List<String> metadataPaths = contextInfo.getMetaDataPath();
         if (metadataPaths == null || metadataPaths.isEmpty())
         {
            return Collections.emptyList();
         }
         else
         {
            List<VirtualFile> result = new ArrayList<VirtualFile>(metadataPaths.size());
            for (String metadataPath : metadataPaths)
            {
               try
               {
                  VirtualFile child = file.getChild(metadataPath);
                  if (child != null)
                     result.add(child);
               }
               catch (Exception ignored)
               {
               }
            }
            return result;
         }
      }
      else
      {
         return Collections.singletonList(file);
      }
   }

   /**
    * Should we check metadata only.
    *
    * @param metadataOnly the metadata only flag
    */
   public void setMetadataOnly(boolean metadataOnly)
   {
      this.metadataOnly = metadataOnly;
   }
}