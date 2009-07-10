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

import java.io.Serializable;

/**
 * MissingDependency.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class MissingDependency implements Serializable
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -1159684023853245283L;
   
   /** The name */
   private String name;
   
   /** The dependency */
   private String dependency;
   
   /** The required state */
   private String requiredState;
   
   /** The actual state */
   private String actualState;

   /**
    * For serialization
    */
   public MissingDependency()
   {
   }

   /**
    * Create a new MissingDependency.
    * 
    * @param name the name
    * @param dependency the dependency
    * @param requiredState the required state
    * @param actualState the actual state
    */
   public MissingDependency(String name, String dependency, String requiredState, String actualState)
   {
      this.name = name;
      this.dependency = dependency;
      this.requiredState = requiredState;
      this.actualState = actualState;
   }

   /**
    * Get the actualState.
    * 
    * @return the actualState.
    */
   public String getActualState()
   {
      return actualState;
   }

   /**
    * Get the dependency.
    * 
    * @return the dependency.
    */
   public String getDependency()
   {
      return dependency;
   }

   /**
    * Get the name.
    * 
    * @return the name.
    */
   public String getName()
   {
      return name;
   }

   /**
    * Get the requiredState.
    * 
    * @return the requiredState.
    */
   public String getRequiredState()
   {
      return requiredState;
   }

   /**
    * Return a string containing the missing dependency for use in IncompleteDeployments
    * @return The formatted missing dependency
    */
   String display()
   {
      return String.format("    Dependency \"%s\" (should be in state \"%s\", but is actually in state \"%s\")\n",
            dependency,
            requiredState,
            actualState);
   }
}
