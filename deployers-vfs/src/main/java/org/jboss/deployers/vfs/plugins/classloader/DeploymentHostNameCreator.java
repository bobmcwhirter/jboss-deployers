/*
* JBoss, Home of Professional Open Source
* Copyright 2008, JBoss Inc., and individual contributors as indicated
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
package org.jboss.deployers.vfs.plugins.classloader;

import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;

/**
 * Join all deployment names.
 *
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 */
public class DeploymentHostNameCreator implements HostNameCreator
{
   /**The prefix */
   private String prefix = "in-memory";

   public String createHostName(VFSDeploymentUnit unit)
   {
      StringBuilder builder = new StringBuilder(prefix);
      VFSDeploymentUnit vdu = unit;
      while (vdu != null)
      {
         builder.append("-").append(vdu.getSimpleName());
         vdu = vdu.getParent();
      }
      return builder.toString();
   }

   /**
    * Set prefix.
    *
    * @param prefix the prefix
    */
   public void setPrefix(String prefix)
   {
      this.prefix = prefix;
   }
}