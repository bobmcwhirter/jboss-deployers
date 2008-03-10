/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.deployers.vfs.plugins.structure;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.structure.spi.helpers.AbstractDeploymentContext;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentContext;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentResourceLoader;
import org.jboss.logging.Logger;
import org.jboss.virtual.VFSUtils;
import org.jboss.virtual.VirtualFile;

/**
 * AbstractVFSDeploymentContext.
 * 
 * @author <a href="adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class AbstractVFSDeploymentContext extends AbstractDeploymentContext implements VFSDeploymentContext
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 4474515937180482776L;

   /** The log */
   private static final Logger log = Logger.getLogger(AbstractVFSDeploymentContext.class);

   /** The root virtual file */
   private VirtualFile root;
   
   /** The meta data locations */
   private List<VirtualFile> metaDataLocations;
   
   /** The class paths */
   private List<VirtualFile> classPath;
   
   /** The loader */
   private transient VFSDeploymentResourceLoader loader;

   /**
    * Get the vfs file name safely
    * 
    * @param root the virutal file
    * @return the name
    */
   static final String safeVirtualFileName(VirtualFile root)
   {
      if (root == null)
         throw new IllegalArgumentException("Null root");
      try
      {
         return root.toURI().toString();
      }
      catch (Exception e)
      {
         return root.getName();
      }
   }

   /**
    * For serialization
    */
   public AbstractVFSDeploymentContext()
   {
   }

   /**
    * Create a new AbstractVFSDeploymentContext.
    * 
    * @param name the name
    * @param simpleName the simple name
    * @param root the virtual file
    * @param relativePath the relative path
    */
   public AbstractVFSDeploymentContext(String name, String simpleName, VirtualFile root, String relativePath)
   {
      super(name, simpleName, relativePath);
      this.root = root;
   }

   /**
    * Create a new AbstractVFSDeploymentContext.
    * 
    * @param root the virtual file
    * @param relativePath the relative path
    */
   public AbstractVFSDeploymentContext(VirtualFile root, String relativePath)
   {
      super(safeVirtualFileName(root), root.getName(), relativePath);
      this.root = root;
   }

   public VirtualFile getRoot()
   {
      return root;
   }
   
   public void setMetaDataPath(List<String> paths)
   {
      if (paths == null)
      {
         setMetaDataLocations(null);
         return;
      }

      try
      {
         List<VirtualFile> locations = new ArrayList<VirtualFile>();
         for (String path : paths)
         {
            if (path == null)
               throw new IllegalArgumentException("Null path in paths: " + paths);

            VirtualFile child = root.getChild(path);
            if (child != null)
               locations.add(child);
            else
               log.debug("Meta data path does not exist: root=" + root.getPathName() + " path=" + path);
         }
         setMetaDataLocations(locations);
      }
      catch (IOException e)
      {
         log.warn("Exception while applying paths: root=" + root.getPathName() + " paths=" + paths);
      }
   }

   public List<VirtualFile> getMetaDataLocations()
   {
      if (metaDataLocations == null)
         return Collections.emptyList();
      
      return metaDataLocations;
   }

   public void setMetaDataLocations(List<VirtualFile> locations)
   {
      this.metaDataLocations = locations;
   }
   
   public VirtualFile getMetaDataFile(String name)
   {
      if (name == null)
         throw new IllegalArgumentException("Null name");
      try
      {
         // There isn't a metadata locations so let's see whether the root matches.
         if (metaDataLocations == null || metaDataLocations.isEmpty())
         {
            // It has to be a plain file
            if (root != null && SecurityActions.isLeaf(root))
            {
               String fileName = root.getName();
               if (fileName.equals(name))
                  return root;
            }
            
            // No match
            return null;
         }
         // Look in the meta data locations
         return searchMetaDataLocations(name);
      }
      catch (Exception e)
      {
         if (log.isTraceEnabled())
            log.trace("Error retrieving meta data: " + name + " reason=" + e);
         return null;
      }
   }

   /**
    * Search the metadata locations.
    * In this impl the first one matching is returned.
    *
    * @param name the file name to find
    * @return found file or null if not found
    */
   protected VirtualFile searchMetaDataLocations(String name)
   {
      VirtualFile result = null;
      for(VirtualFile location : getMetaDataLocations())
      {
         try
         {
            result = location.getChild(name);
            if (result != null)
            {
               if (log.isTraceEnabled())
                  log.trace("Found " + name + " in " + location.getName());
               deployed();
               break;
            }
         }
         catch (IOException e)
         {
            log.debug("Search exception invocation for metafile " + name + " in " + location.getName() + ", reason: " + e);
         }
      }
      return result;
   }

   public List<VirtualFile> getMetaDataFiles(String name, String suffix)
   {
      if (name == null && suffix == null)
         throw new IllegalArgumentException("Null name and suffix");
      try
      {
         // There isn't a metadata location so let's see whether the root matches.
         // i.e. the top level is an xml
         if (metaDataLocations == null || metaDataLocations.isEmpty())
         {
            // It has to be a plain file
            if (root != null && SecurityActions.isLeaf(root))
            {
               String fileName = root.getName();
               if (name != null && fileName.equals(name))
                  return Collections.singletonList(root);
               if (suffix != null && fileName.endsWith(suffix))
                  return Collections.singletonList(root);
            }
            
            // No match
            return Collections.emptyList();
         }
         // Look in the meta data location
         List<VirtualFile> results = new ArrayList<VirtualFile>();
         for (VirtualFile location : metaDataLocations)
         {
            List<VirtualFile> result = location.getChildren(new MetaDataMatchFilter(name, suffix));
            if (result != null && result.isEmpty() == false)
            {
               if (log.isTraceEnabled())
                  log.trace("Found " + name + " in " + location.getName());
               results.addAll(result);
               deployed();
            }
         }
         return results;
      }
      catch (Exception e)
      {
         log.debug("Error retrieving meta data: name=" + name + " suffix=" + suffix, e);
         return Collections.emptyList();
      }
   }

   public VirtualFile getFile(String name)
   {
      return getResourceLoader().getFile(name);
   }

   public List<VirtualFile> getClassPath()
   {
      return classPath;
   }

   public void setClassPath(List<VirtualFile> paths)
   {
      this.classPath = paths;
      if (log.isTraceEnabled() && paths != null)
         log.trace("ClassPath for " + root.getPathName() + " is " + VFSUtils.getPathsString(paths));
   }

   public void addClassPath(List<VirtualFile> files)
   {
      if (files == null)
         throw new IllegalArgumentException("Null files");

      List<VirtualFile> classPath = getClassPath();
      if (classPath == null)
         classPath = new ArrayList<VirtualFile>();
      
      for (VirtualFile file : files)
      {
         if (file == null)
            throw new IllegalArgumentException("Null virtual file in " + files);
         classPath.add(file);
      }
      setClassPath(classPath);
   }

   public void addClassPath(VirtualFile... files)
   {
      if (files == null)
         throw new IllegalArgumentException("Null files");

      List<VirtualFile> classPath = getClassPath();
      if (classPath == null)
         classPath = new ArrayList<VirtualFile>();
      
      for (VirtualFile file : files)
      {
         if (file == null)
            throw new IllegalArgumentException("Null virtual file in " + files);
         classPath.add(file);
      }
      setClassPath(classPath);
   }

   @Override
   public VFSDeploymentContext getTopLevel()
   {
      return (VFSDeploymentContext) super.getTopLevel();
   }

   @Override
   public VFSDeploymentResourceLoader getResourceLoader()
   {
      if (loader != null)
         return loader;
      
      loader = new VFSDeploymentResourceLoaderImpl(getRoot());
      return loader;
   }

   protected DeploymentUnit createDeploymentUnit()
   {
      return new AbstractVFSDeploymentUnit(this);
   }

   @SuppressWarnings("unchecked")
   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
   {
      super.readExternal(in);
      root = (VirtualFile) in.readObject();
      boolean isNullOrEmpty = in.readBoolean();
      if (isNullOrEmpty == false)
         metaDataLocations = (List<VirtualFile>) in.readObject();
      classPath = (List) in.readObject();
   }

   /**
    * @serialData root
    * @serialData metaDataLocation
    * @serialData classPath
    * @param out the output
    * @throws IOException for any error
    */
   public void writeExternal(ObjectOutput out) throws IOException
   {
      super.writeExternal(out);
      out.writeObject(root);
      boolean isNullOrEmpty = metaDataLocations == null || metaDataLocations.isEmpty();
      out.writeBoolean(isNullOrEmpty);
      if (isNullOrEmpty == false)
         out.writeObject(metaDataLocations);
      out.writeObject(classPath);
   }
}
