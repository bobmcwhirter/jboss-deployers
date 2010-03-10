/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
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
package org.jboss.test.deployers.vfs.reflect.support;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.Iterator;

import javassist.CannotCompileException;
import javassist.ClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

import org.jboss.classloader.spi.filter.ClassFilter;

/**
 * Filters loading of test classes. For test purposes only.
 * 
 * @author <a href="mailto:flavia.rainone@jboss.com">Flavia Rainone</a>
 * @version $Revision$
 */

public class FilteredClassPool extends ClassPool
{
   private ClassPool delegate;
   private ClassFilter classFilter;
   
   public FilteredClassPool(ClassPool delegate, ClassFilter classFilter)
   {
      this.delegate = delegate;
      this.classFilter = classFilter;
   }
   
   @Override
   public String toString()
   {
      return "FilteredClassPool (" + delegate + ")";
   }

   @Override
   public void importPackage(String packageName)
   {
      delegate.importPackage(packageName);
   }

   @Override
   public void clearImportedPackages()
   {
      if (delegate != null)
      {
         delegate.clearImportedPackages();
      }
      else
      {
         super.clearImportedPackages();
      }
   }

   @Override
   public Iterator<?> getImportedPackages()
   {
      return delegate.getImportedPackages();
   }

   @Override
   public void recordInvalidClassName(String name)
   {
      delegate.recordInvalidClassName(name);
   }

   @Override
   public Object[] lookupCflow(String name)
   {
      return delegate.lookupCflow(name);
   }

   @Override
   public CtClass getAndRename(String orgName, String newName) throws NotFoundException
   {
      return delegate.getAndRename(orgName, newName);
   }

   @Override
   public CtClass get(String classname) throws NotFoundException
   {
      if (classFilter.matchesClassName(classname))
      {
         return delegate.get(classname);
      }
      throw new NotFoundException(classname);
   }

   @Override
   public CtClass getCtClass(String classname) throws NotFoundException
   {
      if (classFilter.matchesClassName(classname))
      {
         return delegate.getCtClass(classname);
      }
      throw new NotFoundException(classname);
   }

   @Override
   public URL find(String classname)
   {
      if (classFilter.matchesClassName(classname))
      {
         return delegate.find(classname);
      }
      return null;
   }

   @Override
   public CtClass[] get(String[] classnames) throws NotFoundException
   {
      for (String classname: classnames)
      {
         if (!classFilter.matchesClassName(classname))
            throw new NotFoundException(classname);
      }
      return delegate.get(classnames);
   }

   @Override
   public CtMethod getMethod(String classname, String methodname) throws NotFoundException
   {
      if (classFilter.matchesClassName(classname))
      {
         return delegate.getMethod(classname, methodname);
      }
      throw new NotFoundException(classname);
   }

   @Override
   public CtClass makeClass(InputStream classfile) throws IOException, RuntimeException
   {
      return delegate.makeClass(classfile);
   }

   @Override
   public CtClass makeClass(InputStream classfile, boolean ifNotFrozen)
   throws IOException, RuntimeException
   {
      return delegate.makeClass(classfile, ifNotFrozen);
   }

   @Override
   public CtClass makeClassIfNew(InputStream classfile) throws IOException, RuntimeException
   {
      return delegate.makeClassIfNew(classfile);
   }

   @Override
   public CtClass makeClass(String classname) throws RuntimeException
   {
      return delegate.makeClass(classname);
   }

   @Override
   public synchronized CtClass makeClass(String classname, CtClass superclass)
   throws RuntimeException
   {
      return delegate.makeClass(classname, superclass);
   }

   @Override
   public CtClass makeInterface(String name) throws RuntimeException
   {
      return delegate.makeInterface(name);
   }

   @Override
   public synchronized CtClass makeInterface(String name, CtClass superclass)
   throws RuntimeException
   {
      return delegate.makeInterface(name, superclass);
   }

   @Override
   public ClassPath appendSystemPath()
   {
      return delegate.appendSystemPath();
   }

   @Override
   public ClassPath insertClassPath(ClassPath cp)
   {
      return delegate.insertClassPath(cp);
   }

   @Override
   public ClassPath appendClassPath(ClassPath cp)
   {
      return delegate.appendClassPath(cp);
   }

   @Override
   public ClassPath insertClassPath(String pathname) throws NotFoundException
   {
      return delegate.insertClassPath(pathname);
   }

   @Override
   public ClassPath appendClassPath(String pathname) throws NotFoundException
   {
      return delegate.appendClassPath(pathname);
   }

   @Override
   public void removeClassPath(ClassPath cp)
   {
      delegate.removeClassPath(cp);
   }

   @Override
   public void appendPathList(String pathlist) throws NotFoundException
   {
      delegate.appendPathList(pathlist);
   }

   @Override
   public Class<?> toClass(CtClass clazz) throws CannotCompileException
   {
      return delegate.toClass(clazz);
   }

   @Override
   public ClassLoader getClassLoader()
   {
      return delegate.getClassLoader();
   }

   @Override
   @SuppressWarnings("deprecation")
   public Class<?> toClass(CtClass ct, ClassLoader loader) throws CannotCompileException
   {
      return delegate.toClass(ct, loader);
   }

   @Override
   public Class<?> toClass(CtClass ct, ClassLoader loader, ProtectionDomain domain)
   throws CannotCompileException
   {
      return delegate.toClass(ct, loader, domain);
   }
   
   @Override
   protected synchronized CtClass get0(String classname, boolean useCache)
   throws NotFoundException
   {
      if (this.classFilter.matchesClassName(classname))
      {
         return delegate.getCtClass(classname);
      }
      throw new NotFoundException(classname);
   }
   
   @Override
   public int hashCode()
   {
      return this.delegate.hashCode();
   }

   @Override
   public boolean equals(Object other)
   {
      return this == other || this.delegate == other;
   }
}