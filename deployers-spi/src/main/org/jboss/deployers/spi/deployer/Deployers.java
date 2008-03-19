/*
* JBoss, Home of Professional Open Source
* Copyright 2006, JBoss Inc., and individual contributors as indicated
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
package org.jboss.deployers.spi.deployer;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.structure.spi.DeploymentContext;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.managed.api.ManagedObject;

/**
 * Deployers.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public interface Deployers
{
   /**
    * Get the managed objects for a deployer
    * 
    * @param context the context
    * @return the managed objects
    * @throws DeploymentException for any error
    */
   Map<String, ManagedObject> getManagedObjects(DeploymentContext context) throws DeploymentException;
   
   /**
    * Process deployment
    * 
    * @param deploy the deployments to deploy
    * @param undeploy the deployments to remove
    */
   void process(List<DeploymentContext> deploy, List<DeploymentContext> undeploy);

   /**
    * Change the state of a deployment
    * 
    * @param context the context
    * @param stage the stage
    * @throws DeploymentException for any error
    */
   void change(DeploymentContext context, DeploymentStage stage) throws DeploymentException;

   /**
    * Get the deployment stage for a deployment
    * 
    * @param context the context
    * @return the stage or null if not deployed
    * @throws DeploymentException for any error
    */
   DeploymentStage getDeploymentStage(DeploymentContext context) throws DeploymentException;

   /**
    * Check all the deployments are complete
    *
    * @param errors the contexts in error
    * @param missingDeployer the deployments missing a deployer
    * @throws DeploymentException when some deployment is not complete
    */
   void checkComplete(Collection<DeploymentContext> errors, Collection<Deployment> missingDeployer) throws DeploymentException;

   /**
    * Check if deployments are complete
    * 
    * @param contexts the deployments
    * @throws DeploymentException when the deployment is not complete
    */
   void checkComplete(DeploymentContext... contexts) throws DeploymentException;

   /**
    * Check if deployments are structurally complete
    *
    * @param contexts the deployments
    * @throws DeploymentException when the deployment is not complete
    */
   void checkStructureComplete(DeploymentContext... contexts) throws DeploymentException;

   /**
    * Shutdown. Stops any further deployment
    */
   void shutdown();
}
