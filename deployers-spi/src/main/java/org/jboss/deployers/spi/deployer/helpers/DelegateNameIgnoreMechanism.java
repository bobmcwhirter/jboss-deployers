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
import org.jboss.deployers.spi.deployer.matchers.NameIgnoreMechanism;
import org.jboss.deployers.structure.spi.DeploymentUnit;

/**
 * Delegate NIM.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class DelegateNameIgnoreMechanism implements NameIgnoreMechanism
{
   public static final NameIgnoreMechanism INSTANCE = new DelegateNameIgnoreMechanism();

   private boolean reportCycle;

   /**
    * Prepare deployment unit.
    * e.g. might watch only top.
    *
    * @param unit the current deployment unit
    * @return unit from which we check
    */
   protected DeploymentUnit adjustDeploymentUnit(DeploymentUnit unit)
   {
      return unit;
   }

   /**
    * Get NIM delegate.
    *
    * @param unit the current deployment
    * @return delegate NIM or null if not found or cyclic
    */
   protected NameIgnoreMechanism getDelegate(DeploymentUnit unit)
   {
      DeploymentUnit adjustedUnit = adjustDeploymentUnit(unit);
      NameIgnoreMechanism delegate = adjustedUnit.getAttachment(NameIgnoreMechanism.class);
      if (delegate == this && reportCycle)
         throw new IllegalArgumentException("Cyclic invocation: " + this);

      return delegate != this ? delegate : null;
   }

   public boolean ignoreName(DeploymentUnit unit, String name)
   {
      NameIgnoreMechanism delegate = getDelegate(unit);
      return delegate != null && delegate.ignoreName(unit, name);
   }

   public boolean ignorePath(DeploymentUnit unit, String path)
   {
      NameIgnoreMechanism delegate = getDelegate(unit);
      return delegate != null && delegate.ignorePath(unit, path);
   }

   public boolean ignorePath(DeploymentUnit unit, LazyPath path)
   {
      NameIgnoreMechanism delegate = getDelegate(unit);
      return delegate != null && delegate.ignorePath(unit, path);
   }

   /**
    * Should we report error on cyclic usage.
    *
    * @param reportCycle the report error flag
    */
   public void setReportCycle(boolean reportCycle)
   {
      this.reportCycle = reportCycle;
   }
}