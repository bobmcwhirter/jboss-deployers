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
package org.jboss.deployers.plugins.annotations;

import javassist.ClassPool;
import org.jboss.classloader.spi.filter.ClassFilter;
import org.jboss.classloading.spi.dependency.Module;
import org.jboss.classloading.spi.visitor.ResourceFilter;
import org.jboss.deployers.structure.spi.DeploymentUnit;

/**
 * Filtered generic annotation scanner deployer.
 *
 * It first checks if there are some filters present
 * in deployment unit as attachment,
 * else falls back to deployers filters.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class FilteredGenericAnnotationDeployer extends GenericAnnotationDeployer
{
   private ResourceFilter resourceFilter;
   private ClassFilter includedFilter;
   private ClassFilter excludedFilter;

   /**
    * Get filter.
    * Try attachment first, then deployer's filter.
    *
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

   protected GenericAnnotationResourceVisitor createGenericAnnotationResourceVisitor(DeploymentUnit unit, ClassPool pool, ClassLoader classLoader)
   {
      GenericAnnotationResourceVisitor visitor = super.createGenericAnnotationResourceVisitor(unit, pool, classLoader);
      ResourceFilter filter = getFilter(unit, ResourceFilter.class, null, resourceFilter);
      if (filter != null)
         visitor.setResourceFilter(filter);
      return visitor;
   }

   protected Module prepareModule(DeploymentUnit unit, Module original)
   {
      ClassFilter included = getFilter(unit, ClassFilter.class, "included", includedFilter);
      ClassFilter excluded = getFilter(unit, ClassFilter.class, "excluded", excludedFilter);
      // TODO - temp set this two
      return super.prepareModule(unit, original);
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
    * Set included class filter.
    *
    * @param includedFilter included class filter
    */
   public void setIncludedFilter(ClassFilter includedFilter)
   {
      this.includedFilter = includedFilter;
   }

   /**
    * Set excluded class filter.
    *
    * @param excludedFilter excluded class filter
    */
   public void setExcludedFilter(ClassFilter excludedFilter)
   {
      this.excludedFilter = excludedFilter;
   }
}