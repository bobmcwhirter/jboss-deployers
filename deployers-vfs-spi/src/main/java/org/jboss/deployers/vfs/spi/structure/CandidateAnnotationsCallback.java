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
package org.jboss.deployers.vfs.spi.structure;

import java.lang.annotation.Annotation;

import org.jboss.mcann.AnnotationRepository;
import org.jboss.vfs.VirtualFile;

/**
 * CandidateAnnotationsCallback.
 *
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 */
public interface CandidateAnnotationsCallback
{
   /**
    * Execute callback.
    *
    * @param root the resources root
    * @param currentContext the current context
    * @param env the annotation environment
    * @param annotationClass the current annotation class
    */
   void executeCallback(VirtualFile root, StructureContext currentContext, AnnotationRepository env, Class<? extends Annotation> annotationClass);
}