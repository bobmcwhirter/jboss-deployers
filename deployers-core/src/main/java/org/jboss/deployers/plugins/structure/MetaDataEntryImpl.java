/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.deployers.plugins.structure;

import java.io.Serializable;

import org.jboss.deployers.spi.structure.MetaDataEntry;
import org.jboss.deployers.spi.structure.MetaDataType;

/**
 * MetaDataEntryImpl
 *
 * @author <a href="ales.justin@jboss.org">Ales Justin</a>
 */
public class MetaDataEntryImpl implements MetaDataEntry, Serializable
{
   private static final long serialVersionUID = 1l;
   private String path;
   private MetaDataType type;

   public MetaDataEntryImpl(String path)
   {
      this(path, MetaDataType.DEFAULT);
   }

   public MetaDataEntryImpl(String path, MetaDataType type)
   {
      if (path == null)
         throw new IllegalArgumentException("Null path");
      if (type == null)
         throw new IllegalArgumentException("Null type");
      this.path = path;
      this.type = type;
   }

   public String getPath()
   {
      return path;
   }

   public MetaDataType getType()
   {
      return type;
   }

   @Override
   public int hashCode()
   {
      return path.hashCode();
   }

   @Override
   public boolean equals(Object obj)
   {
      if (obj instanceof MetaDataEntry == false)
         return false;

      MetaDataEntry other = (MetaDataEntry)obj;
      if (path.equals(other.getPath()) == false)
         return false;

      return type == other.getType(); 
   }

   @Override
   public String toString()
   {
      return path + " - " + type;
   }
}