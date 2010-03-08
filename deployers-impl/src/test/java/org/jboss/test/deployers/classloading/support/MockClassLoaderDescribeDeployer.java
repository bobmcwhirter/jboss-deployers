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
package org.jboss.test.deployers.classloading.support;

import java.util.HashSet;
import java.util.Set;

import org.jboss.classloading.spi.dependency.policy.ClassLoaderPolicyModule;
import org.jboss.classloading.spi.metadata.ClassLoadingMetaData;
import org.jboss.deployers.plugins.classloading.AbstractClassLoaderDescribeDeployer;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.structure.spi.DeploymentUnit;

/**
 * MockClassLoaderDescribeDeployer.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class MockClassLoaderDescribeDeployer extends AbstractClassLoaderDescribeDeployer implements MockDeployer
{
   public Set<String> deployed = new HashSet<String>();
   public Set<String> undeployed = new HashSet<String>();

   protected ClassLoaderPolicyModule createModule(DeploymentUnit unit, ClassLoadingMetaData metaData) throws DeploymentException
   {
      return new MockDeploymentClassLoaderPolicyModule(unit);
   }

   public void internalDeploy(DeploymentUnit unit) throws DeploymentException
   {
      deployed.add(unit.getName());
      super.internalDeploy(unit);
   }

   public void internalUndeploy(DeploymentUnit unit)
   {
      undeployed.add(unit.getName());
      super.internalUndeploy(unit);
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
      return getClass().getSimpleName();
   }
}
