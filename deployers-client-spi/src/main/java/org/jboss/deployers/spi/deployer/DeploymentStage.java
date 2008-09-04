/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.deployers.spi.deployer;

import java.io.Serializable;

/**
 * DeploymentStage.
 * 
 * @author <a href="adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class DeploymentStage implements Serializable
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 3302613286025012191L;

   /** Our stage */
   private String name;
   
   /** The previous state */
   private String after;
   
   /** The next state */
   private String before;

   /**
    * Safely get the name of stage
    * 
    * @param stage the stage
    * @param context the context for an error
    * @return the stage name
    */
   private static String getStageName(DeploymentStage stage, String context)
   {
      if (stage == null)
         throw new IllegalArgumentException("Null " + context);
      return stage.getName();
   }
   
   /**
    * Create a new DeploymentStage.
    * 
    * @param name the name of the stage
    * @throws IllegalArgumentException for a null name
    */
   public DeploymentStage(String name)
   {
      this(name, (String) null);
   }

   /**
    * Create a new DeploymentStage.
    * 
    * @param name the name of the stage
    * @param after the name of the stage before our stage
    * @throws IllegalArgumentException for a null name
    */
   public DeploymentStage(String name, String after)
   {
      this(name, after, null);
   }

   /**
    * Create a new DeploymentStage.
    * 
    * @param name the name of the stage
    * @param after the stage before our stage
    * @throws IllegalArgumentException for a null parameter
    */
   public DeploymentStage(String name, DeploymentStage after)
   {
      this(name, getStageName(after, "after"), null);
   }

   /**
    * Create a new DeploymentStage.
    * 
    * @param name the name of the stage
    * @param after he stage before our stage
    * @param before the stage after our stage
    * @throws IllegalArgumentException for a null parameter
    */
   public DeploymentStage(String name, DeploymentStage after, DeploymentStage before)
   {
      this(name, getStageName(after, "after"), getStageName(before, "before"));
   }

   /**
    * Create a new DeploymentStage.
    * 
    * @param name the name of the stage
    * @param after the name of the stage before our stage
    * @param before the name of the stage after our stage
    * @throws IllegalArgumentException for a null name
    */
   public DeploymentStage(String name, String after, String before)
   {
      if (name == null)
         throw new IllegalArgumentException("Null name");
      this.name = name;
      this.after = after;
      this.before = before;
   }

   /**
    * Get the name.
    * 
    * @return the name.
    */
   public String getName()
   {
      return name;
   }

   /**
    * Get the after.
    * 
    * @return the after.
    */
   public String getAfter()
   {
      return after;
   }

   /**
    * Get the before.
    * 
    * @return the before.
    */
   public String getBefore()
   {
      return before;
   }
   
   @Override
   public boolean equals(Object obj)
   {
      if (obj == this)
         return true;
      if (obj == null || obj instanceof DeploymentStage == false)
         return false;
      
      DeploymentStage other = (DeploymentStage) obj;
      return getName().equals(other.getName());
   }

   @Override
   public int hashCode()
   {
      return getName().hashCode();
   }
   
   @Override
   public String toString()
   {
      return getName();
   }
}
