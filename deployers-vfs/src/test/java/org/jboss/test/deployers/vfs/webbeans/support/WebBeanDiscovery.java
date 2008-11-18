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

/**
 * A container should implement this interface to allow the Web Beans RI to
 * discover the Web Beans to deploy
 *
 * @author Pete Muir
 * @author Ales Justin
 *
 */
public interface WebBeanDiscovery
{
   /**
    * @return A list of all classes in classpath archives with web-beans.xml files
    */
   public Iterable<Class<?>> discoverWebBeanClasses();

   /**
    * @return A list of all web-beans.xml files in the app classpath
    */
   public Iterable<URL> discoverWebBeansXml();
}