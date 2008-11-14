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

import java.net.URL;
import java.util.List;
import java.util.Set;

import org.jboss.classloader.spi.filter.ClassFilter;
import org.jboss.classloader.spi.filter.PackageClassFilter;
import org.jboss.classloading.spi.visitor.ResourceContext;
import org.jboss.classloading.spi.visitor.ResourceFilter;
import org.jboss.deployers.spi.annotations.PathEntryMetaData;
import org.jboss.deployers.spi.annotations.PathMetaData;
import org.jboss.deployers.spi.annotations.ScanningMetaData;
import org.jboss.deployers.plugins.annotations.FilterablePathEntry;

/**
 * ScanningMetaDataResourceFilter
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class ScanningMetaDataResourceFilter implements ResourceFilter
{
   private ScanningMetaData smd;

   public ScanningMetaDataResourceFilter(ScanningMetaData smd)
   {
      this.smd = smd;
   }

   public boolean accepts(ResourceContext resource)
   {
      if (resource.isClass() == false)
         return false;

      URL url = resource.getUrl();
      String urlString = url.toExternalForm();
      List<PathMetaData> paths = smd.getPaths();
      if (paths != null && paths.isEmpty() == false)
      {
         for (PathMetaData pmda : paths)
         {
            String name = pmda.getPathName();
            // url contains path + we need some include or exclude
            if (urlString.contains(name))
            {
               String resourceName = resource.getResourceName();
               boolean explicitInclude = false; // do we have an explicit include

               Set<PathEntryMetaData> includes = pmda.getIncludes();
               if (includes != null && includes.isEmpty() == false)
               {
                  explicitInclude = true;
                  for (PathEntryMetaData pemda : includes)
                  {
                     ClassFilter filter = getClassFilter(pemda);
                     if (filter.matchesResourcePath(resourceName))
                        return true;
                  }
               }

               Set<PathEntryMetaData> excludes = pmda.getExcludes();
               if (excludes != null && excludes.isEmpty() == false)
               {
                  for (PathEntryMetaData pemda : excludes)
                  {
                     ClassFilter filter = getClassFilter(pemda);
                     if (filter.matchesResourcePath(resourceName))
                        return false;
                  }
               }

               return (explicitInclude == false);
            }
         }
      }
      return false;
   }

   /**
    * Get class filter from path entry meta data.
    *
    * @param pemd the path entry meta data
    * @return class filter
    */
   protected ClassFilter getClassFilter(PathEntryMetaData pemd)
   {
      if (pemd instanceof FilterablePathEntry)
         return ((FilterablePathEntry) pemd).getFilter();
      else
         return PackageClassFilter.createPackageClassFilter(pemd.getName());
   }
}