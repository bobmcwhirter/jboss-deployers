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
package org.jboss.deployers.plugins.annotations;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jboss.deployers.spi.annotations.AnnotationEnvironment;
import org.jboss.deployers.spi.annotations.Element;
import org.jboss.metadata.spi.signature.Signature;
import org.jboss.util.collection.CollectionsFactory;

/**
 * DefaultAnnotationEnvironment.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class DefaultAnnotationEnvironment extends WeakClassLoaderHolder implements AnnotationEnvironment
{
   private Map<Class<? extends Annotation>, Map<ElementType, Set<ClassSignaturePair>>> env;

   public DefaultAnnotationEnvironment(ClassLoader classLoader)
   {
      super(classLoader);
      this.env = new HashMap<Class<? extends Annotation>, Map<ElementType, Set<ClassSignaturePair>>>();
   }

   /**
    * Put the annotation info.
    *
    * @param annClass the annotation class
    * @param type the annotation type
    * @param className the class name
    * @param signature the signature
    */
   void putAnnotation(Class<? extends Annotation> annClass, ElementType type, String className, Signature signature)
   {
      Map<ElementType, Set<ClassSignaturePair>> elements = env.get(annClass);
      if (elements == null)
      {
         elements = new HashMap<ElementType, Set<ClassSignaturePair>>();
         env.put(annClass, elements);
      }
      Set<ClassSignaturePair> classes = elements.get(type);
      if (classes == null)
      {
         classes = CollectionsFactory.createLazySet();
         elements.put(type, classes);
      }
      classes.add(new ClassSignaturePair(className, signature));
   }

   /**
    * Get matching cs pairs.
    *
    * @param annClass the annotation class
    * @param type the annotation type
    * @return class names
    */
   protected Set<ClassSignaturePair> getCSPairs(Class<? extends Annotation> annClass, ElementType type)
   {
      Set<ClassSignaturePair> pairs = null;

      Map<ElementType, Set<ClassSignaturePair>> elements = env.get(annClass);
      if (elements != null)
         pairs = elements.get(type);

      return (pairs != null) ? pairs : Collections.<ClassSignaturePair>emptySet();
   }

   /**
    * Transform class names into classes.
    *
    * @param pairs the cs pairs
    * @return classes
    */
   protected Set<Class<?>> transformToClasses(Set<ClassSignaturePair> pairs)
   {
      Set<Class<?>> classes = new HashSet<Class<?>>(pairs.size());
      for (ClassSignaturePair pair : pairs)
         classes.add(loadClass(pair.getClassName()));
      return classes;
   }

   /**
    * Transform class names into classes.
    *
    * @param type the annotation type
    * @param annClass the annotation class
    * @param expectedAccessibleObjectClass the ao class
    * @return classes
    */
   protected <A extends Annotation, M extends AccessibleObject> Set<Element<A, M>> transformToElements(
         ElementType type,
         Class<A> annClass,
         Class<M> expectedAccessibleObjectClass
   )
   {
      ClassLoader classLoader = getClassLoader();
      Set<ClassSignaturePair> pairs = getCSPairs(annClass, type);
      Set<Element<A, M>> elements = new HashSet<Element<A, M>>();
      for (ClassSignaturePair pair : pairs)
         elements.add(toElement(classLoader, pair, annClass, expectedAccessibleObjectClass));
      return elements;
   }

   /**
    * Transform cs pair to element.
    *
    * @param classLoader the class loader
    * @param pair the cs pair
    * @param annClass the annotation class
    * @param aoClass the ao class
    * @return element
    */
   protected <A extends Annotation, M extends AccessibleObject> Element<A, M> toElement(
         ClassLoader classLoader,
         ClassSignaturePair pair,
         Class<A> annClass,
         Class<M> aoClass)
   {
      return null;
   }


   public Set<Class<?>> classIsAnnotatedWith(Class<? extends Annotation> annotation)
   {
      return transformToClasses(getCSPairs(annotation, ElementType.TYPE));
   }

   public <A extends Annotation> Set<Element<A, Constructor>> classHasConstructorAnnotatedWith(Class<A> annotation)
   {
      return transformToElements(ElementType.CONSTRUCTOR, annotation, Constructor.class);
   }

   public <A extends Annotation> Set<Element<A, Field>> classHasFieldAnnotatedWith(Class<A> annotation)
   {
      return transformToElements(ElementType.FIELD, annotation, Field.class);
   }

   public <A extends Annotation> Set<Element<A, Method>> classHasMethodAnnotatedWith(Class<A> annotation)
   {
      return transformToElements(ElementType.METHOD, annotation, Method.class);
   }

   public <A extends Annotation> Set<Element<A, AccessibleObject>> classHasParameterAnnotatedWith(Class<A> annotation)
   {
      return transformToElements(ElementType.CONSTRUCTOR, annotation, AccessibleObject.class);
   }
}
