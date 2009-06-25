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
 * Comparable deployment stage.
 * 
 * @author <a href="adrian@jboss.org">Adrian Brock</a>
 * @author <a href="ropalka@redhat.com">Richard Opalka</a>
 * @version $Revision: 1.1 $
 */
public final class DeploymentStage implements Comparable< DeploymentStage >, Serializable
{

   // TODO: Do I have to provide read/writeObject() methods as well?
   /** The serialVersionUID */
   private static final long serialVersionUID = 3302613286025012192L;

   /** Our stage */
   private final String name;
   
   /** The previous stage */
   private DeploymentStage after;
   
   /** The next stage */
   private DeploymentStage before;
   
   /** Stage index. */
   private int index;
   
   /** Computed hashCode(). */
   private int hashCode;
   
   /** Computed toString(). */
   private String toString;
   
   /** Initialization flag. */
   private boolean initialized;

   /**
    * Create a new DeploymentStage.
    * 
    * @param name the name of the stage
    */
   public DeploymentStage( final String name )
   {
      if ( name == null )
      {
         throw new NullPointerException( "Null name" );
      }
      
      this.name = name;
   }
   
   /**
    * Initializes this deployment stage and makes it immutable.
    * 
    * @param after the deployment stage preceding this one
    * @param before the deployment stage following this one
    * @param index deployment stage index
    */
   public synchronized void initialize( final DeploymentStage after, final DeploymentStage before, final int index )
   {
      if ( false == this.initialized )
      {
         this.after = after;
         this.before = before;
         this.index = index;
         this.hashCode = computeHashCode();
         this.toString = computeToString();
         this.initialized = true;
      }
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
    * Get the after stage.
    * 
    * @return the after stage.
    */
   public DeploymentStage getAfter()
   {
      return after;
   }

   /**
    * Get the before stage.
    * 
    * @return the before stage.
    */
   public DeploymentStage getBefore()
   {
      return before;
   }

   /**
    * See {@link java.lang.Object#equals(Object)}
    */
   @Override
   public boolean equals( final Object obj )
   {
      if ( obj == this )
      {
         return true;
      }
      if ( obj == null || false == ( obj instanceof DeploymentStage ) )
      {
         return false;
      }
      
      final DeploymentStage other = ( DeploymentStage ) obj;

      if ( false == this.getName().equals( other.getName() ) )
      {
         return false;
      }
      if ( false == ( this.index == other.index ) )
      {
         return false;
      }
      if ( false == this.getAfter().equals( other.getAfter() ) )
      {
         return false;
      }
      if ( false == this.getBefore().equals( other.getBefore() ) )
      {
         return false;
      }
      
      return true;
   }

   /**
    * See {@link java.lang.Object#hashCode()}
    */
   @Override
   public int hashCode()
   {
      return this.hashCode;
   }
   
   /**
    * See {@link java.lang.Object#toString()}
    */
   @Override
   public String toString()
   {
      return this.toString;
   }
   
   /**
    * See {@link java.lang.Comparable#compareTo(Object)}
    */
   public int compareTo( final DeploymentStage other )
   {
      return this.index - other.index;
   }


   /**
    * Computes hashCode - performance optimization.
    * 
    * @return computed hashCode value
    */
   private int computeHashCode()
   {
      int result = 17;
      
      result = 37 * result + this.name.hashCode();
      result = 37 * result + this.index;
      result = 37 * result + ( this.after == null ? 0 : this.after.hashCode() );
      result = 37 * result + ( this.before == null ? 0 : this.before.hashCode() );
      
      return result;
   }

   /**
    * Computes toString - performance optimization.
    * 
    * @return computed toString value
    */
   private String computeToString()
   {
      final StringBuilder sb = new StringBuilder();

      sb.append( this.getName() ).append( "[after=" );
      sb.append( ( this.after != null ) ? this.after.getName() : null );
      sb.append( ",before=" );
      sb.append( ( this.before != null ) ? this.before.getName() : null );
      sb.append( "]" );
      
      return sb.toString();
   }

}
