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
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.Collections;
import java.util.HashSet;

import org.jboss.deployers.spi.annotations.AnnotationEnvironment;
import org.jboss.util.collection.CollectionsFactory;

/**
 * DefaultAnnotationEnvironment.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class DefaultAnnotationEnvironment implements AnnotationEnvironment
{
   private WeakReference<ClassLoader> clRef;
   private Map<Class<? extends Annotation>, Map<ElementType, Set<String>>> env;

   public DefaultAnnotationEnvironment(ClassLoader classLoader)
   {
      if (classLoader == null)
         throw new IllegalArgumentException("Null classloader");

      this.clRef = new WeakReference<ClassLoader>(classLoader);
      this.env = new HashMap<Class<? extends Annotation>, Map<ElementType, Set<String>>>();
   }

   /**
    * Put the annotation info.
    *
    * @param annClass the annotation class
    * @param type the annotation type
    * @param className the class name
    */
   void putAnnotation(Class<? extends Annotation> annClass, ElementType type, String className)
   {
      Map<ElementType, Set<String>> elements = env.get(annClass);
      if (elements == null)
      {
         elements = new HashMap<ElementType, Set<String>>();
         env.put(annClass, elements);
      }
      Set<String> classes = elements.get(type);
      if (classes == null)
      {
         classes = CollectionsFactory.createLazySet();
         elements.put(type, classes);
      }
      classes.add(className);
   }

   /**
    * Get matching class names.
    *
    * @param annClass the annotation class
    * @param type the annotation type
    * @return class names
    */
   protected Set<String> getClassNames(Class<? extends Annotation> annClass, ElementType type)
   {
      Set<String> classNames = null;

      Map<ElementType, Set<String>> elements = env.get(annClass);
      if (elements != null)
         classNames = elements.get(type);

      return (classNames != null) ? classNames : Collections.<String>emptySet();
   }

   /**
    * Transform class names into classes.
    *
    * @param classNames the class names
    * @return classes
    */
   protected Set<Class<?>> transform(Set<String> classNames)
   {
      ClassLoader classLoader = clRef.get();
      if (classLoader == null)
         throw new IllegalArgumentException("ClassLoader was already garbage collected.");

      try
      {
         Set<Class<?>> classes = new HashSet<Class<?>>(classNames.size());
         for (String className : classNames)
            classes.add(classLoader.loadClass(className));
         return classes;
      }
      catch (ClassNotFoundException e)
      {
         throw new RuntimeException(e);
      }
   }

   public Set<Class<?>> classIsAnnotatedWith(Class<? extends Annotation> annotation)
   {
      return transform(getClassNames(annotation, ElementType.TYPE));
   }

   public Set<Class<?>> classHasConstructorAnnotatedWith(Class<? extends Annotation> annotation)
   {
      return transform(getClassNames(annotation, ElementType.CONSTRUCTOR));
   }

   public Set<Class<?>> classHasFieldAnnotatedWith(Class<? extends Annotation> annotation)
   {
      return transform(getClassNames(annotation, ElementType.FIELD));
   }

   public Set<Class<?>> classHasMethodAnnotatedWith(Class<? extends Annotation> annotation)
   {
      return transform(getClassNames(annotation, ElementType.METHOD));
   }

   public Set<Class<?>> classHasParameterAnnotatedWith(Class<? extends Annotation> annotation)
   {
      return transform(getClassNames(annotation, ElementType.PARAMETER)); 
   }
}
