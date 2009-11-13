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

import java.util.Set;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.Ordered;
import org.jboss.deployers.structure.spi.DeploymentUnit;

/**
 * Deployer.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public interface Deployer extends Ordered
{
   /**
    * Get the deployment stage for this deployer
    * 
    * @return the stage
    */
   DeploymentStage getStage();
   
   /**
    * Whether we only want the top level
    * 
    * @return true for top level only
    */
   boolean isTopLevelOnly();

   /**
    * Whether we only want components
    * 
    * @return true for components only
    */
   boolean isComponentsOnly();

   /**
    * Whether we dont want components
    * 
    * @return true for no components
    */
   boolean isWantComponents();
   
   /**
    * Whether we want all inputs.
    *
    * @return true for all inputs
    */
   boolean isAllInputs();
   
   /**
    * Get the input for this deployer.
    *
    * Most deployers operate on a single attachment,
    * if you need multiple inputs use required inputs.
    *
    * By default we require this input,
    * if you wanna make this optional set all-inputs to true.
    * 
    * @return the input type
    */
   Class<?> getInput();
   
   /**
    * Get the otput for this deployer
    * 
    * @return the output type
    */
   Class<?> getOutput();
   
   /**
    * Get the input for this deployer.
    *
    * This is set is mostly meant to help with
    * natural order based on inputs/outputs.
    *
    * If you want to veto your deployer based on inputs,
    * you should use required inputs.
    *
    * Every required input is of course also a plain input.
    * 
    * @return the inputs
    */
   Set<String> getInputs();
   
   /**
    * Get the required input for this deployer.
    *
    * This represents a set of required inputs,
    * if one of them is missing, we veto deployer as not relevant
    * for that deployment, hence it doesn't participate in deployment lifecycle.
    *
    * @return the required inputs
    */
   Set<String> getRequiredInputs();

   /**
    * Get the outputs for this deployer
    * 
    * @return the outputs
    */
   Set<String> getOutputs();
   
   /**
    * Whether to process parents before children (default true)
    * 
    * @return true to process parents first
    */
   boolean isParentFirst();
   
   /**
    * Deploy a deployment
    * 
    * @param unit the unit
    * @throws DeploymentException for any error
    */
   void deploy(DeploymentUnit unit) throws DeploymentException;

   /**
    * Undeploy an deployment
    * 
    * @param unit the unit
    */
   void undeploy(DeploymentUnit unit);
}
