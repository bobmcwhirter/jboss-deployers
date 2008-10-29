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
package org.jboss.deployers.vfs.plugins.annotations;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import javassist.ClassPath;
import javassist.NotFoundException;
import org.jboss.classloader.plugins.ClassLoaderUtils;
import org.jboss.classloader.spi.filter.ClassFilter;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.virtual.VirtualFile;

/**
 * Javassist ClassPath impl based on deployment unit
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class DeploymentUnitClassPath implements ClassPath
{
   private VFSDeploymentUnit unit;
   private Map<String, VirtualFile> cache;

   public DeploymentUnitClassPath(VFSDeploymentUnit unit)
   {
      if (unit == null)
         throw new IllegalArgumentException("Null deployment unit.");
      this.unit = unit;
      this.cache = new HashMap<String, VirtualFile>();
   }

   /**
    * Find file.
    *
    * @param className the classname we're looking for
    * @return virtual file or null if not found
    * @throws IOException for any exception
    */
   protected VirtualFile findFile(String className) throws IOException
   {
      // ignore jdk classes
      if (ClassFilter.JAVA_ONLY.matchesClassName(className))
         return null;

      VirtualFile file = cache.get(className);
      if (file != null)
         return file;

      String path = ClassLoaderUtils.classNameToPath(className);
      List<VirtualFile> classPath = unit.getClassPath();
      if (classPath != null && classPath.isEmpty() == false)
      {
         for (VirtualFile cp : classPath)
         {
            file = cp.getChild(path);
            if (file != null)
            {
               cache.put(className, file);
               return file;
            }
         }
      }

      return null;
   }

   public InputStream openClassfile(String className) throws NotFoundException
   {
      try
      {
         VirtualFile file = findFile(className);
         if (file != null)
            return file.openStream();
      }
      catch (IOException e)
      {
         throw new NotFoundException("Exception finding file: " + className, e);
      }
      throw new NotFoundException("ClassName '" + className + "' not found in deployment unit: " + unit);
   }

   public URL find(String className)
   {
      try
      {
         VirtualFile file = findFile(className);
         if (file != null)
            return file.toURL();
      }
      catch (Exception ignored)
      {
      }
      return null;
   }

   public void close()
   {
      cache.clear();
   }
}