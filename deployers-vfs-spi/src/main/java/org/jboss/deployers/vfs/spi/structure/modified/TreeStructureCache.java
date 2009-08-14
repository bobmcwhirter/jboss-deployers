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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.jboss.virtual.VirtualFile;
import org.jboss.virtual.VirtualFileFilter;
import org.jboss.virtual.plugins.vfs.helpers.PathTokenizer;

/**
 * Tree base structure cache.
 *
 * @param <T> exact value type
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class TreeStructureCache<T> extends AbstractStructureCache<T>
{
   /** The tree root */
   private final Node<T> root = createRoot();

   /**
    * Create new root.
    *
    * @return the new root
    */
   protected Node<T> createRoot()
   {
      return new Node<T>(null, getDefaultValue(), null);
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

   public void initializeCache(VirtualFile root)
   {
      initializeNode(root);
   }

   public T putCacheValue(VirtualFile file, T value)
   {
      // we try to initialize it if it doesn't exist
      Node<T> node = initializeNode(file);
      T previous = node.getValue();
      node.setValue(value);
      return previous;
   }

   public T getCacheValue(VirtualFile file)
   {
      Node<T> node = getNode(file);
      return (node != null ? node.getValue() : null);
   }

   public List<VirtualFile> getLeaves(VirtualFile file, VirtualFileFilter filter)
   {
      Node<T> node = getNode(file);
      if (node != null)
      {
         List<VirtualFile> result = new ArrayList<VirtualFile>();
         Collection<Node<T>> children = node.getChildren();
         if (children != null && children.isEmpty() == false)
         {
            for (Node<T> child : children)
            {
               VirtualFile vf = child.getFile();
               if (filter == null || filter.accepts(vf))
                  result.add(vf);
            }
         }
         return result;
      }
      else
      {
         return null;
      }
   }

   public void invalidateCache(VirtualFile file)
   {
      removeCache(file);
   }

   public void removeCache(VirtualFile file)
   {
      Node<T> node = getNode(file);
      if (node != null)
      {
         Node<T> parent = node.getParent();
         if (parent != null)
            parent.removeChild(node);
         else // clear root
            flush();
      }
   }

   public void removeCache(VirtualFile root, String path)
   {
      Node<T> node = getNode(root);
      if (node != null)
      {
         List<String> tokens = PathTokenizer.getTokens(path);
         Node<T> child = findNode(0, tokens, node);
         if (child != null)
         {
            Node<T> parent = child.getParent();
            if (parent != null)
               parent.removeChild(node);
            else // clear root
               flush();
         }
      }
   }

   /**
    * Find node.
    *
    * @param index the current token index
    * @param tokens the tokens
    * @param node the current node
    * @return found node or null if no match
    */
   protected Node<T> findNode(int index, List<String> tokens, Node<T> node)
   {
      if (index == tokens.size())
         return node;

      Collection<Node<T>> nodes = node.getChildren();
      if (nodes == null || nodes.isEmpty())
         return null;

      String token = tokens.get(index);
      for (Node<T> child : nodes)
      {
         VirtualFile file = child.getFile();
         if (token.equals(file.getName()))
            return findNode(index + 1, tokens, child);
      }
      return null;
   }

   public void flush()
   {
      synchronized (root)
      {
         root.clear();
      }
   }

   /**
    * Get file tokens.
    *
    * @param file the file to tokenize
    * @return file's tokens
    */
   protected List<VirtualFile> tokens(VirtualFile file)
   {
      try
      {
         List<VirtualFile> tokens = new ArrayList<VirtualFile>();
         while(file != null)
         {
            tokens.add(0, file);
            file = file.getParent();
         }
         return tokens;
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }
   }

   /**
    * Get the path's node.
    *
    * @param file the file key
    * @return node or null if it doesn't exist
    */
   protected Node<T> getNode(VirtualFile file)
   {
      List<VirtualFile> tokens = tokens(file);
      synchronized (root)
      {
         Node<T> node = root;
         for (VirtualFile token : tokens)
         {
            node = node.getChild(token);
            if (node == null)
               break;
         }
         return node;
      }
   }

   /**
    * Initialize node for pathName param.
    *
    * @param file the file key
    * @return initialized node
    */
   protected Node<T> initializeNode(VirtualFile file)
   {
      List<VirtualFile> tokens = tokens(file);
      synchronized (root)
      {
         Node<T> node = root;
         boolean newNode = false;
         for (VirtualFile token : tokens)
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
         return node;
      }
   }

   /**
    * Simple node impl.
    *
    * @param <U> the exact value type
    */
   private class Node<U>
   {
      private ReadWriteLock lock = new ReentrantReadWriteLock();

      private VirtualFile file;
      private Node<U> parent;

      private U value;
      private Map<VirtualFile, Node<U>> children;

      private Node(VirtualFile file, U value, Node<U> parent)
      {
         this.file = file;
         this.value = value;
         this.parent = parent;
         if (parent != null)
            parent.addChild(this);
      }

      /**
       * The node file.
       *
       * @return the node file
       */
      public VirtualFile getFile()
      {
         return file;
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
      private void addChild(Node<U> node)
      {
         lock.writeLock().lock();
         try
         {
            if (children == null)
               children = new HashMap<VirtualFile, Node<U>>();

            children.put(node.getFile(), node);
         }
         finally
         {
            lock.writeLock().unlock();
         }
      }

      /**
       * Remove child.
       *
       * @param node the child node
       */
      public void removeChild(Node<U> node)
      {
         lock.writeLock().lock();
         try
         {
            if (children == null)
               return;

            children.remove(node.getFile());

            if (children.isEmpty())
               children = null;
         }
         finally
         {
            lock.writeLock().unlock();
         }
      }

      /**
       * Clear node.
       */
      void clear()
      {
         lock.writeLock().lock();
         try
         {
            value = null;
            children = null;
         }
         finally
         {
            lock.writeLock().unlock();
         }
      }

      /**
       * Get child.
       *
       * @param file the child file
       * @return child node or null if not found
       */
      public Node<U> getChild(VirtualFile file)
      {
         lock.readLock().lock();
         try
         {
            return (children != null) ? children.get(file) : null;
         }
         finally
         {
            lock.readLock().unlock();
         }
      }

      /**
       * Get children.
       *
       * @return the children
       */
      public Collection<Node<U>> getChildren()
      {
         lock.readLock().lock();
         try
         {
            return (children != null) ? children.values() : Collections.<Node<U>>emptySet();
         }
         finally
         {
            lock.readLock().unlock();
         }
      }

      @Override
      public String toString()
      {
         return String.valueOf(file);
      }
   }
}
