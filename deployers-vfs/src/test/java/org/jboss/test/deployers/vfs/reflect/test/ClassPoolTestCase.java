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

import org.jboss.classpool.scoped.ScopedClassPoolRepository;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.reflect.plugins.javassist.JavassistTypeInfoFactory;
import org.jboss.reflect.spi.TypeInfoFactory;
import org.jboss.test.deployers.vfs.reflect.support.ClassPoolTestDelegate;
import org.jboss.test.deployers.vfs.reflect.support.jar.PlainJavaBean;
import org.jboss.test.deployers.vfs.reflect.support.web.AnyServlet;
import org.jboss.vfs.VirtualFile;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import junit.framework.Test;

/**
 * Test case for ClassPool.
 * 
 * @author <a href="mailto:flavia.rainone@jboss.com">Flavia Rainone</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 *
 * @version $Revision$
 */
public class ClassPoolTestCase extends ReflectTest
{
   protected ScopedClassPoolRepository classPoolRepository = null;

   public ClassPoolTestCase(String name)
   {
      super(name);
   }

   public static Test suite()
   {
      return suite(ClassPoolTestCase.class);
   }

   public static ClassPoolTestDelegate getDelegate(Class<?> clazz) throws Exception
   {
      return new ClassPoolTestDelegate(clazz);
   }
   
   @Override
   public void setUp() throws Exception
   {
      super.setUp();
      classPoolRepository = (ScopedClassPoolRepository) getBean("ClassPoolRepository");
      assertNotNull(classPoolRepository);
   }

   @Override
   protected void tearDown() throws Exception
   {
      classPoolRepository = null;
      super.tearDown();
   }

   protected TypeInfoFactory createTypeInfoFactory()
   {
      return new JavassistTypeInfoFactory();
   }

   protected void assertReflect(VirtualFile file, Map<Class<?>, String> classes) throws Exception
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
            ClassPool classPool = classPoolRepository.registerClassLoader(classLoader);
            assertNotNull("ClassPool for " + classLoader + " is null", classPool);
            CtClass ctClass = classPool.getCtClass(className);
            assertNotNull("Class" + className + " retrieved from pool " + classPool + " is null", ctClass);
            assertEquals(className, clazz.getName());
            ClassLoader cl = ctClass.getClassPool().getClassLoader();
            assertEquals("Class has been loaded by the wrong class loader: " + clazz.getName(), loadedClass.getClassLoader(), cl);
         }
      }
      finally
      {
         undeploy(unit);
      }
   }

   protected void assertSimpleHierarchy(ClassLoader topCL, ClassLoader childCL) throws Exception
   {
      ClassPool classPool = classPoolRepository.registerClassLoader(childCL);
      CtClass ctClass = classPool.getCtClass(AnyServlet.class.getName());
      CtMethod ctMethod = ctClass.getDeclaredMethod("getBean");
      assertNotNull("No such 'getBean' method on " + ctClass, ctMethod);

      CtClass returnCtClass = ctMethod.getReturnType();
      classPool = classPoolRepository.registerClassLoader(topCL);
      CtClass returnCtClass2 = classPool.getCtClass(PlainJavaBean.class.getName());
      assertSame(returnCtClass, returnCtClass2);
   }

   protected void assertNonDeploymentModule(ClassLoader cl, Class<?> anysClass) throws Exception
   {
      ClassPool classPool = classPoolRepository.registerClassLoader(cl);
      CtClass asClass = classPool.getCtClass(anysClass.getName());
      CtClass pjbClass = classPool.getCtClass(PlainJavaBean.class.getName());
      CtClass rtClass = asClass.getDeclaredMethod("getBean").getReturnType();
      assertEquals(pjbClass, rtClass);
   }

   protected void assertNonDeploymentModule(ClassLoader cl, Class<?> anysClass, Class<?> tifClass) throws Exception
   {
      ClassPool classPool = classPoolRepository.registerClassLoader(cl);
      ClassPool tifClassPool = classPoolRepository.registerClassLoader(tifClass.getClassLoader());
      ClassPool anysClassPool = classPoolRepository.registerClassLoader(anysClass.getClassLoader());

      CtClass tifCtClass = tifClassPool.getCtClass(tifClass.getName());
      CtClass tifRT = tifCtClass.getDeclaredMethod("getAnys").getReturnType();
      CtClass asCtClass = anysClassPool.getCtClass(anysClass.getName());
      assertEquals(tifRT, asCtClass);

      CtClass pjbCtClass = classPool.getCtClass(PlainJavaBean.class.getName());
      CtClass rtL = asCtClass.getDeclaredMethod("getBean").getReturnType();
      assertEquals(pjbCtClass, rtL);
   }

   protected void assertIsolated(ClassLoader cl1, ClassLoader cl2, Class<?> clazz1, Class<?> clazz2) throws Exception
   {
      ClassPool classPool1 = classPoolRepository.registerClassLoader(cl1);
      ClassPool classPool2 = classPoolRepository.registerClassLoader(cl2);

      CtClass class1 = classPool1.getCtClass(PlainJavaBean.class.getName());
      CtClass class2 = classPool2.getCtClass(PlainJavaBean.class.getName());
      assertNotSame(class1, class2);

      ClassPool classPool3 = classPoolRepository.registerClassLoader(clazz1.getClassLoader());
      CtClass class3 = classPool3.getCtClass(clazz1.getName());
      assertEquals(class1, class3);

      ClassPool classPool4 = classPoolRepository.registerClassLoader(clazz2.getClassLoader());
      CtClass class4 = classPool4.getCtClass(clazz2.getName());
      assertEquals(class2, class4);

      assertNotSame(class3, class4);
   }

   protected void assertDomainHierarchy(ClassLoader topCL, ClassLoader leftCL, ClassLoader rightCL) throws Exception
   {
      ClassPool topClassPool = classPoolRepository.registerClassLoader(topCL);
      ClassPool leftClassPool = classPoolRepository.registerClassLoader(leftCL);
      ClassPool rightClassPool = classPoolRepository.registerClassLoader(rightCL);
      CtClass pjbClass = topClassPool.getCtClass(PlainJavaBean.class.getName());
      CtClass asClassL = leftClassPool.getCtClass(AnyServlet.class.getName());
      CtClass asClassR = rightClassPool.getCtClass(AnyServlet.class.getName());

      CtClass rtClassL = asClassL.getDeclaredMethod("getBean").getReturnType();
      assertEquals(pjbClass, rtClassL);

      CtClass rtClassR = asClassR.getDeclaredMethod("getBean").getReturnType();
      assertEquals(pjbClass, rtClassR);
   }
}