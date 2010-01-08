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
package org.jboss.deployers.vfs.spi.structure;

import java.util.List;

import org.jboss.deployers.spi.structure.MetaDataTypeFilter;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.virtual.VirtualFile;
import org.jboss.virtual.VirtualFileFilter;

/**
 * VFSDeploymentUnit.
 * 
 * @author <a href="adrian@jboss.org">Adrian Brock</a>
 * @author <a href="ales.justin@jboss.org">Ales Justin</a>
 * @version $Revision: 1.1 $
 */
public interface VFSDeploymentUnit extends DeploymentUnit
{
   /**
    * Gets a metadata file. This is a file located under the deployment metadata
    * context(s).
    * 
    * @param name the name to exactly match
    * @return the virtual file or null if not found
    * @throws IllegalArgumentException for a null name
    */
   VirtualFile getMetaDataFile(String name);
   
   /**
    * Gets a metadata file. This is a file located under the deployment metadata
    * context(s).
    *
    * @param name the name to exactly match
    * @param filter the metadata type filter
    * @return the virtual file or null if not found
    * @throws IllegalArgumentException for a null name
    */
   VirtualFile getMetaDataFile(String name, MetaDataTypeFilter filter);

   /**
    * Gets the metadata files for this deployment unit
    * 
    * @param name the name to exactly match
    * @param suffix the suffix to partially match
    * @return the virtual files that match
    * @throws IllegalArgumentException if both the name and suffix are null
    */
   List<VirtualFile> getMetaDataFiles(String name, String suffix);

   /**
    * Gets the metadata files for this deployment unit
    *
    * @param name the name to exactly match
    * @param suffix the suffix to partially match
    * @param filter the metadata type filter
    * @return the virtual files that match
    * @throws IllegalArgumentException if both the name and suffix are null
    */
   List<VirtualFile> getMetaDataFiles(String name, String suffix, MetaDataTypeFilter filter);

   /**
    * Gets the metadata files for this deployment unit
    *
    * @param filter the file filter
    * @return the virtual files that match
    * @throws IllegalArgumentException if both the name and suffix are null
    */
   List<VirtualFile> getMetaDataFiles(VirtualFileFilter filter);

   /**
    * Gets the metadata files for this deployment unit
    *
    * @param filter the file filter
    * @param mdtf the metadata type filter
    * @return the virtual files that match
    * @throws IllegalArgumentException if both the name and suffix are null
    */
   List<VirtualFile> getMetaDataFiles(VirtualFileFilter filter, MetaDataTypeFilter mdtf);
   
   /**
    * Prepend metadata file locations.
    *
    * @param locations the locations
    */
   void prependMetaDataLocation(VirtualFile... locations);

   /**
    * Append metadata file locations.
    *
    * @param locations the locations
    */
   void appendMetaDataLocation(VirtualFile... locations);

   /**
    * Remove metadata file locations.
    *
    * @param locations the locations
    */
   void removeMetaDataLocation(VirtualFile... locations);

   /**
    * Get a resource loader
    * 
    * @return the resource loader
    */
   VFSDeploymentResourceLoader getResourceLoader();

   /**
    * Get a virtual file
    * 
    * @param path the relative path of the file
    * @return the virtual file or null if not found
    */
   VirtualFile getFile(String path);

   /**
    * Get the root
    * 
    * @return the root
    */
   VirtualFile getRoot();

   /**
    * Get the class path
    * 
    * @return the class path
    */
   List<VirtualFile> getClassPath();
   
   /**
    * Set the classpath
    * 
    * @param classPath the classpath
    * @deprecated user view should not have setters
    */
   @Deprecated
   void setClassPath(List<VirtualFile> classPath);
   
   /**
    * Prepend virtual files to the classpath
    * 
    * @param files a virtual file
    */
   void prependClassPath(VirtualFile... files);
   
   /**
    * Prepend virtual files to the classpath
    * 
    * @param files a virtual file
    */
   void prependClassPath(List<VirtualFile> files);
   
   /**
    * Append virtual files to the classpath
    * 
    * @param files a virtual file
    */
   void appendClassPath(VirtualFile... files);
   
   /**
    * Add virtual files to the classpath
    * 
    * @param files a virtual file
    */
   void appendClassPath(List<VirtualFile> files);
   
   /**
    * Append virtual files to the classpath
    * 
    * @param files a virtual file
    */
   void addClassPath(VirtualFile... files);
   
   /**
    * Add virtual files to the classpath
    * 
    * @param files a virtual file
    */
   void addClassPath(List<VirtualFile> files);

   /**
    * Remove classpath files.
    *
    * @param files the files
    */
   void removeClassPath(VirtualFile... files);

   /**
    * Get the top leve deployment unit
    * 
    * @return the top level deployment unit
    */
   VFSDeploymentUnit getTopLevel();
   
   /**
    * Get the parent deployment unit
    * 
    * @return the parent or null if there is no parent
    */
   VFSDeploymentUnit getParent();
   
   /**
    * Get the children
    * 
    * @return the children
    */
   List<VFSDeploymentUnit> getVFSChildren();
}
