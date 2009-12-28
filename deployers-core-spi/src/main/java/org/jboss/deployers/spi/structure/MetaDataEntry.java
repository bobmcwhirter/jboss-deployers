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

/**
 * A metadata entry.
 *
 * @author <a href="ales.justin@jboss.org">Ales Justin</a>
 */
public interface MetaDataEntry extends Serializable
{
   /**
    * Get the path relative to the context virtual file
    *
    * @return the path.
    */
   String getPath();

   /**
    * Get metadata type.
    *
    * e.g. some deployments have alternative metadata locations;
    * JEE .war deployments -- WEB-INF/lib/somelib.jar/META-INF/ejb-jar.xml
    *
    * @return the metadata type
    */
   MetaDataType getType();
}