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
import java.util.Set;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.jboss.deployers.spi.annotations.PathEntryMetaData;
import org.jboss.deployers.spi.annotations.PathMetaData;

/**
 * AbstractPathMetaData
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
@XmlRootElement(name="path")
@XmlType(name="pathType", propOrder={"includes", "excludes"})
public class AbstractPathMetaData implements PathMetaData, Serializable
{
   private static final long serialVersionUID = 1L;

   private String pathName;
   private Set<PathEntryMetaData> includes;
   private Set<PathEntryMetaData> excludes;

   public String getPathName()
   {
      return pathName;
   }

   @XmlAttribute(name = "name", required = true)
   public void setPathName(String pathName)
   {
      this.pathName = pathName;
   }

   public Set<PathEntryMetaData> getIncludes()
   {
      return includes;
   }

   @XmlElement(name = "include", type = AbstractPathEntryMetaData.class)
   public void setIncludes(Set<PathEntryMetaData> includes)
   {
      this.includes = includes;
   }

   public Set<PathEntryMetaData> getExcludes()
   {
      return excludes;
   }

   @XmlElement(name = "exclude", type = AbstractPathEntryMetaData.class)
   public void setExcludes(Set<PathEntryMetaData> excludes)
   {
      this.excludes = excludes;
   }
}