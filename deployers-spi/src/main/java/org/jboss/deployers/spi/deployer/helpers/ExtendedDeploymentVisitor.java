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

package org.jboss.deployers.spi.deployer.helpers;

import org.jboss.deployers.structure.spi.DeploymentUnit;

/**
 * ExtendedDeploymentVisitor
 *
 * Extended version of the {@link DeploymentVisitor} which allows to filter
 * out eligible unit based on the name of the attachments. The {@link #getVisitorTypeName()}
 * and {@link #getVisitorType()} decide which units are eligible for being 
 * processed by this visitor.
 * 
 * @author Jaikiran Pai
 * @version $Revision: $
 * @param <T>
 */
public interface ExtendedDeploymentVisitor<T> extends DeploymentVisitor<T>
{

   /**
    * Returns the name of attachment in the deployment unit, which this
    * visitor is interested in processing.<br>
    *  
    * This along with the {@link #getVisitorType()} will be used to check
    * whether an {@link DeploymentUnit} is eligible to be processed by this {@link DeploymentVisitor}.
    * If the unit contains a attachment with {@link #getVisitorTypeName()} of type {@link #getVisitorType()}
    * then that unit is considered eligible to be passed to this {@link DeploymentVisitor}. 
    * 
    * @return 
    */
   String getVisitorTypeName();

}
