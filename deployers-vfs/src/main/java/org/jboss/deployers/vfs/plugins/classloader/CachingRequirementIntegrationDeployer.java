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

import org.jboss.classloading.spi.metadata.helpers.AbstractRequirement;
import org.jboss.classloading.spi.metadata.RequirementsMetaData;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;

/**
 * Integration deployer.
 * Caches integration requirement.
 *
 * @param <T> exact output type
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 */
public abstract class CachingRequirementIntegrationDeployer<T> extends RequirementIntegrationDeployer<T>
{
   /** The cached requirement key */
   public static final String REQUIREMENT_KEY = CachingRequirementIntegrationDeployer.class.getSimpleName() + "::Requirement";

   /** Should we cache the requirement */
   private boolean cacheRequirement;

   protected CachingRequirementIntegrationDeployer(Class<T> input)
   {
      super(input);
   }

   public void setCacheRequirement(boolean cacheRequirement)
   {
      this.cacheRequirement = cacheRequirement;
   }

   @Override
   public void undeploy(VFSDeploymentUnit unit, T deployment)
   {
      super.undeploy(unit, deployment);
      // remove the cached requirement
      if (unit.isAttachmentPresent(REQUIREMENT_KEY))
      {
         unit.removeAttachment(REQUIREMENT_KEY);
      }
   }

   @Override
   protected AbstractRequirement hasIntegrationModuleRequirement(VFSDeploymentUnit unit, RequirementsMetaData requirements)
   {
      AbstractRequirement abstractRequirement = unit.getAttachment(REQUIREMENT_KEY, AbstractRequirement.class);
      if (abstractRequirement == null)
      {
         abstractRequirement = super.hasIntegrationModuleRequirement(unit, requirements);
         if (cacheRequirement)
            unit.addAttachment(REQUIREMENT_KEY, abstractRequirement, AbstractRequirement.class);
      }
      return abstractRequirement;
   }
}