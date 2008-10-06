/*
* JBoss, Home of Professional Open Source
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
package org.jboss.deployers.structure.spi.helpers;

import java.util.Stack;

/**
 * DeployerTracking.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class DeployerTracking
{
   /** The thread local stack containg the current deployer */
   private static ThreadLocal<Stack<String>> currentDeployer = new ThreadLocal<Stack<String>>()
   {
      protected Stack<String> initialValue()
      {
         return new Stack<String>();
      }
   };

   /**
    * Get the stack.
    *
    * @return the stack
    */
   private static Stack<String> getStack()
   {
      return currentDeployer.get();
   }

   /**
    * Get the current deployer
    * 
    * @return the name of the current deployer
    */
   public static String getCurrentDeployer()
   {
      return (getStack().isEmpty()) ? "UNKNOWN" : getStack().peek();
   }

   /**
    * Push onto stack.
    *
    * @param deployer the current deployer
    */
   public static void push(String deployer)
   {
      getStack().push(deployer);
   }

   /**
    * Pop from the stack.
    */
   public static void pop()
   {
      getStack().pop();
   }
}
