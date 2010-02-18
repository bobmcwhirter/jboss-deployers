/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, JBoss Inc., and individual contributors as indicated
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
package org.jboss.deployers.vfs.plugins.structure;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.vfs.spi.structure.StructureContext;
import org.jboss.deployers.vfs.spi.structure.StructureDeployer;
import org.jboss.vfs.VirtualFile;
import org.jboss.vfs.util.automount.Automounter;
import org.jboss.vfs.util.automount.MountOption;

import java.io.IOException;

/**
 * Abstract {@link StructureDeployer} used to help mount VFS archive based {@link StructureDeployer}s.
 *
 * @author <a href="jbailey@redhat.com">John Bailey</a>
 */
public abstract class AbstractVFSArchiveStructureDeployer extends AbstractVFSStructureDeployer
{
   /**
    * Determine the structure of a deployment invoking the Automounter for archive files.
    *
    * @param context the structure context
    * @return true when it recognized the context
    * @throws DeploymentException for an error
    */
   public boolean determineStructure(StructureContext context) throws DeploymentException
   {
      VirtualFile root = context.getFile();
      boolean valid = false;
      try
      {
         if (root.isFile())
         {
            if (shouldMount(root) == false || mountArchive(root) == false)
            {
               return false;
            }
         }
         valid = doDetermineStructure(context);
      }
      catch (DeploymentException e)
      {
         valid = false;
         throw e;
      }
      finally
      {
         if (valid == false)
         {
            Automounter.cleanup(root);
         }
      }
      return valid;
   }

   /**
    * Template method for VFS archive structure deployers to determine the structure once the archive has been mounted.
    *
    * @param context the structure context
    * @return true if the structure was determined
    * @throws DeploymentException for any deployment error
    */
   protected abstract boolean doDetermineStructure(StructureContext context) throws DeploymentException;

   /**
    * Determine whether the {@link VirtualFile} has a name that matches this structure.
    * Defaults to just checking the suffix.
    *
    * @param root the {@link VirtualFile} root to check
    * @return true if the file name is valid for this {@link StructureDeployer}
    */
   protected boolean hasValidName(VirtualFile root)
   {
      return hasValidSuffix(root.getName());
   }

   /**
    * Template method for VFS archive structure deployers to correctly match file suffixes for their archive type.
    *
    * @param name the name of the file
    * @return true if the file matches the required pattern
    */
   protected abstract boolean hasValidSuffix(String name);

   /**
    * Determine whether to mount the archive.
    *
    * @param virtualFile to check
    * @return true if the {@link VirtualFile} should be mounted
    */
   protected boolean shouldMount(VirtualFile virtualFile)
   {
      return virtualFile.isFile() && hasValidName(virtualFile);
   }

   /**
    * Mounts the the provided file as an archive using the {@link Automounter}
    *
    * @param file the file to mount
    * @return true if the mount was successful
    * @throws DeploymentException for any deployment error
    */
   private boolean mountArchive(VirtualFile file) throws DeploymentException
   {
      try
      {
         performMount(file);
      }
      catch (IOException e)
      {
         throw DeploymentException.rethrowAsDeploymentException("Failed to mount archive: " + file, e);
      }
      return true;
   }

   /**
    * Perform mount.
    *
    * @param file the file to mount
    * @throws IOException for any IO error
    */
   protected void performMount(VirtualFile file) throws IOException
   {
      Automounter.mount(file, MountOption.COPY);
   }
}
