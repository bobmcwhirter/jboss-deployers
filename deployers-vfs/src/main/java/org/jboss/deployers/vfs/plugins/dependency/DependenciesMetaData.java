/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.deployers.vfs.plugins.dependency;

import java.io.Serializable;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.jboss.xb.annotations.JBossXmlSchema;

/**
 * DependenciesMetaData.
 * 
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@JBossXmlSchema(namespace="urn:jboss:dependency:1.0", elementFormDefault=XmlNsForm.QUALIFIED)
@XmlRootElement(name="dependency")
@XmlType(name="dependencyType", propOrder={"items"})
public class DependenciesMetaData implements Serializable
{
   private static final long serialVersionUID = 1;
   /** The dependency items metadata */
   private List<DependencyItemMetaData> items;

   /**
    * Get dependency metadata items.
    *
    * @return the dependency metadata items
    */
   public List<DependencyItemMetaData> getItems()
   {
      return items;
   }

   /**
    * Set dependency metadata items.
    *
    * @param items the dependency metadata items
    */
   @XmlElement(name = "item", type = DependencyItemMetaData.class)
   public void setItems(List<DependencyItemMetaData> items)
   {
      this.items = items;
   }
}
