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
package org.jboss.deployers.plugins.classloading;

import org.jboss.classloading.spi.metadata.ClassLoadingMetaData;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractOptionalRealDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;

/**
 * ClassLoadingDefaultDeployer.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class ClassLoadingDefaultDeployer extends AbstractOptionalRealDeployer<ClassLoadingMetaData>
{
   /** The default classloading metadata */
   private ClassLoadingMetaData defaultMetaData;
   
   /**
    * Create a new ClassLoadingDefaultDeployer.
    */
   public ClassLoadingDefaultDeployer()
   {
      super(ClassLoadingMetaData.class);
      setOutput(ClassLoadingMetaData.class);
      setStage(DeploymentStages.PRE_DESCRIBE);
      setTopLevelOnly(true);
   }

   /**
    * Get the defaultMetaData.
    * 
    * @return the defaultMetaData.
    */
   public ClassLoadingMetaData getDefaultMetaData()
   {
      return defaultMetaData;
   }

   /**
    * Set the defaultMetaData.
    * 
    * @param defaultMetaData the defaultMetaData.
    */
   public void setDefaultMetaData(ClassLoadingMetaData defaultMetaData)
   {
      this.defaultMetaData = defaultMetaData;
   }

   /**
    * Check the state
    */
   public void create()
   {
      if (defaultMetaData == null)
         throw new IllegalStateException("Default metadata has not been configured");
   }
   
   @Override
   public void deploy(DeploymentUnit unit, ClassLoadingMetaData deployment) throws DeploymentException
   {
      if (deployment == null)
      {
         ClassLoadingMetaData cloned = defaultMetaData.clone();
         cloned.setName(unit.getSimpleName());
         unit.addAttachment(ClassLoadingMetaData.class, cloned);
      }
      else if ("<unknown>".equals(deployment.getName()))
      {
         deployment.setName(unit.getSimpleName());
      }
   }
}
