/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.deployers.spi.structure;

import java.io.Serializable;
import java.util.List;

/**
 * A set of subdeployment contexts.
 * 
 * @author Scott.Stark@jboss.org
 * @author adrian@jboss.org
 * @version $Revision: 1.1$
 */
public interface StructureMetaData extends Serializable
{
   /**
    * Get a context by path
    * 
    * @param path the path of the context
    * @return the context or null if there is no such context
    * @throws IllegalArgumentException for a null path
    */
   ContextInfo getContext(String path);
   
   /**
    * Add a context
    * 
    * @param context the context to add
    * @throws IllegalArgumentException for a null context or if the context has no path
    * @throws IllegalStateException if a context is already present with that path
    */
   void addContext(ContextInfo context);

   /**
    * Remove a context
    * 
    * @param context the context to remove
    * @throws IllegalArgumentException for a null context
    */
   void removeContext(ContextInfo context);

   /**
    * Remove a context
    * 
    * @param path the patch of the context to remove
    * @throws IllegalArgumentException for a null path
    */
   void removeContext(String path);
   
   /**
    * Get the contexts
    *
    * @return an immutable set of contexts
    */
   List<ContextInfo> getContexts();
}
