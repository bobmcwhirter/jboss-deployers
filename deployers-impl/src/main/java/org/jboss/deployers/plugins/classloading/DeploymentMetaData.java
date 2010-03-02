/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Inc., and individual contributors
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
package org.jboss.deployers.plugins.classloading;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.io.Serializable;
import java.util.Set;

import org.jboss.deployers.spi.deployer.DeploymentStage;
import org.jboss.deployers.spi.deployer.DeploymentStageXmlAdapter;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.xb.annotations.JBossXmlSchema;

/**
 * Deployment meta data.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@JBossXmlSchema(namespace="urn:jboss:deployment:1.0", elementFormDefault=XmlNsForm.QUALIFIED)
@XmlRootElement(name="deployment")
@XmlType(name="deploymentType", propOrder={"filters"})
public class DeploymentMetaData implements Serializable
{
   private static final long serialVersionUID = 1l;

   private DeploymentStage requiredStage = DeploymentStages.DESCRIBE;
   private boolean lazyResolve;
   private boolean lazyStart;
   private Set<String> filters;

   public DeploymentStage getRequiredStage()
   {
      return requiredStage;
   }

   @XmlAttribute(name = "required-stage")
   @XmlJavaTypeAdapter(DeploymentStageXmlAdapter.class)
   public void setRequiredStage(DeploymentStage requiredStage)
   {
      if (requiredStage == null)
         requiredStage = DeploymentStages.DESCRIBE;
      
      this.requiredStage = requiredStage;
   }

   public boolean isLazyResolve()
   {
      return lazyResolve;
   }

   @XmlAttribute(name = "lazy-resolve")
   public void setLazyResolve(boolean lazyResolve)
   {
      this.lazyResolve = lazyResolve;
   }

   public boolean isLazyStart()
   {
      return lazyStart || (filters != null && filters.isEmpty() == false);
   }

   @XmlAttribute(name = "lazy-start")
   public void setLazyStart(boolean lazyStart)
   {
      this.lazyStart = lazyStart;
   }

   public Set<String> getFilters()
   {
      return filters;
   }

   @XmlElement(name = "lazy-start-filter")
   public void setFilters(Set<String> filters)
   {
      this.filters = filters;
   }
}