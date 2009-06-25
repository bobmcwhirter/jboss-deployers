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
package org.jboss.deployers.plugins.sort;

import java.util.Set;
import java.util.HashSet;

/**
 * Domino dots as a set of values.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 * @param <V> exact set element type
 */
public class SetDots<V> implements Dots<Set<V>>
{
   private Set<V> set;

   public SetDots(Set<V> set)
   {
      if (set == null)
         throw new IllegalArgumentException("Set is null!");
      this.set = set;
   }

   public Set<V> getValue()
   {
      return set;
   }

   public boolean match(Dots<Set<V>> containable)
   {
      if (set.isEmpty() == false)
      {
         for(V value : containable.getValue())
         {
            if (set.contains(value))
            {
               return true;
            }
         }
      }
      return false;
   }

   public boolean contains(Dots<Set<V>> containable)
   {
      Set<V> otherSet = containable.getValue();
      Set<V> copy = new HashSet<V>(otherSet);
      return copy.retainAll(set) == false; 
   }

   public int dimension()
   {
      return set.size();
   }

   @Override
   public String toString()
   {
      return set.toString();
   }
}
