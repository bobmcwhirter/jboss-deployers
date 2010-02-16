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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.jboss.logging.Logger;
import org.jboss.vfs.VFSUtils;
import org.jboss.vfs.VirtualFile;

/**
 * Abstract synch adapter.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class AbstractSynchAdapter implements SynchAdapter
{
   /** The log */
   protected static Logger log = Logger.getLogger(AbstractSynchAdapter.class);

   public long add(VirtualFile fileToAdd, VirtualFile tempRoot, String pathToFile) throws IOException
   {
      File rootDir = tempRoot.getPhysicalFile();
      File newFile = new File(rootDir, pathToFile);
      return copy(fileToAdd, newFile);
   }

   /**
    * Do copy.
    *
    * @param fileToAdd file to add
    * @param newFile new file location
    * @return new timestamp
    * @throws IOException for any error
    */
   protected static long copy(VirtualFile fileToAdd, File newFile) throws IOException
   {
      FileOutputStream out = new FileOutputStream(newFile);
      VFSUtils.copyStreamAndClose(fileToAdd.openStream(), out);
      return newFile.lastModified();
   }

   public boolean delete(VirtualFile fileToDelete) throws IOException
   {
      return fileToDelete.delete();
   }

   /**
    * Merge exception
    */
   static class MergeException extends IOException
   {
      /** The serialVersionUID */
      private static final long serialVersionUID = 5228888050899870372L;

      MergeException(VirtualFile dest, VirtualFile source, Exception cause)
      {
         super("Conflict merging files, dest: " + dest + ", source: " + source);
         initCause(cause);
      }
   }
}
