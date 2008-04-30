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
import java.lang.reflect.AccessibleObject;

import org.jboss.deployers.spi.annotations.Element;
import org.jboss.metadata.plugins.loader.reflection.AnnotatedElementMetaDataLoader;
import org.jboss.metadata.spi.loader.MetaDataLoader;
import org.jboss.metadata.spi.retrieval.AnnotationItem;
import org.jboss.metadata.spi.retrieval.MetaDataRetrieval;
import org.jboss.metadata.spi.signature.ConstructorSignature;
import org.jboss.metadata.spi.signature.FieldSignature;
import org.jboss.metadata.spi.signature.MethodSignature;
import org.jboss.metadata.spi.signature.Signature;
import org.jboss.reflect.plugins.introspection.ReflectionUtils;

/**
 * Default annoattions element.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class DefaultElement<A extends Annotation, M extends AccessibleObject> extends WeakClassLoaderHolder implements Element<A, M>
{
   private String className;
   private Signature signature;
   private Class<A> annClass;
   private Class<M> aoClass;

   private SoftReference<Class<?>> classRef;

   public DefaultElement(ClassLoader classLoader, String className, Signature signature, Class<A> annClass, Class<M> aoClass)
   {
      super(classLoader);

      if (className == null)
         throw new IllegalArgumentException("Null className");
      if (signature == null)
         throw new IllegalArgumentException("Null signature");
      if (annClass == null)
         throw new IllegalArgumentException("Null annotation class");
      if (aoClass == null)
         throw new IllegalArgumentException("Null ao class");

      this.className = className;
      this.signature = signature;
      this.annClass = annClass;
      this.aoClass = aoClass;
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

   /**
    * Get meta data retireval for signature on owner.
    *
    * @return meta data retrieval
    */
   protected MetaDataRetrieval getMetaDataRetrieval()
   {
      MetaDataLoader loader = new AnnotatedElementMetaDataLoader(getOwner());
      MetaDataRetrieval retrieval = loader.getComponentMetaDataRetrieval(signature);
      if (retrieval == null)
         throw new IllegalArgumentException("No such signature " + signature + " on loader: " + loader);

      return retrieval;
   }

   public A getAnnotation()
   {
      AnnotationItem<A> item = getMetaDataRetrieval().retrieveAnnotation(annClass);
      if (item == null)
         throw new IllegalArgumentException("Expecting annotation: " + annClass);

      return item.getAnnotation();
   }

   public M getAccessibleObject()
   {
      AccessibleObject result = null;

      Class<?> clazz = getOwner();
      if (signature instanceof ConstructorSignature)
      {
         try
         {
            result = clazz.getConstructor(signature.getParametersTypes(clazz));
         }
         catch (NoSuchMethodException ignored)
         {
         }
      }
      else if (signature instanceof MethodSignature)
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
}
