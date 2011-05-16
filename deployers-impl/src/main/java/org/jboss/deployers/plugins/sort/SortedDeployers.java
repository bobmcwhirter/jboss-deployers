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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.deployers.spi.deployer.Deployer;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @author <a href="ales.justin@jboss.org">Ales Justin</a>
 * @version $Revision: 1 $
 */
public class SortedDeployers
{
   private static class Entry
   {
      public Deployer deployer;
      public int index;

      private String nameCache;

      private Entry(Deployer deployer)
      {
         this.deployer = deployer;
      }

      public Set<String> getInputs()
      {
         if (deployer.getInputs() == null) return Collections.emptySet();
         return deployer.getInputs();
      }

      public Set<String> getOutputs()
      {
         if (deployer.getOutputs() == null) return Collections.emptySet();
         return deployer.getOutputs();
      }

      public int getRelativeOrder()
      {
         return deployer.getRelativeOrder();
      }

      public int getIndex()
      {
         return index;
      }

      public void setIndex(int index)
      {
         this.index = index;
      }

      public String toString()
      {
         // this speeds up things a few milliseconds :)
         if (nameCache == null) nameCache = deployer.toString();
         return nameCache;
      }
   }

   Map<String, List<Entry>> outputMap = new HashMap<String, List<Entry>>();
   Map<String, List<Entry>> inputMap = new HashMap<String, List<Entry>>();
   List<Entry> entries = new ArrayList<Entry>();
   volatile List<Deployer> deployers = new ArrayList<Deployer>();

   public void addOutputs(Entry deployer)
   {
      if (deployer.getOutputs() == null) return;
      for (String output : deployer.getOutputs())
      {
         List<Entry> list = outputMap.get(output);
         if (list == null)
         {
            list = new ArrayList<Entry>();
            outputMap.put(output, list);
         }
         list.add(deployer);
      }
   }

   public void addInputs(Entry deployer)
   {
      if (deployer.getInputs() == null) return;
      for (String input : deployer.getInputs())
      {
         List<Entry> list = inputMap.get(input);
         if (list == null)
         {
            list = new ArrayList<Entry>();
            inputMap.put(input, list);
         }
         list.add(deployer);
      }
   }

   public void sort(Deployer d)
   {
      Entry n = new Entry(d);
      addOutputs(n);
      addInputs(n);

      if (entries.size() == 0)
      {
         insertAt(n);
         List<Deployer> copy = new ArrayList<Deployer>();
         for (Entry entry : entries)
         {
            copy.add(entry.deployer);
         }
         deployers = copy;
         return;
      }

      insertAfterInputs(n);
      IdentityHashMap<Entry, Entry> visited = new IdentityHashMap<Entry, Entry>();
      traverseOutputs(n, visited);
      relativeOrdering();

      // For some reason, something depends on a new list within MC/VDF
      // be careful if you change this
      List<Deployer> copy = new ArrayList<Deployer>();
      for (Entry entry : entries)
      {
         copy.add(entry.deployer);
      }
      deployers = copy;
   }

   public void removeDeployer(Deployer d)
   {
      Entry removed = null;
      int esize = entries.size();
      for (int i = 0; i < esize; i++)
      {
         Entry entry = entries.get(i);
         if (entry.deployer.equals(d))
         {
            removed = entry;
            removeAt(i);
            break;
         }
      }
      if (removed != null && d.getInputs() != null)
      {
         for (String input : d.getInputs())
         {
            List<Entry> list = inputMap.get(input);
            if (list != null)
            {
               list.remove(removed);
            }
         }
      }
      if (removed != null && d.getOutputs() != null)
      {
         for (String output : d.getOutputs())
         {
            List<Entry> list = outputMap.get(output);
            if (list != null)
            {
               list.remove(removed);
            }
         }
      }
      List<Deployer> copy = new ArrayList<Deployer>();
      for (Entry entry : entries)
      {
         copy.add(entry.deployer);
      }
      deployers = copy;
   }

   public List<Deployer> getDeployers()
   {
      return Collections.unmodifiableList(deployers);
   }

   private void traverseOutputs(Entry n, IdentityHashMap<Entry, Entry> visited)
   {
      if (n.getOutputs() == null) return;
      if (visited.containsKey(n))
      {
         throw new IllegalStateException("Deployer " + n + " is involved in a cyclic dependency.");
      }
      visited.put(n, n);
      for (String output : n.getOutputs())
      {
         List<Entry> inputs = inputMap.get(output);
         if (inputs == null) continue;
         for (Entry deployer : inputs)
         {
            if (deployer.getIndex() < n.getIndex())
            {
               // if both the new deployer and comparing deployer have the same output as input
               // don't change the index of the new deployer.  We always want to insert the deployer at the lowest
               // possible index so that it is guaranteed that a lower index is always "not equal" to it.
               if (n.getInputs().contains(output) && deployer.getOutputs().contains(output))
               {

               }
               else
               {
                  removeAt(deployer.getIndex());
                  deployer.setIndex(0);
                  insertAfterInputs(deployer);
                  traverseOutputs(deployer, visited);
               }
            }
         }

      }
   }

   private void insertAfterInputs(Entry n)
   {
      Set<String> nInputs = n.getInputs();
      Set<String> nOutputs = n.getOutputs();
      if (nInputs == null) return;
      for (String input : nInputs)
      {
         List<Entry> outputs = outputMap.get(input);
         if (outputs == null) continue;
         for (Entry deployer : outputs)
         {
            if (deployer == n) continue;
            if (deployer.getIndex() >= n.getIndex())
            {
               // if both the new deployer and comparing deployer have the same output as input
               // don't change the index of the new deployer.  We always want to insert the deployer at the lowest
               // possible index so that it is guaranteed that a lower index is always "not equal" to it.
               if (nOutputs.contains(input) && deployer.getInputs().contains(input))
               {

               }
               else
               {
                  n.setIndex(deployer.getIndex() + 1);
               }
            }
         }
      }
      insertAt(n);
   }

   void insertAt(Entry n)
   {
      entries.add(n.getIndex(), n);
      int esize = entries.size();
      for (int i = n.getIndex() + 1; i < esize; i++)
      {
         entries.get(i).setIndex(i);
      }
   }

   void removeAt(int index)
   {
      entries.remove(index);
      int esize = entries.size();
      for (int i = index; i < esize; i++)
      {
         entries.get(i).setIndex(i);
      }
   }

   public void relativeOrdering()
   {
      // this algorithm may seem buggy, but I don't think it is.
      // WE can do a simple for loop because deployers are add one at a time
      // since they are added one at a time, the current entry list is already sorted
      // also, we ensure that deployers are inserted at the lowest possible index.  This means
      // that they cannot be "equal to" a lower index than themselves and makes this
      // single for-loop optimization rather than a real sort possible.
      // Time improvement could be saved  if name ordering was removed.
      int esize = entries.size();
      for (int i = 0; i < esize - 1; i++)
      {
         Entry d1 = entries.get(i);
         Entry d2 = entries.get(i + 1);

         // optimization.  If relative order is the same, we don't have to do a swap
         if (d1.getRelativeOrder() == d2.getRelativeOrder())
         {
            String name1 = d1.toString();
            String name2 = d2.toString();
            if (name1.compareTo(name2) < 0) continue;

            if (isIOEqual(d1, d2))
            {
               swap(i, d1, d2);
            }

            continue;
         }
         boolean isEqual = isIOEqual(d1, d2);
         if (isEqual)
         {
            if (d2.getRelativeOrder() < d1.getRelativeOrder())
            {
               swap(i, d1, d2);
            }
         }
      }
   }

   private void swap(int i, Entry d1, Entry d2)
   {
      entries.set(i + 1, d1);
      d1.setIndex(i + 1);
      entries.set(i, d2);
      d2.setIndex(i);
   }

   private boolean isIOEqual(Entry d1, Entry d2)
   {
      boolean isEqual = true;
      Set<String> d1Outputs = d1.getOutputs();
      if (d1Outputs == null) return true;
      for (String output : d1Outputs)
      {
         List<Entry> inputs = inputMap.get(output);
         if (inputs == null) continue;
         if (inputs.contains(d2))
         {
            if (d1.getInputs().contains(output) && d2.getOutputs().contains(output))
            {
               continue;
            }
            isEqual = false;
            break;
         }
      }
      return isEqual;
   }
}
