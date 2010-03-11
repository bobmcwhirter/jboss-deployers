/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, JBoss Inc., and individual contributors as indicated
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

import org.jboss.classloader.spi.filter.ClassFilter;
import org.jboss.classpool.base.IsLocalResourcePluginFactoryRegistry;
import org.jboss.classpool.spi.AbstractClassPoolFactory;
import org.jboss.test.deployers.BootstrapDeployersTestDelegate;
import org.jboss.util.loading.Translatable;

import javassist.ClassPool;

/**
 * ClassPoolTestDelegate, sets a FilteredClassPool as the default ClassPool 
 * of AbstractClassPoolFactory.
 *
 * @author <a href="flavia@jboss.com">Flavia Rainone</a>
 * @author <a href="ales.justin@jboss.org">Ales Justin</a>
 * @version $Revision$
 */
public class ClassPoolTestDelegate extends BootstrapDeployersTestDelegate
{
   private static ClassPool defaultClassPool;

   static
   {
      ClassFilter filter = createNegatingClassFilter();
      defaultClassPool = new FilteredClassPool(AbstractClassPoolFactory.getSystemClassPool(), filter);
      IsLocalResourcePluginFactoryRegistry.addPluginFactory(Translatable.class, new FilteredIsLocalResourcePluginFactory(filter));
   }

   public ClassPoolTestDelegate(Class<?> clazz) throws Exception
   {
      super(clazz);
   }

   protected void deploy() throws Exception
   {
      super.deploy();
      AbstractClassPoolFactory.setSystemClassPool(defaultClassPool);
   }
}