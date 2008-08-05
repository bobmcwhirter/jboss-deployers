/*
* JBoss, Home of Professional Open Source
* Copyright 2007, JBoss Inc., and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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
package org.jboss.deployers.vfs.spi.structure;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.structure.ContextInfo;
import org.jboss.deployers.spi.structure.StructureMetaData;
import org.jboss.virtual.VirtualFile;

/**
 * StructureContext.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class StructureContext
{
   /** The root of the deployment */
   private VirtualFile root;
   
   /** The parent virtual file */
   private VirtualFile parent;
   
   /** The current candidate file */
   private VirtualFile file;
   
   /** The structure metadata */
   private StructureMetaData metaData;
   
   /** The structural deployers */
   private VFSStructuralDeployers deployers;
   
   /** The parent structure context */
   private StructureContext parentContext;

   /**
    * Helper method to check parent is not null before retrieving parameters
    * 
    * @param parentContext the parent context
    * @return the root
    * @throws IllegalArgumentException for a null parent
    */
   private static VirtualFile getRoot(StructureContext parentContext)
   {
      if (parentContext == null)
         throw new IllegalArgumentException("Null parentContext");
      return parentContext.getRoot();
   }
   
   /**
    * Create a new structure context from a root 
    * 
    * @param root the root file
    * @param parent the parent file 
    * @param file the current file
    * @param metaData the structure metadata to build
    * @param deployers the available structure deployers
    * @param parentContext the parentContext
    * @throws IllegalArgumentException for a null parameter (parents can be null)
    */
   public StructureContext(VirtualFile root, VirtualFile parent, VirtualFile file, StructureMetaData metaData, VFSStructuralDeployers deployers, StructureContext parentContext)
   {
      if (root == null)
         throw new IllegalArgumentException("Null root");
      if (file == null)
         throw new IllegalArgumentException("Null file");
      if (metaData == null)
         throw new IllegalArgumentException("Null structure metadata");
      if (deployers == null)
         throw new IllegalArgumentException("Null structural deployers");

      this.root = root;
      this.parent = parent;
      this.file = file;
      this.metaData = metaData;
      this.deployers = deployers;
      this.parentContext = parentContext;
   }

   /**
    * Create a new structure context from a root 
    * 
    * @param root the root file
    * @param metaData the structure metadata to build
    * @param deployers the available structure deployers
    * @throws IllegalArgumentException for a null parameter
    */
   public StructureContext(VirtualFile root, StructureMetaData metaData, VFSStructuralDeployers deployers)
   {
      this(root, null, root, metaData, deployers, null);
   }

   /**
    * Create a new child structure context 
    * 
    * @param file the candidate file
    * @param metaData the structure metadata to build
    * @param parentContext the parentContext
    * @throws IllegalArgumentException for a null parameter
    */
   public StructureContext(VirtualFile file, StructureMetaData metaData, StructureContext parentContext)
   {
      this(getRoot(parentContext), parentContext.getFile(), file, metaData, parentContext.getDeployers(), parentContext);
   }

   /**
    * Get the file name
    * 
    * @return the file name
    */
   public String getName()
   {
      return file.getName();
   }
   
   /**
    * Get the root.
    * 
    * @return the root.
    */
   public VirtualFile getRoot()
   {
      return root;
   }

   /**
    * Get the parent.
    * 
    * @return the parent.
    */
   public VirtualFile getParent()
   {
      return parent;
   }

   /**
    * Whether this is top level.
    * 
    * @return true for top level.
    */
   public boolean isTopLevel()
   {
      return parent == null;
   }

   /**
    * Get the file.
    * 
    * @return the file.
    */
   public VirtualFile getFile()
   {
      return file;
   }

   /**
    * Get the metaData.
    * 
    * @return the metaData.
    */
   public StructureMetaData getMetaData()
   {
      return metaData;
   }

   /**
    * Get the deployers.
    * 
    * @return the deployers.
    */
   public VFSStructuralDeployers getDeployers()
   {
      return deployers;
   }

   /**
    * Get the parentContext.
    * 
    * @return the parentContext.
    */
   public StructureContext getParentContext()
   {
      return parentContext;
   }
   
   /**
    * Determine the child structure
    * 
    * @param child the child
    * @throws DeploymentException for any error
    */
   public void determineChildStructure(VirtualFile child) throws DeploymentException
   {
      if (child == null)
         throw new IllegalArgumentException("Null child");
      
      getDeployers().determineStructure(child, this);
   }
   
   /**
    * Add a context
    * 
    * @param child the child
    */
   public void addChild(ContextInfo child)
   {
      if (child == null)
         throw new IllegalArgumentException("Null child");
      
      getMetaData().addContext(child);
   }
}