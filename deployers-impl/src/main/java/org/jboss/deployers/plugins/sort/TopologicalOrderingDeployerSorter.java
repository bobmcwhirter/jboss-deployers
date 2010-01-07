/*
 * JBoss, Home of Professional Open Source
 * Copyright (c) 2010, JBoss Inc., and individual contributors as indicated
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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.jboss.deployers.spi.Ordered;
import org.jboss.deployers.spi.deployer.Deployer;

/**
 * Implements <a href="http://en.wikipedia.org/wiki/Topological_sorting">topological sorting</a> for acyclic graphs.
 * The algorithm complexity is O(m+n), where <b>m</b> = count of vertices and <b>n</b> = count of edges in general.
 * However this complexity isn't true for this algorithm implementation, because there's backward compatibility
 * requirement that deployers have to be ordered by their relative number or name if they're on the same processing level.
 * <b>IOW this backward compatible sorting requirement breaks linear algorithm complexity :(</b>.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public class TopologicalOrderingDeployerSorter implements DeployerSorter
{

   /**
    * @see {@link DeployerSorter#sortDeployers(List, Deployer)}
    */
   public List<Deployer> sortDeployers(List<Deployer> registeredDeployers, Deployer newDeployer)
   {
      return this.createOrientedGraph(registeredDeployers, newDeployer).sort();
   }

   private Graph createOrientedGraph(final List<Deployer> deployers, final Deployer newDeployer)
   {
      final Graph graph = new Graph();

      for (final Deployer deployer : deployers)
         graph.addVertex(deployer);

      graph.addVertex(newDeployer);
      graph.createEdges();

      return graph;
   }


   private static class Graph
   {
      private Map<String, Dependency> dependencies = new HashMap<String, Dependency>();
      private Set<Vertex> vertices = new HashSet<Vertex>();

      public void addVertex(final Deployer deployer)
      {
         // create disjunct sets
         final Set<String> inputs = new HashSet<String>();
         inputs.addAll(deployer.getInputs());
         final Set<String> outputs = new HashSet<String>();
         outputs.addAll(deployer.getOutputs());
         final Set<String> intersection = this.getIntersection(inputs, outputs);

         // register vertex
         final Vertex vertex = new Vertex(deployer);
         this.vertices.add(vertex);

         // register dependencies
         Dependency dependency = null;
         for (final String in : inputs)
         {
            dependency = this.getDependency(in);
            dependency.consumers.add(vertex);
         }

         for (final String inOut : intersection)
         {
            dependency = this.getDependency(inOut);
            dependency.modifiers.add(vertex);
         }

         for (final String out : outputs)
         {
            dependency = this.getDependency(out);
            dependency.producers.add(vertex);
         }
      }

      public List<Deployer> sort()
      {
         // L ← Empty list that will contain the sorted elements
         List<Deployer> retVal = new LinkedList<Deployer>();
         // S ← Set of all nodes with no incoming edges
         List<Vertex> roots = this.getRoots();
         // ensure backward compatibility
         Collections.sort(roots, Ordered.COMPARATOR);

         // while S is non-empty do
         Vertex root = null;
         Set<Vertex> nextLevel = null;
         while(!roots.isEmpty())
         {
            // remove a node n from S
            root = roots.remove(0);
            // insert n into L
            retVal.add(root.getDeployer());

            // for each node m with an edge e from n to m do
            if (root.hasConsumers())
            {
               // ensure backward compatibility
               nextLevel = new TreeSet<Vertex>(Ordered.COMPARATOR);
               for(final Vertex consumer : root.consumers)
               {
                  // remove edge e from the graph
                  consumer.decrementDegree();
                  // if m has no other incoming edges then insert m into S
                  if (!consumer.hasProducers())
                  {
                     this.remove(consumer);
                     nextLevel.add(consumer);
                  }
               }

               // append to the end of list in sorted order
               roots.addAll(nextLevel);
            }
         }

         if (this.vertices.size() > 0)
         {
            // if graph has edges then graph has at least one cycle
            throw new IllegalStateException("Cycle detected");
         }
         else
         {
            // topologically sorted order
            return retVal;
         }
      }

      private Set<String> getIntersection(final Set<String> inputs, final Set<String> outputs)
      {
         final Set<String> intersection = new HashSet<String>();

         for (final String input : inputs)
            for (final String output : outputs)
               if (input.equals(output)) 
                  intersection.add(input);

         inputs.removeAll(intersection);
         outputs.removeAll(intersection);

         return intersection;
      }

      private Dependency getDependency(final String name)
      {
         if (this.dependencies.containsKey(name))
         {
            return this.dependencies.get(name);
         }
         else
         {
            final Dependency newDependency = new Dependency();
            this.dependencies.put(name, newDependency);
            return newDependency;
         }
      }

      private void createEdges()
      {
         boolean hasModifiers = false;
         Dependency dependency = null;

         for (final String dependencyName : this.dependencies.keySet())
         {
            dependency = this.dependencies.get(dependencyName);
            hasModifiers = dependency.modifiers.size() > 0;

            if (hasModifiers)
            {
               this.createEdges(dependency.producers, dependency.modifiers);
               this.createEdges(dependency.modifiers, dependency.consumers);
            }
            else
            {
               this.createEdges(dependency.producers, dependency.consumers);
            }
         }
      }

      private void createEdges(final List<Vertex> producers, final List<Vertex> consumers)
      {
         for (final Vertex producer : producers)
            for (final Vertex consumer : consumers)
            {
               producer.addConsumer(consumer);
               consumer.incrementDegree();
            }
      }

      private List<Vertex> getRoots()
      {
         final LinkedList<Vertex> retVal = new LinkedList<Vertex>();

         for (final Vertex current : this.vertices)
            if (!current.hasProducers())
               retVal.add(current);

         for (final Vertex root : retVal)
            this.remove(root);

         return retVal;
      }

      private void remove(final Vertex v)
      {
         this.vertices.remove(v);
      }

      private static class Vertex implements Ordered
      {
         // Wrapped deployer
         private Deployer deployer;
         // Incoming edges
         private int inDegree = 0;
         // Outgoing edges
         private List<Vertex> consumers = new LinkedList<Vertex>();

         public Vertex(final Deployer deployer)
         {
            if (deployer == null)
               throw new IllegalArgumentException();

            this.deployer = deployer;
         }

         public void incrementDegree()
         {
            this.inDegree++;
         }

         public void decrementDegree()
         {
            this.inDegree--;
         }

         public void addConsumer(final Vertex v)
         {
            this.consumers.add(v);
         }

         public boolean hasProducers()
         {
            return this.inDegree > 0;
         }

         public boolean hasConsumers()
         {
            return this.consumers.size() > 0;
         }

         public Deployer getDeployer()
         {
            return this.deployer;
         }

         public int getRelativeOrder()
         {
            return this.deployer.getRelativeOrder();
         }

         public void setRelativeOrder(final int order)
         {
            throw new UnsupportedOperationException();
         }

         public String toString()
         {
            return this.deployer.toString();
         }
      }

      private static class Dependency
      {
         // deployers creating this dependency
         private List<Vertex> producers = new LinkedList<Vertex>();
         // deployers modifying this dependency
         private List<Vertex> modifiers = new LinkedList<Vertex>();
         // deployers consuming this dependency
         private List<Vertex> consumers= new LinkedList<Vertex>();
      }

   }

}
