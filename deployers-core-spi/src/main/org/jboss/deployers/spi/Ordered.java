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
package org.jboss.deployers.spi;

import java.util.Comparator;

/**
 * A base interface for that defines the ordering contract and comparator.<p>
 * 
 * Things with a lower order are processed first
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision:$
 */
public interface Ordered
{
   /**
    * Get the relative order
    * 
    * @return the relative order
    */
   int getRelativeOrder();
   
   /**
    * Set the relative order.
    * 
    * @param order the order
    */
   public void setRelativeOrder(int order);

   /** The comparator for relative ordering of deployers */
   Comparator<Ordered> COMPARATOR = new OrderedComparator();
   
   /**
    * The comparator for relative ordering
    */
   public class OrderedComparator implements Comparator<Ordered>
   {
      public int compare(Ordered o1, Ordered o2)
      {
         int relative = o1.getRelativeOrder() - o2.getRelativeOrder();
         if (relative != 0)
            return relative;
         return o1.toString().compareTo(o2.toString());
      }
   }
}
