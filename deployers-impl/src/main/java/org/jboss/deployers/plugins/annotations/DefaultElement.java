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
import java.lang.reflect.AnnotatedElement;

import org.jboss.metadata.spi.signature.ConstructorParametersSignature;
import org.jboss.metadata.spi.signature.ConstructorSignature;
import org.jboss.metadata.spi.signature.FieldSignature;
import org.jboss.metadata.spi.signature.MethodParametersSignature;
import org.jboss.metadata.spi.signature.MethodSignature;
import org.jboss.metadata.spi.signature.Signature;
import org.jboss.reflect.plugins.introspection.ReflectionUtils;

/**
 * Default annotations element.
 *
 * @param <A> the annotation type
 * @param <M> the annotated element type
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class DefaultElement<A extends Annotation, M extends AnnotatedElement> extends AbstractElement<A, M>
{
   protected Signature signature;
   protected Class<M> aoClass;

   public DefaultElement(ClassLoader classLoader, String className, Signature signature, Class<A> annClass, A annotation, Class<M> aoClass)
   {
      super(classLoader, className, annClass, annotation);

      if (signature == null)
         throw new IllegalArgumentException("Null signature");
      if (aoClass == null)
         throw new IllegalArgumentException("Null ao class");

      this.signature = signature;
      this.aoClass = aoClass;
   }

   public M getAnnotatedElement()
   {
      AnnotatedElement result = null;

      Class<?> clazz = getOwner();
      if (signature instanceof ConstructorSignature || signature instanceof ConstructorParametersSignature)
      {
         try
         {
            result = clazz.getConstructor(signature.getParametersTypes(clazz));
         }
         catch (NoSuchMethodException ignored)
         {
         }
      }
      else if (signature instanceof MethodSignature || signature instanceof MethodParametersSignature)
      {
         try
         {
            result = clazz.getMethod(signature.getName(), signature.getParametersTypes(clazz));
         }
         catch (NoSuchMethodException ignored)
         {
         }
      }
      else if (signature instanceof FieldSignature)
      {
         result = ReflectionUtils.findField(clazz, signature.getName());
      }

      if (result == null)
         throw new IllegalArgumentException("Expected accessible object " + className + "." + signature);
      if (aoClass.isInstance(result) == false)
         throw new IllegalArgumentException("Expected accessible object " + className + "." + signature + " of type " + aoClass);

      return aoClass.cast(result);
   }

   public int getHashCode()
   {
      int hash = super.getHashCode();
      hash += 19 * signature.hashCode();
      hash += 37 * aoClass.hashCode();
      return hash;
   }

   @SuppressWarnings({"EqualsWhichDoesntCheckParameterClass"})
   public boolean equals(Object obj)
   {
      if (super.equals(obj) == false)
         return false;

      DefaultElement de = DefaultElement.class.cast(obj);
      if (aoClass.equals(de.aoClass) == false)
         return false;
      if (signature.equals(de.signature) == false)
         return false;

      return true;
   }
}
