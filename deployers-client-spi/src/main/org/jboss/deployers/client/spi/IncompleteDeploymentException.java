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
package org.jboss.deployers.client.spi;

import org.jboss.deployers.spi.DeploymentException;

/**
 * IncompleteDeploymentException.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class IncompleteDeploymentException extends DeploymentException
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1433292979582684692L;

   /** Incomplete deployments */
   private IncompleteDeployments incompleteDeployments;

   /**
    * For serialization
    */
   public IncompleteDeploymentException()
   {
   }
   
   /**
    * Create a new IncompleteDeploymentException.
    * 
    * @param incompleteDeployments the incomplete deployments
    * @throws IllegalArgumentException for null incompleteDeployments
    */
   public IncompleteDeploymentException(IncompleteDeployments incompleteDeployments)
   {
      if (incompleteDeployments == null)
         throw new IllegalArgumentException("Null incompleteDeployments");
      this.incompleteDeployments = incompleteDeployments;
   }

   /**
    * Get the incompleteDeployments.
    * 
    * @return the incompleteDeployments.
    */
   public IncompleteDeployments getIncompleteDeployments()
   {
      return incompleteDeployments;
   }

   @Override
   public String getMessage()
   {
      StringBuilder buffer = new StringBuilder();
      buffer.append("Summary of incomplete deployments (SEE PREVIOUS ERRORS FOR DETAILS):\n");
      // Display all the missing deployers
      buffer.append(incompleteDeployments.getDeploymentsMissingDeployerInfo());
      // Display all the incomplete deployments
      buffer.append(incompleteDeployments.getDeploymentsInErrorInfo());
      // Display all the missing dependencies
      buffer.append(incompleteDeployments.getContextsMissingDependenciesInfo());
      // Display all contexts in error
      buffer.append(incompleteDeployments.getContextsInErrorInfo());
      // buffer to string
      return buffer.toString();
   }
}
