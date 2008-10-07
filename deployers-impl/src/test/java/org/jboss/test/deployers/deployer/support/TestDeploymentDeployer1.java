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

import org.jboss.deployers.spi.deployer.helpers.AbstractComponentDeployer;
import org.jboss.deployers.spi.deployer.helpers.AbstractComponentVisitor;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeploymentVisitor;

/**
 * TestDeploymentDeployer.
 * 
 * @author <a href="adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class TestDeploymentDeployer1 extends AbstractComponentDeployer<TestDeployment1, TestMetaData1>
{
   public TestDeploymentDeployer1()
   {
      setDeploymentVisitor(new TestDeploymentVisitor());
      setComponentVisitor(new TestMetaDataVisitor());
   }

   /**
    * TestDeploymentVisitor.
    */
   public class TestDeploymentVisitor extends AbstractDeploymentVisitor<TestMetaData1, TestDeployment1>
   {
      public Class<TestDeployment1> getVisitorType()
      {
         return TestDeployment1.class;
      }

      protected List<? extends TestMetaData1> getComponents(TestDeployment1 deployment)
      {
         return deployment.getBeans();
      }

      protected Class<TestMetaData1> getComponentType()
      {
         return TestMetaData1.class;
      }

      protected String getComponentName(TestMetaData1 attachment)
      {
         return attachment.getName();
      }
   }

   /**
    * TestMetaDataVisitor.
    */
   public static class TestMetaDataVisitor extends AbstractComponentVisitor<TestMetaData1>
   {
      public Class<TestMetaData1> getVisitorType()
      {
         return TestMetaData1.class;
      }

      protected String getComponentName(TestMetaData1 attachment)
      {
         return attachment.getName();
      }
   }
}
