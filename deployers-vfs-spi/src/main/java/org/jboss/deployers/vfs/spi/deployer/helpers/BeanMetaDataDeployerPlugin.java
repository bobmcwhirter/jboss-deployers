/*
* JBoss, Home of Professional Open Source.
* Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.deployers.vfs.spi.deployer.helpers;

import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.dependency.spi.Controller;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.kernel.spi.dependency.KernelControllerContext;

/**
 * Interface used by BeanMetaDataDeployer to create controller contexts for deployment, and
 * to take care of special needs during undeployment.
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
public interface BeanMetaDataDeployerPlugin
{
   /**
    * The relative order of this creator. BeanMetaDataDeployer will try to 
    * create contexts with values first.
    * @return The relative order
    */
   int getRelativeOrder();
   
   /**
    * Create a controller context
    * 
    * @param controller The controller to which the beans will be deployed
    * @param unit The deployment unit we are deploying
    * @param beanMetaData The bean metadata we are deploying
    * @return the created controller context or null if this controller context creator should not handle the creation of the context 
    */
   KernelControllerContext createContext(Controller controller, DeploymentUnit unit, BeanMetaData beanMetaData);
   
   /**
    * Hook to uninstall a context from the controller if it needs special handling on uninstall. The BeanMetaDataDeployer
    * remembers which KernelContextCreator was used to create a KernelControllerContext and on undeploy will
    * call this method.
    * 
    * @param controller The controller containing the context
    * @param unit The deployment unit we are uninstalling
    * @param beanMetaData The bean metadata of the context that we are uninstalling. Its name is normally 
    * the same as the name of the context to be uninstalled
    * @return true if uninstall was handled here, false if we did not do the uninstall (i.e. nothing special 
    * is required for the uninstall, so it should be handled as normal by the BeanMetaDataDeployer)
    */
   boolean uninstallContext(Controller controller, DeploymentUnit unit, BeanMetaData beanMetaData);
}
