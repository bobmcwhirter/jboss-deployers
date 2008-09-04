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

import org.jboss.managed.api.ManagedOperation.Impact;
import org.jboss.managed.api.annotation.ManagementObject;
import org.jboss.managed.api.annotation.ManagementObjectID;
import org.jboss.managed.api.annotation.ManagementOperation;
import org.jboss.managed.api.annotation.ManagementParameter;
import org.jboss.managed.api.annotation.ManagementProperty;

/**
 * Sample mbean service metadata to test tieing together runtime
 * properties/operations to the root DSMetaData ManagedObject
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
@ManagementObject(isRuntime=true)
public class DSService
{
   private static final long serialVersionUID = 1;

   /** The name of the ManagedObject this runtime view augments */
   private String managementName;
   private String runtimeProp1;
   private int runtimeProp2;

   @ManagementObjectID(type="DataSource")
   public String getManagementName()
   {
      return managementName;
   }
   public void setManagementName(String managementName)
   {
      this.managementName = managementName;
   }

   @ManagementProperty
   public String getRuntimeProp1()
   {
      return runtimeProp1;
   }
   public void setRuntimeProp1(String runtimeProp1)
   {
      this.runtimeProp1 = runtimeProp1;
   }
   @ManagementProperty
   public int getRuntimeProp2()
   {
      return runtimeProp2;
   }
   public void setRuntimeProp2(int runtimeProp2)
   {
      this.runtimeProp2 = runtimeProp2;
   }

   @ManagementOperation(description="Flush the connections in the pool", impact=Impact.WriteOnly)
   public void flushPool()
   {
      
   }
   @ManagementOperation(description="Close the connections in the pool", impact=Impact.WriteOnly)
   public void closePool()
   {
      
   }
   @ManagementOperation(description="Takes a string and returns it", impact=Impact.ReadOnly,
         params={@ManagementParameter(name="input", description="The string to return")})
   public String takesString(String arg1)
   {
      return arg1;
   }
   @ManagementOperation(description="Takes an int and multiples by 10", impact=Impact.ReadOnly,
         params={@ManagementParameter(name="input", description="The int to multiple",
         constraintsFactory=TestManagedParameterConstraintsPopulatorFactory.class)})
   public int constrainedIntx10(int arg1)
   {
      return 10*arg1;
   }
   
}
