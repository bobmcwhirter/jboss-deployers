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
package org.jboss.test.deployers.deployer.support;

import java.io.Serializable;

import org.jboss.managed.api.annotation.ManagementComponent;
import org.jboss.managed.api.annotation.ManagementObject;
import org.jboss.managed.api.annotation.ManagementObjectID;
import org.jboss.managed.api.annotation.ManagementProperty;
import org.jboss.managed.api.annotation.ManagementRuntimeRef;

/**
 * Simple meta data.
 *
 * @author Ales.Justin@jboss.org
 */
@ManagementObject(componentType = @ManagementComponent(type = "RuntimeType", subtype = "LocalTx"))
public class RuntimeComponentMetaData implements Serializable
{
   private static final long serialVersionUID = 1L;

   private String domain;
   private CustomName customName;

   @ManagementProperty(name="domain-name")
   @ManagementObjectID(type="SecurityDomain")
   public String getDomain()
   {
      return domain;
   }

   public void setDomain(String domain)
   {
      this.domain = domain;
   }

   @ManagementProperty(name="custom-name")
   @ManagementRuntimeRef(transformer = CustomRuntimeComponentNameTransformer.class)
   public CustomName getCustomName()
   {
      return customName;
   }

   public void setCustomName(CustomName customName)
   {
      this.customName = customName;
   }
}
