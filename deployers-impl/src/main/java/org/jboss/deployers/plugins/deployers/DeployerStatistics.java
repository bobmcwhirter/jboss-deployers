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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DeployerStatistics.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
class DeployerStatistics
{
   /** The deployer statistics */
   private Map<String, DeployerStatistic> deployers = new ConcurrentHashMap<String, DeployerStatistic>();
   
   /**
    * Add a statistic
    * 
    * @param deployerName the deployer name
    * @param unitName the unit name
    * @param time the time
    */
   public synchronized void addStatistic(String deployerName, String unitName, long time)
   {
      DeployerStatistic stat = deployers.get(deployerName);
      if (stat == null)
      {
         stat = new DeployerStatistic(deployerName);
         deployers.put(deployerName, stat);
      }
      stat.addDetail(unitName, time);
   }
   
   /**
    * List the times
    * 
    * @param details whether to show details
    * @return the times
    */
   public String listTimes(boolean details)
   {
      List<DeployerStatistic> stats = new ArrayList<DeployerStatistic>(deployers.values());
      Collections.sort(stats);
      
      StringBuilder builder = new StringBuilder();
      builder.append("<table><tr><th>Deployer/Deployment</th><th>Time (milliseconds)</th></tr>");
      for (DeployerStatistic stat : stats)
      {
         builder.append("<tr>");
         builder.append("<td>").append(stat.getName()).append("</td>");
         builder.append("<td>").append(stat.getTime()).append("</td>");
         builder.append("</tr>");
         if (details)
         {
            List<BasicStatistic> list = new ArrayList<BasicStatistic>(stat.getDetails().values());
            Collections.sort(list);
            for (BasicStatistic detail : list)
            {
               builder.append("<tr>");
               builder.append("<td>`-- ").append(detail.getName()).append("</td>");
               builder.append("<td>").append(detail.getTime()).append("</td>");
               builder.append("</tr>");
            }
         }
      }
      builder.append("</table>");
      return builder.toString();

   }
}
