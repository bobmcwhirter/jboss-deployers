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

import java.io.IOException;
import java.util.List;

import org.jboss.deployers.spi.structure.ContextInfo;
import org.jboss.deployers.spi.structure.StructureMetaData;
import org.jboss.deployers.structure.spi.main.MainDeployerStructure;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentContext;
import org.jboss.virtual.VirtualFile;
import org.jboss.virtual.VirtualFileFilter;

/**
 * MetaDataStructureModificationChecker.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class MetaDataStructureModificationChecker extends AbstractStructureModificationChecker<Long>
{
   /** The metadata filter */
   private VirtualFileFilter filter;

   public MetaDataStructureModificationChecker(MainDeployerStructure mainDeployer)
   {
      super(mainDeployer);
   }

   /**
    * Set the metadata filter.
    *
    * @param filter the metadata filter
    */
   public void setFilter(VirtualFileFilter filter)
   {
      this.filter = filter;
   }

   protected boolean hasStructureBeenModifed(VirtualFile root, VFSDeploymentContext deploymentContext) throws IOException
   {
      StructureMetaData structureMetaData = deploymentContext.getTransientManagedObjects().getAttachment(StructureMetaData.class);
      return hasStructureBeenModified(root, structureMetaData);
   }

   /**
    * Has structure been modified.
    *
    * @param root the root
    * @param structureMetaData the structure metadata
    * @return true if modifed, false otherwise
    * @throws IOException for any error
    */
   protected boolean hasStructureBeenModified(VirtualFile root, StructureMetaData structureMetaData) throws IOException
   {
      if (structureMetaData == null)
         return false;

      List<ContextInfo> contexts = structureMetaData.getContexts();
      if (contexts != null && contexts.isEmpty() == false)
      {
         for (ContextInfo contextInfo : contexts)
         {
            boolean result = hasStructureBeenModifed(root, contextInfo);
            if (result)
               return true;
         }
      }
      return false;
   }

   /**
    * Has structure been modifed.
    *
    * @param root the root
    * @param contextInfo the context info
    * @return true if modifed, false otherwise
    * @throws IOException for any error
    */
   protected boolean hasStructureBeenModifed(VirtualFile root, ContextInfo contextInfo) throws IOException
   {
      String path = contextInfo.getPath();
      VirtualFile contextRoot = root.getChild(path);
      if (contextRoot != null)
      {
         List<String> metadataPaths = contextInfo.getMetaDataPath();
         if (metadataPaths != null && metadataPaths.isEmpty() == false)
         {
            for (String metaDataPath : metadataPaths)
            {
               VirtualFile mdpVF = contextRoot.getChild(metaDataPath);
               if (mdpVF != null)
               {
                  List<VirtualFile> children = mdpVF.getChildren(filter);
                  if (children != null && children.isEmpty() == false)
                  {
                     for (VirtualFile child : children)
                     {
                        String pathName = child.getPathName();
                        Long timestamp = getCache().getCacheValue(pathName);
                        long lastModified = child.getLastModified();
                        getCache().putCacheValue(pathName, lastModified);
                        if (timestamp != null && timestamp < lastModified)
                        {
                           if (log.isTraceEnabled())
                              log.trace("Metadata location modified: " + child);
                           return true;
                        }
                     }
                  }
               }
            }
         }
         if ("".equals(path) == false)
         {
            StructureMetaData structureMetaData = contextInfo.getPredeterminedManagedObjects().getAttachment(StructureMetaData.class);
            return hasStructureBeenModified(contextRoot, structureMetaData);
         }
      }
      return false;
   }
}