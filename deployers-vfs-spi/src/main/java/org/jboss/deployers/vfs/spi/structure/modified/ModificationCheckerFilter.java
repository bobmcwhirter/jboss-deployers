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
package org.jboss.deployers.vfs.spi.structure.modified;

import org.jboss.virtual.VirtualFile;
import org.jboss.virtual.VirtualFileFilter;

/**
 * ModificationCheckerFilter.
 * 
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public interface ModificationCheckerFilter extends VirtualFileFilter
{
   /**
    * Decide whether we should continue with checking for modification
    * where this filter accepted the root, but root's modification result was false.
    *
    * e.g. no point in checking anything further in archives and leaves,
    * but we should check further if root is a directory
    *
    * @param root the current root
    * @return true if we only check root, false otherwise
    */
   boolean checkRootOnly(VirtualFile root);
}
