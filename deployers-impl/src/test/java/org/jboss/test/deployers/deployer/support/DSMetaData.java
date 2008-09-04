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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jboss.managed.api.annotation.ManagementObject;
import org.jboss.managed.api.annotation.ManagementProperties;
import org.jboss.managed.api.annotation.ManagementProperty;

/**
 * Test datasource deployment like metadata
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
@ManagementObject(properties=ManagementProperties.EXPLICIT)
public class DSMetaData implements Serializable
{
   private static final long serialVersionUID = 1;
   private String diplayName;
   private URL fileUrl;
   private List<String> aliases = new ArrayList<String>();
   private List<ConnMetaData> deployments = new ArrayList<ConnMetaData>();

   public DSMetaData()
   {
      deployments.add(new ConnMetaData());
   }

   @ManagementProperty(name="display-name", description="display name of DS deployment", use={})
   public String getDiplayName()
   {
      return diplayName;
   }

   public void setDiplayName(String diplayName)
   {
      this.diplayName = diplayName;
   }

   @ManagementProperty(description="The ds.xml url", ignored=true)
   public URL getUrl()
   {
      return fileUrl;
   }
   public void setUrl(URL fileUrl)
   {
      this.fileUrl = fileUrl;
   }

   public List<String> getAliases()
   {
      return aliases;
   }
   public void setAliases(List<String> aliases)
   {
      this.aliases = aliases;
   }

   public void addManagedConnectionFactoryDeployment(ConnMetaData deployment)
   {
      this.deployments.add(deployment);
   }

   @ManagementProperty(description="The DS connection factories", managed=true)
   public List<ConnMetaData> getDeployments()
   {
      return deployments;
   }
   public void setDeployments(List<ConnMetaData> deployments)
   {
      this.deployments.clear();
      this.deployments.addAll(deployments);
   }

}
