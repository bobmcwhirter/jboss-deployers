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
package org.jboss.deployers.spi.deployer.helpers;

import org.jboss.deployers.spi.deployer.matchers.LazyPath;
import org.jboss.deployers.structure.spi.DeploymentUnit;

/**
 * Check top level deployment for NIM.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class TopNameIgnoreMechanism extends DelegateNameIgnoreMechanism
{
   @Override
   protected DeploymentUnit adjustDeploymentUnit(DeploymentUnit unit)
   {
      return unit.getTopLevel();
   }

   public boolean ignorePath(DeploymentUnit unit, String path)
   {
      DeploymentUnit top = adjustDeploymentUnit(unit);
      if (top != unit)
      {
         String prefix = unit.getRelativePath();
         path = prefix + (prefix.endsWith("/") == false && path.startsWith("/") == false ? "/" : "") + path;
      }
      return super.ignorePath(unit, path);
   }

   public boolean ignorePath(final DeploymentUnit unit, LazyPath path)
   {
      DeploymentUnit top = adjustDeploymentUnit(unit);
      if (top != unit)
      {
         final LazyPath lp = path;
         path = new LazyPath()
         {
            public String buildPath()
            {
               String prefix = unit.getRelativePath();
               String suffix = lp.buildPath();
               return prefix + (prefix.endsWith("/") == false && suffix.startsWith("/") == false ? "/" : "") + suffix;
            }
         };
      }
      return super.ignorePath(unit, path);
   }
}