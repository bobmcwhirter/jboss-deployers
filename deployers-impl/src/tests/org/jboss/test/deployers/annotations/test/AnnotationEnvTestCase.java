/*
* JBoss, Home of Professional Open Source
* Copyright 2006, JBoss Inc., and individual contributors as indicated
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
package org.jboss.test.deployers.annotations.test;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

import junit.framework.Test;
import org.jboss.classloader.plugins.ClassLoaderUtils;
import org.jboss.deployers.client.spi.DeployerClient;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.spi.annotations.AnnotationEnvironment;
import org.jboss.deployers.spi.annotations.Element;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.test.deployers.annotations.support.AnnotationsHolder;
import org.jboss.test.deployers.annotations.support.TestAnnotation;

/**
 * AnnotationEnvTestCase.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class AnnotationEnvTestCase extends AnnotationsTest
{
   public AnnotationEnvTestCase(String name)
   {
      super(name);
   }

   public static Test suite()
   {
      return suite(AnnotationEnvTestCase.class);
   }

   @SuppressWarnings("unchecked")
   public void testDirectClassUsage() throws Exception
   {
      DeployerClient deployer = getMainDeployer();

      Deployment deployment = createSimpleDeployment("a");
      addClassLoadingMetaData(
            deployment,
            deployment.getName(),
            null,
            ClassLoaderUtils.classNameToPath("org.jboss.test.deployers.annotations.support.AnnotationsHolder"),
            ClassLoaderUtils.classNameToPath("org.jboss.test.deployers.annotations.support.TestAnnotation")
      );

      DeploymentUnit unit = assertDeploy(deployer, deployment);
      try
      {
         ClassLoader cl = unit.getClassLoader();
         Class<TestAnnotation> taClass = (Class<TestAnnotation>)cl.loadClass("org.jboss.test.deployers.annotations.support.TestAnnotation");

         assertNotLoaded(unit, "org.jboss.test.deployers.annotations.support.AnnotationsHolder");
         // annotations are loaded, OK?
         assertLoaded(unit, "org.jboss.test.deployers.annotations.support.TestAnnotation");

         AnnotationEnvironment env = getAnnotationEnvironment(unit);
         Set<Class<?>> classes = env.classIsAnnotatedWith(taClass);
         assertNotNull(classes);
         assertEquals(1, classes.size());
         assertEquals(AnnotationsHolder.class.getName(), classes.iterator().next().getName());

         assertLoaded(unit, "org.jboss.test.deployers.annotations.support.AnnotationsHolder");

         Element<TestAnnotation, Constructor> ec = getSingleton(env.classHasConstructorAnnotatedWith(taClass));
         Annotation ta = ec.getAnnotation();
         assertNotNull(ta);
         assertEquals("constructor", getValue(ta));
         assertInstanceOf(ec.getAccessibleObject(), Constructor.class, false);

         Element<TestAnnotation, Field> ef = getSingleton(env.classHasFieldAnnotatedWith(taClass));
         ta = ef.getAnnotation();
         assertNotNull(ta);
         assertEquals("field", getValue(ta));
         assertInstanceOf(ef.getAccessibleObject(), Field.class, false);

         Element<TestAnnotation, Method> em = getSingleton(env.classHasMethodAnnotatedWith(taClass));
         ta = em.getAnnotation();
         assertNotNull(ta);
         assertEquals("method", getValue(ta));
         assertInstanceOf(em.getAccessibleObject(), Method.class, false);

         Set<Element<TestAnnotation, AccessibleObject>> eps = env.classHasParameterAnnotatedWith(taClass);
         assertNotNull(eps);
         assertEquals(2, eps.size());
         for (Element<TestAnnotation, AccessibleObject> ep : eps)
         {
            ta = ep.getAnnotation();
            assertNotNull(ta);
            Object value = getValue(ta);
            AccessibleObject ao = ep.getAccessibleObject();
            if ("cparameter".equals(value))
               assertInstanceOf(ao, Constructor.class, false);
            else if ("mparameter".equals(value))
               assertInstanceOf(ao, Method.class, false);
            else
               fail("Illegal annotation value: " + value);
         }
      }
      finally
      {
         assertUndeploy(deployer, deployment);
      }
   }

   @SuppressWarnings("unchecked")
   public void testSimpleClassUsage() throws Exception
   {
      DeployerClient deployer = getMainDeployer();

      Deployment deployment = createSimpleDeployment("a");
      addClassLoadingMetaData(
            deployment,
            deployment.getName(),
            null,
            ClassLoaderUtils.classNameToPath("org.jboss.test.deployers.annotations.support.AnnotationsHolder"),
            ClassLoaderUtils.classNameToPath("org.jboss.test.deployers.annotations.support.TestAnnotation")
      );

      DeploymentUnit unit = assertDeploy(deployer, deployment);
      try
      {
         String annotationName = "org.jboss.test.deployers.annotations.support.TestAnnotation";

         assertNotLoaded(unit, "org.jboss.test.deployers.annotations.support.AnnotationsHolder");
         // annotations are loaded, OK?
         assertLoaded(unit, "org.jboss.test.deployers.annotations.support.TestAnnotation");

         AnnotationEnvironment env = getAnnotationEnvironment(unit);
         Set<Class<?>> classes = env.classIsAnnotatedWith(annotationName);
         assertNotNull(classes);
         assertEquals(1, classes.size());
         assertEquals(AnnotationsHolder.class.getName(), classes.iterator().next().getName());

         assertLoaded(unit, "org.jboss.test.deployers.annotations.support.AnnotationsHolder");

         Element<Annotation, Constructor> ec = getSingleton(env.classHasConstructorAnnotatedWith(annotationName));
         Annotation ta = ec.getAnnotation();
         assertNotNull(ta);
         assertEquals("constructor", getValue(ta));
         assertInstanceOf(ec.getAccessibleObject(), Constructor.class, false);

         Element<Annotation, Field> ef = getSingleton(env.classHasFieldAnnotatedWith(annotationName));
         ta = ef.getAnnotation();
         assertNotNull(ta);
         assertEquals("field", getValue(ta));
         assertInstanceOf(ef.getAccessibleObject(), Field.class, false);

         Element<Annotation, Method> em = getSingleton(env.classHasMethodAnnotatedWith(annotationName));
         ta = em.getAnnotation();
         assertNotNull(ta);
         assertEquals("method", getValue(ta));
         assertInstanceOf(em.getAccessibleObject(), Method.class, false);

         Set<Element<Annotation, AccessibleObject>> eps = env.classHasParameterAnnotatedWith(annotationName);
         assertNotNull(eps);
         assertEquals(2, eps.size());
         for (Element<Annotation, AccessibleObject> ep : eps)
         {
            ta = ep.getAnnotation();
            assertNotNull(ta);
            Object value = getValue(ta);
            AccessibleObject ao = ep.getAccessibleObject();
            if ("cparameter".equals(value))
               assertInstanceOf(ao, Constructor.class, false);
            else if ("mparameter".equals(value))
               assertInstanceOf(ao, Method.class, false);
            else
               fail("Illegal annotation value: " + value);
         }
      }
      finally
      {
         assertUndeploy(deployer, deployment);
      }
   }
}
