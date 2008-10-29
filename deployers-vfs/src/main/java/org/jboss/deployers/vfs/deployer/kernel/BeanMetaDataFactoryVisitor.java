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
package org.jboss.deployers.vfs.deployer.kernel;

import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeploymentVisitor;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.metadata.spi.scope.CommonLevels;

/**
 * BeanMetaDataVisitor.<p>
 *
 * @param <T> exact attachment type
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 */
public abstract class BeanMetaDataFactoryVisitor<T> extends AbstractDeploymentVisitor<BeanMetaData, T>
{
   protected Class<BeanMetaData> getComponentType()
   {
      return BeanMetaData.class;
   }

   protected DeploymentUnit addComponent(DeploymentUnit unit, BeanMetaData attachment)
   {
      DeploymentUnit component = super.addComponent(unit, attachment);
      String className = attachment.getBean();
      if (className != null)
      {
         Object qualifier;
         if (attachment.getClassLoader() == null)
         {
            ClassLoader cl = unit.getClassLoader();
            try
            {
               qualifier = cl.loadClass(className);
            }
            catch (Exception e)
            {
               throw new IllegalArgumentException("Exception loading class for ScopeKey addition.", e);              
            }
         }
         else
         {
            qualifier = className;
         }
         component.getScope().addScope(CommonLevels.CLASS, qualifier);
      }

      return component;
   }

   protected String getComponentName(BeanMetaData attachment)
   {
      return attachment.getName();
   }
}
