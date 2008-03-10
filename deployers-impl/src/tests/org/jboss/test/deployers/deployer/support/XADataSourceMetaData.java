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

import org.jboss.managed.api.annotation.ManagementComponent;
import org.jboss.managed.api.annotation.ManagementObject;
import org.jboss.managed.api.annotation.ManagementProperty;
import org.jboss.managed.plugins.WritethroughManagedPropertyImpl;

/**
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
@ManagementObject(componentType=@ManagementComponent(type="DataSource", subtype="XA"))
public class XADataSourceMetaData extends ConnMetaData
{
   private static final long serialVersionUID = 1;

   private String xaDataSourceClass;
   private int xaResourceTimeout;

   public XADataSourceMetaData()
   {
   }

   @ManagementProperty(propertyFactory=WritethroughManagedPropertyImpl.class)
   public String getXaDataSourceClass()
   {
      return xaDataSourceClass;
   }

   public void setXaDataSourceClass(String xaDataSourceClass)
   {
      this.xaDataSourceClass = xaDataSourceClass;
   }

   @ManagementProperty(propertyFactory=WritethroughManagedPropertyImpl.class)
   public int getXaResourceTimeout()
   {
      return xaResourceTimeout;
   }

   public void setXaResourceTimeout(int xaResourceTimeout)
   {
      this.xaResourceTimeout = xaResourceTimeout;
   }

}
