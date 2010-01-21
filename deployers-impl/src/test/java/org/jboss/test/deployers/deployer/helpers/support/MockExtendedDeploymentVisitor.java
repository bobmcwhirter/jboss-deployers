/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.test.deployers.deployer.helpers.support;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.helpers.DeploymentVisitor;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.logging.Logger;

/**
 * MockExtendedDeploymentVisitor
 * 
 * A mock implementation of {@link DeploymentVisitor}, to be used in testing
 * the {@link org.jboss.deployers.spi.deployer.helpers.ExactAttachmentDeployerWithVisitor}
 * 
 * @param <T> the type
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class MockExtendedDeploymentVisitor<T> implements DeploymentVisitor<T>
{
   private String typeName;
   
   private Class<T> type;
   
   public static final String PROCESSED_ATTACHMENT_NAME = "Processed";
   
   private static Logger logger = Logger.getLogger(MockExtendedDeploymentVisitor.class);
   
   public MockExtendedDeploymentVisitor(String typeName, Class<T> type)
   {
      this.type = type;
      this.typeName = typeName;
   }
   
   public String getVisitorTypeName()
   {
      return this.typeName;
   }

   /**
    * Just a mock implementation which adds a counter as an attachment to the unit
    * to indicate the number of times this visitor has processed the unit
    */
   public void deploy(DeploymentUnit unit, T deployment) throws DeploymentException
   {
      logger.info("Processing unit " + unit + " for deployment " + deployment);
      // if the unit has already been processed by this visitor then increment the count in the
      // attachment
      Integer count = unit.getAttachment(PROCESSED_ATTACHMENT_NAME, Integer.class);
      if (count == null)
      {
         count = 0;
      }
      count ++;
      unit.addAttachment(PROCESSED_ATTACHMENT_NAME, count);
   }

   /**
    * @see DeploymentVisitor#getVisitorType()
    */
   public Class<T> getVisitorType()
   {
      return this.type;
   }

   public void undeploy(DeploymentUnit unit, T deployment)
   {
   }
}
