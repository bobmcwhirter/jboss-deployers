/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.deployers.vfs.plugins.structure.link;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.vfs.spi.structure.StructureContext;
import org.jboss.deployers.vfs.spi.structure.StructureDeployer;
import org.jboss.logging.Logger;
import org.jboss.util.StringPropertyReplacer;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VFSUtils;
import org.jboss.vfs.VirtualFile;
import org.jboss.vfs.util.automount.Automounter;

/**
 * .vfslink.properties feature port
 * We don't recognize structure, we just prepare .vfslink.properties mounts.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class VFSLinkStructureDeployer implements StructureDeployer
{
   /** The log */
   protected static final Logger log = Logger.getLogger("org.jboss.deployers.vfs.structure");

   /** The link properties */
   public static final String VFS_LINK_PROPERTIES_SUFFIX = ".vfslink.properties";
   /** The link name */
   public static final String VFS_LINK_NAME = "vfs.link.name";
   /** The link target */
   public static final String VFS_LINK_TARGET = "vfs.link.target";

   public boolean determineStructure(StructureContext context) throws DeploymentException
   {
      VirtualFile root = context.getFile();
      VirtualFile vfslinks = root.getChild("META-INF/jboss-vfslinks.txt");
      if (vfslinks.exists())
      {
         try
         {
            InputStream is = vfslinks.openStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            try
            {
               String line;
               while((line = reader.readLine()) != null)
               {
                  // ignore comments
                  if (line.startsWith("#") == false)
                     mountLinks(context, line);
               }
            }
            finally
            {
               VFSUtils.safeClose(is);
            }
         }
         catch (IOException e)
         {
            throw DeploymentException.rethrowAsDeploymentException("Cannot read jboss-vfslinks.txt", e);
         }
      }
      return false;
   }

   protected void mountLinks(StructureContext context, String path) throws IOException
   {
      VirtualFile root = context.getFile();
      VirtualFile link = root.getChild(path + VFS_LINK_PROPERTIES_SUFFIX);
      if (link.exists())
      {
         List<LinkInfo> links = new ArrayList<LinkInfo>();
         parseLinkProperties(link, links);
         mountLinks(context, link.getParent(), path, links);
      }
      else
      {
         log.warn("No " + VFS_LINK_PROPERTIES_SUFFIX + " match for link: " + path);
      }
   }

   protected void parseLinkProperties(VirtualFile link, List<LinkInfo> links) throws IOException
   {
      InputStream is = link.openStream();
      try
      {
         Properties props = new Properties();
         props.load(is);

         for(int n = 0; ; n ++)
         {
            String nameKey = VFS_LINK_NAME + "." + n;
            String name = props.getProperty(nameKey);
            String uriKey = VFS_LINK_TARGET + "." + n;
            String uri = props.getProperty(uriKey);
            // End when the value is null since a link may not have a name
            if (uri == null)
            {
               break;
            }
            // Replace any system property references
            uri = StringPropertyReplacer.replaceProperties(uri);
            LinkInfo li = new LinkInfo(name, new URI(uri));
            links.add(li);
         }
      }
      catch (URISyntaxException e)
      {
         IOException ioe = new IOException();
         ioe.initCause(e);
         throw ioe;
      }
      finally
      {
         VFSUtils.safeClose(is);
      }
   }

   @SuppressWarnings({"UnusedDeclaration"})
   protected void mountLinks(StructureContext context, VirtualFile parent, String path, List<LinkInfo> links) throws IOException
   {
      VirtualFile root = context.getFile();
      VirtualFile link = parent.getChild(path);
      for (LinkInfo li : links)
      {
         VirtualFile linkChild = link.getChild(li.name);
         Closeable closeable = VFS.mountReal(new File(li.uri), linkChild);
         Automounter.addHandle(root, closeable);
      }
   }

   public boolean isSupportsCandidateAnnotations()
   {
      return false;
   }

   public int getRelativeOrder()
   {
      return Integer.MIN_VALUE + 10;
   }

   public void setRelativeOrder(int order)
   {
   }

   private static class LinkInfo
   {
      private String name;
      private URI uri;

      private LinkInfo(String name, URI uri)
      {
         this.name = name;
         this.uri = uri;
      }
   }
}
