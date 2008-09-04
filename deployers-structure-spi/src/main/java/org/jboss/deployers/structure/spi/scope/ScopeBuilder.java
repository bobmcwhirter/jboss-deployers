/*
* JBoss, Home of Professional Open Source
* Copyright 2007, JBoss Inc., and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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
package org.jboss.deployers.structure.spi.scope;

import org.jboss.deployers.structure.spi.DeploymentContext;
import org.jboss.metadata.spi.repository.MutableMetaDataRepository;
import org.jboss.metadata.spi.scope.ScopeKey;

/**
 * ScopeBuilder.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public interface ScopeBuilder
{
   /**
    * Get the scope for a deployment
    * 
    * @param context the context
    * @return the key
    */
   ScopeKey getDeploymentScope(DeploymentContext context);

   /**
    * Get the mutable scope for a deployment
    * 
    * @param context the context
    * @return the key
    */
   ScopeKey getMutableDeploymentScope(DeploymentContext context);

   /**
    * Get the scope for a component
    * 
    * @param context the context
    * @return the key
    */
   ScopeKey getComponentScope(DeploymentContext context);

   /**
    * Get the mutable scope for a component
    * 
    * @param context the context
    * @return the key
    */
   ScopeKey getMutableComponentScope(DeploymentContext context);
   
   /**
    * Initialise the metadata retrievals for a deployment context
    *
    * @param repository the repository
    * @param context the context
    */
   void initMetaDataRetrieval(MutableMetaDataRepository repository, DeploymentContext context);
   
   /**
    * Initialise the mutable metadata retrieval for a deployment context
    *
    * @param repository the repository
    * @param context the context
    */
   void initMutableMetaDataRetrieval(MutableMetaDataRepository repository, DeploymentContext context);
}
