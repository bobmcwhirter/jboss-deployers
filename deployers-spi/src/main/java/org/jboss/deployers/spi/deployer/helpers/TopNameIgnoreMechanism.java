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

import org.jboss.deployers.spi.deployer.matchers.NameIgnoreMechanism;
import org.jboss.deployers.structure.spi.DeploymentUnit;

/**
 * Check top level deployment for NIM.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class TopNameIgnoreMechanism implements NameIgnoreMechanism
{
   private boolean reportError;

   /**
    * Get top NIM.
    *
    * @param unit the current deployment
    * @return top NIM or null if not found / top unit
    */
   protected NameIgnoreMechanism getTop(DeploymentUnit unit)
   {
      if (unit.isTopLevel())
         if (reportError)
            throw new IllegalArgumentException("Potential cyclic usage: " + unit);
         else
            return null;

      DeploymentUnit top = unit.getTopLevel();
      return top.getAttachment(NameIgnoreMechanism.class);
   }

   public boolean ignoreName(DeploymentUnit unit, String name)
   {
      NameIgnoreMechanism top = getTop(unit);
      return top != null && top.ignoreName(unit, name);
   }

   public boolean ignorePath(DeploymentUnit unit, String path)
   {
      NameIgnoreMechanism top = getTop(unit);
      return top != null && top.ignorePath(unit, path);
   }

   /**
    * Should we report error on cyclic usage.
    *
    * @param reportError the report error flag
    */
   public void setReportError(boolean reportError)
   {
      this.reportError = reportError;
   }
}