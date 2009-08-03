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
package org.jboss.test.deployers.vfs.structure.modified.support;

import java.util.List;

import org.jboss.deployers.spi.structure.ContextInfo;
import org.jboss.deployers.spi.structure.StructureMetaData;
import org.jboss.deployers.vfs.plugins.structure.modify.ModificationTypeMatcher;
import org.jboss.deployers.vfs.spi.structure.StructureContext;
import org.jboss.deployers.vfs.spi.structure.StructureDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSStructuralDeployers;
import org.jboss.virtual.VirtualFile;

/**
 * Esb use case.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class EsbModificationTypeMatcher implements ModificationTypeMatcher
{
   private StructureDeployer warStructureDeployer = null; // get it injected
   private VFSStructuralDeployers deployers = null; // get it injected

   public boolean determineModification(VirtualFile root, StructureMetaData structureMetaData)
   {
      try
      {
         if (root.getName().endsWith(".esb"))
         {
            VirtualFile wars = root.getChild("wars");
            if (wars != null)
            {
               List<VirtualFile> children = wars.getChildren();
               for (VirtualFile war : children)
               {
                  StructureContext context = new StructureContext(root, root, war, structureMetaData, deployers, null);
                  warStructureDeployer.determineStructure(context);
               }
            }
            return true;
         }
      }
      catch (Exception ignored)
      {
      }
      return false;
   }

   public boolean determineModification(VirtualFile root, ContextInfo contextInfo)
   {
      // cannot change sub-deployment
      return false;
   }
}
