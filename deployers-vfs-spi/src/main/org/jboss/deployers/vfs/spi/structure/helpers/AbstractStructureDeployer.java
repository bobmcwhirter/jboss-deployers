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
import java.util.ArrayList;
import java.util.List;

import org.jboss.deployers.spi.structure.ClassPathEntry;
import org.jboss.deployers.spi.structure.ContextInfo;
import org.jboss.deployers.spi.structure.StructureMetaData;
import org.jboss.deployers.spi.structure.StructureMetaDataFactory;
import org.jboss.deployers.vfs.spi.structure.StructureDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSStructuralDeployers;
import org.jboss.logging.Logger;
import org.jboss.virtual.VFSUtils;
import org.jboss.virtual.VirtualFile;
import org.jboss.virtual.VirtualFileVisitor;
import org.jboss.virtual.VisitorAttributes;
import org.jboss.util.collection.CollectionsFactory;

/**
 * AbstractStructureDeployer.<p>
 * 
 * We don't care about the order by default.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 * @version $Revision: 1.1 $
 */
public abstract class AbstractStructureDeployer implements StructureDeployer
{
   /** The log */
   protected Logger log = Logger.getLogger(getClass());
   
   /** The relative order */
   private int relativeOrder = Integer.MAX_VALUE;

   /** The candidate structure visitor factory */
   private CandidateStructureVisitorFactory candidateStructureVisitorFactory = DefaultCandidateStructureVisitorFactory.INSTANCE;

   /** The context info order */
   private Integer contextInfoOrder;

   /**
    * Get the relative path between two virtual files
    * 
    * @param parent the parent
    * @param child the child
    * @return the relative path
    */
   public static final String getRelativePath(VirtualFile parent, VirtualFile child)
   {
      if (child == null)
         throw new IllegalArgumentException("Null child");
      
      String childPath = child.getPathName();
      if (parent != null)
      {
         String parentPath = parent.getPathName();
         
         if (parentPath.length() == childPath.length())
            return "";
         
         // Not sure about this? It is obviously not a direct child if it is shorter?
         if (parentPath.length() < childPath.length())
         {
            if (parentPath.endsWith("/") == false)
               parentPath = parentPath + "/";
            if (childPath.startsWith(parentPath))
                return childPath.substring(parentPath.length());
         }
      }
      
      if (childPath.endsWith("/"))
         childPath = childPath.substring(0, childPath.length()-1);

      return childPath;
   }

   public int getRelativeOrder()
   {
      return relativeOrder;
   }
   
   public void setRelativeOrder(int order)
   {
      this.relativeOrder = order;
   }

   public void setContextInfoOrder(Integer contextInfoOrder)
   {
      this.contextInfoOrder = contextInfoOrder;
   }

   /**
    * Get the candidateStructureVisitorFactory.
    * 
    * @return the candidateStructureVisitorFactory.
    */
   public CandidateStructureVisitorFactory getCandidateStructureVisitorFactory()
   {
      return candidateStructureVisitorFactory;
   }

   /**
    * Set the candidateStructureVisitorFactory.
    * 
    * @param candidateStructureVisitorFactory the candidateStructureVisitorFactory.
    * @throws IllegalArgumentException for a null candidate structure
    */
   public void setCandidateStructureVisitorFactory(CandidateStructureVisitorFactory candidateStructureVisitorFactory)
   {
      if (candidateStructureVisitorFactory == null)
         throw new IllegalArgumentException("Null candidateStructureVisitorFactory");
      this.candidateStructureVisitorFactory = candidateStructureVisitorFactory;
   }

   /**
    * See if a file corresponds to a top-level deployment.
    * 
    * @param parent the parent file
    * @return true when top level
    */
   public boolean isTopLevel(VirtualFile parent)
   {
      return parent == null;
   }

   /**
    * Add an entry to the context classpath.
    * 
    * @param root - the root file the classpath entry should be relative to
    * @param entry - the candidate file to add as a classpath entry
    * @param includeEntry - a flag indicating if the entry should be added to
    *    the classpath
    * @param includeRootManifestCP - a flag indicating if the entry metainf
    *    manifest classpath should be included.
    * @param context - the context to populate
    * @throws IOException on any IO error
    */
   protected void addClassPath(VirtualFile root, VirtualFile entry, boolean includeEntry, boolean includeRootManifestCP, ContextInfo context) throws IOException
   {
      boolean trace = log.isTraceEnabled();
      
      List<VirtualFile> paths = new ArrayList<VirtualFile>();

      // The path we have been told to add
      if (includeEntry)
         paths.add(entry);

      // Add the manifest locations
      if (includeRootManifestCP && isLeaf(entry) == false)
      {
         try
         {
            VFSUtils.addManifestLocations(entry, paths);
         }
         catch(Exception e)
         {
            if (trace)
               log.trace("Failed to add manifest locations", e);
         }
      }

      // Translate from VirtualFile to relative paths
      for (VirtualFile vf : paths)
      {
         String entryPath = getRelativePath(root, vf);
         ClassPathEntry cpe = StructureMetaDataFactory.createClassPathEntry(entryPath);
         context.addClassPathEntry(cpe);
         if (trace)
            log.trace("Added classpath entry " + entryPath + " for " + vf.getName() + " from " + root);
      }
   }

   /**
    * Add all children as candidates
    * 
    * @param root the root context
    * @param parent the parent context
    * @param metaData the structure meta data
    * @param deployers the structure deployers
    * @throws Exception for any error
    */
   protected void addAllChildren(VirtualFile root, VirtualFile parent, StructureMetaData metaData, VFSStructuralDeployers deployers) throws Exception
   {
      addChildren(root, parent, metaData, deployers, null);
   }

   /**
    * Add all children as candidates
    * 
    * @param root the root context
    * @param parent the parent context
    * @param metaData the structure meta data
    * @param deployers the structure deployers
    * @param attributes the visitor attributes uses {@link VisitorAttributes#DEFAULT} when null
    * @throws Exception for any error
    */
   protected void addChildren(VirtualFile root, VirtualFile parent, StructureMetaData metaData, VFSStructuralDeployers deployers, VisitorAttributes attributes) throws Exception
   {
      if (parent == null)
         throw new IllegalArgumentException("Null parent");
      
      VirtualFileVisitor visitor = candidateStructureVisitorFactory.createVisitor(root, parent, metaData, deployers, attributes);
      parent.visit(visitor);
   }
   
   /**
    * Tests whether the virtual file is a leaf
    * 
    * @param file the virtual file
    * @return true when it is a leaf
    * @throws IOException for any error
    */
   protected boolean isLeaf(VirtualFile file) throws IOException
   {
      return SecurityActions.isLeaf(file);
   }
   
   /**
    * Create a context
    * 
    * @param root the root context
    * @param structureMetaData the structure metadata
    * @return the context info
    * @throws IllegalArgumentException for a null root or structure metaData
    */
   protected ContextInfo createContext(VirtualFile root, StructureMetaData structureMetaData)
   {
      return createContext(root, (String)null, structureMetaData);
   }

   /**
    * Create a context
    *
    * @param root the root context
    * @param metaDataPath the metadata path
    * @param structureMetaData the structure metadata
    * @return the context info
    * @throws IllegalArgumentException for a null root or structure metaData
    */
   protected ContextInfo createContext(VirtualFile root, String metaDataPath, StructureMetaData structureMetaData)
   {
      ContextInfo result = applyMetadataPath(root, metaDataPath);
      applyStructure(root, structureMetaData, result);
      return result;
   }

   /**
    * Apply metadata on root to create context.
    *
    * @param root the root context
    * @param metaDataPath the metadata path
    * @return the context info
    */
   protected ContextInfo applyMetadataPath(VirtualFile root, String metaDataPath)
   {
      if (root == null)
         throw new IllegalArgumentException("Null root");

      // Determine whether the metadata path exists
      if (metaDataPath != null)
      {
         try
         {
            VirtualFile child = root.getChild(metaDataPath);
            if (child == null)
               metaDataPath = null;
         }
         catch (IOException e)
         {
            log.warn("Not using metadata path " + metaDataPath + " for " + root.getName() + " reason: " + e.getMessage());
            metaDataPath = null;
         }
      }

      // Create and link the context
      if (metaDataPath != null)
         return StructureMetaDataFactory.createContextInfo("", metaDataPath, null);
      else
         return StructureMetaDataFactory.createContextInfo("", null);
   }

   /**
    * Create a context
    *
    * @param root the root context
    * @param metaDataPaths the metadata paths
    * @param structureMetaData the structure metadata
    * @return the context info
    * @throws IllegalArgumentException for a null root or structure metaData
    */
   protected ContextInfo createContext(VirtualFile root, String[] metaDataPaths, StructureMetaData structureMetaData)
   {
      ContextInfo result = applyMetadataPaths(root, metaDataPaths);
      applyStructure(root, structureMetaData, result);
      return result;
   }

   /**
    * Apply metadata on root to create context.
    *
    * @param root the root context
    * @param metaDataPaths the metadata paths
    * @return the context info
    */
   protected ContextInfo applyMetadataPaths(VirtualFile root, String[] metaDataPaths)
   {
      if (root == null)
         throw new IllegalArgumentException("Null root");

      List<String> metaDataPath = CollectionsFactory.createLazyList();
      // Determine whether the metadata paths exists
      if (metaDataPaths != null && metaDataPaths.length > 0)
      {
         for(String path : metaDataPaths)
         {
            try
            {
               VirtualFile child = root.getChild(path);
               if (child != null)
                  metaDataPath.add(path);
            }
            catch (IOException e)
            {
               log.warn("Not using metadata path " + path + " for " + root.getName() + " reason: " + e.getMessage());
            }
         }
      }

      // Create and link the context
      if (metaDataPath.isEmpty())
         return StructureMetaDataFactory.createContextInfo("", null);
      else
         return StructureMetaDataFactory.createContextInfo("", metaDataPath, null);
   }

   /**
    * Apply structure metadata on context.
    *
    * @param root the root context
    * @param structureMetaData the structure metadata
    * @param context the new created context
    */
   protected void applyStructure(VirtualFile root, StructureMetaData structureMetaData, ContextInfo context)
   {
      boolean trace = log.isTraceEnabled();

      if (root == null)
         throw new IllegalArgumentException("Null root");
      if (structureMetaData == null)
         throw new IllegalArgumentException("Null structure metadata");

      applyContextInfo(root, context);

      structureMetaData.addContext(context);
      if (trace)
         log.trace("Added context " + context + " from " + root.getName());
   }

   /**
    * Apply context info.
    * Can be overridden for specific root.
    *
    * @param root the root file
    * @param result the new context info
    */
   protected void applyContextInfo(VirtualFile root, ContextInfo result)
   {
      if (result != null && contextInfoOrder != null)
         result.setRelativeOrder(contextInfoOrder);
   }
}
