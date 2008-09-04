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
import java.lang.ref.SoftReference;
import java.lang.reflect.AnnotatedElement;

import org.jboss.deployers.spi.annotations.Element;

/**
 * Abstract annotations element.
 *
 * @param <A> the annotation type
 * @param <M> the annotated element type
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public abstract class AbstractElement<A extends Annotation, M extends AnnotatedElement> extends WeakClassLoaderHolder implements Element<A, M>
{
   protected String className;
   protected Class<A> annClass;
   private A annotation;

   private SoftReference<Class<?>> classRef;

   public AbstractElement(ClassLoader classLoader, String className, Class<A> annClass, A annotation)
   {
      super(classLoader);

      if (className == null)
         throw new IllegalArgumentException("Null className");
      if (annClass == null)
         throw new IllegalArgumentException("Null annotation class");

      this.className = className;
      this.annClass = annClass;
      this.annotation = annotation;
   }

   public String getOwnerClassName()
   {
      return className;
   }

   public Class<?> getOwner()
   {
      if (classRef != null)
      {
         Class<?> clazz = classRef.get();
         if (clazz != null)
            return clazz;
      }

      Class<?> clazz = loadClass(className);
      classRef = new SoftReference<Class<?>>(clazz);
      return clazz;
   }

   public A getAnnotation()
   {
      if (annotation == null)
         annotation = readAnnotation();

      return annotation;
   }

   /**
    * Read the annotation.
    *
    * @return the read annotation
    */
   protected A readAnnotation()
   {
      AnnotatedElement annotatedElement = getAnnotatedElement();
      return annotatedElement.getAnnotation(annClass);
   }

   public int getHashCode()
   {
      int hash = className.hashCode();
      hash += 7 * annClass.hashCode();
      if (annotation != null)
         hash += 11 * annotation.hashCode();
      return hash;
   }

   public boolean equals(Object obj)
   {
      if (obj == null || getClass().equals(obj.getClass()) == false)
         return false;

      AbstractElement ae = AbstractElement.class.cast(obj);
      if (className.equals(ae.className) == false)
         return false;
      if (annClass.equals(ae.annClass) == false)
         return false;

      // we don't check annotation
      // since I doubt classname + annClass + signature + aoClass is not enough
      // the only way this could happen is probably if class was diff version - diff annotation values

      return true;
   }
}