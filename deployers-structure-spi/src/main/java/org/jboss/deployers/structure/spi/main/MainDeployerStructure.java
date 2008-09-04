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

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.structure.spi.DeploymentContext;
import org.jboss.deployers.structure.spi.DeploymentUnit;

/**
 * MainDeployerStructure.
 * 
 * @author <a href="adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public interface MainDeployerStructure
{
   /**
    * Get a deployment unit
    * 
    * @param name the name of the unit
    * @return the unit or null if not found
    * @throws IllegalArgumentException for a null name
    */
   DeploymentUnit getDeploymentUnit(String name);

   /**
    * Get a deployment context
    * 
    * @param name the name of the context
    * @param errorNotFound whether to throw an error if not found
    * @return the context
    * @throws IllegalArgumentException for a null name
    * @throws DeploymentException for not found
    */
   DeploymentUnit getDeploymentUnit(String name, boolean errorNotFound) throws DeploymentException;

   /**
    * Get a deployment context
    * 
    * @param name the name of the context
    * @return the context or null if not found
    * @throws IllegalArgumentException for a null name
    */
   @Deprecated
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
   @Deprecated
   DeploymentContext getDeploymentContext(String name, boolean errorNotFound) throws DeploymentException;
}
