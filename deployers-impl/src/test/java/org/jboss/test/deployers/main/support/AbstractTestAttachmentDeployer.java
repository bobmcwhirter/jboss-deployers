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

import org.jboss.dependency.plugins.AbstractControllerContext;
import org.jboss.dependency.plugins.AbstractDependencyItem;
import org.jboss.dependency.spi.Controller;
import org.jboss.dependency.spi.ControllerContext;
import org.jboss.dependency.spi.ControllerContextActions;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.dependency.spi.DependencyInfo;
import org.jboss.dependency.spi.DependencyItem;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.helpers.AbstractRealDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;

/**
 * Test attachments deployer.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public abstract class AbstractTestAttachmentDeployer extends AbstractRealDeployer
{
   private Controller controller;
   private ControllerContextActions actions = new TestControllerContextActions();

   protected AbstractTestAttachmentDeployer(Controller controller)
   {
      this.controller = controller;
      setInput(TestAttachment.class);
      setUseUnitName(true);
   }

   protected void internalDeploy(DeploymentUnit unit) throws DeploymentException
   {
      TestAttachment attachment = unit.getAttachment(TestAttachment.class);
      if (attachment != null)
      {
         Object name = attachment.getName();
         ControllerContext context = controller.getContext(name, null);
         if (context == null)
            context = createControllerContext(name, actions);
         
         DependencyInfo dependencyInfo = context.getDependencyInfo();
         Object dependency = attachment.getDependency();
         if (dependency != null)
         {
            DependencyItem item = createDependencyItem(name, dependency);
            dependencyInfo.addIDependOn(item);
         }
         try
         {
            if (attachment.isInstall())
               controller.install(context);
         }
         catch (Throwable t)
         {
            throw DeploymentException.rethrowAsDeploymentException("Unexpected.", t);
         }
      }
   }

   protected void internalUndeploy(DeploymentUnit unit)
   {
      TestAttachment attachment = unit.getAttachment(TestAttachment.class);
      if (attachment != null)
      {
         if (attachment.isInstall())
            controller.uninstall(attachment.getName());
      }
   }

   protected ControllerContext createControllerContext(Object name, ControllerContextActions actions)
   {
      return new AbstractControllerContext(name, actions);
   }

   protected DependencyItem createDependencyItem(Object name, Object dependency)
   {
      return new AbstractDependencyItem(name, dependency, ControllerState.INSTALLED, null);
   }

   private class TestControllerContextActions implements ControllerContextActions
   {
      public void install(ControllerContext context, ControllerState fromState, ControllerState toState) throws Throwable
      {
         context.setState(toState);
      }

      public void uninstall(ControllerContext context, ControllerState fromState, ControllerState toState)
      {
         context.setState(toState);
      }
   }
}
