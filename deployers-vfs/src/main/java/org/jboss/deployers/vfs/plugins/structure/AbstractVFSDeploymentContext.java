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
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jboss.deployers.spi.structure.MetaDataEntry;
import org.jboss.deployers.spi.structure.MetaDataType;
import org.jboss.deployers.spi.structure.MetaDataTypeFilter;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.structure.spi.helpers.AbstractDeploymentContext;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentContext;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentResourceLoader;
import org.jboss.logging.Logger;
import org.jboss.virtual.VFSUtils;
import org.jboss.virtual.VirtualFile;
import org.jboss.virtual.VirtualFileFilter;

/**
 * AbstractVFSDeploymentContext.
 *
 * @author <a href="adrian@jboss.org">Adrian Brock</a>
 * @author <a href="ales.justin@jboss.org">Ales Justin</a>
 * @version $Revision: 1.1 $
 */
public class AbstractVFSDeploymentContext extends AbstractDeploymentContext implements VFSDeploymentContext, AbstractVFSDeploymentContextMBean
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 4474515937180482776L;

   /** The log */
   private static final Logger log = Logger.getLogger(AbstractVFSDeploymentContext.class);

   /** The root virtual file */
   private VirtualFile root;

   /** The meta data locations */
   /** This map usage is simply pair notion which simplifies API */
   private Map<VirtualFile, MetaDataType> metaDataLocations;

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

   public void setMetaDataPath(List<MetaDataEntry> paths)
   {
      if (paths == null)
      {
         setMetaDataLocations(null);
         return;
      }

      try
      {
         Map<VirtualFile, MetaDataType> locations = new LinkedHashMap<VirtualFile, MetaDataType>();
         for (MetaDataEntry entry : paths)
         {
            if (entry == null)
               throw new IllegalArgumentException("Null entry in paths: " + paths);

            String path = entry.getPath();
            VirtualFile child = root.getChild(path);
            if (child != null)
               locations.put(child, entry.getType());
            else
               log.debugf("Meta data path does not exist: root=%1s path=%2s", root.getPathName(), path);
         }
         setMetaDataLocationsMap(locations);
      }
      catch (IOException e)
      {
         log.warn("Exception while applying paths: root=" + root.getPathName() + " paths=" + paths);
      }
   }

   public List<VirtualFile> getMetaDataLocations()
   {
      return getMetaDataLocations(MetaDataTypeFilter.DEFAULT);
   }

   public List<VirtualFile> getMetaDataLocations(MetaDataTypeFilter filter)
   {
      if (filter == null)
         throw new IllegalArgumentException("Null filter");

      if (metaDataLocations == null || metaDataLocations.isEmpty())
      {
         return Collections.emptyList();
      }
      else
      {
         List<VirtualFile> result = new ArrayList<VirtualFile>();
         for(Map.Entry<VirtualFile, MetaDataType> entry : metaDataLocations.entrySet())
         {
            if (filter.accepts(entry.getValue()))
            {
               VirtualFile location = entry.getKey();
               result.add(location);
            }
         }
         return result;
      }
   }

   public void setMetaDataLocations(List<VirtualFile> locations)
   {
      Map<VirtualFile, MetaDataType> locationsMap = null;
      if (locations != null)
      {
         locationsMap = new LinkedHashMap<VirtualFile, MetaDataType>();
         for (VirtualFile file : locations)
            locationsMap.put(file, MetaDataType.DEFAULT);
      }
      setMetaDataLocationsMap(locationsMap);
   }

   /**
    * Set metadata locations map.
    *
    * @param locations the metadata locations
    */
   protected void setMetaDataLocationsMap(Map<VirtualFile, MetaDataType> locations)
   {
      this.metaDataLocations = locations;
   }

   public VirtualFile getMetaDataFile(String name)
   {
      return getMetaDataFile(name, MetaDataTypeFilter.DEFAULT);
   }

   public VirtualFile getMetaDataFile(String name, MetaDataTypeFilter filter)
   {
      if (name == null)
         throw new IllegalArgumentException("Null name");
      if (filter == null)
         throw new IllegalArgumentException("Null filter");
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
         return searchMetaDataLocations(name, filter);
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
      return searchMetaDataLocations(name, MetaDataTypeFilter.DEFAULT);
   }

   /**
    * Search the metadata locations.
    * In this impl the first one matching is returned.
    *
    * @param name the file name to find
    * @param filter the metadata type filter
    * @return found file or null if not found
    */
   protected VirtualFile searchMetaDataLocations(String name, MetaDataTypeFilter filter)
   {
      VirtualFile result = null;
      for(Map.Entry<VirtualFile, MetaDataType> entry : metaDataLocations.entrySet())
      {
         if (filter.accepts(entry.getValue()))
         {
            VirtualFile location = entry.getKey();
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
               log.debugf("Search exception invocation for metafile %1s in %2s, reason: %3s", name, location.getName(), e);
            }
         }
      }
      return result;
   }

   public List<VirtualFile> getMetaDataFiles(String name, String suffix)
   {
      return getMetaDataFiles(name, suffix, MetaDataTypeFilter.DEFAULT);
   }

   public List<VirtualFile> getMetaDataFiles(String name, String suffix, MetaDataTypeFilter mdtf)
   {
      if (name == null && suffix == null)
         throw new IllegalArgumentException("Null name and suffix");

      VirtualFileFilter filter = new MetaDataMatchFilter(name, suffix);
      return getMetaDataFiles(filter, mdtf);
   }

   public List<VirtualFile> getMetaDataFiles(VirtualFileFilter filter)
   {
      return getMetaDataFiles(filter, MetaDataTypeFilter.DEFAULT);     
   }

   public List<VirtualFile> getMetaDataFiles(VirtualFileFilter filter, MetaDataTypeFilter mdtf)
   {
      if (filter == null || mdtf == null)
      {
         // we don't wanna guess what needs to be filtered
         // if all is what you want, use your own ALL filter
         throw new IllegalArgumentException("Null filter - VFS or MetaDataType: vfs=" + filter + ", mdt=" + mdtf);
      }

      try
      {
         // There isn't a metadata location so let's see whether the root matches.
         // i.e. the top level is an xml
         if (metaDataLocations == null || metaDataLocations.isEmpty())
         {
            // It has to be a plain file
            if (root != null && SecurityActions.isLeaf(root) && filter.accepts(root))
            {
               return Collections.singletonList(root);
            }
            else
            {
               // No match
               return Collections.emptyList();
            }
         }
         // Look in the meta data location
         List<VirtualFile> results = new ArrayList<VirtualFile>();
         for(Map.Entry<VirtualFile, MetaDataType> entry : metaDataLocations.entrySet())
         {
            if (mdtf.accepts(entry.getValue()))
            {
               VirtualFile location = entry.getKey();
               List<VirtualFile> result = location.getChildren(filter);
               if (result != null && result.isEmpty() == false)
               {
                  if (log.isTraceEnabled())
                     log.trace("Found results with " + filter + " in " + location.getName());
                  results.addAll(result);
                  deployed();
               }
            }
         }
         return results;
      }
      catch (Exception e)
      {
         log.debugf(e, "Error retrieving meta data: filter=%1s", filter);
         return Collections.emptyList();
      }
   }

   public void prependMetaDataLocation(VirtualFile... locations)
   {
      if (locations == null)
         throw new IllegalArgumentException("Null locations");

      Map<VirtualFile, MetaDataType> locationsMap = new LinkedHashMap<VirtualFile, MetaDataType>();
      for (int i = locations.length-1; i >= 0; --i)
      {
         VirtualFile location = locations[i];
         if (location == null)
            throw new IllegalArgumentException("Null virtual file in " + Arrays.toString(locations));
         locationsMap.put(location, MetaDataType.DEFAULT);
      }

      // add the old ones
      if (metaDataLocations != null)
         locationsMap.putAll(metaDataLocations);

      setMetaDataLocationsMap(locationsMap);
   }

   public void appendMetaDataLocation(VirtualFile... locations)
   {
      if (locations == null)
         throw new IllegalArgumentException("Null location");

      Map<VirtualFile, MetaDataType> locationsMap = new LinkedHashMap<VirtualFile, MetaDataType>();
      if (metaDataLocations != null)
         locationsMap.putAll(metaDataLocations); // add the old ones

      for (VirtualFile location : locations)
      {
         if (location == null)
            throw new IllegalArgumentException("Null virtual file in " + Arrays.toString(locations));
         locationsMap.put(location, MetaDataType.DEFAULT);
      }
      setMetaDataLocationsMap(locationsMap);
   }

   public void removeMetaDataLocation(VirtualFile... locations)
   {
      if (locations == null || locations.length == 0)
         return;

      for (VirtualFile location : locations)
      {
         metaDataLocations.remove(location);
      }
   }

   public VirtualFile getFile(String name)
   {
      return getResourceLoader().getFile(name);
   }

   /**
    * Get mutable classpath.
    *
    * @return the mutable classpath
    */
   protected List<VirtualFile> getMutableClassPath()
   {
      return classPath;
   }

   public List<VirtualFile> getClassPath()
   {
      if (classPath == null || classPath.isEmpty())
      {
         return Collections.emptyList();
      }
      else
      {
         return Collections.unmodifiableList(classPath);
      }
   }

   public void setClassPath(List<VirtualFile> paths)
   {
      this.classPath = paths;
      if (log.isTraceEnabled() && paths != null)
         log.trace("ClassPath for " + root.getPathName() + " is " + VFSUtils.getPathsString(paths));
   }

   public void appendClassPath(List<VirtualFile> files)
   {
      if (files == null)
         throw new IllegalArgumentException("Null files");

      List<VirtualFile> classPath = getMutableClassPath();
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

   public void prependClassPath(VirtualFile... files)
   {
      if (files == null)
         throw new IllegalArgumentException("Null files");

      List<VirtualFile> classPath = getMutableClassPath();
      if (classPath == null)
         classPath = new ArrayList<VirtualFile>();

      for (int i = files.length-1; i >= 0; --i)
      {
         VirtualFile file = files[i];
         if (file == null)
            throw new IllegalArgumentException("Null virtual file in " + Arrays.toString(files));
         classPath.add(0, file);
      }
      setClassPath(classPath);
   }

   public void prependClassPath(List<VirtualFile> files)
   {
      if (files == null)
         throw new IllegalArgumentException("Null files");

      List<VirtualFile> classPath = getMutableClassPath();
      if (classPath == null)
         classPath = new ArrayList<VirtualFile>();

      for (int i = files.size()-1; i >= 0; --i)
      {
         VirtualFile file = files.get(i);
         if (file == null)
            throw new IllegalArgumentException("Null virtual file in " + files);
         classPath.add(0, file);
      }
      setClassPath(classPath);
   }

   public void appendClassPath(VirtualFile... files)
   {
      if (files == null)
         throw new IllegalArgumentException("Null files");

      List<VirtualFile> classPath = getMutableClassPath();
      if (classPath == null)
         classPath = new ArrayList<VirtualFile>();

      for (VirtualFile file : files)
      {
         if (file == null)
            throw new IllegalArgumentException("Null virtual file in " + Arrays.toString(files));
         classPath.add(file);
      }
      setClassPath(classPath);
   }

   public void removeClassPath(VirtualFile... files)
   {
      if (files == null || files.length == 0)
         return;

      for (VirtualFile file : files)
      {
         classPath.remove(file);
      }
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

   @Override
   public void cleanup()
   {
      try
      {
         root.cleanup();
      }
      finally
      {
         super.cleanup();
      }
   }
   
   @SuppressWarnings("unchecked")
   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
   {
      super.readExternal(in);
      root = (VirtualFile) in.readObject();
      boolean isNullOrEmpty = in.readBoolean();
      if (isNullOrEmpty == false)
         metaDataLocations = (Map<VirtualFile, MetaDataType>) in.readObject();
      classPath = (List) in.readObject();
   }

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
