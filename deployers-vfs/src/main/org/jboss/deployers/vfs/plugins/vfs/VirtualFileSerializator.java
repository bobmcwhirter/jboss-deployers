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
package org.jboss.deployers.vfs.plugins.vfs;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

import org.jboss.virtual.VFS;
import org.jboss.virtual.VirtualFile;

/**
 * A minimal way of serializing VirtualFiles.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class VirtualFileSerializator implements Serializable
{
   private static final long serialVersionUID = 1L;

   private static final ObjectStreamField[] serialPersistentFields =
   {
      new ObjectStreamField("rootUrl", URL.class),
      new ObjectStreamField("path", String.class),
   };

   /** Minimal info to get full vfs file structure */
   private URL rootUrl;
   private String path;
   /** The root */
   private transient VirtualFile file;

   public VirtualFileSerializator()
   {
   }

   public VirtualFileSerializator(VirtualFile file)
   {
      this.file = file;
   }

   /**
    * Transform VirtualFileSerializators to VirtualFiles.
    *
    * @param serializators the serializators
    * @return virtual files
    * @throws IOException for any error
    */
   public static List<VirtualFile> toVirtualFiles(List<VirtualFileSerializator> serializators) throws IOException
   {
      if (serializators == null)
         return null;
      if (serializators.isEmpty())
         return Collections.emptyList();

      List<VirtualFile> files = new ArrayList<VirtualFile>(serializators.size());
      for (VirtualFileSerializator serializator : serializators)
         files.add(serializator.getFile());

      return files;
   }

   /**
    * Transform VirtualFiles to VirtualFileSerializators.
    *
    * @param files the virtual files
    * @return serializators
    * @throws IOException for any error
    */
   public static List<VirtualFileSerializator> toVirtualFileSerializators(List<VirtualFile> files) throws IOException
   {
      if (files == null)
         return null;
      if (files.isEmpty())
         return Collections.emptyList();

      List<VirtualFileSerializator> serializators = new ArrayList<VirtualFileSerializator>(files.size());
      for (VirtualFile file : files)
         serializators.add(new VirtualFileSerializator(file));

      return serializators;
   }

   /**
    * Get the virtual file.
    *
    * @return virtual file instance
    * @throws IOException for any error
    */
   @SuppressWarnings("deprecation")
   public VirtualFile getFile() throws IOException
   {
      if (file == null)
      {
         VirtualFile root = VFS.getRoot(rootUrl);
         file = root.findChild(path);
      }
      return file;
   }

   // write just url and path
   private void writeObject(ObjectOutputStream out) throws IOException, URISyntaxException
   {
      URL url = rootUrl;
      if (url == null)
      {
         VFS vfs = getFile().getVFS();
         url = vfs.getRoot().toURL();
      }
      String pathName = path;
      if (pathName == null)
         pathName = getFile().getPathName();

      ObjectOutputStream.PutField fields = out.putFields();
      fields.put("rootUrl", url);
      fields.put("path", pathName);
      out.writeFields();
   }

   // read url and path
   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
   {
      ObjectInputStream.GetField fields = in.readFields();
      rootUrl = (URL) fields.get("rootUrl", null);
      path = (String) fields.get("path", null);
   }
}
