/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors as indicated
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
package org.jboss.deployers.vfs.spi.deployer;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.helpers.AbstractOptionalRealDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;

/**
 * An abstract more complicated VFS real deployer where the input
 * is optional instead of mandatory.
 * 
 * @param <T> the deployment type 
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @author adrian@jboss.org
 * @version $Revision$
 */
public abstract class AbstractOptionalVFSRealDeployer<T> extends AbstractOptionalRealDeployer<T>
{
   /**
    * Create a new AbstractOptionalVFSRealDeployer.
    * 
    * @param optionalInput the optional input
    * @throws IllegalArgumentException for null input
    */
   public AbstractOptionalVFSRealDeployer(Class<T> optionalInput)
   {
      super(optionalInput);
   }

   public void deploy(DeploymentUnit unit, T deployment) throws DeploymentException
   {
      if (unit instanceof VFSDeploymentUnit == false)
         return;
      
      VFSDeploymentUnit vfsDeploymentUnit = (VFSDeploymentUnit) unit;
      deploy(vfsDeploymentUnit, deployment);
   }

   /**
    * Deploy a deployment
    * 
    * @param unit the unit
    * @param deployment the attachment
    * @throws DeploymentException for any error
    */
   public abstract void deploy(VFSDeploymentUnit unit, T deployment) throws DeploymentException;

   @Override
   public void undeploy(DeploymentUnit unit, T deployment)
   {
      if (unit instanceof VFSDeploymentUnit == false)
         return;
      
      VFSDeploymentUnit vfsDeploymentUnit = (VFSDeploymentUnit) unit;
      undeploy(vfsDeploymentUnit, deployment);
   }
   
   /**
    * Undeploy a deployment
    * 
    * @param unit the unit
    * @param deployment the attachment
    */
   public void undeploy(VFSDeploymentUnit unit, T deployment)
   {
      // Nothing
   }
}
