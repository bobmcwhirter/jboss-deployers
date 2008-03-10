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
package org.jboss.test.deployers.main.support;

import org.jboss.deployers.client.spi.DeployerClient;
import org.jboss.deployers.client.spi.Deployment;

/**
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public abstract class DeployerTestRunnable implements Runnable
{
   protected DeployerClient main;
   protected Deployment deployment;
   private Throwable problem;

   protected DeployerTestRunnable(DeployerClient main, Deployment deployment)
   {
      this.main = main;
      this.deployment = deployment;
   }

   public void run()
   {
      try
      {
         internalRun();
      }
      catch (Throwable t)
      {
         setProblem(t);
      }
   }

   protected abstract void internalRun() throws Throwable;

   public boolean isValid()
   {
      return (problem == null);
   }

   public Throwable getProblem()
   {
      return problem;
   }

   public void setProblem(Throwable problem)
   {
      this.problem = problem;
   }

   public String toString()
   {
      return getClass().getSimpleName() + ": " + deployment + "/" + problem;
   }
}
