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

import java.io.IOException;

import org.jboss.virtual.VirtualFile;

/**
 * Synch adapter.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public interface SynchAdapter
{
   /**
    * Add new file to temp.
    *
    * @param fileToAdd file to add
    * @param tempRoot temp root
    * @param pathToFile the path to file
    * @return addition timestamp
    * @throws IOException for any error
    */
   long add(VirtualFile fileToAdd, VirtualFile tempRoot, String pathToFile) throws IOException;

   /**
    * Update file.
    *
    * @param fileToUpdate file to update
    * @param modifiedFile the modified file
    * @return the update timestamp
    * @throws IOException for any error
    */
   long update(VirtualFile fileToUpdate, VirtualFile modifiedFile) throws IOException;

   /**
    * Add new file to temp.
    *
    * @param fileToDelete file to delete
    * @throws IOException for any error
    */
   void delete(VirtualFile fileToDelete) throws IOException;
}