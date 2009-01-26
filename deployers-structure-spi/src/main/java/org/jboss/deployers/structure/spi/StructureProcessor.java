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
package org.jboss.deployers.structure.spi;

import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.spi.structure.ContextInfo;
import org.jboss.deployers.spi.structure.StructureMetaData;

/**
 * Pre and post structure metadata processor.
 *
 * @author ales.justin@jboss.org
 */
public interface StructureProcessor
{
   /**
    * Prepare structure metadata.
    *
    * @param deployment the deployment
    * @param structureMetaData the structure metadata
    */
   void prepareStructureMetaData(Deployment deployment, StructureMetaData structureMetaData);

   /**
    * Prepare context info.
    *
    * @param parentDeploymentContext the parent deployment context
    * @param contextInfo the context info
    */
   void prepareContextInfo(DeploymentContext parentDeploymentContext, ContextInfo contextInfo);

   /**
    * Apply structure metadata.
    *
    * @param deploymentContext the deployment context
    * @param structureMetaData the structure metadata
    */
   void applyStructureMetaData(DeploymentContext deploymentContext, StructureMetaData structureMetaData);

   /**
    * Apply context info.
    *
    * @param deploymentContext the deployment context
    * @param contextInfo the context info
    */
   void applyContextInfo(DeploymentContext deploymentContext, ContextInfo contextInfo);
}