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

import org.jboss.metadata.spi.signature.Signature;

/**
 * Gathering annotation information.
 *
 * Only push all this info into AnnotationEnvironment if
 * complete lookup was successful.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
class CommitElement
{
   private Annotation annotation;
   private ElementType type;
   private String className;
   private Signature signature;

   CommitElement(Annotation annotation, ElementType type, String className, Signature signature)
   {
      this.annotation = annotation;
      this.type = type;
      this.className = className;
      this.signature = signature;
   }

   /**
    * Get the annotation.
    *
    * @return the annotation
    */
   public Annotation getAnnotation()
   {
      return annotation;
   }

   /**
    * Get element type.
    *
    * @return the element type
    */
   public ElementType getType()
   {
      return type;
   }

   /**
    * Get class name.
    *
    * @return the class name
    */
   public String getClassName()
   {
      return className;
   }

   /**
    * Get signature.
    *
    * @return the signature
    */
   public Signature getSignature()
   {
      return signature;
   }
}