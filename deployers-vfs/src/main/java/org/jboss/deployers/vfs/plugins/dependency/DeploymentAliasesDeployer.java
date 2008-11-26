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

import java.util.Set;

import org.jboss.dependency.spi.Controller;
import org.jboss.dependency.spi.ControllerContext;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractSimpleRealDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;

/**
 * DeploymentAliasesDeployer.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class DeploymentAliasesDeployer extends AbstractSimpleRealDeployer<DeploymentAliases>
{
   private Controller controller;

   public DeploymentAliasesDeployer(Controller controller)
   {
      super(DeploymentAliases.class);
      if (controller == null)
         throw new IllegalArgumentException("Null controller");
      this.controller = controller;
      setStage(DeploymentStages.POST_PARSE);
   }

   public void deploy(DeploymentUnit unit, DeploymentAliases deployment) throws DeploymentException
   {
      Set<Object> aliases = deployment.getAliases();
      if (aliases != null && aliases.isEmpty() == false)
      {
         ControllerContext context = unit.getAttachment(ControllerContext.class);
         if (context == null)
            throw new DeploymentException("Missing deployment controller context: " + unit.getName());
         
         Object contextName = context.getName();
         try
         {
            for (Object alias : aliases)
            {
               controller.addAlias(alias, contextName);
            }
         }
         catch (Throwable t)
         {
            throw DeploymentException.rethrowAsDeploymentException("Exception adding alias.", t);
         }
      }
   }

   @Override
   public void undeploy(DeploymentUnit unit, DeploymentAliases deployment)
   {
      Set<Object> aliases = deployment.getAliases();
      if (aliases != null && aliases.isEmpty() == false)
      {
         for (Object alias : aliases)
         {
            try
            {
               controller.removeAlias(alias);
            }
            catch (Throwable ignored)
            {
            }
         }
      }
   }
}