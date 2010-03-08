/*
* JBoss, Home of Professional Open Source
* Copyright 2010, Red Hat Inc., and individual contributors as indicated
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
package org.jboss.test.deployers.classloading.support;

import java.util.HashSet;
import java.util.Set;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.helpers.AbstractRealDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;

/**
 * MockDeployer
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class MockDeployerImpl extends AbstractRealDeployer implements MockDeployer
{
   public Set<String> deployed = new HashSet<String>();
   public Set<String> undeployed = new HashSet<String>();

   String name;
   
   public MockDeployerImpl(String name)
   {
      this.name = name;
   }
   
   public void internalDeploy(DeploymentUnit unit) throws DeploymentException
   {
      deployed.add(unit.getName());
   }

   public void internalUndeploy(DeploymentUnit unit)
   {
      undeployed.add(unit.getName());
   }

   public void clear()
   {
      deployed.clear();
      undeployed.clear();
   }

   public Set<String> getDeployed()
   {
      return deployed;
   }

   public Set<String> getUnDeployed()
   {
      return undeployed;
   }
   
   @Override
   public String toString()
   {
      return name;
   }
}
