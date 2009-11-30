/*
 * JBoss, Home of Professional Open Source.
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
package org.jboss.test.system.deployers.support;

import java.util.Collections;
import java.util.Set;

import org.jboss.deployers.spi.deployer.helpers.AbstractParsingDeployerWithOutput;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.system.metadata.ServiceDeployment;
import org.jboss.system.metadata.ServiceMetaData;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class SMDParsingDeployer extends AbstractParsingDeployerWithOutput<ServiceDeployment>
{
   private ServiceMetaData smd;

   public SMDParsingDeployer(ServiceMetaData smd)
   {
      super(ServiceDeployment.class);
      this.smd = smd;
   }

   protected ServiceDeployment getServiceDeployment() throws Exception
   {
      ServiceDeployment serviceDeployment = new ServiceDeployment();
      serviceDeployment.setServices(Collections.singletonList(smd));
      return serviceDeployment;
   }

   @Override
   protected ServiceDeployment parse(DeploymentUnit arg0, Set<String> arg1, ServiceDeployment arg2) throws Exception
   {
      return getServiceDeployment();
   }

   @Override
   protected ServiceDeployment parse(DeploymentUnit arg0, Set<String> arg1, String arg2, ServiceDeployment arg3) throws Exception
   {
      return getServiceDeployment();
   }

   protected ServiceDeployment parse(DeploymentUnit deploymentUnit, String s, ServiceDeployment deployment) throws Exception
   {
      return getServiceDeployment();
   }

   protected ServiceDeployment parse(DeploymentUnit deploymentUnit, String s, String s1, ServiceDeployment deployment) throws Exception
   {
      return getServiceDeployment();
   }
}
