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
package org.jboss.deployers.spi.deployer.helpers;

import org.jboss.deployers.structure.spi.DeploymentUnit;

/**
 * Search a DeploymentUnit structure based on type rules.
 *
 * @author ales.justin@jboss.org
 */
public abstract class AbstractAttachmentLocatorType implements AttachmentLocatorType
{
   /**
    * Check expected class is nto null.
    *
    * @param expectedClass the expected class
    * @return expected class as a string
    */
   static String checkExpectedType(Class<?> expectedClass)
   {
      if (expectedClass == null)
         throw new IllegalArgumentException("Null expected type.");

      return expectedClass.getName();
   }

   public <T> T search(DeploymentUnit unit, Class<T> expectedType)
   {
      return search(unit, checkExpectedType(expectedType), expectedType);
   }
}