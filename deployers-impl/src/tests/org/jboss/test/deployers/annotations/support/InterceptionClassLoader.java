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
package org.jboss.test.deployers.annotations.support;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

/**
 * InterceptionClassLoader.
 *
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 */
public class InterceptionClassLoader extends ClassLoader
{
   private ClassLoader delegate;
   private Set<String> loaded = new HashSet<String>();

   public InterceptionClassLoader(ClassLoader delegate)
   {
      super();

      if (delegate == null)
         throw new IllegalArgumentException("Null delegate.");
      this.delegate = delegate;
   }

   public Set<String> getLoaded()
   {
      return loaded;
   }

   public ClassLoader getDelegate()
   {
      return delegate;
   }

   public Class<?> loadClass(String name) throws ClassNotFoundException
   {
      loaded.add(name);
      return delegate.loadClass(name);
   }

   public URL getResource(String name)
   {
      return delegate.getResource(name);
   }

   public Enumeration<URL> getResources(String name) throws IOException
   {
      return delegate.getResources(name);
   }

   public InputStream getResourceAsStream(String name)
   {
      return delegate.getResourceAsStream(name);
   }

   public synchronized void setDefaultAssertionStatus(boolean enabled)
   {
      delegate.setDefaultAssertionStatus(enabled);
   }

   public synchronized void setPackageAssertionStatus(String packageName, boolean enabled)
   {
      delegate.setPackageAssertionStatus(packageName, enabled);
   }

   public synchronized void setClassAssertionStatus(String className, boolean enabled)
   {
      delegate.setClassAssertionStatus(className, enabled);
   }

   public synchronized void clearAssertionStatus()
   {
      delegate.clearAssertionStatus();
   }

   public int hashCode()
   {
      return delegate.hashCode();
   }

   public boolean equals(Object obj)
   {
      return delegate.equals(obj);
   }

   public String toString()
   {
      return delegate.toString();
   }
}