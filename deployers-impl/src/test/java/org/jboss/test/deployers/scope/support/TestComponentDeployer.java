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
package org.jboss.test.deployers.scope.support;

import java.util.List;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.helpers.AbstractComponentDeployer;
import org.jboss.deployers.spi.deployer.helpers.DeploymentVisitor;
import org.jboss.deployers.structure.spi.DeploymentUnit;

/**
 * TestDeploymentDeployer.
 * 
 * @author <a href="adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class TestComponentDeployer extends AbstractComponentDeployer<TestComponentMetaDataContainer, TestComponentMetaData>
{
   public TestComponentDeployer()
   {
      setDeploymentVisitor(new TestComponentMetaDataContainerVisitor());
      setComponentVisitor(new TestComponentMetaDataVisitor());
   }

   protected static void addTestComponent(DeploymentUnit unit, TestComponentMetaData test)
   {
      DeploymentUnit component = unit.addComponent(test.name);
      component.addAttachment(TestComponentMetaData.class, test);
   }

   protected static void removeTestComponent(DeploymentUnit unit, TestComponentMetaData test)
   {
      unit.removeComponent(test.name);
   }
   
   public class TestComponentMetaDataContainerVisitor implements DeploymentVisitor<TestComponentMetaDataContainer>
   {
      public Class<TestComponentMetaDataContainer> getVisitorType()
      {
         return TestComponentMetaDataContainer.class;
      }

      public void deploy(DeploymentUnit unit, TestComponentMetaDataContainer deployment) throws DeploymentException
      {
         try
         {
            List<TestComponentMetaData> tests = deployment.componentMetaData;
            if (tests == null || tests.isEmpty())
               return;
            
            for (TestComponentMetaData test : tests)
               addTestComponent(unit, test);
         }
         catch (Throwable t)
         {
            throw DeploymentException.rethrowAsDeploymentException("Error deploying: " + deployment, t);
         }
      }

      public void undeploy(DeploymentUnit unit, TestComponentMetaDataContainer deployment)
      {
         List<TestComponentMetaData> tests = deployment.componentMetaData;
         if (tests == null)
            return;
         
         for (TestComponentMetaData test : tests)
         {
            unit.removeComponent(test.name);
         }
      }
   }

   public static class TestComponentMetaDataVisitor implements DeploymentVisitor<TestComponentMetaData>
   {
      public Class<TestComponentMetaData> getVisitorType()
      {
         return TestComponentMetaData.class;
      }

      public void deploy(DeploymentUnit unit, TestComponentMetaData deployment) throws DeploymentException
      {
         addTestComponent(unit, deployment);
      }

      public void undeploy(DeploymentUnit unit, TestComponentMetaData deployment)
      {
         removeTestComponent(unit, deployment);
      }
   }
}
