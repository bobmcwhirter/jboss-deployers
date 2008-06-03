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

import java.util.Set;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.annotations.AnnotationEnvironment;
import org.jboss.deployers.structure.spi.DeploymentUnit;

/**
 * AbstractComponentDeployer.
 *
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 */
public class AbstractAnnotationDeployer extends AbstractSimpleRealDeployer<AnnotationEnvironment>
{
   /** The annotation processors */
   private AnnotationProcessor[] processors;

   public AbstractAnnotationDeployer(AnnotationProcessor... processors)
   {
      super(AnnotationEnvironment.class);
      if (processors != null && processors.length > 0)
      {
         this.processors = processors;
         for (AnnotationProcessor processor : processors)
         {
            addInput(processor.getAnnotation());
            addOutput(processor.getOutput());
         }
      }
      else
         throw new IllegalArgumentException("Null or empty processors.");
   }

   @SuppressWarnings("unchecked")
   public void deploy(DeploymentUnit unit, AnnotationEnvironment deployment) throws DeploymentException
   {
      for (AnnotationProcessor processor : processors)
      {
         String attachmentName = processor.getAnnotation().getName();
         Object annotationAttachment = unit.getAttachment(attachmentName);
         Object metadata = processor.createMetaData(annotationAttachment);
         if (metadata != null)
            unit.addAttachment(attachmentName, metadata);

         Set<Class<?>> classes = deployment.classIsAnnotatedWith(processor.getAnnotation());
         for (Class<?> clazz : classes)
         {
            metadata = processor.createMetaDataFromClass(clazz);
            if (metadata != null)
               unit.addAttachment(attachmentName + "#" + clazz.getName(), metadata);
         }
      }
   }
}