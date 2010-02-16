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

import org.jboss.deployers.spi.structure.ContextInfo;
import org.jboss.deployers.spi.structure.StructureMetaData;
import org.jboss.deployers.vfs.spi.structure.StructureContext;
import org.jboss.logging.Logger;
import org.jboss.vfs.VirtualFile;
import org.jboss.vfs.VirtualFileFilter;
import org.jboss.vfs.VisitorAttributes;
import org.jboss.vfs.util.AbstractVirtualFileVisitor;

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

   /** The structure context */
   private StructureContext context;

   /** Ignore directories */
   private boolean ignoreDirectories;

   /** A filter */
   private VirtualFileFilter filter;
   
   /**
    * Create a new CandidateStructureVisitor.
    * 
    * @param context the context
    * @throws IllegalArgumentException for a null parent
    */
   public AbstractCandidateStructureVisitor(StructureContext context)
   {
      this(context, null);
   }
   
   /**
    * Create a new CandidateStructureVisitor.
    * 
    * @param context the context
    * @param attributes the attributes
    * @throws IllegalArgumentException for a null parent
    */
   public AbstractCandidateStructureVisitor(StructureContext context, VisitorAttributes attributes)
   {
      super(attributes);
      if (context == null)
         throw new IllegalArgumentException("Null context");
      this.context = context;
   }

   /**
    * Get the parent deployment context
    * 
    * @return the parent.
    */
   public VirtualFile getParent()
   {
      return context.getFile();
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
      String path = AbstractStructureDeployer.getRelativePath(context, file);
      StructureMetaData metaData = context.getMetaData();
      ContextInfo contextInfo = metaData.getContext(path);
      if (contextInfo == null)
      {
         // Ignore directories when asked
         if (ignoreDirectories && file.isDirectory())
            return;
         // Apply any filter
         if (filter != null && filter.accepts(file) == false)
            return;

         try
         {
            // Ask the deployers to process this file
            context.determineChildStructure(file);
         }
         catch (Exception e)
         {
            log.debugf("Ignoring %1s reason=%2s", file, e);
         }
      }
   }
}
