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

import java.util.List;

import org.jboss.deployers.spi.structure.ContextInfo;
import org.jboss.deployers.spi.structure.ModificationType;
import org.jboss.deployers.spi.structure.StructureMetaData;
import org.jboss.logging.Logger;
import org.jboss.virtual.VirtualFile;

/**
 * Abstract modification type matcher.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class AbstractModificationTypeMatcher implements ModificationTypeMatcher
{
   protected Logger log = Logger.getLogger(getClass());

   private boolean checkChildren;
   private boolean topLevelOnly;
   private boolean childrenOnly;

   private ModificationType modificationType;

   public boolean determineModification(VirtualFile root, StructureMetaData structureMetaData)
   {
      ContextInfo contextInfo = structureMetaData.getContext("");
      if (childrenOnly == false)
      {
         boolean result = isModificationDetermined(root, contextInfo);
         if (result)
         {
            contextInfo.setModificationType(modificationType);
            return true;
         }
      }

      if (checkChildren)
      {
         List<ContextInfo> contexts = structureMetaData.getContexts();
         if (contexts != null && contexts.isEmpty() == false)
         {
            for (ContextInfo child : contexts)
            {
               String path = child.getPath();
               // Only process the child contexts
               if ("".equals(path) == false)
               {
                  try
                  {
                     VirtualFile file = root.getChild(path);
                     if (file != null && isModificationDetermined(file, child))
                     {
                        contextInfo.setModificationType(modificationType);
                        return true;
                     }
                  }
                  catch (Exception e)
                  {
                     log.debug("Exception checking child context (" + child + ") for modification, cause: " + e);
                  }
               }
            }
         }
      }
      return false;
   }

   public boolean determineModification(VirtualFile root, ContextInfo contextInfo)
   {
      boolean result = false;
      if (topLevelOnly == false)
      {
         result = isModificationDetermined(root, contextInfo);
         if (result)
         {
            contextInfo.setModificationType(modificationType);
         }
      }
      return result;
   }

   /**
    * Is modification determined.
    *
    * @param file the file
    * @param contextInfo the context info
    * @return true if we should apply modification type, false otherwise
    */
   protected abstract boolean isModificationDetermined(VirtualFile file, ContextInfo contextInfo);

   /**
    * Do we apply modification to the top structure context.
    *
    * @param checkChildren the apply to top flag
    */
   public void setCheckChildren(boolean checkChildren)
   {
      this.checkChildren = checkChildren;
   }

   /**
    * Is this matcher top level only.
    *
    * @param topLevelOnly the top level only flag
    */
   public void setTopLevelOnly(boolean topLevelOnly)
   {
      this.topLevelOnly = topLevelOnly;
   }

   /**
    * Is this matcher children only.
    *
    * @param childrenOnly the children only flag
    */
   public void setChildrenOnly(boolean childrenOnly)
   {
      this.childrenOnly = childrenOnly;
   }

   /**
    * Set the modification type.
    *
    * @param modificationType the modification type
    */
   public void setModificationType(ModificationType modificationType)
   {
      this.modificationType = modificationType;
   }
}