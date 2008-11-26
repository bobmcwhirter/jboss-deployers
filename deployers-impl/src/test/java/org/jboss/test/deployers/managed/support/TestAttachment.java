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
package org.jboss.test.deployers.managed.support;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.jboss.metatype.api.values.SimpleValueSupport;
import org.jboss.metatype.api.values.MetaValue;
import org.jboss.metatype.api.values.MetaValueFactory;

/**
 * TestAttachment.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class TestAttachment implements Cloneable, Serializable
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;
   
   private Map<String, Object> fields = new HashMap<String, Object>();

   public Object getProperty(String name)
   {
      return fields.get(name);
   }

   public void setProperty(String name, Object value)
   {
      MetaValue mv;
      if (value instanceof MetaValue)
         mv = (MetaValue)value;
      else if (value instanceof Serializable)
         mv = SimpleValueSupport.wrap((Serializable)value);
      else
         mv = MetaValueFactory.getInstance().create(value);

      fields.put(name, mv);
   }
   
   public TestAttachment clone()
   {
      try
      {
         TestAttachment clone = (TestAttachment) super.clone();
         clone.fields = new HashMap<String, Object>(clone.fields);
         return clone;
      }
      catch (CloneNotSupportedException e)
      {
         throw new Error("Unexpected", e);
      }
   }
}
