/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.deployers.structure.spi.main;

import java.util.Collection;

import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.structure.spi.DeploymentContext;

/**
 * Expose some of the internals via proper interface.
 * Should be used with care or not at all,
 * as these are impl details, which are subject to change.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public interface MainDeployerInternals
{
   /**
    * Get a deployment context
    *
    * @param name the name of the context
    * @return the context or null if not found
    * @throws IllegalArgumentException for a null name
    */
   DeploymentContext getDeploymentContext(String name);

   /**
    * Get a deployment context
    *
    * @param name the name of the context
    * @param errorNotFound whether to throw an error if not found
    * @return the context
    * @throws IllegalArgumentException for a null name
    * @throws DeploymentException for not found
    */
   DeploymentContext getDeploymentContext(String name, boolean errorNotFound) throws DeploymentException;

   /**
    * Get a top level deployment context by name
    *
    * @param name the name
    * @return the context
    */
   DeploymentContext getTopLevelDeploymentContext(String name);

   /**
    * Get all deployments.
    *
    * @return all deployments
    */
   Collection<DeploymentContext> getAll();

   /**
    * Get errors.
    *
    * @return the errors
    */
   Collection<DeploymentContext> getErrors();

   /**
    * Get missing deployers deployments.
    *
    * @return the missing deployer deployments
    */
   Collection<Deployment> getMissingDeployer();
}
