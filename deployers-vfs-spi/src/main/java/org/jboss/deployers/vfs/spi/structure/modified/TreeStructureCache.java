/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.deployers.vfs.spi.structure.modified;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import org.jboss.virtual.plugins.vfs.helpers.PathTokenizer;

/**
 * Tree base structure cache.
 *
 * @param <T> exact value type
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class TreeStructureCache<T> implements StructureCache<T>
{
   /** The tree root */
   private Node<T> root = createRoot();

   /**
    * Create new root.
    *
    * @return the new root
    */
   protected Node<T> createRoot()
   {
      return new Node<T>("", getDefaultValue(), null);
   }

   /**
    * Get default node value.
    *
    * @return the default node value
    */
   protected T getDefaultValue()
   {
      return null;
   }

   public void initializeCache(String pathName)
   {
      initializeNode(pathName);
   }

   public T putCacheValue(String pathName, T value)
   {
      Node<T> node = getNode(pathName);
      if (node == null)
      {
         initializeNode(pathName);
         node = getNode(pathName);
      }

      T previous = node.getValue();
      node.setValue(value);
      return previous;
   }

   public T getCacheValue(String pathName)
   {
      Node<T> node = getNode(pathName);
      return (node != null ? node.getValue() : null);
   }

   public Set<String> getLeaves(String pathName)
   {
      Node<T> node = getNode(pathName);
      return (node != null) ? node.getChildrenNames() : null;
   }

   public void invalidateCache(String pathName)
   {
      removeCache(pathName);
   }

   public void removeCache(String pathName)
   {
      Node<T> node = getNode(pathName);
      if (node != null)
      {
         Node<T> parent = node.getParent();
         if (parent != null)
            parent.removeChild(node);
         else // clear root
            node.clear();
      }
   }

   public synchronized void flush()
   {
      root = createRoot();
   }

   /**
    * Get the path's node.
    *
    * @param path the path
    * @return node or null if it doesn't exist
    */
   protected Node<T> getNode(String path)
   {
      List<String> tokens = PathTokenizer.getTokens(path);
      Node<T> node = root;
      for (String token : tokens)
      {
         node = node.getChild(token);
         if (node == null)
            break;
      }
      return node;
   }

   /**
    * Initialize node for pathName param.
    *
    * @param pathName the path name
    */
   protected synchronized void initializeNode(String pathName)
   {
      List<String> tokens = PathTokenizer.getTokens(pathName);
      Node<T> node = root;
      boolean newNode = false;
      for (String token : tokens)
      {
         if (newNode)
         {
            node = new Node<T>(token, getDefaultValue(), node);
         }
         else
         {
            Node<T> child = node.getChild(token);
            if (child == null)
            {
               child = new Node<T>(token, getDefaultValue(), node);
               newNode = true;
            }

            node = child;
         }
      }
   }

   /**
    * Simple node impl.
    *
    * @param <U> the exact value type
    */
   private class Node<U>
   {
      private String name;
      private String fullName;
      private Node<U> parent;

      private U value;
      private Map<String, Node<U>> children;
      private Set<String> names;

      private Node(String name, U value, Node<U> parent)
      {
         this.name = name;
         this.value = value;
         this.parent = parent;
         if (parent != null)
            parent.addChild(this);
      }

      /**
       * The node name.
       *
       * @return the node name
       */
      public String getName()
      {
         return name;
      }

      /**
       * Get full name.
       *
       * @return the full name
       */
      public String getFullName()
      {
         if (fullName == null)
         {
            Node<U> parent = getParent();
            if (parent != null && parent.getParent() != null)
            {
               fullName = parent.getFullName() + "/" + getName();
            }
            else
            {
               fullName = getName();
            }
         }
         return fullName;
      }

      /**
       * Get node value.
       *
       * @return the node value
       */
      public U getValue()
      {
         return value;
      }

      /**
       * Set the node value.
       *
       * @param value the value
       */
      public void setValue(U value)
      {
         this.value = value;
      }

      /**
       * Get parent node.
       *
       * @return the parent node
       */
      public Node<U> getParent()
      {
         return parent;
      }

      /**
       * Add child.
       *
       * @param node the child node
       */
      private synchronized void addChild(Node<U> node)
      {
         if (children == null)
            children = new HashMap<String, Node<U>>();
         if (names == null)
            names = new HashSet<String>();

         children.put(node.getName(), node);
         names.add(node.getFullName());
      }

      /**
       * Remove child.
       *
       * @param node the child node
       */
      public synchronized void removeChild(Node<U> node)
      {
         if (children == null || names == null)
            return;
         
         children.remove(node.getName());
         names.remove(node.getFullName());

         if (children.isEmpty())
            children = null;
         if (names.isEmpty())
            names = null;
      }

      /**
       * Clear node.
       */
      public synchronized void clear()
      {
         value = null;
         children = null;
         names = null;
      }

      /**
       * Get child.
       *
       * @param name the child name
       * @return child node or null if not found
       */
      public Node<U> getChild(String name)
      {
         return (children != null) ? children.get(name) : null;
      }

      /**
       * Get children names.
       *
       * @return the children names
       */
      public Set<String> getChildrenNames()
      {
         return (names != null) ? names : Collections.<String>emptySet();
      }

      /**
       * Get children.
       *
       * @return the children
       */
      public Collection<Node<U>> getChildren()
      {
         return (children != null) ? children.values() : Collections.<Node<U>>emptySet();
      }
   }
}
