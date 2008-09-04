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
package org.jboss.deployers.spi;

/**
 * DeploymentException.
 * 
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class DeploymentException extends Exception
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 4495361010574179178L;

   /**
    * Rethrow a throwable as a deployment exception if it isn't already.
    *
    * @param message the message
    * @param t the throwable
    * @return never
    * @throws DeploymentException always
    */
   public static DeploymentException rethrowAsDeploymentException(String message, Throwable t) throws DeploymentException
   {
      if (t instanceof DeploymentException)
         throw (DeploymentException) t;
      else
         throw new DeploymentException(message, t);
   }

   /**
    * Constructs a <code>DeploymentException</code> with <code>null</code>
    * as its error detail message.
    */
   public DeploymentException()
   {
   }

   /**
    * Constructs a <code>DeploymentException</code> with the specified detail
    * message. The error message string <code>s</code> can later be
    * retrieved by the <code>{@link java.lang.Throwable#getMessage}</code>
    * method of class <code>java.lang.Throwable</code>.
    * 
    * @param s the message
    */
   public DeploymentException(String s)
   {
      super(s);
   }
   
   /**
    * Constructs a <code>DeploymentException</code> with the specified detail message and
    * cause.  <p>Note that the detail message associated with
    * <code>cause</code> is <i>not</i> automatically incorporated in
    * this exception's detail message.
    * 
    * @param s the message
    * @param cause the underlying cause
    */   
   public DeploymentException(String s, Throwable cause)
   {
      super(s, cause);
   }
   
   /**
     * Constructs a <code>DeploymentException</code> with the specified cause and a detail
     * message of <tt>(cause==null ? null : cause.toString())</tt> (which
     * typically contains the class and detail message of <tt>cause</tt>).
     * This constructor is useful for exceptions that are little more than
     * wrappers for other throwables (for example, {@link
     * java.security.PrivilegedActionException}).
     * 
     * @param cause the underlying cause
    */
   public DeploymentException(Throwable cause)
   {
      super(cause);
   }
}
