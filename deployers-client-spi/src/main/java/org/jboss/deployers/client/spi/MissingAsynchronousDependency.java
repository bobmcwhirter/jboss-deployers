/*
* JBoss, Home of Professional Open Source.
* Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.deployers.client.spi;

/**
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
public class MissingAsynchronousDependency extends MissingDependency
{
   private static final long serialVersionUID = 1L;

   /**
    * For serialization
    */
   public MissingAsynchronousDependency()
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
   public MissingAsynchronousDependency(String name, String dependency, String requiredState, String actualState)
   {
      super(name, dependency, requiredState, actualState);
   }
   
   @Override
   String display()
   {
      return String.format("    Dependency \"%s\" (is currently being installed in a background thread)\n",
            super.getDependency());
   }
}
