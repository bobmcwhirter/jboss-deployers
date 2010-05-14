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

import org.jboss.dependency.spi.ControllerContext;
import org.jboss.deployers.structure.spi.DeploymentUnit;

/**
 * Get info from context's deployment.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public enum FromDeployment
{
   DEPLOYMENT(new DefaultFromDeploymentDelegate()),
   SIMPLE_NAME(new SimpleNameFromDeploymentDelegate()),
   TOP_DEPLOYMENT(new TopDeploymentDelegate()),
   TOP_SIMPLE_NAME(new TopSimpleNameDeploymentDelegate());

   private FromDeploymentDelegate delegate;

   FromDeployment(FromDeploymentDelegate delegate)
   {
      this.delegate = delegate;
   }

   /**
    * Get deployment info.
    *
    * @param context the context
    * @return deployment's info
    */
   public Object executeLookup(ControllerContext context)
   {
      return delegate.executeLookup(context);
   }

   /**
    * Get matching FromDeployment enum.
    *
    * @param type the type string
    * @return matching FromDeployment or exception
    */
   public static FromDeployment getInstance(String type)
   {
      if (type == null)
         throw new IllegalArgumentException("Null type");

      for (FromDeployment fd : values())
      {
         if (type.equalsIgnoreCase(fd.name()))
            return fd;
      }

      throw new IllegalArgumentException("NO such FromDeployment: " + type);
   }

   private static class DefaultFromDeploymentDelegate extends AbstractFromDeploymentDelegate
   {
      public Object executeLookup(ControllerContext context)
      {
         return getDeploymentUnit(context);
      }
   }

   private static class SimpleNameFromDeploymentDelegate extends AbstractFromDeploymentDelegate
   {
      public Object executeLookup(ControllerContext context)
      {
         DeploymentUnit unit = getDeploymentUnit(context);
         return unit != null ? unit.getSimpleName() : null;
      }
   }

   private static class TopDeploymentDelegate extends AbstractFromDeploymentDelegate
   {
      public Object executeLookup(ControllerContext context)
      {
         DeploymentUnit unit = getDeploymentUnit(context);
         return unit != null ? unit.getTopLevel() : null;
      }
   }

   private static class TopSimpleNameDeploymentDelegate extends AbstractFromDeploymentDelegate
   {
      public Object executeLookup(ControllerContext context)
      {
         DeploymentUnit unit = getDeploymentUnit(context);
         return unit != null ? unit.getTopLevel().getSimpleName() : null;
      }
   }
}
