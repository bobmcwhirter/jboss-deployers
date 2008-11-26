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

import org.jboss.dependency.spi.DependencyItem;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeploymentVisitor;
import org.jboss.deployers.spi.deployer.helpers.AbstractRealDeployerWithInput;
import org.jboss.deployers.structure.spi.DeploymentUnit;

/**
 * DeploymentDependencyDeployer.
 * 
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class DeploymentDependencyDeployer extends AbstractRealDeployerWithInput<DeploymentDependencies>
{
   public DeploymentDependencyDeployer()
   {
      setStage(DeploymentStages.POST_PARSE);
      setDeploymentVisitor(new DependencyItemComponentVisitor());
   }

   private class DependencyItemComponentVisitor extends AbstractDeploymentVisitor<DependencyItem, DeploymentDependencies>
   {
      @Override
      protected DeploymentUnit addComponent(DeploymentUnit unit, DependencyItem attachment)
      {
         unit.addIDependOn(attachment);
         return null;
      }

      @Override
      protected void removeComponent(DeploymentUnit unit, DependencyItem attachment)
      {
         unit.removeIDependOn(attachment);
      }

      protected List<? extends DependencyItem> getComponents(DeploymentDependencies deployment)
      {
         return new ArrayList<DependencyItem>(deployment.getDependencies());
      }

      protected Class<DependencyItem> getComponentType()
      {
         return DependencyItem.class;
      }

      protected String getComponentName(DependencyItem attachment)
      {
         throw new UnsupportedOperationException("No component name.");
      }

      public Class<DeploymentDependencies> getVisitorType()
      {
         return DeploymentDependencies.class;
      }
   }
}
