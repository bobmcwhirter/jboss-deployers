/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.deployers.vfs.plugins.dependency;

import org.jboss.dependency.spi.ControllerContext;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractSimpleRealDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;

/**
 * DependenciesMetaDataDeployer.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class DependenciesMetaDataDeployer extends AbstractSimpleRealDeployer<DependenciesMetaData>
{
   public DependenciesMetaDataDeployer()
   {
      super(DependenciesMetaData.class);
      addOutput(DeploymentDependencies.class);
      setStage(DeploymentStages.POST_PARSE);
   }

   public void deploy(DeploymentUnit unit, DependenciesMetaData deployment) throws DeploymentException
   {
      ControllerContext context = unit.getTopLevel().getAttachment(ControllerContext.class);
      if (context == null)
         throw new DeploymentException("Missing deployment controller context: " + unit.getName());

      Object contextName = context.getName();
      unit.addAttachment(DeploymentDependencies.class, new DeploymentDependenciesImpl(contextName, deployment));
   }
}