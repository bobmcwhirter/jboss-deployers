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
package org.jboss.deployers.vfs.plugins.classloader;

import java.util.List;
import java.util.Set;

import org.jboss.classloading.plugins.metadata.PackageRequirement;
import org.jboss.classloading.spi.metadata.Requirement;
import org.jboss.classloading.spi.metadata.RequirementsMetaData;
import org.jboss.classloading.spi.metadata.helpers.AbstractRequirement;

/**
 * Checks package requirements.
 *
 * @param <T> exact output type
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 */
public class PackageRequirementIntegrationDeployer<T> extends CachingRequirementIntegrationDeployer<T>
{
   private Set<String> packages;

   public PackageRequirementIntegrationDeployer(Class<T> input)
   {
      super(input);
   }

   protected AbstractRequirement hasIntegrationModuleRequirement(RequirementsMetaData metadata)
   {
      if (metadata == null)
         return null;

      List<Requirement> requirements = metadata.getRequirements();
      if (requirements != null && requirements.isEmpty() == false && packages != null && packages.isEmpty() == false)
      {
         for (Requirement requirement : requirements)
         {
            if (requirement instanceof PackageRequirement)
            {
               PackageRequirement pr = (PackageRequirement)requirement;
               if (packages.contains(pr.getName()))
                  return pr;
            }
         }
      }
      return null;
   }

   /**
    * Get the matching package names.
    *
    * @return the package names
    */
   public Set<String> getPackages()
   {
      return packages;
   }

   /**
    * Set the matching package names.
    *
    * @param packages the package names
    */
   public void setPackages(Set<String> packages)
   {
      this.packages = packages;
   }
}