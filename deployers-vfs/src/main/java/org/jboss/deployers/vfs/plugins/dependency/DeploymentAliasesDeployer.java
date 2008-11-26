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

import java.util.ArrayList;
import java.util.List;

import org.jboss.dependency.spi.Controller;
import org.jboss.dependency.spi.ControllerContext;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeploymentVisitor;
import org.jboss.deployers.spi.deployer.helpers.AbstractRealDeployerWithInput;
import org.jboss.deployers.structure.spi.DeploymentUnit;

/**
 * DeploymentAliasesDeployer.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class DeploymentAliasesDeployer extends AbstractRealDeployerWithInput<DeploymentAliases>
{
   private Controller controller;

   public DeploymentAliasesDeployer(Controller controller)
   {
      if (controller == null)
         throw new IllegalArgumentException("Null controller");
      this.controller = controller;
      setStage(DeploymentStages.POST_PARSE);
      setDeploymentVisitor(new DeploymentAliasDeploymentVisitor());
   }

   private class DeploymentAliasDeploymentVisitor extends AbstractDeploymentVisitor<Object, DeploymentAliases>
   {
      @Override
      protected DeploymentUnit addComponent(DeploymentUnit unit, Object attachment)
      {
         ControllerContext context = unit.getAttachment(ControllerContext.class);
         if (context == null)
            throw new IllegalArgumentException("Missing deployment controller context: " + unit.getName());

         Object contextName = context.getName();
         try
         {
            controller.addAlias(attachment, contextName);
            return null;
         }
         catch (Throwable t)
         {
            throw new RuntimeException(t);
         }
      }

      @Override
      protected void removeComponent(DeploymentUnit unit, Object attachment)
      {
         controller.removeAlias(attachment);
      }

      protected List<Object> getComponents(DeploymentAliases deployment)
      {
         return new ArrayList<Object>(deployment.getAliases());
      }

      protected Class<Object> getComponentType()
      {
         return Object.class;
      }

      protected String getComponentName(Object attachment)
      {
         throw new UnsupportedOperationException("No component name.");
      }

      public Class<DeploymentAliases> getVisitorType()
      {
         return DeploymentAliases.class;
      }
   }
}