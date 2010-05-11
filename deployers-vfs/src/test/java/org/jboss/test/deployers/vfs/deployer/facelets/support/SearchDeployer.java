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
package org.jboss.test.deployers.vfs.deployer.facelets.support;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;

/**
 * This deployer's purpose is to trigger
 * mock impl of Facelets's Classpath class.
 *
 * Better deployer examples could be found elsewhere. ;-)
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class SearchDeployer extends AbstractDeployer
{
   private String prefix;
   private String suffix;

   private Set<URL> urls = new HashSet<URL>();

   public SearchDeployer(String prefix, String suffix)
   {
      if (prefix == null)
         throw new IllegalArgumentException("Null prefix.");
      if (suffix == null)
         throw new IllegalArgumentException("Null suffix.");

      this.prefix = prefix;
      this.suffix = suffix;
      setStage(DeploymentStages.REAL);
   }

   public void deploy(DeploymentUnit unit) throws DeploymentException
   {
      try
      {
         URL[] foundUrls = Classpath.search(unit.getClassLoader(), prefix, suffix);
         if (foundUrls != null)
         {
            Set<VirtualFile> files = new HashSet<VirtualFile>();
            for (URL url : foundUrls)
            {
               VirtualFile file = VFS.getChild(url);
               if (files.add(file))
                  urls.add(url);
            }
         }
      }
      catch (Exception e)
      {
         DeploymentException.rethrowAsDeploymentException("Error doing facelets search, prefix=" + prefix + ", suffix=" + suffix, e);
      }
   }

   public void undeploy(DeploymentUnit unit)
   {
      urls = null;
   }

   public Set<URL> getUrls()
   {
      return urls;
   }
}
