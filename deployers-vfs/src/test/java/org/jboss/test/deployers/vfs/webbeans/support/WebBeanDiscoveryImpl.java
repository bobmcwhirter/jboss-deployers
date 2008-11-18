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
package org.jboss.test.deployers.vfs.webbeans.support;

import java.net.URL;
import java.util.Set;
import java.util.HashSet;

/**
 * WBD impl.
 * 
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class WebBeanDiscoveryImpl implements WebBeanDiscovery
{
   private Set<Class<?>> classes = new HashSet<Class<?>>();
   private Set<URL> urls = new HashSet<URL>();

   public void addWebBeanClass(Class<?> clazz)
   {
      classes.add(clazz);
   }

   public void addWebBeansXmlURL(URL url)
   {
      urls.add(url);      
   }

   public Iterable<Class<?>> discoverWebBeanClasses()
   {
      return classes;
   }

   public Iterable<URL> discoverWebBeansXml()
   {
      return urls;
   }
}