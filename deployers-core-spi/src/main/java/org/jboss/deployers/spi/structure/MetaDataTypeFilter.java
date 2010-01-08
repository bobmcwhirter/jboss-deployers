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
package org.jboss.deployers.spi.structure;

/**
 * A metadata type filter
 *
 * @author <a href="ales.justin@jboss.org">Ales Justin</a>
 */
public interface MetaDataTypeFilter
{
   /**
    * Do we accept the type.
    *
    * @param type the current type to check
    * @return true if we accept the type, false otherwise
    */
   boolean accepts(MetaDataType type);

   public static final MetaDataTypeFilter DEFAULT = new MetaDataTypeFilter()
   {
      public boolean accepts(MetaDataType type)
      {
         return MetaDataType.DEFAULT == type;
      }
   };

   public static final MetaDataTypeFilter ALL = new MetaDataTypeFilter()
   {
      public boolean accepts(MetaDataType type)
      {
         return true;
      }
   };
}