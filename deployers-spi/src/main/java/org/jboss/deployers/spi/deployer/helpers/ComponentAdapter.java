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
package org.jboss.deployers.spi.deployer.helpers;

import org.jboss.deployers.structure.spi.DeploymentUnit;

/**
 * Simple component adapter.
 *
 * @param <T> exact attachment type
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public abstract class ComponentAdapter<T>
{
   /**
    * Get component name.
    *
    * @param attachment the attachment
    * @return the component name
    */
   protected abstract String getComponentName(T attachment);

   /**
    * Get attachment name.
    * By default we return visitor type's name.
    *
    * @param attachment the attachment
    * @return the attachment name
    */
   protected abstract String getAttachmentName(T attachment);

   /**
    * Add component.
    *
    * @param unit the deployment unit
    * @param attachment the attachment
    * @return newly created component deployment unit
    */
   protected DeploymentUnit addComponent(DeploymentUnit unit, T attachment)
   {
      DeploymentUnit component = unit.addComponent(getComponentName(attachment));
      component.addAttachment(getAttachmentName(attachment), attachment);
      return component;
   }

   /**
    * Remove component.
    *
    * @param unit the deployment unit
    * @param attachment the attachment
    */
   protected void removeComponent(DeploymentUnit unit, T attachment)
   {
      unit.removeComponent(getComponentName(attachment));
   }
}