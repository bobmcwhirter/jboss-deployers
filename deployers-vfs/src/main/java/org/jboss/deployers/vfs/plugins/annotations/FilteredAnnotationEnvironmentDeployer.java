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
package org.jboss.deployers.vfs.plugins.annotations;

import org.jboss.classloading.spi.dependency.Module;
import org.jboss.classloading.spi.visitor.ResourceFilter;
import org.jboss.deployers.plugins.annotations.GenericAnnotationResourceVisitor;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.deployers.vfs.plugins.util.ClasspathUtils;

import java.net.URL;

/**
 * Filtered annotation environment deployer.
 *
 * It first checks if there are some filters present
 * in deployment unit as attachment,
 * else falls back to deployers filters.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class FilteredAnnotationEnvironmentDeployer extends ScopedAnnotationEnvironmentDeployer
{
   private ResourceFilter resourceFilter;
   private ResourceFilter recurseFilter;

   public FilteredAnnotationEnvironmentDeployer()
   {
      addInput(ResourceFilter.class.getName() + ".resource");
      addInput(ResourceFilter.class.getName() + ".recurse");
   }

   /**
    * Get filter.
    * Try attachment first, then deployer's filter.
    *
    * @param <T> the expected class type
    * @param unit the deployment unit
    * @param expectedClass the expected class
    * @param suffix the suffix
    * @param defaultValue the default value
    * @return found filter or null
    */
   protected <T> T getFilter(DeploymentUnit unit, Class<T> expectedClass, String suffix, T defaultValue)
   {
      String name = expectedClass.getName() + "." + (suffix != null ? suffix : "");
      T result = unit.getAttachment(name, expectedClass);
      if (result == null)
         result = defaultValue;
      return result;
   }

   /**
    * We look for filter attachments:
    * * org.jboss.classloading.spi.visitor.ResourceFilter.resource - plain resource filter
    * * org.jboss.classloading.spi.visitor.ResourceFilter.recurse  - recurse resource filter
    *
    * @param unit the deployment unit
    * @param module the underlying module
    * @param visitor the current generic annotation resource visitor
    */
   protected void visitModule(VFSDeploymentUnit unit, Module module, GenericAnnotationResourceVisitor visitor) throws DeploymentException
   {
      ResourceFilter filter = getFilter(unit, ResourceFilter.class, "resource", resourceFilter);
      if (filter == null)
         filter = visitor.getFilter();
      ResourceFilter recurse = getFilter(unit, ResourceFilter.class, "recurse", recurseFilter);
      try
      {
         URL[] urls = ClasspathUtils.getUrls(unit);
         module.visit(visitor, filter, recurse, urls);
      }
      catch (Exception e)
      {
         throw DeploymentException.rethrowAsDeploymentException("Exception visiting module", e);
      }
   }

   /**
    * Set resource filter.
    *
     * @param resourceFilter the resource filter
    */
   public void setResourceFilter(ResourceFilter resourceFilter)
   {
      this.resourceFilter = resourceFilter;
   }

   /**
    * Set recurse filter.
    *
    * @param recurseFilter the recurse filter
    */
   public void setRecurseFilter(ResourceFilter recurseFilter)
   {
      this.recurseFilter = recurseFilter;
   }
}