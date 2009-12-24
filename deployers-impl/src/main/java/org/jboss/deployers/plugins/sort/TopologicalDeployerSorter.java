/*
 * JBoss, Home of Professional Open Source
 * Copyright (c) 2009, JBoss Inc., and individual contributors as indicated
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

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.jboss.deployers.spi.Ordered;
import org.jboss.deployers.spi.deployer.Deployer;
import org.jboss.util.graph.Edge;
import org.jboss.util.graph.Graph;
import org.jboss.util.graph.Vertex;

/**
 * Simple topological sorting: http://en.wikipedia.org/wiki/Topological_sorting.
 * 
 * Each input or output is a task, dependency between tasks is determined by deployer.
 * e.g. Deployer D has input X and output Y, hence we have 2 tasks and the dependency between them,
 * meaning that task X needs to be finished before task Y.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class TopologicalDeployerSorter implements DeployerSorter
{
   @SuppressWarnings({"unchecked"})
   public List<Deployer> sortDeployers(List<Deployer> original, Deployer newDeployer)
   {
      Graph<Integer> graph = new Graph<Integer>();
      Map<String, Vertex<Integer>> vertices = new HashMap<String, Vertex<Integer>>();
      List<Deployer> splitList = new SplitList<Deployer>(original, newDeployer);
      List<DeployerNode> nodes = new ArrayList<DeployerNode>();
      for (Deployer deployer : splitList)
      {
         Set<String> inputs = deployer.getInputs();
         Set<Vertex<Integer>> ivd = fillVertices(inputs, vertices, graph);
         Set<String> outputs = deployer.getOutputs();
         Set<Vertex<Integer>> ovd = fillVertices(outputs, vertices, graph);
         nodes.add(new DeployerNode(deployer, ivd, ovd));
         for (String input : inputs)
         {
            for (String output : outputs)
            {
               Vertex<Integer> from = vertices.get(input);
               Vertex<Integer> to = vertices.get(output);
               // ignore pass-through
               if (from != to && ivd.contains(to) == false && ovd.contains(from) == false)
                  graph.addEdge(from, to, 0);
            }
         }
      }
      Stack<Vertex<Integer>> noIncoming = new Stack<Vertex<Integer>>();
      for (Vertex<Integer> vertex : vertices.values())
      {
         if (vertex.getIncomingEdgeCount() == 0)
            noIncoming.push(vertex);
      }
      List<Vertex<Integer>> sorted = new ArrayList<Vertex<Integer>>();
      while(noIncoming.isEmpty() == false)
      {
         Vertex<Integer> n = noIncoming.pop();
         sorted.add(n);
         n.setData(sorted.size());
         List<Edge<Integer>> edges = new ArrayList<Edge<Integer>>(n.getOutgoingEdges());
         for (Edge<Integer> edge : edges)
         {
            Vertex<Integer> m = edge.getTo();
            graph.removeEdge(n, m);
            if (m.getIncomingEdgeCount() == 0)
               noIncoming.push(m);
         }
      }
      if (graph.getEdges().isEmpty() == false)
         throw new IllegalStateException("We have a cycle: " + newDeployer + ", previous: " + original);

      // FIXME - transitive compare doesn't work here -- find a better way to map deployers onto ordered inputs/outputs
      Collections.sort(nodes, DeployerNodeComparator.INSTANCE);
      List<Deployer> sortedDeployers = new ArrayList<Deployer>();
      for (DeployerNode node : nodes)
         sortedDeployers.add(node.deployer);
      return sortedDeployers;
   }

   private static Set<Vertex<Integer>> fillVertices(Set<String> keys, Map<String, Vertex<Integer>> vertices, Graph<Integer> graph)
   {
      Map<Vertex<Integer>, Object> dv = new IdentityHashMap<Vertex<Integer>, Object>();
      for (String key : keys)
         dv.put(getVertex(key, vertices, graph), 0);
      return dv.keySet();
   }

   private static Vertex<Integer> getVertex(String key, Map<String, Vertex<Integer>> vertices, Graph<Integer> graph)
   {
      Vertex<Integer> vertex = vertices.get(key);
      if (vertex == null)
      {
         vertex = new Vertex<Integer>(key);
         vertices.put(key, vertex);
         graph.addVertex(vertex);
      }
      return vertex;
   }

   private class SplitList<T> extends AbstractList<T>
   {
      private List<T> head;
      private List<T> tail;

      private SplitList(List<T> head, T tail)
      {
         this(head, Collections.singletonList(tail));
      }

      private SplitList(List<T> head, List<T> tail)
      {
         this.head = head;
         this.tail = tail;
      }

      @Override
      public T get(int index)
      {
         int headSize = head.size();
         if (index < headSize)
            return head.get(index);
         else
            return tail.get(index - headSize);
      }

      @Override
      public int size()
      {
         return head.size() + tail.size();
      }
   }

   private class DeployerNode implements Ordered
   {
      private Deployer deployer;
      private Set<Vertex<Integer>> inputs;
      private Set<Vertex<Integer>> outputs;
      private int minIn = -1;
      private int maxIn = -1;
      private int minOut = -1;
      private int maxOut = -1;

      private DeployerNode(Deployer deployer, Set<Vertex<Integer>> inputs, Set<Vertex<Integer>> outputs)
      {
         this.deployer = deployer;
         this.inputs = inputs;
         if (inputs.isEmpty())
         {
            minIn = 0;
            maxIn = 0;
         }
         this.outputs = outputs;
         if (outputs.isEmpty())
         {
            minOut = 0;
            maxOut = 0;
         }
      }

      public int getRelativeOrder()
      {
         return deployer.getRelativeOrder();
      }

      public void setRelativeOrder(int order)
      {
      }

      @Override
      public String toString()
      {
         return deployer.toString();
      }

      public int sizeIn()
      {
         return inputs.size();
      }

      public int sizeOut()
      {
         return outputs.size();
      }

      public int getMinIn()
      {
         if (minIn == -1)
            minIn = Collections.min(inputs, VertextComparator.INSTANCE).getData();

         return minIn;
      }

      public int getMaxIn()
      {
         if (maxIn == -1)
            maxIn = Collections.max(inputs, VertextComparator.INSTANCE).getData();

         return maxIn;
      }

      public int getMinOut()
      {
         if (minOut == -1)
            minOut = Collections.min(outputs, VertextComparator.INSTANCE).getData();

         return minOut;
      }

      public int getMaxOut()
      {
         if (maxOut == -1)
            maxOut = Collections.max(outputs, VertextComparator.INSTANCE).getData();

         return maxOut;
      }
   }

   private static int overlap(DeployerNode dn1, DeployerNode dn2)
   {
      int maxOut_1 = dn1.getMaxOut();
      int minIn_2 = dn2.getMinIn();

      // not comparable
      if (maxOut_1 == 0 || minIn_2 == 0)
         return -1;

      if (maxOut_1 < minIn_2)
         return 0; // bigger

      int minOut_1 = dn1.getMinOut();
      int maxIn_2 = dn2.getMaxIn();

      // inclusion
      if (minOut_1 <= minIn_2 && maxOut_1 >= maxIn_2)
         return maxIn_2 - minIn_2 + 1;
      if (minIn_2 <= minOut_1 && maxOut_1 <= maxIn_2)
         return maxOut_1 - minOut_1 + 1;

      // overlap
      if (minOut_1 <= minIn_2 && minIn_2 <= maxOut_1 && maxOut_1 <= maxIn_2)
         return maxOut_1 - minIn_2 + 1;
      if (minOut_1 >= minIn_2 && minIn_2 >= maxOut_1 && maxOut_1 >= maxIn_2)
         return maxIn_2 - minOut_1 + 1;

      return -1;
   }

   private static class DeployerNodeComparator implements Comparator<DeployerNode>
   {
      static final DeployerNodeComparator INSTANCE = new DeployerNodeComparator();

      public int compare(DeployerNode dn1, DeployerNode dn2)
      {
         int overlap12 = overlap(dn1, dn2);
         int overlap21 = overlap(dn2, dn1);

         if (overlap12 > 0 && overlap21 > 0)
         {
            if (overlap12 != overlap21)
               return overlap21 - overlap12;

            Set<Vertex<Integer>> tail1 = new HashSet<Vertex<Integer>>(dn1.outputs);
            tail1.retainAll(dn2.inputs);
            Set<Vertex<Integer>> tail2 = new HashSet<Vertex<Integer>>(dn2.outputs);
            tail2.retainAll(dn1.inputs);
            int s1 = tail1.size();
            int s2 = tail2.size();
            if (s1 != s2)
            {
               return s2 - s1;
            }
            else
            {
               overlap12 = overlap21 = -1; // reset               
            }
         }

         if (overlap12 >= 0)
            return -1;

         if (overlap21 >= 0)
            return 1;

         return Ordered.COMPARATOR.compare(dn1, dn2);
      }
   }

   private static class VertextComparator implements Comparator<Vertex<Integer>>
   {
      static final VertextComparator INSTANCE = new VertextComparator();

      public int compare(Vertex<Integer> v1, Vertex<Integer> v2)
      {
         return v1.getData() - v2.getData();
      }
   }
}