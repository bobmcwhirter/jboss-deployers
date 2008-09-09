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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DeployerStatistic.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
class DeployerStatistic extends BasicStatistic
{
   /** The details */
   private Map<String, BasicStatistic> details = new ConcurrentHashMap<String, BasicStatistic>();
   
   /**
    * Create a new DeployerStatistic.
    * 
    * @param name the name
    */
   public DeployerStatistic(String name)
   {
      super(name);
   }

   /**
    * Get the details.
    * 
    * @return the details.
    */
   public Map<String, BasicStatistic> getDetails()
   {
      return details;
   }
   
   /**
    * Add a detail
    * 
    * @param name the name
    * @param time the time
    */
   public synchronized void addDetail(String name, long time)
   {
      BasicStatistic previous = details.get(name);
      if (previous != null)
         previous.addTime(time);
      else
         details.put(name, new BasicStatistic(name, time));
      addTime(time);
   }
}
