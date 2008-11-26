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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

import org.jboss.dependency.spi.ControllerState;

/**
 * DependencyItemMetaData.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@XmlRootElement(name="item")
@XmlType(name="itemType")
public class DependencyItemMetaData implements Serializable
{
   private static final long serialVersionUID = 1;

   private Object value;
   private ControllerState whenRequired = ControllerState.DESCRIBED;
   private ControllerState dependentState;

   /**
    * Get the value.
    *
    * @return the value
    */
   public Object getValue()
   {
      return value;
   }

   /**
    * Set the value.
    *
    * @param value the value
    */
   @XmlValue
   public void setValue(Object value)
   {
      this.value = value;
   }

   /**
    * Get when required state.
    *
    * @return the when required state
    */
   public ControllerState getWhenRequired()
   {
      return whenRequired;
   }

   /**
    * Set when required state.
    *
    * @param whenRequired the when required state
    */
   @XmlAttribute(name = "whenRequired")
   public void setWhenRequired(ControllerState whenRequired)
   {
      this.whenRequired = whenRequired;
   }

   /**
    * Get dependent state.
    *
    * @return the dependent state
    */
   public ControllerState getDependentState()
   {
      return dependentState;
   }

   /**
    * Set dependent state.
    *
    * @param dependentState the dependent state
    */
   @XmlAttribute(name = "dependentState")
   public void setDependentState(ControllerState dependentState)
   {
      this.dependentState = dependentState;
   }
}