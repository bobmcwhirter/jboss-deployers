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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStage;
import org.jboss.deployers.spi.deployer.helpers.AbstractRealDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;

/**
 * TestSimpleDeployer.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class TestSimpleDeployer extends AbstractRealDeployer
{
   private static int order = 0;
   
   private Map<String, Integer> deployed = new HashMap<String, Integer>();
   private Map<String, Integer> undeployed = new HashMap<String, Integer>();

   private String name;
   
   public static void reset()
   {
      order = 0;
   }
   
   public TestSimpleDeployer()
   {
      this(Integer.MAX_VALUE);
      name = super.toString();
   }
   
   public TestSimpleDeployer(String name)
   {
      this(Integer.MAX_VALUE);
      this.name = name;
   }
   
   public TestSimpleDeployer(int relativeOrder)
   {
      setRelativeOrder(relativeOrder);
      setType("test");
      name = "TestSimpleDeployer" + relativeOrder;
   }
   
   public TestSimpleDeployer(DeploymentStage stage)
   {
      setStage(stage);
      setType("test");
      name = super.toString();
   }

   public String toString()
   {
      return name;
   }
   
   public void clear()
   {
      deployed.clear();
      undeployed.clear();
   }
   
   public int getDeployOrder()
   {
      if (deployed.isEmpty())
         return -1;
      return Collections.max(deployed.values());
   }

   public int getUndeployOrder()
   {
      if (undeployed.isEmpty())
         return -1;
      return Collections.max(undeployed.values());
   }
   
   public Set<String> getDeployedUnits()
   {
      return deployed.keySet();
   }

   public Set<String> getUndeployedUnits()
   {
      return undeployed.keySet();
   }
   
   public Map<String, Integer> getDeployed()
   {
      return deployed;
   }

   public Map<String, Integer> getUndeployed()
   {
      return undeployed;
   }

   public void internalDeploy(DeploymentUnit unit) throws DeploymentException
   {
      log.debug(this + " deploy  : " + unit.getName());
      deployed.put(unit.getName(), ++order);
      if (this.equals(unit.getAttachment("fail")))
         throw new DeploymentException("Asked to fail");
   }

   public void internalUndeploy(DeploymentUnit unit)
   {
      log.debug(this + " undeploy: " + unit.getName());
      undeployed.put(unit.getName(), ++order);
   }
}
