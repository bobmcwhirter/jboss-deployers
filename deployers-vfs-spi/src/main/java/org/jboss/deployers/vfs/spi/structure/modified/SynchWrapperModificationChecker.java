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
import org.jboss.virtual.VisitorAttributes;

/**
 * Synch wrapper modification checker.
 *
 * If there is no modification, we check if the deployment is perhaps a temp,
 * only then checking if we need to update some resource.
 * e.g. some .jsp or .xhtml file for JBossWeb to pick up the change
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class SynchWrapperModificationChecker extends AbstractStructureModificationChecker<Long>
{
   /** The true checker delegate */
   private AbstractStructureModificationChecker<Long> delegate;

   /** The synch adapter */
   private SynchAdapter synchAdapter;

   /** the visitor attributes */
   private VisitorAttributes originalAttributes;
   private VisitorAttributes tempAttributes;

   public SynchWrapperModificationChecker(AbstractStructureModificationChecker<Long> delegate, SynchAdapter synchAdapter)
   {
      if (delegate == null)
         throw new IllegalArgumentException("Null delegate");
      if (synchAdapter == null)
         throw new IllegalArgumentException("Null synch adapter");

      this.delegate = delegate;
      this.synchAdapter = synchAdapter;
   }

   @Override
   protected StructureCache<Long> getCache()
   {
      return delegate.getCache();
   }

   @Override
   protected MainDeployerStructure getMainDeployerStructure()
   {
      return delegate.getMainDeployerStructure();
   }

   @Override
   protected boolean hasRootBeenModified(VirtualFile root) throws IOException
   {
      return delegate.hasRootBeenModified(root);
   }

   protected boolean hasStructureBeenModifed(VirtualFile root, VFSDeploymentContext deploymentContext) throws IOException
   {
      boolean modified = delegate.hasStructureBeenModifed(root, deploymentContext);
      // it was not modifed & we're actually temped
      if (modified == false && root != deploymentContext.getRoot())
      {
         // check for update or delete
         UpdateDeleteVisitor udVisitor = new UpdateDeleteVisitor(tempAttributes, getCache(), synchAdapter, root);
         VirtualFile tempRoot = deploymentContext.getRoot();
         tempRoot.visit(udVisitor);
         // check for addition
         AddVisitor addVisitor = new AddVisitor(originalAttributes, getCache(), synchAdapter, tempRoot, root.getPathName().length());
         root.visit(addVisitor);
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

   /**
    * Set original visitor attributes.
    *
    * @param attributes the attributes
    */
   public void setOriginalAttributes(VisitorAttributes attributes)
   {
      this.originalAttributes = attributes;
   }

   /**
    * Set temp visitor attributes.
    *
    * @param attributes the attributes
    */
   public void setTempAttributes(VisitorAttributes attributes)
   {
      this.tempAttributes = attributes;
   }
}