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
package org.jboss.test.deployers.deployer.support;

import java.io.Serializable;
import java.util.List;

import org.jboss.managed.api.annotation.ManagementObject;

/**
 * The mbean service metadata
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
@ManagementObject
public class TestServiceMetaData implements Serializable
{
   private static final long serialVersionUID = 1;

   /** The ObjectName */
   private String objectName;
   
   /** The code */
   private String code;

   /**The attributes */
   private List<TestServiceAttributeMetaData> attributes;

   public String getCode()
   {
      return code;
   }

   public void setCode(String code)
   {
      this.code = code;
   }

   public String getObjectName()
   {
      return objectName;
   }

   public void setObjectName(String objectName)
   {
      this.objectName = objectName;
   }
   
   public List<TestServiceAttributeMetaData> getAttributes()
   {
      return attributes;
   }

   /**
    * Set the attributes.
    * 
    * @param attributes the attributes.
    */
   public void setAttributes(List<TestServiceAttributeMetaData> attributes)
   {
      if (attributes == null)
         throw new IllegalArgumentException("Null attributes");
      this.attributes = attributes;
   }

}
