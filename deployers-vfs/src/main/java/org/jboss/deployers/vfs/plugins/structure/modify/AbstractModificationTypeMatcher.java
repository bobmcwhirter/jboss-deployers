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

import org.jboss.deployers.spi.structure.ContextInfo;
import org.jboss.deployers.spi.structure.ModificationType;
import org.jboss.deployers.spi.structure.StructureMetaData;
import org.jboss.deployers.vfs.spi.structure.StructureContext;
import org.jboss.logging.Logger;

/**
 * Abstract modification type matcher.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class AbstractModificationTypeMatcher implements ModificationTypeMatcher
{
   protected Logger log = Logger.getLogger(getClass());
   
   private boolean applyModificationToTop;
   private ModificationType modificationType;

   public boolean determineModification(StructureContext structureContext)
   {
      boolean result = isModificationDetermined(structureContext);
      if (result)
      {
         if (applyModificationToTop && structureContext.isTopLevel() == false)
         {
            // we need to modify an existing ContextInfo
            StructureContext topSC = getTopStructureContext(structureContext);
            StructureMetaData topSMD = topSC.getMetaData();
            ContextInfo contextInfo = topSMD.getContext("");
            if (contextInfo.getModificationType() != null)
            {
               log.debug("Ignoring modification type change, already set: " + contextInfo);
            }
            else
            {
               contextInfo.setModificationType(modificationType);
            }
         }
         else
         {
            // prepare the info for the actual creation
            structureContext.setModificationType(modificationType);
         }
      }
      return result;
   }

   /**
    * Do we have a modification match.
    *
    * @param structureContext the structure context
    * @return true if we should apply the modification
    */
   protected abstract boolean isModificationDetermined(StructureContext structureContext);

   /**
    * Get top structure context.
    *
    * @param context the current structure context
    * @return the top structure context
    */
   protected StructureContext getTopStructureContext(StructureContext context)
   {
      while (context.getParentContext() != null)
         context = context.getParentContext();

      return context;
   }

   /**
    * Do we apply modification to the top structure context.
    *
    * @param applyModificationToTop the apply to top flag
    */
   public void setApplyModificationToTop(boolean applyModificationToTop)
   {
      this.applyModificationToTop = applyModificationToTop;
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