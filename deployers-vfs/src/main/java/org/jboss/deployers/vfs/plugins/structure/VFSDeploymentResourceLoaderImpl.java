/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.deployers.vfs.plugins.structure;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;

import org.jboss.deployers.vfs.spi.structure.VFSDeploymentResourceLoader;
import org.jboss.virtual.VirtualFile;

/**
 * VFSDeploymentResourceLoader.
 * 
 * @author <a href="adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class VFSDeploymentResourceLoaderImpl implements VFSDeploymentResourceLoader
{
   /** The deployment resource loader */
   private VirtualFile root;
   
   /**
    * Create a new VFSDeploymentResourceLoader.
    * 
    * @param root the root
    * @throws IllegalArgumentException for a null root
    */
   public VFSDeploymentResourceLoaderImpl(VirtualFile root)
   {
      if (root == null)
         throw new IllegalArgumentException("Null root");
      this.root = root;
   }

   public VirtualFile getFile(String path)
   {
      try
      {
         return root.getChild(path);
      }
      catch (IOException e)
      {
         return null;
      }
   }

   public URL getResource(String name)
   {
      try
      {
         VirtualFile child = root.getChild(name);
         return child != null ? child.toURL() : null;
      }
      catch (URISyntaxException e)
      {
         return null;
      }
      catch (MalformedURLException e)
      {
         return null;
      }
      catch (IOException e)
      {
         return null;
      }
   }

   public Enumeration<URL> getResources(String name) throws IOException
   {
      try
      {
         VirtualFile child = root.getChild(name);
         if (child != null)
         {
            Vector<URL> vector = new Vector<URL>();
            vector.add(child.toURL());
            return vector.elements();
         }
         else
            return null;
      }
      catch (URISyntaxException e)
      {
         return null;
      }
      catch (MalformedURLException e)
      {
         return null;
      }
   }
}
