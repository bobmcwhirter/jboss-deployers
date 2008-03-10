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

import java.io.Serializable;
import java.util.Properties;

import org.jboss.managed.api.annotation.ManagementObject;
import org.jboss.managed.api.annotation.ManagementObjectID;
import org.jboss.managed.api.annotation.ManagementProperty;
import org.jboss.managed.plugins.WritethroughManagedPropertyImpl;

/**
 * Test connection factory like metadata.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
@ManagementObject
public class ConnMetaData implements Serializable
{
   private static final long serialVersionUID = 1;
   private int minSize;
   private int maxSize;
   private String connType;
   private String jndiName;
   private String username;
   private char[] password;
   private boolean localTransactions;
   private TxSupportMetaData txSupportMetaData;
   private Properties connProperties;
   private SecMetaData securityMetaData;

   @ManagementProperty(name="datasource-type", constraintsFactory=AllowedDsTypes.class,
         propertyFactory=WritethroughManagedPropertyImpl.class)
   public String getConnType()
   {
      return connType;
   }
   public void setConnType(String connType)
   {
      this.connType = connType;
   }

   @ManagementProperty(name="jndi-name", propertyFactory=WritethroughManagedPropertyImpl.class)
   @ManagementObjectID(type="DataSource")
   public String getJndiName()
   {
      return jndiName;
   }
   public void setJndiName(String jndiName)
   {
      this.jndiName = jndiName;
   }
   @ManagementProperty(name="max-size", propertyFactory=WritethroughManagedPropertyImpl.class)
   public int getMaxSize()
   {
      return maxSize;
   }
   public void setMaxSize(int maxSize)
   {
      this.maxSize = maxSize;
   }
   @ManagementProperty(name="min-size", propertyFactory=WritethroughManagedPropertyImpl.class)
   public int getMinSize()
   {
      return minSize;
   }
   public void setMinSize(int minSize)
   {
      this.minSize = minSize;
   }
   @ManagementProperty(propertyFactory=WritethroughManagedPropertyImpl.class)
   public char[] getPassword()
   {
      return password;
   }
   public void setPassword(char[] password)
   {
      this.password = password;
   }
   @ManagementProperty(propertyFactory=WritethroughManagedPropertyImpl.class)
   public String getUsername()
   {
      return username;
   }
   public void setUsername(String username)
   {
      this.username = username;
   }
   @ManagementProperty(name="connection-properties", propertyFactory=WritethroughManagedPropertyImpl.class)
   public Properties getConnProperties()
   {
      return connProperties;
   }
   public void setConnProperties(Properties connProperties)
   {
      this.connProperties = connProperties;
   }

   @ManagementProperty(name="local-transaction")
   public Boolean getLocalTransactions()
   {
      return localTransactions;
   }

   public void setLocalTransactions(Boolean localTransactions)
   {
      this.localTransactions = localTransactions;
   }

   @ManagementProperty(name="tx-metadata",
         constraintsFactory=AllowedTxSupportMetaData.class)
   public TxSupportMetaData getTransactionSupportMetaData()
   {
      return txSupportMetaData;
   }
   public void setTransactionSupportMetaData(TxSupportMetaData md)
   {
      this.txSupportMetaData = md;
   }   

   @ManagementProperty(name="security-domain", managed=true)
   public SecMetaData getSecurityMetaData()
   {
      return securityMetaData;
   }
   public void setSecurityMetaData(SecMetaData securityMetaData)
   {
      this.securityMetaData = securityMetaData;
   }
}
