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
package org.jboss.deployers.vfs.plugins.classloader;

import java.io.IOException;
import java.net.URL;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.vfs.spi.deployer.AbstractOptionalVFSRealDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.virtual.VFS;
import org.jboss.virtual.VirtualFile;

/**
 * Integration deployer.
 * Adds integration path to deployment classpath.
 *
 * @param <T> exact output type
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 */
public abstract class UrlIntegrationDeployer<T> extends AbstractOptionalVFSRealDeployer<T>
{
   /** Location of integration jar */
   private URL integrationURL;

   public UrlIntegrationDeployer(Class<T> input)
   {
      super(input);
      // We have to run before the classloading is setup
      setStage(DeploymentStages.POST_PARSE);
   }

   /**
    * Get the integration url.
    *
    * @return the integration url
    */
   public URL getIntegrationURL()
   {
      return integrationURL;
   }

   /**
    * Set integration url.
    *
    * @param url the integration url
    */
   public void setIntegrationURL(URL url)
   {
      this.integrationURL = url;
   }

   @Override
   public void deploy(VFSDeploymentUnit unit, T metaData) throws DeploymentException
   {
      if (isIntegrationDeployment(unit))
      {
         try
         {
            VirtualFile integration = VFS.getRoot(integrationURL);
            unit.addClassPath(integration);
         }
         catch (IOException e)
         {
            throw DeploymentException.rethrowAsDeploymentException("Error adding integration path.", e);
         }
      }
   }

   /**
    * Is unit integration deployment unit?
    *
    * @param unit the deployment unit
    * @return true if the unit is integration deployment
    */
   protected abstract boolean isIntegrationDeployment(VFSDeploymentUnit unit);
}