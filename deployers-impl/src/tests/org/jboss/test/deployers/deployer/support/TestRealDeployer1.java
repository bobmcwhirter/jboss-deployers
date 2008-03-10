/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
import org.jboss.deployers.spi.deployer.helpers.AbstractRealDeployerWithInput;
import org.jboss.deployers.spi.deployer.helpers.DeploymentVisitor;
import org.jboss.deployers.structure.spi.DeploymentUnit;

/**
 * TestDeploymentDeployer.
 * 
 * @author <a href="adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class TestRealDeployer1 extends AbstractRealDeployerWithInput<TestMetaData1>
{
   public List<TestMetaData1> deployed = new ArrayList<TestMetaData1>();
   
   public TestRealDeployer1()
   {
      setDeploymentVisitor(new TestMetaDataVisitor());
      setComponentsOnly(true);
   }
   
   public class TestMetaDataVisitor implements DeploymentVisitor<TestMetaData1>
   {
      public Class<TestMetaData1> getVisitorType()
      {
         return TestMetaData1.class;
      }

      public void deploy(DeploymentUnit unit, TestMetaData1 deployment) throws DeploymentException
      {
         deployed.add(deployment);
      }

      public void undeploy(DeploymentUnit unit, TestMetaData1 deployment)
      {
         deployed.remove(deployment);
      }
   }
}
