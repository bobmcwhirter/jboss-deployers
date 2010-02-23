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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStage;
import org.jboss.deployers.spi.deployer.helpers.AbstractRealDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;

/**
 * TestSimpleDeployer.
 *
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 */
public class TestSimpleDeployer extends AbstractRealDeployer
{
   private List<String> deployed = new CopyOnWriteArrayList<String>();
   private List<String> undeployed = new CopyOnWriteArrayList<String>();
   private List<String> failed = new CopyOnWriteArrayList<String>();

   private String name;

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
      name = "TestSimpleDeployer" + relativeOrder;
   }

   public TestSimpleDeployer(DeploymentStage stage)
   {
      setStage(stage);
      name = super.toString();
   }

   public TestSimpleDeployer(String name, DeploymentStage stage)
   {
      setStage(stage);
      this.name = name;
   }

   public String toString()
   {
      return name;
   }

   public void clear()
   {
      deployed.clear();
      undeployed.clear();
      failed.clear();
   }

   public List<String> getDeployedUnits()
   {
      return deployed;
   }

   public List<String> getUndeployedUnits()
   {
      return undeployed;
   }

   public List<String> getFailed()
   {
      return failed;
   }

   public void internalDeploy(DeploymentUnit unit) throws DeploymentException
   {
      log.debug(this + " deploy  : " + unit.getName());
      deployed.add(unit.getName());
      if (this.equals(unit.getAttachment("fail")))
      {
         failed.add(unit.getName());
         throw new DeploymentException("Asked to fail");
      }
   }

   public void internalUndeploy(DeploymentUnit unit)
   {
      log.debug(this + " undeploy: " + unit.getName());
      undeployed.add(unit.getName());
   }
}
