/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.deployers.vfs.deployer.bean.support;

import org.jboss.managed.api.annotation.ManagementObject;
import org.jboss.managed.api.annotation.ManagementObjectRef;
import org.jboss.managed.api.annotation.ManagementProperty;

/**
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision:$
 */
@ManagementObject
public class AnnotatedBean
{
   /** Should not be a ManagedProperty */
   private String ignored;
   private String prop1;
   private int prop2;
   /** A ManagedProperty whose annotation should come from the deployment descriptor */
   private int propWithXmlOverride;
   private Simple bean;

   @ManagementProperty(ignored=true)
   public String getIgnored()
   {
      return ignored;
   }
   public void setIgnored(String ignored)
   {
      this.ignored = ignored;
   }
   public String getProp1()
   {
      return prop1;
   }
   public void setProp1(String prop1)
   {
      this.prop1 = prop1;
   }
   public int getProp2()
   {
      return prop2;
   }
   public void setProp2(int prop2)
   {
      this.prop2 = prop2;
   }

   public int getPropWithXmlOverride()
   {
      return propWithXmlOverride;
   }
   public void setPropWithXmlOverride(int propWithXmlOverride)
   {
      this.propWithXmlOverride = propWithXmlOverride;
   }

   @ManagementObjectRef(name="Simple", type="MCBean")
   public Simple getBean()
   {
      return bean;
   }
   public void setBean(Simple bean)
   {
      this.bean = bean;
   }
}
