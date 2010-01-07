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
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

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
public class InOutTopologicalDeployerSorter implements DeployerSorter
{
   @SuppressWarnings({"unchecked"})
   public List<Deployer> sortDeployers(List<Deployer> original, Deployer newDeployer)
   {
      Graph<Integer> graph = new Graph<Integer>();
      Map<String, Set<Deployer>> output2deployer = new HashMap<String, Set<Deployer>>();
      List<Deployer> splitList = new SplitList<Deployer>(original, newDeployer);
      Set<Deployer> notUsed = new TreeSet<Deployer>(Ordered.COMPARATOR);
      for (Deployer deployer : splitList)
      {
         boolean used = false;

         Set<String> inputs = deployer.getInputs();
         Set<Vertex<Integer>> ivd = fillVertices(inputs, graph);
         Set<String> outputs = deployer.getOutputs();
         Set<Vertex<Integer>> ovd = fillVertices(outputs, graph);
         ivd.retainAll(ovd); // intersection
         for (String output : outputs)
         {
            Set<Deployer> deployers = output2deployer.get(output);
            if (deployers == null)
            {
               deployers = new TreeSet<Deployer>(Ordered.COMPARATOR);
               output2deployer.put(output, deployers);
            }
            deployers.add(deployer);
            used = true;

            for (String input : inputs)
            {
               Vertex<Integer> from = graph.findVertexByName(input);
               Vertex<Integer> to = graph.findVertexByName(output);
               // ignore pass-through
               if (from != to && ivd.contains(from) == false)
                  graph.addEdge(from, to, 0);
            }
         }

         if (used == false)
            notUsed.add(deployer);
      }
      Stack<Vertex<Integer>> noIncoming = new Stack<Vertex<Integer>>();
      for (Vertex<Integer> vertex : graph.getVerticies())
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

      Set<Deployer> sortedDeployers = new LinkedHashSet<Deployer>();
      for (Vertex<Integer> v : sorted)
      {
         Set<Deployer> deployers = output2deployer.get(v.getName());
         if (deployers != null)
         {
            Deployer first = deployers.iterator().next();
            Iterator<Deployer> notUsedIter = notUsed.iterator();
            while(notUsedIter.hasNext())
            {
               Deployer next = notUsedIter.next();
               if (next.getInputs().isEmpty() && Ordered.COMPARATOR.compare(next, first) < 0)
               {
                  sortedDeployers.add(next);
                  notUsedIter.remove();
               }
            }
            for (Deployer deployer : deployers)
            {
               if (sortedDeployers.contains(deployer) == false)
                  sortedDeployers.add(deployer);               
            }
         }
      }
      sortedDeployers.addAll(notUsed); // add the one's with no output
      return new ArrayList<Deployer>(sortedDeployers);
   }

   private static Set<Vertex<Integer>> fillVertices(Set<String> keys, Graph<Integer> graph)
   {
      Map<Vertex<Integer>, Object> dv = new IdentityHashMap<Vertex<Integer>, Object>();
      for (String key : keys)
         dv.put(getVertex(key, graph), 0);
      return dv.keySet();
   }

   private static Vertex<Integer> getVertex(String key, Graph<Integer> graph)
   {
      Vertex<Integer> vertex = graph.findVertexByName(key);
      if (vertex == null)
      {
         vertex = new Vertex<Integer>(key);
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
         this.head = head;
         this.tail = Collections.singletonList(tail);
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
}