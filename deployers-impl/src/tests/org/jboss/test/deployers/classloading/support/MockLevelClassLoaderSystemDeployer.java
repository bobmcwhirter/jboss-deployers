/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.deployers.classloading.support;

import java.util.ArrayList;
import java.util.List;

import org.jboss.deployers.plugins.classloading.AbstractLevelClassLoaderSystemDeployer;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.structure.spi.DeploymentUnit;

/**
 * MockLevelClassLoaderSystemDeployer.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class MockLevelClassLoaderSystemDeployer extends AbstractLevelClassLoaderSystemDeployer
{
   public List<String> deployed = new ArrayList<String>();
   public List<String> undeployed = new ArrayList<String>();

   public void deploy(DeploymentUnit unit) throws DeploymentException
   {
      deployed.add(unit.getName());
      super.deploy(unit);
   }

   public void undeploy(DeploymentUnit unit)
   {
      undeployed.add(unit.getName());
      super.undeploy(unit);
   }
}
