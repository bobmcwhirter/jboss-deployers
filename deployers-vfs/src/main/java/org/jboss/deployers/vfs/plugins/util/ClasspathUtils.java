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
package org.jboss.deployers.vfs.plugins.util;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.logging.Logger;
import org.jboss.vfs.VirtualFile;

/**
 * Classpath utils.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class ClasspathUtils
{
   /** The log */
   private static final Logger log = Logger.getLogger(ClasspathUtils.class);

   /**
    * Get the classpath urls that only belong to the unit.
    *
    * @param unit the deployment unit
    * @return matching urls
    * @throws Exception for any error
    */
   public static URL[] getUrls(VFSDeploymentUnit unit) throws Exception
   {
      List<VirtualFile> classpath = unit.getClassPath();
      if (classpath != null && classpath.isEmpty() == false)
      {
         List<URL> urls = new ArrayList<URL>();
         VirtualFile root = unit.getRoot();
         for (VirtualFile cp : classpath)
         {
            VirtualFile check = cp;
            while(check != null && check.equals(root) == false)
               check = check.getParent();

            if (check != null)
               urls.add(cp.toURL());
         }
         if (urls.isEmpty() == false)
         {
            if (log.isTraceEnabled())
               log.trace("Explicit urls: " + urls);

            return urls.toArray(new URL[urls.size()]);
         }
      }
      return new URL[0];
   }
}
