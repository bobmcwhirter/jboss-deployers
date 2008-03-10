/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.deployers.structure.spi.helpers;

import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.attachments.Attachments;
import org.jboss.deployers.spi.structure.StructureMetaData;
import org.jboss.deployers.spi.structure.StructureMetaDataFactory;
import org.jboss.deployers.structure.spi.DeploymentContext;
import org.jboss.deployers.structure.spi.StructuralDeployers;
import org.jboss.deployers.structure.spi.StructureBuilder;

/**
 * AbstractStructuralDeployers.
 * 
 * @author <a href="adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class AbstractStructuralDeployers implements StructuralDeployers
{
   /** The structure builder */
   private StructureBuilder structureBuilder;

   /**
    * Get the structureBuilder.
    * 
    * @return the structureBuilder.
    */
   public StructureBuilder getStructureBuilder()
   {
      return structureBuilder;
   }

   /**
    * Set the structureBuilder.
    * 
    * @param structureBuilder the structureBuilder.
    */
   public void setStructureBuilder(StructureBuilder structureBuilder)
   {
      this.structureBuilder = structureBuilder;
   }

   public DeploymentContext determineStructure(Deployment deployment) throws DeploymentException
   {
      StructureBuilder builder = getStructureBuilder();
      if (builder == null)
         throw new IllegalStateException("No structure builder has been configured");
      
      Attachments attachments = deployment.getPredeterminedManagedObjects();
      StructureMetaData structureMetaData = attachments.getAttachment(StructureMetaData.class);
      if (structureMetaData == null)
      {
         structureMetaData = StructureMetaDataFactory.createStructureMetaData();
         try
         {
            determineStructure(deployment, structureMetaData);
         }
         catch (DeploymentException e)
         {
            throw e;
         }
         catch (Throwable t)
         {
            throw DeploymentException.rethrowAsDeploymentException("Exception determining structure: " + deployment, t);
         }
      }
      
      return structureBuilder.populateContext(deployment, structureMetaData);
   }
   
   protected void determineStructure(Deployment deployment, StructureMetaData structure) throws Exception
   {
      // NOTHING
   }
}
