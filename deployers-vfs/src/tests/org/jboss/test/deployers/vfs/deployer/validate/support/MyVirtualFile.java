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
package org.jboss.test.deployers.vfs.deployer.validate.support;

import java.net.URL;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URI;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.jboss.virtual.VirtualFile;
import org.jboss.virtual.spi.VirtualFileHandler;
import org.jboss.virtual.spi.VFSContext;

/**
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class MyVirtualFile extends VirtualFile
{
   /**
    * The serialVersionUID
    */
   private static final long serialVersionUID = 1L;

   public MyVirtualFile()
   {
      super(getVirtualFileHandler());
   }

   public String getName()
   {
      return "";
   }

   public InputStream openStream() throws IOException
   {
      return null;
   }

   private static VirtualFileHandler getVirtualFileHandler()
   {
      return new VirtualFileHandler()
      {
         /**
          * The serialVersionUID
          */
         private static final long serialVersionUID = 1L;

         public String getName()
         {
            return null;
         }

         public String getPathName()
         {
            return null;
         }

         public URL toVfsUrl() throws MalformedURLException, URISyntaxException
         {
            return null;
         }

         public URI toURI() throws URISyntaxException
         {
            return null;
         }

         public URL toURL() throws MalformedURLException, URISyntaxException
         {
            return null;
         }

         public long getLastModified() throws IOException
         {
            return 0;
         }

         public boolean hasBeenModified() throws IOException
         {
            return false;
         }

         public long getSize() throws IOException
         {
            return 0;
         }

         public boolean exists() throws IOException
         {
            return false;
         }

         public boolean isLeaf() throws IOException
         {
            return true;
         }

         public boolean isHidden() throws IOException
         {
            return false;
         }

         public String getLocalPathName()
         {
            return null;
         }

         public boolean isNested() throws IOException
         {
            return false;
         }

         public InputStream openStream() throws IOException
         {
            return null;
         }

         public VirtualFileHandler getParent() throws IOException
         {
            return null;
         }

         public List<VirtualFileHandler> getChildren(boolean ignoreErrors) throws IOException
         {
            return null;
         }

         public VirtualFileHandler getChild(String path) throws IOException
         {
            return null;
         }

         public VFSContext getVFSContext()
         {
            return null;
         }

         public VirtualFile getVirtualFile()
         {
            return null;
         }

         public void close()
         {

         }

         public void replaceChild(VirtualFileHandler original, VirtualFileHandler replacement)
         {

         }
      };
   }
}
