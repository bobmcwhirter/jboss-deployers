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
import java.util.Set;

/**
 * Information holder about annotation processing.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public interface AnnotationEnvironment
{
   /**
    * Get all classes annotated with annotation param.
    *
    * @param annotation the annotation we're querying for
    * @return set of matching classes
    */
   Set<Class<?>> classIsAnnotatedWith(Class<? extends Annotation> annotation);

   /**
    * Get all classes who have some constructor annotated with annotation param.
    *
    * @param annotation the annotation we're querying for
    * @return set of matching classes
    */
   Set<Class<?>> classHasConstructorAnnotatedWith(Class<? extends Annotation> annotation);

   /**
    * Get all classes who have some field annotated with annotation param.
    *
    * @param annotation the annotation we're querying for
    * @return set of matching classes
    */
   Set<Class<?>> classHasFieldAnnotatedWith(Class<? extends Annotation> annotation);

   /**
    * Get all classes who have some method annotated with annotation param.
    *
    * @param annotation the annotation we're querying for
    * @return set of matching classes
    */
   Set<Class<?>> classHasMethodAnnotatedWith(Class<? extends Annotation> annotation);      

   /**
    * Get all classes who have some method's/constructor's parameter annotated with annotation param.
    *
    * @param annotation the annotation we're querying for
    * @return set of matching classes
    */
   Set<Class<?>> classHasParameterAnnotatedWith(Class<? extends Annotation> annotation);
}
