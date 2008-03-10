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
package org.jboss.test.deployers.deployer.support;

import java.util.ArrayList;
import java.util.List;

import org.jboss.dependency.spi.DependencyItem;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractRealDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;

/**
 * TestDescribeDeployer.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class TestDescribeDeployer extends AbstractRealDeployer
{
   public List<String> deployed = new ArrayList<String>();
   public List<String> undeployed = new ArrayList<String>();
   
   public TestDescribeDeployer()
   {
      setStage(DeploymentStages.DESCRIBE);
   }
   
   public void internalDeploy(DeploymentUnit unit) throws DeploymentException
   {
      deployed.add(unit.getName());
      TestDependencyMetaData dependencies = unit.getAttachment(TestDependencyMetaData.class);
      if (dependencies != null)
      {
         for (DependencyItem item : dependencies.iDependOn)
            unit.addIDependOn(item);
      }
   }
   
   @Override
   public void internalUndeploy(DeploymentUnit unit)
   {
      undeployed.add(unit.getName());
      TestDependencyMetaData dependencies = unit.getAttachment(TestDependencyMetaData.class);
      if (dependencies != null)
      {
         for (DependencyItem item : dependencies.iDependOn)
            unit.removeIDependOn(item);
      }
   }
}
