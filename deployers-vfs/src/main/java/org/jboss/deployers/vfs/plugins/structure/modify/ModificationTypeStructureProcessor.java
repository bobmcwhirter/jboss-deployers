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

import java.util.ArrayList;
import java.util.List;

import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.spi.structure.ContextInfo;
import org.jboss.deployers.spi.structure.StructureMetaData;
import org.jboss.deployers.structure.spi.StructureProcessor;
import org.jboss.deployers.structure.spi.DeploymentContext;
import org.jboss.deployers.vfs.spi.client.VFSDeployment;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentContext;
import org.jboss.vfs.VirtualFile;

/**
 * Determine if we need some modification.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class ModificationTypeStructureProcessor implements StructureProcessor
{
   private List<ModificationTypeMatcher> matchers;

   public void prepareStructureMetaData(Deployment deployment, StructureMetaData structureMetaData)
   {
      if (deployment instanceof VFSDeployment == false)
         return;

      VFSDeployment vfsDeployment = VFSDeployment.class.cast(deployment);
      VirtualFile root = vfsDeployment.getRoot();
      checkForModification(root, structureMetaData);
   }

   public void prepareContextInfo(DeploymentContext parentDeploymentContext, ContextInfo contextInfo)
   {
      if (parentDeploymentContext instanceof VFSDeploymentContext == false || contextInfo == null)
         return;

      VFSDeploymentContext vfsParentDeploymentContext = VFSDeploymentContext.class.cast(parentDeploymentContext);
      VirtualFile root = vfsParentDeploymentContext.getFile(contextInfo.getPath());
      checkForModification(root, contextInfo);
   }

   public void applyStructureMetaData(DeploymentContext deploymentContext, StructureMetaData structureMetaData)
   {
   }

   public void applyContextInfo(DeploymentContext deploymentContext, ContextInfo contextInfo)
   {
   }

   /**
    * Check for modification.
    *
    * @param root the deployment root
    * @param structureMetaData the structure metadata
    */
   protected void checkForModification(VirtualFile root, StructureMetaData structureMetaData)
   {
      ContextInfo contex = structureMetaData.getContext("");
      if (contex == null || contex.getModificationType() != null)
         return;

      if (matchers != null && matchers.isEmpty() == false)
      {
         for (ModificationTypeMatcher matcher : matchers)
         {
            if (matcher.determineModification(root, structureMetaData))
            {
               break;
            }
         }
      }
   }

   /**
    * Check for modification.
    *
    * @param root the deployment root
    * @param contextInfo the context info
    */
   protected void checkForModification(VirtualFile root, ContextInfo contextInfo)
   {
      if (root == null || contextInfo == null || contextInfo.getModificationType() != null)
         return;

      if (matchers != null && matchers.isEmpty() == false)
      {
         for (ModificationTypeMatcher matcher : matchers)
         {
            if (matcher.determineModification(root, contextInfo))
            {
               break;
            }
         }
      }
   }

   /**
    * Set modification type matchers.
    *
    * @param matchers the modification type matchers.
    */
   public void setMatchers(List<ModificationTypeMatcher> matchers)
   {
      this.matchers = matchers;
   }

   /**
    * Add modification type matcher.
    *
    * @param matcher the modification type matcher
    */
   public void addMatcher(ModificationTypeMatcher matcher)
   {
      if (matchers == null)
         matchers = new ArrayList<ModificationTypeMatcher>();

      matchers.add(matcher);
   }

   /**
    * Remove modification type matcher.
    *
    * @param matcher the modification type matcher
    */
   public void removeMatcher(ModificationTypeMatcher matcher)
   {
      if (matchers != null)
      {
         matchers.remove(matcher);
      }
   }
}
