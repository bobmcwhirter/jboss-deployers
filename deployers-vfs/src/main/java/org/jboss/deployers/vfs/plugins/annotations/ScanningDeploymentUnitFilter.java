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

import org.jboss.classloading.spi.visitor.ResourceContext;
import org.jboss.classloading.spi.visitor.ResourceFilter;
import org.jboss.deployers.spi.annotations.PathMetaData;
import org.jboss.deployers.spi.annotations.ScanningMetaData;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnitFilter;
import org.jboss.logging.Logger;

/**
 * ScanningDeploymentUnitFilter
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class ScanningDeploymentUnitFilter implements VFSDeploymentUnitFilter
{
   private Logger log = Logger.getLogger(getClass());

   public boolean accepts(VFSDeploymentUnit unit)
   {
      ScanningMetaData smd = unit.getAttachment(ScanningMetaData.class);
      if (smd != null)
      {
         // recurse
         ResourceFilter recurse = createRecurseFilter(smd);
         if (recurse != null)
         {
            ResourceFilter previousRecurse = unit.addAttachment(ResourceFilter.class.getName() + ".recurse", recurse, ResourceFilter.class);
            if (previousRecurse != null)
               log.debugf("Overridding previous recurse filter: %1s", previousRecurse);
         }

         // resource
         ResourceFilter filter = createResourceFilter(smd);
         if (filter != null)
         {
            ResourceFilter previousFilter = unit.addAttachment(ResourceFilter.class.getName() + ".resource", filter, ResourceFilter.class);
            if (previousFilter != null)
               log.debugf("Overridding previous resource filter: %1s", previousFilter);
         }
      }
      return true;
   }

   /**
    * Create recurse filter.
    *
    * @param smd the scanning metadata
    * @return recurse filter
    */
   protected ResourceFilter createRecurseFilter(ScanningMetaData smd)
   {
      return new ScanningMetaDataRecurseFilter(smd);
   }

   /**
    * Create resource filter.
    *
    * @param smd the scanning metadata
    * @return resource filter
    */
   protected ResourceFilter createResourceFilter(ScanningMetaData smd)
   {
      return new ScanningMetaDataResourceFilter(smd);
   }

   /**
    * Simple recurse filter.
    * It searches for path substring in url string.
    */
   private class ScanningMetaDataRecurseFilter implements ResourceFilter
   {
      private ScanningMetaData smd;

      private ScanningMetaDataRecurseFilter(ScanningMetaData smd)
      {
         this.smd = smd;
      }

      public boolean accepts(ResourceContext resource)
      {
         URL url = resource.getUrl();
         String urlString = url.toExternalForm();
         List<PathMetaData> paths = smd.getPaths();
         if (paths != null && paths.isEmpty() == false)
         {
            for (PathMetaData pmd : paths)
            {
               String name = pmd.getPathName();
               // url contains path
               if (urlString.contains(name))
               {
                  return true;
               }
            }
         }
         return false;
      }
   }
}
