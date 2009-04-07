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

import org.jboss.deployers.vfs.spi.structure.VFSDeploymentContext;
import org.jboss.deployers.structure.spi.main.MainDeployerStructure;
import org.jboss.virtual.VirtualFile;

/**
 * Synch wrapper modification checker.
 *
 * If there is no modification, we check if the deployment is perhaps a temp,
 * only then checking if we need to update some resource.
 * e.g. some .jsp or .xhtml file for JBossWeb to pick up the change
 *
 * @param <T> exact checker type
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class SynchWrapperModificationChecker<T> extends AbstractStructureModificationChecker<T>
{
   /** The true checker delegate */
   private AbstractStructureModificationChecker<T> delegate;

   public SynchWrapperModificationChecker(AbstractStructureModificationChecker<T> delegate)
   {
      if (delegate == null)
         throw new IllegalArgumentException("Null delegate");

      this.delegate = delegate;
   }

   @Override
   protected StructureCache<T> getCache()
   {
      return delegate.getCache();
   }

   @Override
   protected MainDeployerStructure getMainDeployerStructure()
   {
      return delegate.getMainDeployerStructure();
   }

   protected boolean hasStructureBeenModifed(VirtualFile root, VFSDeploymentContext deploymentContext) throws IOException
   {
      boolean modified = delegate.hasStructureBeenModifed(root, deploymentContext);
      // it was not modifed & we're actually temped
      if (modified == false && root != deploymentContext.getRoot())
      {
         // TODO - synch
      }
      return modified;
   }

   public void addStructureRoot(VirtualFile root)
   {
      delegate.addStructureRoot(root);
   }

   public void removeStructureRoot(VirtualFile root)
   {
      delegate.removeStructureRoot(root);
   }
}