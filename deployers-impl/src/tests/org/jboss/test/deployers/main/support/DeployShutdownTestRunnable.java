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

import java.util.Set;

import org.jboss.deployers.client.spi.DeployerClient;
import org.jboss.deployers.client.spi.Deployment;

/**
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public abstract class DeployShutdownTestRunnable extends DeployerTestRunnable
{
   protected DeployShutdownTestRunnable(DeployerClient main, Deployment deployment)
   {
      super(main, deployment);
   }

   public boolean isValid()
   {
      if (deployment instanceof TestDeployment == false)
         throw new IllegalArgumentException("Deployment should be test deployment.");

      TestDeployment testDeployment = (TestDeployment)deployment;
      String name = testDeployment.getAvoidNameHitName();
      Set<String> names = testDeployment.getNames();
      return (super.isValid() ^ names.contains(name)) == false;
   }
}
