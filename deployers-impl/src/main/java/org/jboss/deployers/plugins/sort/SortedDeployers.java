package org.jboss.deployers.plugins.sort;

import org.jboss.deployers.spi.deployer.Deployer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
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
   ArrayList<Entry> entries = new ArrayList<Entry>();
   ArrayList<Deployer> deployers = new ArrayList<Deployer>();

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
         deployers.clear();
         for (Entry entry : entries)
         {
            deployers.add(entry.deployer);
         }
         return;
      }

      insertAfterInputs(n);
      IdentityHashMap visited = new IdentityHashMap();
      traverseOutputs(n, visited);
      relativeOrdering();

      deployers = new ArrayList<Deployer>();
      for (Entry entry : entries)
      {
         deployers.add(entry.deployer);
      }
   }

   public void removeDeployer(Deployer d)
   {
      Entry removed = null;
      for (int i = 0; i < entries.size(); i++)
      {
         if (entries.get(0).deployer == d)
         {
            removed = entries.get(0);
            removeAt(i);
            break;
         }
      }
      if (d.getInputs() != null)
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
      if (d.getOutputs() != null)
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
      deployers.clear();
      for (Entry entry : entries)
      {
         deployers.add(entry.deployer);
      }
   }

   public List<Deployer> getDeployers()
   {
      return deployers;
   }

   private void traverseOutputs(Entry n, IdentityHashMap visited)
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
      if (n.getInputs() == null) return;
      for (String input : n.getInputs())
      {
         List<Entry> outputs = outputMap.get(input);
         if (outputs == null) continue;
         for (Entry deployer : outputs)
         {
            if (deployer == n) continue;
            if (deployer.getIndex() >= n.getIndex()) n.setIndex(deployer.getIndex() + 1);
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
      for (int i = 0; i < entries.size() - 1; i++)
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
      if (d1.getOutputs() == null) return true;
      for (String output : d1.getOutputs())
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
