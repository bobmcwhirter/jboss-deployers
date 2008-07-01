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
package org.jboss.deployers.vfs.deployer.kernel;

import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.NamedAliasMetaData;
import org.jboss.dependency.spi.Controller;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.helpers.AbstractSimpleRealDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;

/**
 * DeploymentAliasMetaDataDeployer.<p>
 *
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 */
public class DeploymentAliasMetaDataDeployer extends AbstractSimpleRealDeployer<NamedAliasMetaData>
{
   /** The controller */
   private final Controller controller;

   /**
    * Create a new AliasDeployer.
    *
    * @param controller the controller
    * @throws IllegalArgumentException for a null controller
    */
   public DeploymentAliasMetaDataDeployer(Controller controller)
   {
      super(NamedAliasMetaData.class);
      if (controller == null)
         throw new IllegalArgumentException("Null controller");
      this.controller = controller;
      addInput(BeanMetaData.class);
      setComponentsOnly(true);
   }

   @Override
   public void deploy(DeploymentUnit unit, NamedAliasMetaData deployment) throws DeploymentException
   {
      try
      {
         controller.addAlias(deployment.getAliasValue(), deployment.getName());
      }
      catch (Throwable t)
      {
         throw DeploymentException.rethrowAsDeploymentException("Error deploying alias: " + deployment.getName(), t);
      }
   }

   @Override
   public void undeploy(DeploymentUnit unit, NamedAliasMetaData deployment)
   {
      controller.removeAlias(deployment.getAliasValue());
   }
}
