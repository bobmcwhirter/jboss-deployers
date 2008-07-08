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

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.AnnotatedElement;
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
import org.jboss.logging.Logger;
import org.jboss.metadata.spi.signature.Signature;
import org.jboss.util.collection.CollectionsFactory;

/**
 * DefaultAnnotationEnvironment.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class DefaultAnnotationEnvironment extends WeakClassLoaderHolder implements AnnotationEnvironment, Serializable
{
   /** The serial version UID */
   private static final long serialVersionUID = 1L;
   /** The log */
   private static final Logger log = Logger.getLogger(DefaultAnnotationEnvironment.class);
   /** The info map */
   private transient Map<Class<? extends Annotation>, Map<ElementType, Set<ClassSignaturePair>>> env;
   /** Should we keep the annotation */
   private boolean keepAnnotations;

   public DefaultAnnotationEnvironment(ClassLoader classLoader)
   {
      super(classLoader);
      env = new HashMap<Class<? extends Annotation>, Map<ElementType, Set<ClassSignaturePair>>>();
   }

   /**
    * Set the keep annotations flag.
    *
    * @param keepAnnotations the keep annotations flag
    */
   public void setKeepAnnotations(boolean keepAnnotations)
   {
      this.keepAnnotations = keepAnnotations;
   }

   /**
    * Get env map.
    *
    * @return the env map
    */
   protected Map<Class<? extends Annotation>, Map<ElementType, Set<ClassSignaturePair>>> getEnv()
   {
      if (env == null)
         throw new IllegalArgumentException("Null env, previously serialized?");

      return env;
   }

   /**
    * Put the annotation info.
    *
    * @param annotation the annotation
    * @param type the annotation type
    * @param className the class name
    * @param signature the signature
    */
   void putAnnotation(Annotation annotation, ElementType type, String className, Signature signature)
   {
      Class<? extends Annotation> annClass = annotation.annotationType();

      if (log.isTraceEnabled())
         log.trace("Adding annotation @" + annClass.getSimpleName() + " for " + className + " at type " + type + ", signature: " + signature);

      Map<Class<? extends Annotation>, Map<ElementType, Set<ClassSignaturePair>>> env = getEnv();

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

      ClassSignaturePair pair;
      if (keepAnnotations)
         pair = new ClassSignaturePair(className, signature, annotation);
      else
         pair = new ClassSignaturePair(className, signature);
      classes.add(pair);
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

      Map<ElementType, Set<ClassSignaturePair>> elements = getEnv().get(annClass);
      if (elements != null)
         pairs = elements.get(type);

      return (pairs != null) ? pairs : Collections.<ClassSignaturePair>emptySet();
   }

   /**
    * Transform class names into classes.
    *
    * @param type the annotation type
    * @param annClass the annotation class
    * @param aoClass the ao class
    * @return classes
    */
   protected <A extends Annotation, M extends AnnotatedElement> Set<Element<A, M>> transformToElements(
         ElementType type,
         Class<A> annClass,
         Class<M> aoClass
   )
   {
      Set<ClassSignaturePair> pairs = getCSPairs(annClass, type);
      if (pairs.isEmpty())
         return Collections.emptySet();

      ClassLoader classLoader = getClassLoader();
      Set<Element<A, M>> elements = new HashSet<Element<A, M>>();
      for (ClassSignaturePair pair : pairs)
      {
         String className = pair.getClassName();
         A annotation = annClass.cast(pair.getAnnotation());

         Element<A, M> element;
         if (type == ElementType.TYPE)
            element = new ClassElement<A, M>(classLoader, className, annClass, annotation);
         else if (type == ElementType.PARAMETER)
            element = new ParametersElement<A,M>(classLoader, className, pair.getSignature(), annClass, annotation, aoClass);
         else
            element = new DefaultElement<A,M>(classLoader, className, pair.getSignature(), annClass, annotation, aoClass);
         elements.add(element);
      }
      return elements;
   }

   public boolean hasClassAnnotatedWith(Class<? extends Annotation> annotation)
   {
      return getCSPairs(annotation, ElementType.TYPE).isEmpty() == false;
   }

   public <A extends Annotation> Set<Element<A, Class>> classIsAnnotatedWith(Class<A> annotation)
   {
      return transformToElements(ElementType.TYPE, annotation, Class.class);
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

   public <A extends Annotation> Set<Element<A, AnnotatedElement>> classHasParameterAnnotatedWith(Class<A> annotation)
   {
      return transformToElements(ElementType.PARAMETER, annotation, AnnotatedElement.class);
   }

   /**
    * Load the annotation class.
    *
    * @param annotationName the annoation class name
    * @return annotation class
    */
   @SuppressWarnings("unchecked")
   protected Class<Annotation> getAnnotationClass(String annotationName)
   {
      Class<?> clazz = loadClass(annotationName);
      if (Annotation.class.isAssignableFrom(clazz) == false)
         throw new IllegalArgumentException("Annotation name " + annotationName + " doesn't extend Annotation class.");
      return (Class<Annotation>)clazz;
   }

   public boolean hasClassAnnotatedWith(String annotationName)
   {
      return hasClassAnnotatedWith(getAnnotationClass(annotationName));
   }

   public Set<Element<Annotation, Class>> classIsAnnotatedWith(String annotationName)
   {
      return classIsAnnotatedWith(getAnnotationClass(annotationName));
   }

   public Set<Element<Annotation, Constructor>> classHasConstructorAnnotatedWith(String annotationName)
   {
      return classHasConstructorAnnotatedWith(getAnnotationClass(annotationName));
   }

   public Set<Element<Annotation, Field>> classHasFieldAnnotatedWith(String annotationName)
   {
      return classHasFieldAnnotatedWith(getAnnotationClass(annotationName));
   }

   public Set<Element<Annotation, Method>> classHasMethodAnnotatedWith(String annotationName)
   {
      return classHasMethodAnnotatedWith(getAnnotationClass(annotationName));
   }

   public Set<Element<Annotation, AnnotatedElement>> classHasParameterAnnotatedWith(String annotationName)
   {
      return classHasParameterAnnotatedWith(getAnnotationClass(annotationName));
   }
}
