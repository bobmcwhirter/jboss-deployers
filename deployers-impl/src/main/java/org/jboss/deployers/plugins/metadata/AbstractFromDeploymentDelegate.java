/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.deployers.plugins.metadata;

import org.jboss.dependency.spi.Controller;
import org.jboss.dependency.spi.ControllerContext;
import org.jboss.deployers.structure.spi.DeploymentRegistry;
import org.jboss.deployers.structure.spi.DeploymentUnit;

/**
 * Get info from context's deployment.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class AbstractFromDeploymentDelegate implements FromDeploymentDelegate
{
   /**
    * Get context's owning deployment.
    *
    * @param context the context to check
    * @return matching deployment or null
    */
   protected DeploymentUnit getDeploymentUnit(ControllerContext context)
   {
      Controller controller = context.getController();
      ControllerContext cc = controller.getInstalledContext(DeploymentRegistry.class);
      if (cc != null)
      {
         Object target = cc.getTarget();
         return DeploymentRegistry.class.cast(target).getDeployment(context);
      }
      return null;
   }
}