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
package org.jboss.test.deployers.vfs.reflect.support;

import org.jboss.classpool.scoped.ScopedClassPoolRepository;

import org.jboss.reflect.plugins.javassist.JavassistTypeInfoFactoryImpl;
import org.jboss.reflect.plugins.javassist.classpool.ClassPoolFactory;
import org.jboss.reflect.plugins.javassist.classpool.DefaultClassPoolFactory;
import org.jboss.reflect.plugins.javassist.classpool.RepositoryClassPoolFactory;

/**
 * Initializes the pool factory.
 * 
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:flavia.rainone@jboss.com">Flavia Rainone</a>
 */
public class PoolFactoryInitializer
{
   private ScopedClassPoolRepository poolRepository;
   
   public PoolFactoryInitializer(ScopedClassPoolRepository repository)
   {
      this.poolRepository = repository;
   }

   public void start()
   {
      ClassPoolFactory classPoolFactory = new RepositoryClassPoolFactory(poolRepository);
      JavassistTypeInfoFactoryImpl.setPoolFactory(classPoolFactory);
   }

   public void stop()
   {
      JavassistTypeInfoFactoryImpl.setPoolFactory(DefaultClassPoolFactory.getInstance());
   }
}
