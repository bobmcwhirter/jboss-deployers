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
package org.jboss.deployers.vfs.spi.deployer;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.helpers.AbstractRealDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;

/**
 * AbstractRealDeployer.
 *
 * @Deprecated if you've left it until the real deployer to look at the file system then you are lazy
 * @author <a href="adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
@Deprecated
public abstract class AbstractVFSRealDeployer extends AbstractRealDeployer
{
   public void internalDeploy(DeploymentUnit unit) throws DeploymentException
   {
      if (unit instanceof VFSDeploymentUnit == false)
         return;
      
      VFSDeploymentUnit vfsDeploymentUnit = (VFSDeploymentUnit) unit;
      deploy(vfsDeploymentUnit);
   }

   public void internalUndeploy(DeploymentUnit unit)
   {
      if (unit instanceof VFSDeploymentUnit == false)
         return;
      
      VFSDeploymentUnit vfsDeploymentUnit = (VFSDeploymentUnit) unit;
      undeploy(vfsDeploymentUnit);
   }
   
   /**
    * Deploy a vfs deployment
    * 
    * @param unit the unit
    * @throws DeploymentException for any error
    */
   public abstract void deploy(VFSDeploymentUnit unit) throws DeploymentException;

   /**
    * Undeploy a vfs deployment
    * 
    * @param unit the unit
    */
   public void undeploy(VFSDeploymentUnit unit)
   {
      // Nothing
   }
}
