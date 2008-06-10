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
package org.jboss.deployers.plugins.annotations;

import java.lang.ref.WeakReference;

/**
 * ClassLoader holder helper.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
abstract class WeakClassLoaderHolder
{
   private transient WeakReference<ClassLoader> clRef;

   public WeakClassLoaderHolder(ClassLoader classLoader)
   {
      if (classLoader == null)
         throw new IllegalArgumentException("Null classloader");

      clRef = new WeakReference<ClassLoader>(classLoader);
   }

   /**
    * Get the classloader from weak ref.
    *
    * @return the classloader
    */
   protected ClassLoader getClassLoader()
   {
      if (clRef == null)
         throw new IllegalArgumentException("Null classloader ref, previously serialized?");

      ClassLoader classLoader = clRef.get();
      if (classLoader == null)
         throw new IllegalArgumentException("ClassLoader was already garbage collected.");

      return classLoader;
   }

   /**
    * Load class from class name.
    *
    * @param className the class name
    * @return loaded class
    */
   protected Class<?> loadClass(String className)
   {
      try
      {
         return Class.forName(className, false, getClassLoader());
      }
      catch (ClassNotFoundException e)
      {
         throw new RuntimeException(e);
      }
   }
}