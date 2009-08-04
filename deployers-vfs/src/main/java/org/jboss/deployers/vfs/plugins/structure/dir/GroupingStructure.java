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
package org.jboss.deployers.vfs.plugins.structure.dir;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.structure.ContextInfo;
import org.jboss.deployers.vfs.plugins.structure.AbstractVFSStructureDeployer;
import org.jboss.deployers.vfs.spi.structure.StructureContext;
import org.jboss.util.collection.CollectionsFactory;
import org.jboss.virtual.VirtualFile;
import org.jboss.virtual.VirtualFileFilter;

/**
 * Similar to jar or directory structure,
 * being able to handle sub-dirs, but a bit more strict then dir structure.
 * Only listed sub-directories are candidates for potential sub-deployments.
 *
 * It also allows you to set metadata paths, libs and should the root be part of classpath.
 * Each of sub-dirs can be filtered per path or by default per lib or group.
 *
 * In order to accept anything one must first set shortCircuitFilter instance.
 *
 * @see org.jboss.deployers.vfs.plugins.structure.jar.JARStructure
 * @see org.jboss.deployers.vfs.plugins.structure.dir.DirectoryStructure
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class GroupingStructure extends AbstractVFSStructureDeployer
{
   private static final String[] META_INF = {"META-INF"};

   private VirtualFileFilter shortCircuitFilter; // by default null, so we don't accept anything
   private boolean rootClasspathEntry;

   // paths
   private String[] metaDataPaths;
   private Set<String> libs;
   private Set<String> groups;

   // filters
   private VirtualFileFilter libFilter;
   private VirtualFileFilter groupFilter;
   private Map<String, VirtualFileFilter> filters; // filter per path

   public GroupingStructure()
   {
      setRelativeOrder(9000); // before jar structure
      setRootClasspathEntry(true); // by default root is part of classpath
      setLibs(Collections.<String>emptySet()); // empty libs
      setMetaDataPaths(META_INF); // default metadata locations
      setGroups(CollectionsFactory.<String>createLazySet()); // lazy groups
      setFilters(Collections.<String, VirtualFileFilter>emptyMap()); // empty filters
   }

   public boolean determineStructure(StructureContext structureContext) throws DeploymentException
   {
      VirtualFile file = structureContext.getFile();

      if (shortCircuitFileCheck(file) == false)
         return false;

      ContextInfo context = null;
      try
      {
         context = createContext(structureContext, metaDataPaths);

         if (rootClasspathEntry)
            addClassPath(structureContext, file, true, true, context);

         // add any archives in libs
         for (String lib : libs)
         {
            VirtualFile libVF = file.getChild(lib);
            if (libVF != null)
            {
               VirtualFileFilter lf = filters.get(lib);
               if (lf == null)
                  lf = libFilter;

               List<VirtualFile> archives = libVF.getChildren(lf);
               for (VirtualFile archive : archives)
                  addClassPath(structureContext, archive, true, true, context);
            }
            else
            {
               if (log.isTraceEnabled())
                  log.trace("No such lib: " + lib + ", " + file);
            }
         }

         // check only children of defined sub-dirs / groups
         for (String group : groups)
         {
            VirtualFile groupVF = file.getChild(group);
            if (groupVF != null)
            {
               VirtualFileFilter gf = filters.get(group);
               if (gf == null)
                  gf = groupFilter;

               List<VirtualFile> children = groupVF.getChildren(gf);
               for (VirtualFile child : children)
               {
                  structureContext.determineChildStructure(child);
               }
            }
            else
            {
               if (log.isTraceEnabled())
                  log.trace("No such group: " + group + ", " + file);
            }
         }

         return true;
      }
      catch (Exception e)
      {
         // Remove the invalid context
         if (context != null)
            structureContext.removeChild(context);

         throw DeploymentException.rethrowAsDeploymentException("Error determining structure: " + file.getName(), e);
      }
   }

   /**
    * Do short circuit file check.
    *
    * @param file the file
    * @return true if we accept the file
    */
   protected boolean shortCircuitFileCheck(VirtualFile file)
   {
      return shortCircuitFilter != null && shortCircuitFilter.accepts(file);
   }

   /**
    * Set short circuit file filter.
    *
    * @param shortCircuitFilter the short circuit file filter
    */
   public void setShortCircuitFilter(VirtualFileFilter shortCircuitFilter)
   {
      this.shortCircuitFilter = shortCircuitFilter;
   }

   /**
    * Is root part of classpath.
    *
    * @param rootClasspathEntry root cp flag
    */
   public void setRootClasspathEntry(boolean rootClasspathEntry)
   {
      this.rootClasspathEntry = rootClasspathEntry;
   }

   /**
    * Set libs; added as cp entries.
    *
    * @param libs the libs
    */
   public void setLibs(Set<String> libs)
   {
      if (libs == null)
         throw new IllegalArgumentException("Null libs");

      this.libs = libs;
   }

   /**
    * Set default lib filter.
    *
    * @param libFilter the filter
    */
   public void setLibFilter(VirtualFileFilter libFilter)
   {
      this.libFilter = libFilter;
   }

   /**
    * Set the default metadata paths.
    *
    * @param metaDataPaths the meta data paths
    */
   public void setMetaDataPaths(String[] metaDataPaths)
   {
      this.metaDataPaths = metaDataPaths;
   }

   /**
    * Set groups.
    * Aka grouping sub-directories.
    *
    * @param groups the groups
    */
   public void setGroups(Set<String> groups)
   {
      if (groups == null)
         throw new IllegalArgumentException("Null groups");

      this.groups = groups;
   }

   /**
    * Add group.
    *
    * @param group the group
    */
   public void addGroup(String group)
   {
      groups.add(group);
   }

   /**
    * Remove group.
    *
    * @param group the group
    */
   public void removeGroup(String group)
   {
      groups.remove(group);
   }

   /**
    * Set default group filter.
    *
    * @param groupFilter the filter
    */
   public void setGroupFilter(VirtualFileFilter groupFilter)
   {
      this.groupFilter = groupFilter;
   }

   /**
    * Set filter per path.
    *
    * @param filters the filters
    */
   public void setFilters(Map<String, VirtualFileFilter> filters)
   {
      this.filters = filters;
   }
}
