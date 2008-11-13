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
package org.jboss.deployers.plugins.annotations;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlAttribute;

import org.jboss.deployers.spi.annotations.PathEntryMetaData;

/**
 * AbstractPathEntryMetaData
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
@XmlType(name="pathEntryType")
public class AbstractPathEntryMetaData implements PathEntryMetaData, Serializable
{
   private static final long serialVersionUID = 1L;

   private String name;

   private String getNameInternal()
   {
      if (name == null)
         throw new IllegalArgumentException("Null name");

      return name;
   }

   public String getName()
   {
      return name;
   }

   @XmlAttribute(name = "name")
   public void setName(String name)
   {
      this.name = name;
   }

   @Override
   public int hashCode()
   {
      return getNameInternal().hashCode();
   }

   @Override
   public boolean equals(Object obj)
   {
      if (obj instanceof PathEntryMetaData == false)
         return false;

      PathEntryMetaData pemd = (PathEntryMetaData)obj;
      return getNameInternal().equals(pemd.getName());
   }
}