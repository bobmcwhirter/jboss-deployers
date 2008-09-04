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

import org.jboss.dependency.spi.Controller;
import org.jboss.dependency.spi.ControllerContext;
import org.jboss.dependency.spi.ControllerContextActions;
import org.jboss.dependency.spi.DependencyItem;

/**
 * Test attachments deployer.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class OrderedTestAttachmentDeployer extends TestAttachmentDeployer
{
   public OrderedTestAttachmentDeployer(Controller controller)
   {
      super(controller);
   }

   protected ControllerContext createControllerContext(Object name, ControllerContextActions actions)
   {
      return new OrderedControllerContext(name, actions);
   }

   protected DependencyItem createDependencyItem(Object name, Object dependency)
   {
      TestDemandDependencyItem item = new TestDemandDependencyItem(name);
//      TestDemandDependencyItem item = new TestDemandDependencyItem(name, dependency);
      item.setDemand(dependency);
      return item;
   }
}