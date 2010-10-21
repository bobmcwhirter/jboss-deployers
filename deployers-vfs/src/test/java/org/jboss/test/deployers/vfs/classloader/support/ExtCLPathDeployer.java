/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.test.deployers.vfs.classloader.support;

import java.net.URL;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.vfs.spi.deployer.AbstractVFSRealDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;
import org.jboss.vfs.util.MatchAllVirtualFileFilter;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@SuppressWarnings({"deprecation"})
public class ExtCLPathDeployer extends AbstractVFSRealDeployer
{
   public ExtCLPathDeployer()
   {
      setStage(DeploymentStages.POST_PARSE);
   }

   public void deploy(VFSDeploymentUnit unit) throws DeploymentException
   {
      try
      {
         URL resource = getClass().getResource("/classloader/resources");
         VirtualFile root = VFS.getChild(resource);
         unit.appendClassPath(root.getChildren(MatchAllVirtualFileFilter.INSTANCE));
         VirtualFile app = root.getChild("app");
         unit.appendClassPath(app.getChildren(MatchAllVirtualFileFilter.INSTANCE));
      }
      catch (Exception e)
      {
         throw DeploymentException.rethrowAsDeploymentException("Error", e);
      }
   }
}
