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
package org.jboss.deployers.structure.spi;

import java.util.Set;

import org.jboss.dependency.spi.ControllerContext;

/**
 * This registry keeps a track of deployment's context.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public interface DeploymentRegistry
{
   /**
    * Put context to deployment mapping.
    *
    * @param context the context
    * @param unit the deployment
    * @return previous mapping value
    */
   DeploymentUnit putContext(ControllerContext context, DeploymentUnit unit);

   /**
    * Remove context to deployment mapping.
    *
    * @param context the context
    * @param unit the deployment
    * @return previous mapping value
    */
   DeploymentUnit removeContext(ControllerContext context, DeploymentUnit unit);

   /**
    * Get owner deployment for context.
    *
    * @param context the context
    * @return owning deployment unit
    */
   DeploymentUnit getDeployment(ControllerContext context);

   /**
    * Get contexts owned by deployment unit.
    *
    * @param unit the deployment unit
    * @return set of contexts
    */
   Set<ControllerContext> getContexts(DeploymentUnit unit);
}
