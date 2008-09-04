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
package org.jboss.deployers.spi.deployer.helpers;

import java.lang.annotation.Annotation;

/**
 * Annotation processor.
 *
 * @param <A> the annotation type
 * @param <T> the output type
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 */
public interface AnnotationProcessor<A extends Annotation, T>
{
   /**
    * Get the annotation class.
    *
    * @return the annotation class
    */
   Class<A> getAnnotation();

   /**
    * Get output class.
    *
    * @return the output class
    */
   Class<T> getOutput();

   /**
    * Create metadata attachment.
    *
    * @param attachment the previous attachment
    * @return the new metadata instance or null if cannot be created
    */
   T createMetaData(Object attachment);

   /**
    * Create metadata from class.
    *
    * @param clazz the class containing annotation
    * @return the new metadata from class or null if cannot be created
    */
   T createMetaDataFromClass(Class<?> clazz);
}