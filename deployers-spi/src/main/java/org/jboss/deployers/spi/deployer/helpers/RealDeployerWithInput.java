/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.deployers.spi.deployer.helpers;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.logging.Logger;

/**
 * 
 * RealDeployerWithInput - A deployer which by default is set to process
 * deployments during the {@link DeploymentStages#REAL} phase. This deployer
 * is similar to {@link AbstractRealDeployerWithInput} except that, this one 
 * is more efficient when used with a {@link ExtendedDeploymentVisitor}.
 * <br>
 * This deployer uses the {@link ExtendedDeploymentVisitor} to filter out deployers
 * for processing the units. It internally relies on {@link ExtendedDeploymentVisitor#getVisitorType()}
 * and {@link ExtendedDeploymentVisitor#getVisitorTypeName()} to decide whether the deployer
 * is eligible for processing the unit. See {@link #deploy(DeploymentUnit, ExtendedDeploymentVisitor)}
 * and {@link #undeploy(DeploymentUnit, ExtendedDeploymentVisitor)} for more details 
 * about this deployer.
 * 
 * Note that the deployment stage for this deployer can be changed by invoking 
 * {@link #setStage(org.jboss.deployers.spi.deployer.DeploymentStage)} during the construction of the 
 * deployer
 * 
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class RealDeployerWithInput<T> extends AbstractRealDeployer
{

   /**
    * Logger
    */
   private static Logger logger = Logger.getLogger(RealDeployerWithInput.class);

   /**
    * Visitor which will be used for processing the deployment unit
    */
   private ExtendedDeploymentVisitor<T> visitor;

   /**
    * Constructor
    * @param visitor
    */
   public RealDeployerWithInput(ExtendedDeploymentVisitor<T> visitor)
   {
      this.visitor = visitor;
      // set the input for this deployer
      setInput(visitor.getVisitorType());
   }
   
   /**
    * Returns the {@link ExtendedDeploymentVisitor} associated with this deployer
    * @return
    */
   public ExtendedDeploymentVisitor<T> getDeploymentVisitor()
   {
      return this.visitor;
   }

   @Override
   protected void internalDeploy(DeploymentUnit unit) throws DeploymentException
   {
      deploy(unit, this.visitor);

   }

   /**
    * First checks whether the <code>visitor</code> is eligible for
    * processing the <code>unit</code>. The {@link ExtendedDeploymentVisitor#getVisitorTypeName()}
    * and {@link ExtendedDeploymentVisitor#getVisitorType()} APIs are used to decide whether
    * the unit is eligible to be processed by the visitor. If the unit contains an attachment with 
    * the visitorTypeName and of type visitorType, then the visitor is considered
    * valid for this deployment unit. Eligible visitor are then allowed to process
    * the unit by calling the {@link ExtendedDeploymentVisitor#deploy(DeploymentUnit, Object)} API
    * 
    * 
    * @param unit The deployment unit being processed
    * @param visitor The visitor which will be checked for eligibility for processing the unit
    *                  based on the {@link ExtendedDeploymentVisitor#getVisitorTypeName()} and
    *                  {@link ExtendedDeploymentVisitor#getVisitorType()} values
    *                  
    * @throws DeploymentException If any exception occurs during deployment
    * @throws NullPointerException If either of <code>unit</code> or <code>visitor</code> is null
    */
   protected void deploy(DeploymentUnit unit, ExtendedDeploymentVisitor<T> visitor) throws DeploymentException
   {
      // get hold of the attachment based on the visitorTypeName and visitorType
      String attachmentName = visitor.getVisitorTypeName();
      Class<T> attachmentType = visitor.getVisitorType();

      T attachment = unit.getAttachment(attachmentName, attachmentType);

      // no such attachment, so let's skip this visitor for this unit
      if (attachment == null)
      {
         if (logger.isTraceEnabled())
         {
            logger.trace("Skipping " + visitor + " during deploy, since no attachment named " + attachmentName
                  + " of type " + attachmentType + " found in unit " + unit);
         }
         return;
      }

      // attachment found, let the visitor process this unit
      visitor.deploy(unit, attachment);

   }

   /**
    * 
    * First checks whether the <code>visitor</code> is eligible for
    * processing the <code>unit</code>. The {@link ExtendedDeploymentVisitor#getVisitorTypeName()}
    * and {@link ExtendedDeploymentVisitor#getVisitorType()} APIs are used to decide whether
    * the unit is eligible to be processed by the visitor. If the unit contains an attachment with 
    * the visitorTypeName and of type visitorType, then the visitor is considered
    * valid for this deployment unit. Eligible visitor are then allowed to process
    * the unit by calling the {@link ExtendedDeploymentVisitor#deploy(DeploymentUnit, Object)} API

    * @param unit The deployment unit to be processed
    * @param visitor The visitor which will be checked for eligibility for processing the unit
    *                  based on the {@link ExtendedDeploymentVisitor#getVisitorTypeName()} and
    *                  {@link ExtendedDeploymentVisitor#getVisitorType()} values
    *                  
    * @throws DeploymentException If any exception occurs during deployment
    * @throws NullPointerException If either of <code>unit</code> or <code>visitor</code> is null
    */
   protected void undeploy(DeploymentUnit unit, ExtendedDeploymentVisitor<T> visitor) throws DeploymentException
   {
      // get hold of the attachment based on the visitorTypeName and visitorType
      String attachmentName = visitor.getVisitorTypeName();
      Class<T> attachmentType = visitor.getVisitorType();

      T attachment = unit.getAttachment(attachmentName, attachmentType);

      // no such attachment, so let's skip this visitor for this unit
      if (attachment == null)
      {
         if (logger.isTraceEnabled())
         {
            logger.trace("Skipping " + visitor + " during undeploy, since no attachment named " + attachmentName
                  + " of type " + attachmentType + " found in unit " + unit);
         }
         return;
      }

      // attachment found, let the visitor process this unit
      visitor.undeploy(unit, attachment);

   }

}
