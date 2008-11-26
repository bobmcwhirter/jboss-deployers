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

import org.jboss.dependency.spi.DependencyItem;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractSimpleRealDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;

/**
 * DeploymentDependencyDeployer.
 * 
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class DeploymentDependencyDeployer extends AbstractSimpleRealDeployer<DeploymentDependencies>
{
   public DeploymentDependencyDeployer()
   {
      super(DeploymentDependencies.class);
      setStage(DeploymentStages.POST_PARSE);
   }

   public void deploy(DeploymentUnit unit, DeploymentDependencies deployment) throws DeploymentException
   {
      Set<DependencyItem> dependencies = deployment.getDependencies();
      if (dependencies != null && dependencies.isEmpty() == false)
      {
         for (DependencyItem di : dependencies)
         {
            unit.addIDependOn(di);
         }
      }
   }

   @Override
   public void undeploy(DeploymentUnit unit, DeploymentDependencies deployment)
   {
      Set<DependencyItem> dependencies = deployment.getDependencies();
      if (dependencies != null && dependencies.isEmpty() == false)
      {
         for (DependencyItem di : dependencies)
         {
            try
            {
               unit.removeIDependOn(di);
            }
            catch (Throwable ignored)
            {
            }
         }
      }
   }
}
