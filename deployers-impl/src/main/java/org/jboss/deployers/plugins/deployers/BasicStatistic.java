/*
* JBoss, Home of Professional Open Source
* Copyright 2008, JBoss Inc., and individual contributors as indicated
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
package org.jboss.deployers.plugins.deployers;

/**
 * BasicStatistic.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
class BasicStatistic implements Comparable<BasicStatistic>
{
   /** The name */
   private String name;
   
   /** The total time */
   private long time = 0;
   
   /**
    * Create a new BasicStatistic.
    * 
    * @param name the name
    */
   public BasicStatistic(String name)
   {
      this.name = name;
   }
   
   /**
    * Create a new BasicStatistic with the given time.
    * 
    * @param name the name
    * @param time the time
    */
   public BasicStatistic(String name, long time)
   {
      this.name = name;
      this.time = time;
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
    * Get the time.
    * 
    * @return the time.
    */
   public long getTime()
   {
      return time;
   }
   
   /**
    * Add some time
    * 
    * @param time the time to add
    */
   public void addTime(long time)
   {
      this.time += time;
   }

   public int compareTo(BasicStatistic o)
   {
      return (int) (o.getTime() - getTime());
   }
}
