/*
* JBoss, Home of Professional Open Source
* Copyright 2007, JBoss Inc., and individual contributors as indicated
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
package org.jboss.test.deployers.scope.support;

import java.lang.annotation.Annotation;

import org.jboss.metadata.plugins.loader.AbstractMetaDataLoader;
import org.jboss.metadata.spi.retrieval.AnnotationItem;
import org.jboss.metadata.spi.retrieval.AnnotationsItem;
import org.jboss.metadata.spi.retrieval.MetaDataItem;
import org.jboss.metadata.spi.retrieval.MetaDataRetrieval;
import org.jboss.metadata.spi.retrieval.simple.SimpleAnnotationItem;
import org.jboss.metadata.spi.retrieval.simple.SimpleAnnotationsItem;
import org.jboss.metadata.spi.retrieval.simple.SimpleMetaDataItem;
import org.jboss.metadata.spi.scope.ScopeKey;
import org.jboss.metadata.spi.signature.Signature;

/**
 * TestComponentMetaDataLoader.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class TestComponentMetaDataLoader extends AbstractMetaDataLoader
{
   TestComponentMetaData componentMetaData;
   
   public TestComponentMetaDataLoader(ScopeKey scopeKey, TestComponentMetaData componentMetaData)
   {
      super(scopeKey);
      this.componentMetaData = componentMetaData;
   }
   
   public MetaDataRetrieval getComponentMetaDataRetrieval(Signature signature)
   {
      // TODO method/field annotations go here
      return null;
   }

   public boolean isEmpty()
   {
      return componentMetaData.classAnnotations.isEmpty();
   }

   public <T extends Annotation> AnnotationItem<T> retrieveAnnotation(Class<T> annotationType)
   {
      for (Annotation annotation : componentMetaData.classAnnotations)
      {
         if (annotation.annotationType().equals(annotationType))
            return new SimpleAnnotationItem<T>(annotationType.cast(annotation));
      }
      return null;
   }

   @SuppressWarnings("unchecked")
   public AnnotationsItem retrieveAnnotations()
   {
      if (componentMetaData.classAnnotations.isEmpty())
         return SimpleAnnotationsItem.NO_ANNOTATIONS;
      
      Annotation[] annotations = componentMetaData.classAnnotations.toArray(new Annotation[0]);
      AnnotationItem[] items = new AnnotationItem[annotations.length];
      for (int i = 0; i < items.length; ++i)
         items[i] = new SimpleAnnotationItem(annotations[i]);
      return new SimpleAnnotationsItem(items);
   }

   @SuppressWarnings("unchecked")
   public MetaDataItem retrieveMetaData(String name)
   {
      for (Annotation annotation : componentMetaData.classAnnotations)
      {
         if (annotation.annotationType().getName().equals(name))
            return new SimpleMetaDataItem(name, annotation);
      }
      return null;
   }
}
