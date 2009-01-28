/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.deployers.vfs.plugins.client;

import org.jboss.deployers.vfs.spi.client.VFSDeployment;
import org.jboss.deployers.vfs.spi.client.VFSDeploymentFactory;
import org.jboss.logging.Logger;
import org.jboss.virtual.VirtualFile;

/**
 * DefaultVFSDeploymentFactory.
 * 
 * @author <a href="adrian@jboss.org">Adrian Brock</a>
 * @author <a href="ales.justin@jboss.org">Ales Justin</a>
 * @version $Revision: 1.1 $
 */
public class DefaultVFSDeploymentFactory extends VFSDeploymentFactory
{
   /** The log */
   protected Logger log = Logger.getLogger(getClass());

   @Override
   protected VFSDeployment newVFSDeployment(VirtualFile root)
   {
      return new AbstractVFSDeployment(root);
   }

   public void destroyVFSDeployment(VFSDeployment deployment)
   {
      VirtualFile root = deployment.getRoot();
      try
      {
         if (root != null && root.exists())
         {
            root.cleanup();
         }
      }
      catch (Exception e)
      {
         log.warn("Exception destroying deployment (" + deployment +  "): " + e);         
      }
   }
}
