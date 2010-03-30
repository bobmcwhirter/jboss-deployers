/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Inc., and individual contributors
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
package org.jboss.deployers.plugins.classloading;

import org.jboss.dependency.spi.Controller;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.dependency.spi.ControllerStateModel;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.classloading.DeploymentMetaData;
import org.jboss.deployers.spi.deployer.DeploymentStage;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractSimpleRealDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;

/**
 * Deployment meta data validation deployer.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class DeploymentValidationDeployer extends AbstractSimpleRealDeployer<DeploymentMetaData>
{
   private static final ControllerState DESCRIBED = ControllerState.newState(DeploymentStages.DESCRIBE.getName());
   private static final ControllerState REAL = ControllerState.newState(DeploymentStages.REAL.getName());

   private ControllerStateModel states;
   private boolean throwException = true;

   public DeploymentValidationDeployer(Controller controller)
   {
      super(DeploymentMetaData.class);

      if (controller == null)
         throw new IllegalArgumentException("Null Controller");
      this.states = controller.getStates();

      setStage(DeploymentStages.POST_PARSE);
   }

   public void deploy(DeploymentUnit unit, DeploymentMetaData deployment) throws DeploymentException
   {
      DeploymentStage requiredStage = deployment.getRequiredStage();
      ControllerState state = ControllerState.getInstance(requiredStage.getName());

      if (throwException && deployment.isLazyResolve() && states.isAfterState(state, DESCRIBED))
         throw new DeploymentException("Required stage is after DESCRIBED with lazy resolve enabled: " + requiredStage);
      if (throwException && deployment.isLazyStart() && states.isAfterState(state, REAL))
         throw new DeploymentException("Required stage is after REAL with lazy start enabled: " + requiredStage);

      if (unit.isTopLevel())
         unit.setRequiredStage(requiredStage);
      else if (DeploymentStages.DESCRIBE.equals(requiredStage) == false)
         log.warnf("Ignoring non-default required stage (%1s) for sub-deployment: %2s", requiredStage, unit);
   }

   public void setThrowException(boolean throwException)
   {
      this.throwException = throwException;
   }
}