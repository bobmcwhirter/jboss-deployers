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
package org.jboss.deployers.vfs.plugins.structure;

import org.jboss.classloader.spi.filter.ClassFilter;
import org.jboss.classloading.plugins.vfs.VFSResourceVisitor;
import org.jboss.classloading.spi.visitor.ResourceFilter;
import org.jboss.deployers.plugins.annotations.GenericAnnotationResourceVisitor;
import org.jboss.deployers.spi.annotations.AnnotationEnvironment;
import org.jboss.deployers.structure.spi.DeploymentResourceLoader;
import org.jboss.deployers.structure.spi.helpers.DeploymentResourceClassLoader;
import org.jboss.deployers.vfs.spi.structure.helpers.AbstractStructureDeployer;
import org.jboss.virtual.VirtualFile;

/**
 * VFS aware structure deployer.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public abstract class AbstractVFSStructureDeployer extends AbstractStructureDeployer
{
   private ClassFilter included;
   private ClassFilter excluded;
   private ResourceFilter filter;
   private ResourceFilter recurseFilter;

   protected AnnotationEnvironment createAnnotationEnvironment(VirtualFile root)
   {
      DeploymentResourceLoader loader = new VFSDeploymentResourceLoaderImpl(root);
      ClassLoader classLoader = new DeploymentResourceClassLoader(loader);
      GenericAnnotationResourceVisitor visitor = new GenericAnnotationResourceVisitor(classLoader);
      ResourceFilter filter = this.filter;
      if (filter == null)
         filter = visitor.getFilter();
      VFSResourceVisitor.visit(new VirtualFile[]{root}, null, included, excluded, classLoader, visitor, filter, recurseFilter);
      return visitor.getEnv();
   }

   /**
    * Set the included class filter.
    *
    * @param included the included class filter
    */
   public void setIncluded(ClassFilter included)
   {
      this.included = included;
   }

   /**
    * Set the excluded class filter.
    *
    * @param excluded the excluded class filter
    */
   public void setExcluded(ClassFilter excluded)
   {
      this.excluded = excluded;
   }

   /**
    * Set the filter.
    *
    * @param filter the filter
    */
   public void setFilter(ResourceFilter filter)
   {
      this.filter = filter;
   }

   /**
    * Set the recurse filter.
    *
    * @param recurseFilter the recurse filter
    */
   public void setRecurseFilter(ResourceFilter recurseFilter)
   {
      this.recurseFilter = recurseFilter;
   }
}
