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

import java.util.List;

import org.jboss.deployers.structure.spi.helpers.AbstractDeploymentUnit;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentContext;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentResourceLoader;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.virtual.VirtualFile;

/**
 * AbstractVFSDeploymentUnit.
 * 
 * @author <a href="adrian@jboss.org">Adrian Brock</a>
 * @author <a href="ales.justin@jboss.org">Ales Justin</a>
 * @version $Revision: 1.1 $
 */
public class AbstractVFSDeploymentUnit extends AbstractDeploymentUnit implements VFSDeploymentUnit
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -3300229322654319315L;

   /**
    * For serialization
    */
   public AbstractVFSDeploymentUnit()
   {
   }

   /**
    * Create a new AbstractVFSDeploymentUnit.
    * 
    * @param deploymentContext the deployment context
    * @throws IllegalArgumentException for a null deployment context
    */
   public AbstractVFSDeploymentUnit(VFSDeploymentContext deploymentContext)
   {
      super(deploymentContext);
   }

   public VirtualFile getMetaDataFile(String name)
   {
      return getDeploymentContext().getMetaDataFile(name);
   }

   public List<VirtualFile> getMetaDataFiles(String name, String suffix)
   {
      return getDeploymentContext().getMetaDataFiles(name, suffix);
   }

   public void prependMetaDataFile(VirtualFile... files)
   {
      getDeploymentContext().prependMetaDataFile(files);
   }

   public void appendMetaDataFile(VirtualFile... files)
   {
      getDeploymentContext().appendMetaDataFile(files);
   }

   public void removeMetaDataFile(VirtualFile... files)
   {
      getDeploymentContext().removeMetaDataFile(files);
   }

   @Override
   public VFSDeploymentResourceLoader getResourceLoader()
   {
      return getDeploymentContext().getResourceLoader();
   }

   public VirtualFile getFile(String path)
   {
      return getDeploymentContext().getFile(path);
   }

   public VirtualFile getRoot()
   {
      return getDeploymentContext().getRoot();
   }

   public List<VirtualFile> getClassPath()
   {
      return getDeploymentContext().getClassPath();
   }

   public void setClassPath(List<VirtualFile> classPath)
   {
      getDeploymentContext().setClassPath(classPath);
   }
   
   public void prependClassPath(List<VirtualFile> files)
   {
      getDeploymentContext().prependClassPath(files);
   }

   public void prependClassPath(VirtualFile... files)
   {
      getDeploymentContext().prependClassPath(files);
   }
   
   public void appendClassPath(List<VirtualFile> files)
   {
      getDeploymentContext().appendClassPath(files);
   }

   public void appendClassPath(VirtualFile... files)
   {
      getDeploymentContext().appendClassPath(files);
   }
   
   public void addClassPath(List<VirtualFile> files)
   {
      getDeploymentContext().appendClassPath(files);
   }

   public void addClassPath(VirtualFile... files)
   {
      getDeploymentContext().appendClassPath(files);
   }

   public void removeClassPath(VirtualFile... files)
   {
      getDeploymentContext().removeClassPath(files);
   }

   @Override
   public VFSDeploymentUnit getParent()
   {
      return (VFSDeploymentUnit) super.getParent();
   }

   @Override
   public VFSDeploymentUnit getTopLevel()
   {
      return (VFSDeploymentUnit) super.getTopLevel();
   }

   @SuppressWarnings("unchecked")
   public List<VFSDeploymentUnit> getVFSChildren()
   {
      return (List) super.getChildren();
   }

   @Override
   protected VFSDeploymentContext getDeploymentContext()
   {
      return (VFSDeploymentContext) super.getDeploymentContext();
   }
}
