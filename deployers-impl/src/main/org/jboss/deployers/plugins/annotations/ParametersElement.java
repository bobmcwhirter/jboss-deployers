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
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.jboss.metadata.spi.signature.ConstructorParametersSignature;
import org.jboss.metadata.spi.signature.MethodParametersSignature;
import org.jboss.metadata.spi.signature.Signature;

/**
 * Parameters annotations element.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class ParametersElement<A extends Annotation, M extends AnnotatedElement> extends DefaultElement<A, M>
{
   public ParametersElement(ClassLoader classLoader, String className, Signature signature, Class<A> annClass, A annotation, Class<M> aoClass)
   {
      super(classLoader, className, signature, annClass, annotation, aoClass);
   }

   protected A readAnnotation()
   {
      Annotation[] annotations = null;
      Class<?> clazz = getOwner();
      if (signature instanceof ConstructorParametersSignature)
      {
         ConstructorParametersSignature cps = (ConstructorParametersSignature)signature;
         try
         {
            Constructor constructor = clazz.getConstructor(signature.getParametersTypes(clazz));
            annotations = constructor.getParameterAnnotations()[cps.getParam()];
         }
         catch (NoSuchMethodException ignored)
         {
         }
      }
      else if (signature instanceof MethodParametersSignature)
      {
         MethodParametersSignature mps = (MethodParametersSignature)signature;
         try
         {
            Method method = clazz.getMethod(signature.getName(), signature.getParametersTypes(clazz));
            annotations = method.getParameterAnnotations()[mps.getParam()];
         }
         catch (NoSuchMethodException ignored)
         {
         }
      }

      if (annotations == null || annotations.length == 0)
         throw new IllegalArgumentException("Expected annotations " + className + "." + signature);

      for(Annotation annotation : annotations)
      {
         if (annClass.equals(annotation.annotationType()))
            return annClass.cast(annotation);
      }

      throw new IllegalArgumentException("No matching annotation: " + Arrays.asList(annotations));
   }
}