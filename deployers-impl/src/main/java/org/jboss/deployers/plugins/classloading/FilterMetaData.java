/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Inc., and individual contributors
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
package org.jboss.deployers.plugins.classloading;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

import java.io.Serializable;

/**
 * Filter meta data.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@XmlRootElement(name="lazy-start-filter")
@XmlType(name="lazyStartFilterType")
public class FilterMetaData implements Serializable
{
   private static final long serialVersionUID = 1l;

   private boolean recurse;
   private String value;

   public boolean isRecurse()
   {
      return recurse;
   }

   @XmlAttribute
   public void setRecurse(boolean recurse)
   {
      this.recurse = recurse;
   }

   public String getValue()
   {
      if (value == null)
         throw new IllegalArgumentException("Null value");

      return value;
   }

   @XmlValue
   public void setValue(String value)
   {
      this.value = value;
   }

   @Override
   public int hashCode()
   {

      return getValue().hashCode();
   }

   @Override
   public boolean equals(Object obj)
   {
      if (obj == null)
         return false;

      if (obj == this)
         return true;

      if (obj instanceof FilterMetaData == false)
         return false;

      FilterMetaData other = (FilterMetaData) obj;
      return recurse == other.isRecurse() && getValue().equals(other.getValue());
   }
}