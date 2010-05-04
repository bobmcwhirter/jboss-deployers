/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.deployers.plugins.metadata;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.jboss.beans.metadata.plugins.AbstractValueMetaData;
import org.jboss.beans.metadata.spi.MetaDataVisitor;
import org.jboss.dependency.spi.Controller;
import org.jboss.dependency.spi.ControllerContext;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.reflect.spi.TypeInfo;
import org.jboss.xb.annotations.JBossXmlSchema;

/**
 * Deployment value meta data.
 *
 * It allows injection on deployment info into beans.
 * e.g. <property name="appName"><from-deployment type="simple_name" bean="Tx"/></property>
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@JBossXmlSchema(namespace="urn:jboss:bean-deployer:deployment:2.0", elementFormDefault=XmlNsForm.QUALIFIED)
@XmlRootElement(name="from-deployment")
@XmlType(name="fromDeploymentType", propOrder={})
public class FromDeploymentValueMetaData extends AbstractValueMetaData
{
   private static final long serialVersionUID = 1l;

   private transient ControllerContext context;
   private FromDeployment fromDeployment;
   private String bean;
   private ControllerState state;

   public Object getValue(TypeInfo info, ClassLoader cl) throws Throwable
   {
      ControllerContext lookup = context;
      if (bean != null)
      {
         Controller controller = context.getController();
         lookup = controller.getContext(bean, state, false);
         if (lookup == null)
            throw new IllegalArgumentException("No such bean '" + bean + "' in state " + state);
      }
      return fromDeployment.executeLookup(lookup);
   }

   public void initialVisit(MetaDataVisitor vistor)
   {
      context = vistor.getControllerContext();
      vistor.initialVisit(this);
   }

   @XmlAttribute(name = "type", required = true)
   public void setFromDeployment(String type)
   {
      fromDeployment = FromDeployment.getInstance(type);
   }

   @XmlAttribute
   public void setBean(String bean)
   {
      this.bean = bean;
   }

   @XmlAttribute
   public void setState(ControllerState state)
   {
      this.state = state;
   }
}
