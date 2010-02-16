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
package org.jboss.deployers.vfs.deployer.kernel;

import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

import org.jboss.deployers.vfs.spi.deployer.AbstractVFSParsingDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.kernel.spi.deployment.KernelDeployment;
import org.jboss.kernel.plugins.deployment.props.DeploymentBuilder;
import org.jboss.vfs.VirtualFile;

/**
 * Properties2BeansDeployer.<p>
 *
 * This deployer is responsible for looking for -beans.properties
 * and creating the metadata object.
 *
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 */
public class Properties2BeansDeployer extends AbstractVFSParsingDeployer<KernelDeployment>
{
   /**
    * Create a new Properties2BeansDeployer.
    */
   public Properties2BeansDeployer()
   {
      super(KernelDeployment.class);
      setSuffix("-beans.properties");
      // Enable ManagedObject creation based on annotations by default
      setBuildManagedObject(true);
      setAllowMultipleFiles(true);
   }

   @Override
   protected void init(VFSDeploymentUnit unit, KernelDeployment metaData, VirtualFile file) throws Exception
   {
      String name = file.toURI().toString();
      metaData.setName(name);
   }

   protected KernelDeployment parse(VFSDeploymentUnit unit, VirtualFile file, KernelDeployment root) throws Exception
   {
      Properties properties = new Properties();
      InputStream is = openStreamAndValidate(file);
      try
      {
         properties.load(is);

         DeploymentBuilder deploymentBuilder = new DeploymentBuilder(properties);
         return deploymentBuilder.build();
      }
      finally
      {
         try
         {
            is.close();
         }
         catch (IOException ignored)
         {
         }
      }
   }
}
