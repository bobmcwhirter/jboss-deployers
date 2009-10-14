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
public enum AttachmentLocatorEnum implements AttachmentLocatorType
{
   LOCAL(new LocalAttachmentLocatorType()),
   PARENT(new ParentAttachmentLocatorType()),
   TOP(new TopAttachmentLocatorType()),
   HIERARCHY(new HierarchyAttachmentLocatorType());

   private AttachmentLocatorType type;

   AttachmentLocatorEnum(AttachmentLocatorType type)
   {
      this.type = type;
   }

   public <T> T search(DeploymentUnit unit, Class<T> expectedType)
   {
      return search(unit, AbstractAttachmentLocatorType.checkExpectedType(expectedType), expectedType);
   }

   public <T> T search(DeploymentUnit unit, String name, Class<T> expectedType)
   {
      return type.search(unit, name, expectedType);
   }

   public static class LocalAttachmentLocatorType extends AbstractAttachmentLocatorType
   {
      public <T> T search(DeploymentUnit unit, String name, Class<T> expectedType)
      {
         return unit.getAttachment(name, expectedType);
      }
   }

   public static class ParentAttachmentLocatorType extends AbstractAttachmentLocatorType
   {
      public <T> T search(DeploymentUnit unit, String name, Class<T> expectedType)
      {
         DeploymentUnit parent = unit.getParent();
         return (parent != null) ? parent.getAttachment(name, expectedType) : null;
      }
   }

   public static class TopAttachmentLocatorType extends AbstractAttachmentLocatorType
   {
      public <T> T search(DeploymentUnit unit, String name, Class<T> expectedType)
      {
         return unit.getTopLevel().getAttachment(name, expectedType);
      }
   }

   public static class HierarchyAttachmentLocatorType extends AbstractAttachmentLocatorType
   {
      public <T> T search(DeploymentUnit unit, String name, Class<T> expectedType)
      {
         return AttachmentLocator.searchAncestors(unit, name, expectedType);
      }
   }
}