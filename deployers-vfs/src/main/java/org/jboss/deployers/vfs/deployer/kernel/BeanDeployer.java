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

import org.jboss.deployers.vfs.spi.deployer.SchemaResolverDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.kernel.spi.deployment.KernelDeployment;
import org.jboss.virtual.VirtualFile;

/**
 * BeanDeployer.<p>
 * 
 * This deployer is responsible for looking for -beans.xml
 * and creating the metadata object.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class BeanDeployer extends SchemaResolverDeployer<KernelDeployment>
{
   /**
    * Create a new BeanDeployer.
    */
   public BeanDeployer()
   {
      super(KernelDeployment.class);
      setSuffix("-beans.xml");
      setJarExtension(".beans");
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

}
