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
package org.jboss.test.deployers.vfs.reflect.test;

import java.util.HashMap;
import java.util.Map;

import javassist.ClassPool;
import javassist.CtClass;

import org.jboss.classpool.spi.ClassPoolRepository;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.reflect.plugins.javassist.JavassistTypeInfoFactory;
import org.jboss.reflect.spi.TypeInfoFactory;
import org.jboss.virtual.VirtualFile;

/**
 * Abstract test for ClassPool.
 *
 * @author <a href="mailto:flavia.rainone@jboss.com">Flavia Rainone</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 *
 * @version $Revision$
 */
public abstract class ClassPoolTest extends ReflectTest
{
   protected ClassPoolTest(String name)
   {
      super(name);
   }

   protected TypeInfoFactory createTypeInfoFactory()
   {
      return new JavassistTypeInfoFactory();
   }

   protected void assertClassPool(VirtualFile file, Class<?> ... classes) throws Exception
   {
      Map<Class<?>, String> map = new HashMap<Class<?>, String>();
      for (Class<?> clazz : classes)
      {
         map.put(clazz, null);
      }
      assertClassPool(file, map);
   }

   protected void assertClassPool(VirtualFile file, Map<Class<?>, String> classes) throws Exception
   {
      DeploymentUnit unit = assertDeploy(file);
      try
      {
         for (Map.Entry<Class<?>, String> entry : classes.entrySet())
         {
            DeploymentUnit du = getDeploymentUnit(unit, entry.getValue());
            ClassLoader classLoader = getClassLoader(du);
            Class<?> clazz = entry.getKey();
            String className = clazz.getName();
            // sanity check
            Class<?> loadedClass = assertLoadClass(className, classLoader);
            ClassPoolRepository repository = ClassPoolRepository.getInstance();
            ClassPool classPool = repository.registerClassLoader(classLoader);
            CtClass ctClass = classPool.getCtClass(className);
            assertEquals(className, clazz.getName());
            ClassLoader cl = ctClass.getClassPool().getClassLoader();
            assertEquals(loadedClass.getClassLoader(), cl);
         }
      }
      finally
      {
         undeploy(unit);
      }
   }
}