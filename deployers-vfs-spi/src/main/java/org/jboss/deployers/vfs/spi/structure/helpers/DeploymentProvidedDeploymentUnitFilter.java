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
package org.jboss.deployers.vfs.spi.structure.helpers;

import org.jboss.deployers.spi.deployer.helpers.AttachmentLocatorEnum;
import org.jboss.deployers.spi.deployer.helpers.AttachmentLocatorType;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.structure.spi.DeploymentUnitFilter;

/**
 * DeploymentProvidedDeploymentUnitFilter.
 *
 * It delegates the filtering to DeploymentUnitFilter found in attachment.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class DeploymentProvidedDeploymentUnitFilter extends VFS2BaseBridgeDeploymentUnitFilter
{
   /** By default we do local search */
   private AttachmentLocatorType searchType = AttachmentLocatorEnum.LOCAL;

   protected boolean doAccepts(DeploymentUnit unit)
   {
      DeploymentUnitFilter filter = searchType.search(unit, DeploymentUnitFilter.class);
      return filter == null || filter.accepts(unit);
   }

   /**
    * Set search type.
    *
    * @param searchType the search type
    */
   public void setSearchType(AttachmentLocatorType searchType)
   {
      if (searchType == null)
         throw new IllegalArgumentException("Null search type");

      this.searchType = searchType;
   }
}