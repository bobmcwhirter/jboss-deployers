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

/**
 * A deployer which by default is set to process
 * deployments during the {@link DeploymentStages#REAL} phase. This deployer
 * is similar to {@link AbstractRealDeployerWithInput} except that, this one 
 * is more efficient when used with a {@link DeploymentVisitor}.
 * <br>
 * This deployer uses the {@link DeploymentVisitor} to filter out deployers
 * for processing the units. It internally relies on {@link DeploymentVisitor#getVisitorType()}
 * and exact attachment name to decide whether the deployer
 * is eligible for processing the unit. See {@link #deploy(DeploymentUnit)}
 * and {@link #undeploy(DeploymentUnit)} for more details about this deployer.
 * 
 * Note that the deployment stage for this deployer can be changed by invoking 
 * {@link #setStage(org.jboss.deployers.spi.deployer.DeploymentStage)} during the construction of the 
 * deployer
 * 
 * @param <T> the attachement type
 * @author Jaikiran Pai
 * @author Ales Justin
 * @version $Revision: $
 */
public class ExactAttachmentDeployerWithVisitor<T> extends AbstractRealDeployer
{
   /** The attachment name */
   private String attachmentName;

   /**
    * Visitor which will be used for processing the deployment unit
    */
   private DeploymentVisitor<T> visitor;

   /**
    * Constructor.
    *
    * @param attachmentName the attachment name
    * @param visitor the visitor
    */
   public ExactAttachmentDeployerWithVisitor(String attachmentName, DeploymentVisitor<T> visitor)
   {
      if (attachmentName == null)
         throw new IllegalArgumentException("Null attachment name");
      if (visitor == null)
         throw new IllegalArgumentException("Null visitor");

      this.attachmentName = attachmentName;
      this.visitor = visitor;
      // set the input for this deployer
      setInput(visitor.getVisitorType());
   }

   /**
    * First checks whether the <code>visitor</code> is eligible for
    * processing the <code>unit</code>. The attachmentName
    * and {@link DeploymentVisitor#getVisitorType()} APIs are used to decide whether
    * the unit is eligible to be processed by the visitor. If the unit contains an attachment with
    * the attachmentName and of type visitorType, then the visitor is considered
    * valid for this deployment unit. Eligible visitor are then allowed to process
    * the unit by calling the {@link DeploymentVisitor#deploy(DeploymentUnit, Object)} API
    *
    *
    * @param unit The deployment unit being processed
    * @throws DeploymentException If any exception occurs during deployment
    */
   @Override
   protected void internalDeploy(DeploymentUnit unit) throws DeploymentException
   {
      Class<T> attachmentType = visitor.getVisitorType();
      T attachment = unit.getAttachment(attachmentName, attachmentType);

      // no such attachment, so let's skip this visitor for this unit
      if (attachment == null)
      {
         if (log.isTraceEnabled())
         {
            log.trace("Skipping " + visitor + " during deploy, since no attachment named " + attachmentName
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
    * processing the <code>unit</code>. The attchmentName
    * and {@link DeploymentVisitor#getVisitorType()} APIs are used to decide whether
    * the unit is eligible to be processed by the visitor. If the unit contains an attachment with 
    * the attachmentName and of type visitorType, then the visitor is considered
    * valid for this deployment unit. Eligible visitor are then allowed to process
    * the unit by calling the {@link DeploymentVisitor#deploy(DeploymentUnit, Object)} API

    * @param unit The deployment unit to be processed
    */
   protected void internalUndeploy(DeploymentUnit unit)
   {
      Class<T> attachmentType = visitor.getVisitorType();
      T attachment = unit.getAttachment(attachmentName, attachmentType);

      // no such attachment, so let's skip this visitor for this unit
      if (attachment == null)
      {
         if (log.isTraceEnabled())
         {
            log.trace("Skipping " + visitor + " during undeploy, since no attachment named " + attachmentName
                  + " of type " + attachmentType + " found in unit " + unit);
         }
         return;
      }

      // attachment found, let the visitor process this unit
      visitor.undeploy(unit, attachment);
   }
}
