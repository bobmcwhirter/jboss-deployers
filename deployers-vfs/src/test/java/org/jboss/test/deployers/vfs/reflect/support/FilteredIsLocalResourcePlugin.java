/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.deployers.vfs.reflect.support;

import java.net.URL;

import org.jboss.classloader.spi.filter.ClassFilter;
import org.jboss.classpool.base.BaseClassPool;
import org.jboss.classpool.base.TranslatableClassLoaderIsLocalResourcePlugin;

/**
 * Filters out the classes that are in the excluded classpath. 
 * 
 * @author  <a href="flavia.rainone@jboss.com">Flavia Rainone</a>
 * @version $Revision 1.1 $
 */
public class FilteredIsLocalResourcePlugin extends TranslatableClassLoaderIsLocalResourcePlugin
{
   private ClassFilter classFilter;
   
   public FilteredIsLocalResourcePlugin(BaseClassPool pool, ClassFilter classFilter)
   {
      super(pool);
      this.classFilter = classFilter;
   }

   @Override
   protected boolean isSameInParent(String classResourceName, URL foundURL)
   {
      if (classFilter.matchesResourcePath(classResourceName))
      {
         return super.isSameInParent(classResourceName, foundURL);
      }
      return false;
   }
}