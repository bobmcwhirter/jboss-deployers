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

/**
 * Annotation holder element.
 *
 * @param <A> exact annotation type
 * @param <M> exact annotated element type
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public interface Element<A extends Annotation, M extends AnnotatedElement>
{
   /**
    * Get the owner class name.
    *
    * Until we hit getOwner method the class should not be loaded.
    *
    * @return the owner classname
    */
   String getOwnerClassName();

   /**
    * Get the annotation owner class.
    *
    * @return the annotation owner class
    */
   Class<?> getOwner();

   /**
    * Get the annotation instance.
    *
    * @return the annotation instance
    */
   A getAnnotation();

   /**
    * Get the annotated element that holds the annotation.
    *
    * @return the annotated element instance
    */
   M getAnnotatedElement();
}