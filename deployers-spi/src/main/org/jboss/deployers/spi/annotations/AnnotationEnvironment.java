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
package org.jboss.deployers.spi.annotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * Information holder about annotation processing.
 *
 * Implementations should delay the actual class loading
 * until it's absolutely necessary.
 *
 * All methods that have annotation name as parameter
 * will use unit's classloader to load the actual annotation class.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public interface AnnotationEnvironment
{
   /**
    * Does this annotation environment contain a class
    * which is annotated with annotation parameter.
    * This only applies to annotations for ElementType.TYPE level.
    *
    * This method should be used if we have no intention
    * to do real lookup of annotated classes, but we're
    * only interested in existance of the annotation.
    * e.g. deployment unit contains @Stateful EJBs
    *
    * @param annotation the annotation we're querying for
    * @return true if there exists a class with annotation param
    * @see #hasClassAnnotatedWith(Class annotation)
    */
   boolean hasClassAnnotatedWith(Class<? extends Annotation> annotation);

   /**
    * Does this annotation environment contain a class
    * which is annotated with annotation parameter.
    * This only applies to annotations for ElementType.TYPE level.
    *
    * This method should be used if we have no intention
    * to do real lookup of annotated classes, but we're
    * only interested in existance of the annotation.
    * e.g. deployment unit contains @Stateful EJBs
    *
    * @param annotationName the annotation name we're querying for
    * @return true if there exists a class with annotation param
    * @see #hasClassAnnotatedWith(Class annotation)
    */
   boolean hasClassAnnotatedWith(String annotationName);

   /**
    * Get all classes annotated with annotation param.
    *
    * @param <A> the annotation type
    * @param annotation the annotation we're querying for
    * @return set of matching classes
    */
   <A extends Annotation> Set<Element<A, Class<?>>> classIsAnnotatedWith(Class<A> annotation);

   /**
    * Get all classes annotated with annotation param.
    *
    * @param annotationName the annotation name we're querying for
    * @return set of matching classes
    */
   Set<Element<Annotation, Class<?>>> classIsAnnotatedWith(String annotationName);

   /**
    * Get all classes who have some constructor annotated with annotation param.
    *
    * @param <A> the annotation type
    * @param annotation the annotation we're querying for
    * @return set of matching classes
    */
   <A extends Annotation> Set<Element<A, Constructor<?>>> classHasConstructorAnnotatedWith(Class<A> annotation);

   /**
    * Get all classes who have some constructor annotated with annotation param.
    *
    * @param annotationName the annotation name we're querying for
    * @return set of matching classes
    */
   Set<Element<Annotation, Constructor<?>>> classHasConstructorAnnotatedWith(String annotationName);

   /**
    * Get all classes who have some field annotated with annotation param.
    *
    * @param <A> the annotation type
    * @param annotation the annotation we're querying for
    * @return set of matching classes
    */
   <A extends Annotation> Set<Element<A, Field>> classHasFieldAnnotatedWith(Class<A> annotation);

   /**
    * Get all classes who have some field annotated with annotation param.
    *
    * @param annotationName the annotation name we're querying for
    * @return set of matching classes
    */
   Set<Element<Annotation, Field>> classHasFieldAnnotatedWith(String annotationName);

   /**
    * Get all classes who have some method annotated with annotation param.
    *
    * @param <A> the annotation type
    * @param annotation the annotation we're querying for
    * @return set of matching classes
    */
   <A extends Annotation> Set<Element<A, Method>> classHasMethodAnnotatedWith(Class<A> annotation);

   /**
    * Get all classes who have some method annotated with annotation param.
    *
    * @param annotationName the annotation name we're querying for
    * @return set of matching classes
    */
   Set<Element<Annotation, Method>> classHasMethodAnnotatedWith(String annotationName);

   /**
    * Get all classes who have some method's/constructor's parameter annotated with annotation param.
    *
    * @param <A> the annotation type
    * @param annotation the annotation we're querying for
    * @return set of matching classes
    */
   <A extends Annotation> Set<Element<A, AnnotatedElement>> classHasParameterAnnotatedWith(Class<A> annotation);

   /**
    * Get all classes who have some method's/constructor's parameter annotated with annotation param.
    *
    * @param annotationName the annotation name we're querying for
    * @return set of matching classes
    */
   Set<Element<Annotation, AnnotatedElement>> classHasParameterAnnotatedWith(String annotationName);
}
