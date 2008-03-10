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
import org.jboss.managed.api.annotation.ManagementObjectRef;
import org.jboss.managed.api.annotation.ManagementProperty;

/**
 * Simple meta data.
 *
 * @author Ales.Justin@jboss.org
 */
@ManagementObject(componentType = @ManagementComponent(type = "SimpleType", subtype = "LocalTx"))
public class SimpleMetaData implements Serializable
{
   private static final long serialVersionUID = 1L;

   public enum SecurityDeploymentType {NONE, APPLICATION, DOMAIN, DOMAIN_AND_APPLICATION}

   private String domain;
   private SimpleMetaData.SecurityDeploymentType type;
   private String jndiName;

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

   @ManagementProperty(name="security-criteria")
   public SimpleMetaData.SecurityDeploymentType getType()
   {
      return type;
   }

   public void setType(SimpleMetaData.SecurityDeploymentType type)
   {
      this.type = type;
   }

   @ManagementProperty(name="jndi-name")
   @ManagementObjectRef(type="DataSource")
   public String getJndiName()
   {
      return jndiName;
   }

   public void setJndiName(String jndiName)
   {
      this.jndiName = jndiName;
   }
}
