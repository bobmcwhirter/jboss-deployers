/*
* JBoss, Home of Professional Open Source
* Copyright 2006, JBoss Inc., and individual contributors as indicated
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
package org.jboss.deployers.vfs.spi.structure.helpers;

import java.io.IOException;

import org.jboss.deployers.spi.structure.ContextInfo;
import org.jboss.deployers.spi.structure.StructureMetaData;
import org.jboss.deployers.vfs.spi.structure.VFSStructuralDeployers;
import org.jboss.logging.Logger;
import org.jboss.virtual.VirtualFile;
import org.jboss.virtual.VirtualFileFilter;
import org.jboss.virtual.VisitorAttributes;
import org.jboss.virtual.plugins.vfs.helpers.AbstractVirtualFileVisitor;

/**
 * Visits the structure and creates candidates
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class AbstractCandidateStructureVisitor extends AbstractVirtualFileVisitor
{
   /** The log */
   private static final Logger log = Logger.getLogger(AbstractCandidateStructureVisitor.class);

   /** The root deployment file */
   private final VirtualFile root;

   /** The parent deployment file */
   private final VirtualFile parent;

   /** The meta data */
   private final StructureMetaData metaData;
   
   /** The deployers */
   private final VFSStructuralDeployers deployers;

   /** Ignore directories */
   private boolean ignoreDirectories;

   /** A filter */
   private VirtualFileFilter filter;
   
   /**
    * Create a new CandidateStructureVisitor.
    * 
    * @param root the root
    * @param parent the parent
    * @param metaData the structure meta data
    * @param deployers the structure deployers
    * @throws IllegalArgumentException for a null parent
    */
   public AbstractCandidateStructureVisitor(VirtualFile root, VirtualFile parent, StructureMetaData metaData, VFSStructuralDeployers deployers)
   {
      this(root, parent, metaData, deployers, null);
   }
   
   /**
    * Create a new CandidateStructureVisitor.
    * 
    * @param root the root
    * @param parent the parent
    * @param metaData the structure meta data
    * @param deployers the structure deployers
    * @param attributes the attributes
    * @throws IllegalArgumentException for a null parent
    */
   public AbstractCandidateStructureVisitor(VirtualFile root, VirtualFile parent, StructureMetaData metaData, VFSStructuralDeployers deployers, VisitorAttributes attributes)
   {
      super(attributes);
      if (parent == null)
         throw new IllegalArgumentException("Null parent");
      this.root = root;
      this.parent = parent;
      this.metaData = metaData;
      this.deployers = deployers;
   }

   /**
    * Get the parent deployment context
    * 
    * @return the parent.
    */
   public VirtualFile getParent()
   {
      return parent;
   }

   /**
    * Get the ignoreDirectories.
    * 
    * @return the ignoreDirectories.
    */
   public boolean isIgnoreDirectories()
   {
      return ignoreDirectories;
   }

   /**
    * Get the filter.
    * 
    * @return the filter.
    */
   public VirtualFileFilter getFilter()
   {
      return filter;
   }

   /**
    * Set the filter.
    * 
    * @param filter the filter.
    */
   public void setFilter(VirtualFileFilter filter)
   {
      this.filter = filter;
   }

   /**
    * Set the ignoreDirectories.
    * 
    * @param ignoreDirectories the ignoreDirectories.
    */
   public void setIgnoreDirectories(boolean ignoreDirectories)
   {
      this.ignoreDirectories = ignoreDirectories;
   }

   public void visit(VirtualFile file)
   {
      String path = AbstractStructureDeployer.getRelativePath(parent, file);
      ContextInfo context = metaData.getContext(path);
      if (context == null)
      {
         // Ignore directories when asked
         try
         {
            if (ignoreDirectories && SecurityActions.isLeaf(file) == false)
               return;
         }
         catch (IOException e)
         {
            log.debug("Ignoring " + file + " reason=" + e);
            return;
         }
         
         // Apply any filter
         if (filter != null && filter.accepts(file) == false)
            return;

         try
         {
            // Ask the deployers to process this file
            deployers.determineStructure(root, parent, file, metaData);
         }
         catch (Exception e)
         {
            log.debug("Ignoring " + file + " reason=" + e);
         }
      }
   }
}
