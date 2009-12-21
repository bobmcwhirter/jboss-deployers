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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.deployers.spi.Ordered;
import org.jboss.deployers.spi.deployer.Deployer;

/**
 * @author <a href="cdewolf@redhat.com">Carlo de Wolf</a>
 */
public class KahnDeployerSorter implements DeployerSorter
{
   private static class Edge
   {
      Deployer from;
      String input;
      Deployer to;

      Edge(Deployer from, String input, Deployer to)
      {
         if(from.equals(to))
            throw new IllegalArgumentException("cyclic edge");
         this.from = from;
         this.input = input;
         this.to = to;
      }

      @Override
      public boolean equals(Object o)
      {
         if(this == o) return true;
         if(o == null || getClass() != o.getClass()) return false;

         Edge edge = (Edge) o;

         if(from != null ? !from.equals(edge.from) : edge.from != null) return false;
         if(!input.equals(edge.input)) return false;
         if(!to.equals(edge.to)) return false;

         return true;
      }

      @Override
      public int hashCode()
      {
         int result = from != null ? from.hashCode() : 0;
         result = 31 * result + input.hashCode();
         result = 31 * result + to.hashCode();
         return result;
      }

      @Override
      public String toString()
      {
         return "Edge{" +
            "from=" + from +
            ", input='" + input + '\'' +
            ", to=" + to +
            '}';
      }
   }

   private static class ScoredDeployer
   {
      Deployer deployer;
      int score;

      ScoredDeployer(Deployer deployer)
      {
         this.deployer = deployer;
         this.score = deployer.getRelativeOrder();
      }

      @Override
      public String toString()
      {
         return "ScoredDeployer{" +
            "deployer=" + deployer +
            ", score=" + score +
            '}';
      }
   }

   protected int compare(Deployer one, Deployer two)
   {
      int relation = one.getRelativeOrder() - two.getRelativeOrder();
      if(relation == 0)
         relation = one.hashCode() - two.hashCode();
      assert relation != 0;
      return relation;
   }

   protected Collection<Edge> createEdges(Deployer from, Map<String, Collection<Deployer>> inputCache, Set<String> outputs)
   {
      Collection<Edge> result = new ArrayList<Edge>();
      for(String output : outputs)
      {
         Collection<Deployer> deployers = inputCache.get(output);
         if(deployers != null) for(Deployer to : deployers)
         {
            if(from != to)
               result.add(new Edge(from, output, to));
         }
      }
      return result;
   }

   protected Collection<Edge> createEdges(Map<String, Collection<Deployer>> outputCache, Set<String> inputs, Deployer to)
   {
      Collection<Edge> result = new ArrayList<Edge>();
      for(String input : inputs)
      {
         Collection<Deployer> deployers = outputCache.get(input);
         if(deployers != null) for(Deployer from : deployers)
         {
            result.add(new Edge(from, input, to));
         }
      }
      return result;
   }

   protected Collection<Edge> findInputs(Map<Deployer, Set<Edge>> edgeCache, Deployer from, Map<String, Collection<Deployer>> cache, Set<String> inputs)
   {
      Collection<Edge> result = new ArrayList<Edge>();
      for(String input : inputs)
      {
         //result.addAll(cache.get(input));
         Collection<Deployer> deployers = cache.get(input);
         if(deployers != null) for(Deployer d : deployers)
         {
            if(d != from)
            {
               //result.add(new Edge(from, input, d));
               Edge potential = new Edge(from, input, d);
               Set<Edge> edges = edgeCache.get(d);
               if(edges.contains(potential))
                  result.add(potential);
            }
         }
      }
      return result;
   }

   protected boolean isInputMaster(Collection<Deployer> deployers, Deployer deployer, String input)
   {
      for(Deployer other : deployers)
      {
         if(isTransient(other, input))
         {
            if(compare(other, deployer) > 0)
               return false;
         }
      }
      return true;
   }

   protected boolean isTransient(Deployer deployer, String input)
   {
      return deployer.getInputs().contains(input) && deployer.getOutputs().contains(input);
   }

   protected void process(Deployer deployer, Collection<Deployer> s, Map<String, Collection<Deployer>> inputCache, Map<Deployer, Set<Edge>> edgeCache, Set<String> outputs, Map<String, Collection<Deployer>> outputCache)
   {
      outputs.addAll(deployer.getOutputs());
      if(deployer.getInputs() == null || deployer.getInputs().size() == 0)
         s.add(deployer);
      else
      {
         Set<Edge> edges = edgeCache.get(deployer);
         assert edges == null;
         edges = new HashSet<Edge>();
         edgeCache.put(deployer, edges);

         for(String input : deployer.getInputs())
         {
            Collection<Deployer> c = inputCache.get(input);
            if(c == null)
            {
               c = new ArrayList<Deployer>();
               inputCache.put(input, c);
            }
            c.add(deployer);
         }

         Collection<Edge> c = createEdges(outputCache, deployer.getInputs(), deployer);
         if(c.isEmpty())
         {
            // might be a bit premature, see below
            s.add(deployer);
         }
         else
         {
            edges.addAll(c);
         }
      }

      for(String output : deployer.getOutputs())
      {
         Collection<Deployer> c = outputCache.get(output);
         if(c == null)
         {
            c = new ArrayList<Deployer>();
            outputCache.put(output, c);
         }
         c.add(deployer);
      }

      Collection<Edge> edges = createEdges(deployer, inputCache, deployer.getOutputs());
      for(Edge e : edges)
      {
         Set<Edge> cachedEdges = edgeCache.get(e.to);
         if(cachedEdges == null)
         {
            cachedEdges = new HashSet<Edge>();
            edgeCache.put(e.to, cachedEdges);
         }
         cachedEdges.add(e);
         // remove a prematurely added deployer
         s.remove(e.to);
      }
   }

   /**
    * Break the cyclic graph by processing transient deployers. Basically choosing one edge which we want removed.
    */
   public void processTransientDeployers(List<Deployer> s, Map<String, Collection<Deployer>> inputCache, Map<String, Collection<Deployer>> outputCache, Map<Deployer, Set<Edge>> edgeCache)
   {
      for(String input : inputCache.keySet())
      {
         Collection<Deployer> others = outputCache.get(input);
         if(others == null)
            continue;
         List<Deployer> deployers = new ArrayList<Deployer>(inputCache.get(input));
         deployers.retainAll(others);
         if(deployers.isEmpty() || deployers.size() == 1)
            continue;

         List<ScoredDeployer> scoredDeployers = new ArrayList<ScoredDeployer>();
         // TODO: too slow
         for(Deployer d : deployers)
         {
            ScoredDeployer scoredDeployer = new ScoredDeployer(d);
            scoredDeployers.add(scoredDeployer);
            
            Set<Edge> edges = edgeCache.get(d);
            for(Edge e : new HashSet<Edge>(edges))
            {
               if(deployers.contains(e.from) && input.equals(e.input))
                  edges.remove(e);
               else if(deployers.contains(e.from))
                  scoredDeployer.score++;
            }
         }

         Comparator<? super ScoredDeployer> comparator = new Comparator<ScoredDeployer>() {
            public int compare(ScoredDeployer o1, ScoredDeployer o2)
            {
               int relation = o1.score - o2.score;
               if(relation == 0)
                  relation = Ordered.COMPARATOR.compare(o1.deployer, o2.deployer);
               return relation;
            }
         };
         Collections.sort(scoredDeployers, comparator);

         for(int i = 1; i < scoredDeployers.size(); i++)
         {
            Set<Edge> edges = edgeCache.get(scoredDeployers.get(i).deployer);
            edges.add(new Edge(scoredDeployers.get(0).deployer, input, scoredDeployers.get(i).deployer));
         }

         // add the top one if it doesn't have any incoming edges
         // note that the normal process step does the same thing, so exclude it.
         Deployer deployer = scoredDeployers.get(0).deployer;
         if(edgeCache.get(deployer).isEmpty() && !s.contains(deployer))
            s.add(deployer);
      }
   }

   public List<Deployer> sortDeployers(List<Deployer> original, Deployer newDeployer)
   {
      List<Deployer> result = new ArrayList<Deployer>();
      // S ← Set of all nodes with no incoming edges
      List<Deployer> s = new ArrayList<Deployer>();
      Map<String, Collection<Deployer>> inputCache = new HashMap<String, Collection<Deployer>>();
      Map<Deployer, Set<Edge>> edgeCache = new IdentityHashMap<Deployer, Set<Edge>>();
      Set<String> outputs = new HashSet<String>();
      Map<String, Collection<Deployer>> outputCache = new HashMap<String, Collection<Deployer>>();
      for(Deployer deployer : original)
      {
         process(deployer, s, inputCache, edgeCache, outputs, outputCache);
      }
      process(newDeployer, s, inputCache, edgeCache, outputs, outputCache);

      // find transient deployers and sort them out
      processTransientDeployers(s, inputCache, outputCache, edgeCache);

      // never, ever ask why this is here.
      // I really mean it, do not ask.
      // Seriously I will not give a sensible answer to this one.
      // Okay, okay, stop bitching. There is a requirement that deployer need to be in name ordering.
      // setupSillyNameEdges(deployer, newDeployer); // hmm does work out
      Collections.sort(s, Ordered.COMPARATOR);

      while(!s.isEmpty())
      {
         Deployer deployer = s.remove(0);
         result.add(deployer);
         // for each node m with an edge e from n to m do
         for(Edge e : findInputs(edgeCache, deployer, inputCache, deployer.getOutputs()))
         {
            // remove edge e from the graph
            Set<Edge> edges = edgeCache.get(e.to);
            edges.remove(e);
            // if m has no other incoming edges then insert m into S
            if(edges.isEmpty())
               s.add(e.to);
         }
      }
      // if graph has edges then output error message (graph has at least one cycle)
      String message = "";
      for(Set<Edge> edges : edgeCache.values())
      {
         if(!edges.isEmpty())
            message += "edges: " + edges;
      }
      if(message.length() > 0)
         throw new IllegalStateException(message);

      assert result.size() == original.size() + 1 : "not all deployers made it";
      return result;
   }
}
