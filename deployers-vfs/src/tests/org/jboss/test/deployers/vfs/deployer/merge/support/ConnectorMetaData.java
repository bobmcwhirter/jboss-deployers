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
package org.jboss.test.deployers.vfs.deployer.merge.support;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class ConnectorMetaData implements Serializable
{
   public static final long serialVersionUID = 1l;

   private double version;
   private String attribute;
   private String description;
   private String element;

   public double getVersion()
   {
      return version;
   }

   @XmlAttribute
   public void setVersion(double version)
   {
      this.version = version;
   }

   public String getAttribute()
   {
      return attribute;
   }

   @XmlAttribute(name = "attrib")
   public void setAttribute(String attribute)
   {
      this.attribute = attribute;
   }

   public String getDescription()
   {
      return description;
   }

   @XmlElement
   public void setDescription(String description)
   {
      this.description = description;
   }

   public String getElement()
   {
      return element;
   }

   @XmlElement(name = "elt")
   public void setElement(String element)
   {
      this.element = element;
   }
}