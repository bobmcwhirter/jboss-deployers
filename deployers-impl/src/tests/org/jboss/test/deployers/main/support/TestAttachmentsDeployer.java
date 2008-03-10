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

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.helpers.AbstractComponentDeployer;
import org.jboss.deployers.spi.deployer.helpers.DeploymentVisitor;
import org.jboss.deployers.structure.spi.DeploymentUnit;

/**
 * Test attachments deployer.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class TestAttachmentsDeployer extends AbstractComponentDeployer<TestAttachments, TestAttachment>
{
   public TestAttachmentsDeployer()
   {
      setDeploymentVisitor(new TestAttachmentsVisitor());
      setComponentVisitor(new TesAttachmentVisitor());
   }

   protected static void addTestComponent(DeploymentUnit unit, TestAttachment bean)
   {
      DeploymentUnit component = unit.addComponent(bean.getName().toString());
      component.addAttachment(TestAttachment.class.getName(), bean);
   }

   protected static void removeTestComponent(DeploymentUnit unit, TestAttachment bean)
   {
      unit.removeComponent(bean.getName().toString());
   }

   /**
    * TestAttachmentsVisitor.
    */
   public static class TestAttachmentsVisitor implements DeploymentVisitor<TestAttachments>
   {
      public Class<TestAttachments> getVisitorType()
      {
         return TestAttachments.class;
      }

      public void deploy(DeploymentUnit unit, TestAttachments deployment) throws DeploymentException
      {
         List<TestAttachment> beans = deployment.getAttachments();
         if (beans != null && beans.isEmpty() == false)
         {
            for (TestAttachment bean : beans)
               addTestComponent(unit, bean);
         }
      }

      public void undeploy(DeploymentUnit unit, TestAttachments deployment)
      {
         List<TestAttachment> beans = deployment.getAttachments();
         if (beans != null && beans.isEmpty() == false)
         {
            for (TestAttachment bean : beans)
               removeTestComponent(unit, bean);
         }
      }
   }

   /**
    * TestAttachmentVisitor.
    */
   public static class TesAttachmentVisitor implements DeploymentVisitor<TestAttachment>
   {
      public Class<TestAttachment> getVisitorType()
      {
         return TestAttachment.class;
      }

      public void deploy(DeploymentUnit unit, TestAttachment deployment) throws DeploymentException
      {
         addTestComponent(unit, deployment);
      }

      public void undeploy(DeploymentUnit unit, TestAttachment deployment)
      {
         removeTestComponent(unit, deployment);
      }
   }
}
