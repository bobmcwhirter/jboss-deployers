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

import org.jboss.dependency.plugins.AbstractDependencyItem;
import org.jboss.dependency.spi.Controller;
import org.jboss.dependency.spi.ControllerContext;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.util.JBossStringBuilder;

/**
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class TestDemandDependencyItem extends AbstractDependencyItem
{
   private Object demand;

   public TestDemandDependencyItem(Object name)
   {
      this(name, null);
   }

   public TestDemandDependencyItem(Object name, Object demand)
   {
      super(name, demand, ControllerState.INSTALLED, null);
   }

   public Object getDemand()
   {
      return demand;
   }

   public void setDemand(Object demand)
   {
      this.demand = demand;
   }

   public boolean resolve(Controller controller)
   {
      Object name = getDemand();
      ControllerContext context = controller.getInstalledContext(name);
      if (context != null)
      {
         setIDependOn(context.getName());
         addDependsOnMe(controller, context);
         setResolved(true);
      }
      else
      {
         setResolved(false);
      }
      return isResolved();
   }

   @Override
   public void unresolved()
   {
      setIDependOn(null);
      setResolved(false);
   }

   public void toString(JBossStringBuilder buffer)
   {
      super.toString(buffer);
      buffer.append(" demand=").append(getDemand());
   }

   public void toShortString(JBossStringBuilder buffer)
   {
      buffer.append(getName()).append(" demands ").append(getDemand());
   }

   @Override
   public String toHumanReadableString()
   {
      StringBuilder builder = new StringBuilder();
      builder.append("Demands '").append(getDemand()).append("'");
      return builder.toString();
   }
}
