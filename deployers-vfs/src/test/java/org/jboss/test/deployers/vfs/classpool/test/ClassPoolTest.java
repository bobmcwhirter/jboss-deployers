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
package org.jboss.test.deployers.vfs.classpool.test;

import java.util.Map;
import java.util.Set;
import java.util.HashMap;

import org.jboss.classloader.plugins.jdk.AbstractJDKChecker;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.deployer.kernel.BeanMetaDataFactoryVisitor;
import org.jboss.reflect.plugins.javassist.JavassistTypeInfoFactory;
import org.jboss.reflect.spi.TypeInfo;
import org.jboss.reflect.spi.TypeInfoFactory;
import org.jboss.test.deployers.BootstrapDeployersTest;
import org.jboss.virtual.VirtualFile;

/**
 * Abstract test for ClassPool.
 *
 * @author <a href="mailto:flavia.rainone@jboss.com">Flavia Rainone</a>
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 *
 * @version $Revision$
 */
public abstract class ClassPoolTest extends BootstrapDeployersTest
{
   protected ClassPoolTest(String name)
   {
      super(name);
   }

   @Override
   protected void setUp() throws Exception
   {
      // excluding class that knows hot to load from system classloader
      Set<Class<?>> excluded = AbstractJDKChecker.getExcluded();
      excluded.add(BeanMetaDataFactoryVisitor.class);

      super.setUp();
   }

   @Override
   protected void tearDown() throws Exception
   {
      super.tearDown();
      // reverting exclusion performed on setUp
      Set<Class<?>> excluded = AbstractJDKChecker.getExcluded();
      excluded.remove(BeanMetaDataFactoryVisitor.class);
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
         TypeInfoFactory typeInfoFactory = new JavassistTypeInfoFactory();
         for (Map.Entry<Class<?>, String> entry : classes.entrySet())
         {
            DeploymentUnit du = getDeploymentUnit(unit, entry.getValue());
            ClassLoader classLoader = getClassLoader(du);
            Class<?> clazz = entry.getKey();
            String className = clazz.getName();
            assertLoadClass(className, classLoader);
            TypeInfo typeInfo = typeInfoFactory.getTypeInfo(className, classLoader);
            assertEquals(className, typeInfo.getName());
            ClassLoader cl = typeInfo.getClassLoader();
            assertEquals(classLoader, cl);
         }
      }
      finally
      {
         undeploy(unit);
      }
   }

   protected DeploymentUnit getDeploymentUnit(DeploymentUnit parent, String name)
   {
      if (name == null || "".equals(name))
         return parent;

      return assertChild(parent, name);
   }
}