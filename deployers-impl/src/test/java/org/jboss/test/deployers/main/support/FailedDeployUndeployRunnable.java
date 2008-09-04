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
import org.jboss.deployers.spi.attachments.MutableAttachments;
import org.jboss.deployers.spi.deployer.Deployer;

/**
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class FailedDeployUndeployRunnable extends DeployUndeployRunnable
{
   public FailedDeployUndeployRunnable(DeployerClient main, Deployment deployment, Deployer deployer)
   {
      super(main, deployment);
      MutableAttachments mutable = (MutableAttachments) deployment.getPredeterminedManagedObjects();
      mutable.addAttachment("fail", deployer);
   }

   public boolean isValid()
   {
      return super.isValid() == false;
   }
}
