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

import java.util.List;

import org.jboss.deployers.structure.spi.DeploymentUnit;

/**
 * Search a DeploymentUnit structure from child to parent for a matching
 * attachment.
 * 
 * @author Scott.Stark@jboss.org
 * @author adrian@jboss.org
 * @author ales.justin@jboss.org
 * @version $Revision:$
 */
public class AttachmentLocator
{
   /**
    * Get a named attachment
    * 
    * @param unit the deployment unit
    * @param name the name of the attachment
    * @return the attachment or null if not present
    * @throws IllegalArgumentException for a null name
    */
   @Deprecated
   public static Object search(DeploymentUnit unit, String name)
   {
      return searchAncestors(unit, name);
   }

   /**
    * Get named attachment of a given type
    * 
    * @param <T> the expected type
    * @param unit the deployment unit
    * @param name the name of the attachment
    * @param expectedType the expected type
    * @return the attachment or null if not present
    * @throws IllegalArgumentException for a null name or expectedType
    */
   @Deprecated
   public static <T> T search(DeploymentUnit unit, String name, Class<T> expectedType)
   {
      return searchAncestors(unit, name, expectedType);
   }

   /**
    * Get an attachment of the given type
    * 
    * @param <T> the expected type
    * @param type the type
    * @param unit the deployment unit
    * @return the attachment or null if not present
    * @throws IllegalArgumentException for a null name or type
    */
   @Deprecated
   public static <T> T search(DeploymentUnit unit, Class<T> type)
   {
      if (type == null)
         throw new IllegalArgumentException("Null expected type.");

      return searchAncestors(unit, type);
   }

   /**
    * Get a named attachment, search ancestors
    *
    * @param unit the deployment unit
    * @param name the name of the attachment
    * @return the attachment or null if not present
    * @throws IllegalArgumentException for a null name
    */
   public static Object searchAncestors(DeploymentUnit unit, String name)
   {
      Object attachment = null;
      while (attachment == null && unit != null)
      {
         attachment = unit.getAttachment(name);
         unit = unit.getParent();
      }
      return attachment;
   }

   /**
    * Get named attachment of a given type, search ancestors
    *
    * @param <T> the expected type
    * @param unit the deployment unit
    * @param name the name of the attachment
    * @param expectedType the expected type
    * @return the attachment or null if not present
    * @throws IllegalArgumentException for a null name or expectedType
    */
   public static <T> T searchAncestors(DeploymentUnit unit, String name, Class<T> expectedType)
   {
      Object result = searchAncestors(unit, name);
      if (result == null)
         return null;

      if (expectedType == null)
         throw new IllegalArgumentException("Null expected type.");

      return expectedType.cast(result);
   }

   /**
    * Get an attachment of the given type, search ancestors
    *
    * @param <T> the expected type
    * @param type the type
    * @param unit the deployment unit
    * @return the attachment or null if not present
    * @throws IllegalArgumentException for a null name or type
    */
   public static <T> T searchAncestors(DeploymentUnit unit, Class<T> type)
   {
      if (type == null)
         throw new IllegalArgumentException("Null expected type.");

      return searchAncestors(unit, type.getName(), type);
   }

   /**
    * Get a named attachment, search in children
    *
    * @param unit the deployment unit
    * @param name the name of the attachment
    * @return the attachment or null if not present
    * @throws IllegalArgumentException for a null name
    */
   public static Object searchChildren(DeploymentUnit unit, String name)
   {
      Object attachment = unit.getAttachment(name);
      if (attachment != null)
         return attachment;

      List<DeploymentUnit> components = unit.getComponents();
      if (components != null && components.isEmpty() == false)
      {
         for (DeploymentUnit component : components)
         {
            Object result = searchChildren(component, name);
            if (result != null)
               return result;
         }
      }

      List<DeploymentUnit> children = unit.getChildren();
      if (children != null && children.isEmpty() == false)
      {
         for (DeploymentUnit child : children)
         {
            Object result = searchChildren(child, name);
            if (result != null)
               return result;
         }
      }

      return null;
   }

   /**
    * Get named attachment of a given type, search in children
    *
    * @param <T> the expected type
    * @param unit the deployment unit
    * @param name the name of the attachment
    * @param expectedType the expected type
    * @return the attachment or null if not present
    * @throws IllegalArgumentException for a null name or expectedType
    */
   public static <T> T searchChildren(DeploymentUnit unit, String name, Class<T> expectedType)
   {
      Object result = searchChildren(unit, name);
      if (result == null)
         return null;

      if (expectedType == null)
         throw new IllegalArgumentException("Null expected type.");

      return expectedType.cast(result);
   }

   /**
    * Get an attachment of the given type, search in children
    *
    * @param <T> the expected type
    * @param type the type
    * @param unit the deployment unit
    * @return the attachment or null if not present
    * @throws IllegalArgumentException for a null name or type
    */
   public static <T> T searchChildren(DeploymentUnit unit, Class<T> type)
   {
      if (type == null)
         throw new IllegalArgumentException("Null expected type.");

      return searchChildren(unit, type.getName(), type);
   }
}
