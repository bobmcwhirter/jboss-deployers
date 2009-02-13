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
package org.jboss.deployers.spi.deployer.managed;

import java.util.Map;
import java.util.Set;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.managed.api.ManagedObject;

/**
 * ManagedObjectCreator.
 * 
 * TODO JBMICROCONT-181 Critique
 *      Rather having the contexts create the managed objects directly
 *      they should provide the underlying metadata, e.g. the Fields + other information
 *      from which we can create the managed object.
 *      This is important because it is likely that multiple builders/deployers will
 *      play populate an object, e.g. (possibly?)
 *      - the parsing deployer will describe the properties/metadata
 *      - there maybe transformation, augmentation by some other deployers
 *      - the component deployers will want to link their component metadata back into
 *            the parent by reference, e.g. an mbean as part of sar/-service.xml  
 *      - the real deployer will describe any runtime support, e.g. managed operations and statistics
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @author Scott.Stark@jboss.org
 * @version $Revision: 1.1 $
 */
public interface ManagedObjectCreator
{
   /**
    * Build managed objects for this deployment context
    * 
    * @param unit the deployment unit
    * @param attachmentNames the attachment names
    * @param managedObjects the managed objects
    * @throws DeploymentException
    */
   void build(DeploymentUnit unit, Set<String> attachmentNames,
         Map<String, ManagedObject> managedObjects) throws DeploymentException;
}
