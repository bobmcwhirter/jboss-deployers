/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.deployers.vfs.plugins.structure.modify;

import org.jboss.deployers.spi.structure.ContextInfo;
import org.jboss.deployers.spi.structure.StructureMetaData;
import org.jboss.vfs.VirtualFile;

/**
 * Determine if we need some modification.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public interface ModificationTypeMatcher
{
   /**
    * Should we modify the file.
    *
    * @param root the deployment root
    * @param structureMetaData the current structure metadata
    * @return true if we determined modification
    */
   boolean determineModification(VirtualFile root, StructureMetaData structureMetaData);

   /**
    * Should we modify the file.
    *
    * @param root the deployment root
    * @param contextInfo the current context info
    * @return true if we determined modification
    */
   boolean determineModification(VirtualFile root, ContextInfo contextInfo);
}