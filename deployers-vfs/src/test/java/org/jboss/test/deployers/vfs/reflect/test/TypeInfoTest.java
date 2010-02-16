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

import java.util.Map;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.reflect.spi.TypeInfo;
import org.jboss.reflect.spi.TypeInfoFactory;
import org.jboss.vfs.VirtualFile;
import org.jboss.test.deployers.vfs.reflect.support.web.AnyServlet;
import org.jboss.test.deployers.vfs.reflect.support.jar.PlainJavaBean;

/**
 * Test case for TypeInfo.
 *
 * @author <a href="mailto:flavia.rainone@jboss.com">Flavia Rainone</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 *
 * @version $Revision$
 */
public abstract class TypeInfoTest extends ReflectTest
{
   public TypeInfoTest(String name)
   {
      super(name);
   }

   protected void assertReflect(VirtualFile file, Map<Class<?>, String> classes) throws Exception
   {
      DeploymentUnit unit = assertDeploy(file);
      try
      {
         TypeInfoFactory typeInfoFactory = createTypeInfoFactory();
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

   protected void assertSimpleHierarchy(ClassLoader topCL, ClassLoader childCL) throws Exception
   {
      TypeInfoFactory typeInfoFactory = createTypeInfoFactory();

      TypeInfo ti = typeInfoFactory.getTypeInfo(AnyServlet.class.getName(), childCL);
      TypeInfo rt = assertReturnType(ti, "getBean");
      TypeInfo cti = typeInfoFactory.getTypeInfo(PlainJavaBean.class.getName(), topCL);
      assertEquals(rt, cti);
   }

   protected void assertNonDeploymentModule(ClassLoader cl, Class<?> anysClass) throws Exception
   {
      TypeInfoFactory factory = createTypeInfoFactory();

      TypeInfo asTIL = factory.getTypeInfo(anysClass);
      TypeInfo pjbTI = factory.getTypeInfo(PlainJavaBean.class.getName(), cl);
      TypeInfo rtL = assertReturnType(asTIL, "getBean");
      assertEquals(pjbTI, rtL);
   }

   protected void assertNonDeploymentModule(ClassLoader cl, Class<?> anysClass, Class<?> tifClass) throws Exception
   {
      TypeInfoFactory factory = createTypeInfoFactory();

      TypeInfo tifTIL = factory.getTypeInfo(tifClass);
      TypeInfo tifRT = assertReturnType(tifTIL, "getAnys");
      TypeInfo asTIL = factory.getTypeInfo(anysClass);
      assertEquals(tifRT, asTIL);

      TypeInfo pjbTI = factory.getTypeInfo(PlainJavaBean.class.getName(), cl);
      TypeInfo rtL = assertReturnType(asTIL, "getBean");
      assertEquals(pjbTI, rtL);
   }

   protected void assertIsolated(ClassLoader cl1, ClassLoader cl2, Class<?> clazz1, Class<?> clazz2) throws Exception
   {
      TypeInfoFactory factory = createTypeInfoFactory();

      TypeInfo ti1 = factory.getTypeInfo(PlainJavaBean.class.getName(), cl1);
      TypeInfo ti2 = factory.getTypeInfo(PlainJavaBean.class.getName(), cl2);
      assertNotEquals(ti1, ti2);

      TypeInfo ti3 = factory.getTypeInfo(clazz1);
      assertEquals(ti1, ti3);

      TypeInfo ti4 = factory.getTypeInfo(clazz2);
      assertEquals(ti2, ti4);

      assertNotEquals(ti3, ti4);
   }

   protected void assertDomainHierarchy(ClassLoader topCL, ClassLoader leftCL, ClassLoader rightCL) throws Exception
   {
      TypeInfoFactory factory = createTypeInfoFactory();

      TypeInfo pjbTI = factory.getTypeInfo(PlainJavaBean.class.getName(), topCL);
      TypeInfo asTIL = factory.getTypeInfo(AnyServlet.class.getName(), leftCL);
      TypeInfo asTIR = factory.getTypeInfo(AnyServlet.class.getName(), rightCL);

      TypeInfo rtL = assertReturnType(asTIL, "getBean");
      assertEquals(pjbTI, rtL);

      TypeInfo rtR = assertReturnType(asTIR, "getBean");
      assertEquals(pjbTI, rtR);
   }
}