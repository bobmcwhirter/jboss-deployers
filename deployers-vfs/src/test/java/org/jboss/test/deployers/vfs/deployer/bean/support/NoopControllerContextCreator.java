/*
* JBoss, Home of Professional Open Source.
* Copyright 2006, Red Hat Middleware LLC, and individual contributors
* as indicated by the @author tags. See the copyright.txt file in the
* distribution for a full listing of individual contributors. 
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
package org.jboss.test.deployers.vfs.deployer.bean.support;

import java.util.ArrayList;
import java.util.List;

import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.dependency.spi.Controller;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.deployer.helpers.KernelControllerContextCreator;
import org.jboss.kernel.spi.dependency.KernelControllerContext;

/**
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
public class NoopControllerContextCreator implements KernelControllerContextCreator
{
   private static List<Integer> triggered = new ArrayList<Integer>();
   
   private int order;
   
   public NoopControllerContextCreator(int order)
   {
      this.order = order;
   }
   
   public static List<Integer> getTriggered()
   {
      return triggered;
   }
   
   public KernelControllerContext createContext(Controller controller, DeploymentUnit unit, BeanMetaData beanMetaData)
   {
      if (controller == null)
         throw new IllegalArgumentException("Null controller");
      if (unit == null)
         throw new IllegalArgumentException("Null unit");
      if (beanMetaData == null)
         throw new IllegalArgumentException("Null beanMetadata");
      triggered.add(order);
      return null;
   }

   public int getRelativeOrder()
   {
      return order;
   }

   public boolean uninstallContext(Controller controller, DeploymentUnit unit, BeanMetaData beanMetaData)
   {
      throw new IllegalStateException("Should not be called since NoopCCC never creates any deployments");
   }
}
