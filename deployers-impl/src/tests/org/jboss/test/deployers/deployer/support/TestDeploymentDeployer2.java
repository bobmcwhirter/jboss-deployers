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
public class TestDeploymentDeployer2 extends AbstractComponentDeployer<TestDeployment2, TestMetaData2>
{
   public TestDeploymentDeployer2()
   {
      setDeploymentVisitor(new TestDeploymentVisitor());
      setComponentVisitor(new TestMetaDataVisitor());
   }

   protected static void addTestComponent(DeploymentUnit unit, TestMetaData2 test)
   {
      DeploymentUnit component = unit.addComponent(test.getName());
      component.addAttachment(TestMetaData2.class, test);
   }

   protected static void removeTestComponent(DeploymentUnit unit, TestMetaData2 test)
   {
      unit.removeComponent(test.getName());
   }
   
   /**
    * TestDeploymentVisitor.
    */
   public class TestDeploymentVisitor implements DeploymentVisitor<TestDeployment2>
   {
      public Class<TestDeployment2> getVisitorType()
      {
         return TestDeployment2.class;
      }

      public void deploy(DeploymentUnit unit, TestDeployment2 deployment) throws DeploymentException
      {
         try
         {
            List<TestMetaData2> tests = deployment.getBeans();
            if (tests == null || tests.isEmpty())
               return;
            
            for (TestMetaData2 test : tests)
               addTestComponent(unit, test);
         }
         catch (Throwable t)
         {
            throw DeploymentException.rethrowAsDeploymentException("Error deploying: " + deployment, t);
         }
      }

      public void undeploy(DeploymentUnit unit, TestDeployment2 deployment)
      {
         List<TestMetaData2> tests = deployment.getBeans();
         if (tests == null)
            return;
         
         for (TestMetaData2 test : tests)
         {
            unit.removeComponent(test.getName());
         }
      }
   }

   /**
    * TestMetaDataVisitor.
    */
   public static class TestMetaDataVisitor implements DeploymentVisitor<TestMetaData2>
   {
      public Class<TestMetaData2> getVisitorType()
      {
         return TestMetaData2.class;
      }

      public void deploy(DeploymentUnit unit, TestMetaData2 deployment) throws DeploymentException
      {
         addTestComponent(unit, deployment);
      }

      public void undeploy(DeploymentUnit unit, TestMetaData2 deployment)
      {
         removeTestComponent(unit, deployment);
      }
   }
}
