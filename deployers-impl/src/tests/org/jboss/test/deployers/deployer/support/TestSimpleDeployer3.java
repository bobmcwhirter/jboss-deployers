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

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStage;
import org.jboss.deployers.spi.deployer.helpers.AbstractRealDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;

/**
 * TestSimpleDeployer3.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class TestSimpleDeployer3 extends AbstractRealDeployer
{
   public List<String> deployed = new ArrayList<String>();
   public List<String> undeployed = new ArrayList<String>();
   
   public TestSimpleDeployer3(DeploymentStage stage)
   {
      setStage(stage);
   }
   
   public List<String> getDeployedUnits()
   {
      return deployed;
   }
   
   public List<String> getUndeployedUnits()
   {
      return undeployed;
   }
   
   public String toString()
   {
      return "TestSimpleDeployer3(" + getStage() + ")";
   }
   
   public void clear()
   {
      deployed.clear();
      undeployed.clear();
   }

   public void internalDeploy(DeploymentUnit unit) throws DeploymentException
   {
      log.debug(this + " deploy  : " + unit.getName());
      deployed.add(unit.getName());
      if (this.equals(unit.getAttachment("fail")))
         throw new RuntimeException("Asked to fail");
   }

   public void internalUndeploy(DeploymentUnit unit)
   {
      log.debug(this + " undeploy: " + unit.getName());
      undeployed.add(unit.getName());
   }
}
