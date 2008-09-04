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

import org.jboss.metadata.spi.signature.Signature;
import org.jboss.util.JBossObject;

/**
 * Class name and signature pair.
 * With those two we can re-create annotation value.
 *
 * If the keepAnnotations flag is on in DefaultAnnotationEnvironment
 * we cache the annotation value from GenericAnnotationResourceVisitor.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class ClassSignaturePair extends JBossObject
{
   private String className;
   private Signature signature;
   private Annotation annotation;

   public ClassSignaturePair(String className, Signature signature)
   {
      this(className, signature, null);
   }

   public ClassSignaturePair(String className, Signature signature, Annotation annotation)
   {
      if (className == null)
         throw new IllegalArgumentException("Null class name");

      this.className = className;
      this.signature = signature;
      this.annotation = annotation;
   }

   /**
    * Get the classname.
    *
    * @return the classname
    */
   public String getClassName()
   {
      return className;
   }

   /**
    * Get the signature.
    *
    * @return the signature
    */
   public Signature getSignature()
   {
      return signature;
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

   protected int getHashCode()
   {
      int hash = className.hashCode();
      if (signature != null)
         hash += 7 * signature.hashCode();
      return hash;
   }

   public boolean equals(Object obj)
   {
      if (obj instanceof ClassSignaturePair == false)
         return false;

      ClassSignaturePair csPair = (ClassSignaturePair)obj;
      if (className.equals(csPair.getClassName()))
         return equals(signature, csPair.getSignature());
      else
         return false;
   }
}
