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
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.jboss.deployers.spi.annotations.AnnotationEnvironment;
import org.jboss.deployers.spi.structure.ClassPathEntry;
import org.jboss.deployers.spi.structure.ContextInfo;
import org.jboss.deployers.spi.structure.StructureMetaData;
import org.jboss.deployers.spi.structure.StructureMetaDataFactory;
import org.jboss.deployers.vfs.spi.structure.CandidateAnnotationsCallback;
import org.jboss.deployers.vfs.spi.structure.StructureContext;
import org.jboss.deployers.vfs.spi.structure.StructureDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSStructuralDeployers;
import org.jboss.logging.Logger;
import org.jboss.util.collection.CollectionsFactory;
import org.jboss.virtual.VFSUtils;
import org.jboss.virtual.VirtualFile;
import org.jboss.virtual.VirtualFileVisitor;
import org.jboss.virtual.VisitorAttributes;

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

   /** The supports annotations flag */
   private boolean supportsCandidateAnnotations;

   /** The candidate annotations */
   private Set<Class<? extends Annotation>> candidateAnnotations;

   /**
    * Get the relative path between two virtual files
    * 
    * @param context the structure context
    * @param child the child
    * @return the relative path
    */
   public static final String getRelativePath(StructureContext context, VirtualFile child)
   {
      if (context == null)
         throw new IllegalArgumentException("Null context");

      return getRelativePath(context.getParent(), child);
   }

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
    * Get the candidate annotations.
    *
    * @return the candidate annotations
    */
   public Set<Class<? extends Annotation>> getCandidateAnnotations()
   {
      return candidateAnnotations;
   }

   /**
    * Set the candidate annotations.
    *
    * @param candidateAnnotations the candidate annotations
    */
   public void setCandidateAnnotations(Set<Class<? extends Annotation>> candidateAnnotations)
   {
      this.candidateAnnotations = candidateAnnotations;
   }

   /**
    * Add candidate annotation.
    *
    * @param annotationClass the candidate annotation class
    */
   public void addCandidateAnnotation(Class<? extends Annotation> annotationClass)
   {
      if (candidateAnnotations == null)
         candidateAnnotations = new LinkedHashSet<Class<? extends Annotation>>();
      candidateAnnotations.add(annotationClass);
   }

   public boolean isSupportsCandidateAnnotations()
   {
      return supportsCandidateAnnotations;
   }

   /**
    * Set supportsCandidateAnnotations flag.
    *
    * @param supportsCandidateAnnotations the support candidate annotations flag
    */
   public void setSupportsCandidateAnnotations(boolean supportsCandidateAnnotations)
   {
      this.supportsCandidateAnnotations = supportsCandidateAnnotations;
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
    * Add an entry to the context classpath.
    * 
    * @param structureContext - the structure context
    * @param entry - the candidate file to add as a classpath entry
    * @param includeEntry - a flag indicating if the entry should be added to
    *    the classpath
    * @param includeRootManifestCP - a flag indicating if the entry metainf
    *    manifest classpath should be included.
    * @param context - the context to populate
    * @throws IOException on any IO error
    */
   protected void addClassPath(StructureContext structureContext, VirtualFile entry, boolean includeEntry, boolean includeRootManifestCP, ContextInfo context) throws IOException
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
      VirtualFile root = structureContext.getRoot();
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
    * Create annotation environment
    *
    * @param root the deployment root
    * @return new annotation environment
    */
   protected abstract AnnotationEnvironment createAnnotationEnvironment(VirtualFile root);

   /**
    * Check for candidate annotations.
    *
    * @param context the structure context
    * @param roots the roots to check
    * @return return true if one of the roots includes some candidate annotation
    */
   protected boolean checkCandidateAnnotations(StructureContext context, VirtualFile... roots)
   {
      if (roots == null || roots.length == 0)
         throw new IllegalArgumentException("Null or empty roots");

      if (candidateAnnotations == null || candidateAnnotations.isEmpty())
         return true;

      StructureContext parentContext = context.getParentContext();
      if (parentContext == null)
         return true;

      Set<CandidateAnnotationsCallback> callbacks = parentContext.getCallbacks(CandidateAnnotationsCallback.class);
      if (callbacks.isEmpty())
         return true;

      boolean result = false;
      for(VirtualFile root : roots)
      {
         AnnotationEnvironment env = createAnnotationEnvironment(root);
         for (Class<? extends Annotation> annotationClass : candidateAnnotations)
         {
            if (env.hasClassAnnotatedWith(annotationClass))
            {
               result = true;
               for (CandidateAnnotationsCallback callback : callbacks)
                  callback.executeCallback(root, context, env, annotationClass);
            }
         }
      }
      return result;
   }

   /**
    * Add all children as candidates
    * 
    * @param context the structure context
    * @throws Exception for any error
    */
   protected void addAllChildren(StructureContext context) throws Exception
   {
      addChildren(context, null);
   }

   /**
    * Add all children as candidates
    * 
    * @param context the structure context

    * @param attributes the visitor attributes uses {@link VisitorAttributes#DEFAULT} when null
    * @throws Exception for any error
    */
   protected void addChildren(StructureContext context, VisitorAttributes attributes) throws Exception
   {
      if (context == null)
         throw new IllegalArgumentException("Null context");

      VirtualFile file = context.getFile();
      VirtualFileVisitor visitor = candidateStructureVisitorFactory.createVisitor(context, attributes);
      file.visit(visitor);
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

      StructureContext context = new StructureContext(root, null, parent, metaData, deployers, null);
      addChildren(context, attributes);
   }
   
   /**
    * Tests whether the virtual file is a leaf
    * 
    * @param file the virtual file
    * @return true when it is a leaf
    * @throws IOException for any error
    */
   protected static boolean isLeaf(VirtualFile file) throws IOException
   {
      return SecurityActions.isLeaf(file);
   }
   
   /**
    * Create a context
    * 
    * @param context the structure context
    * @return the context info
    * @throws IllegalArgumentException for a null root or structure metaData
    */
   protected ContextInfo createContext(StructureContext context)
   {
      return createContext(context, (String) null);
   }

   /**
    * Create a context
    *
    * @param context the structure context
    * @param metaDataPath the metadata path
    * @return the context info
    * @throws IllegalArgumentException for a null root or structure metaData
    */
   protected ContextInfo createContext(StructureContext context, String metaDataPath)
   {
      ContextInfo result = applyMetadataPath(context, metaDataPath);
      applyStructure(context, result);
      return result;
   }

   /**
    * Apply metadata on root to create context.
    *
    * @param context the context
    * @param metaDataPath the metadata path
    * @return the context info
    */
   protected ContextInfo applyMetadataPath(StructureContext context, String metaDataPath)
   {
      if (context == null)
         throw new IllegalArgumentException("Null context");

      // Determine whether the metadata path exists
      if (metaDataPath != null)
      {
         VirtualFile root = context.getFile();
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
    * @param context the structure context
    * @param metaDataPaths the metadata paths
    * @return the context info
    * @throws IllegalArgumentException for a null root or structure metaData
    */
   protected ContextInfo createContext(StructureContext context, String[] metaDataPaths)
   {
      ContextInfo result = applyMetadataPaths(context, metaDataPaths);
      applyStructure(context, result);
      return result;
   }

   /**
    * Apply metadata on root to create context.
    *
    * @param context the structure context
    * @param metaDataPaths the metadata paths
    * @return the context info
    */
   protected ContextInfo applyMetadataPaths(StructureContext context, String[] metaDataPaths)
   {
      if (context == null)
         throw new IllegalArgumentException("Null context");

      VirtualFile root = context.getFile();
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
    * @param context the structure context
    * @param contextInfo the new created context
    */
   protected void applyStructure(StructureContext context, ContextInfo contextInfo)
   {
      boolean trace = log.isTraceEnabled();

      if (context == null)
         throw new IllegalArgumentException("Null context");

      VirtualFile root = context.getRoot();
      applyContextInfo(context, contextInfo);
      context.addChild(contextInfo);
      if (trace)
         log.trace("Added context " + context + " from " + root.getName());
   }

   /**
    * Apply context info.
    * Can be overridden for specific root.
    *
    * @param context the structure context
    * @param result the new context info
    */
   protected void applyContextInfo(StructureContext context, ContextInfo result)
   {
      if (result != null && contextInfoOrder != null)
      {
         result.setRelativeOrder(contextInfoOrder);
      }
   }
}
