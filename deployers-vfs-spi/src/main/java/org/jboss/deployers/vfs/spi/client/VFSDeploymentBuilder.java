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
package org.jboss.deployers.vfs.spi.client;

import java.security.AccessController;

import org.jboss.util.builder.AbstractBuilder;

/**
 * VFSDeploymentBuilder.
 * 
 * @author <a href="adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
class VFSDeploymentBuilder
{
   /** The default attachments builder */
   private static String DEFAULT_FACTORY = "org.jboss.deployers.vfs.plugins.client.DefaultVFSDeploymentFactory"; 
   
   /** The singleton */
   private static VFSDeploymentFactory singleton;

   /**
    * Get the instance
    * 
    * @return the instance
    */
   static synchronized VFSDeploymentFactory getInstance()
   {
      if (singleton == null)
      {
         AbstractBuilder<VFSDeploymentFactory> builder = new AbstractBuilder<VFSDeploymentFactory>(VFSDeploymentFactory.class, DEFAULT_FACTORY);
         singleton = AccessController.doPrivileged(builder);
      }
      return singleton;
   }
}
