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

import java.net.URL;
import java.util.List;

import org.jboss.classloading.spi.metadata.ClassLoadingMetaData;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.vfs.spi.deployer.AbstractVFSRealDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.util.id.GUID;
import org.jboss.virtual.MemoryFileFactory;
import org.jboss.virtual.VirtualFile;

/**
 * TempURLDeployer.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 * @version $Revision: 1.1 $
 */
public class InMemoryClassesDeployer extends AbstractVFSRealDeployer
{
   /** The name of the dynamic class root */
   public static final String DYNAMIC_CLASS_URL_KEY = "DYNAMIC_CLASS_URL_KEY";

   /** The name of the dynamic class root */
   public static final String DYNAMIC_CLASS_KEY = "DYNAMIC_CLASS_KEY";

   /**
    * Create a new TempURLDeployer.
    */
   public InMemoryClassesDeployer()
   {
      // Make it run before the classloader describe deployer
      setStage(DeploymentStages.DESCRIBE);
      setOutput(ClassLoadingMetaData.class);
      setTopLevelOnly(true);
   }
   
   @Override
   public void deploy(VFSDeploymentUnit unit) throws DeploymentException
   {
      try
      {
         URL dynamicClassRoot = new URL("vfsmemory", GUID.asString(), "");
         VirtualFile classes = MemoryFileFactory.createRoot(dynamicClassRoot).getRoot();
         unit.addAttachment(DYNAMIC_CLASS_URL_KEY, dynamicClassRoot);
         unit.addAttachment(DYNAMIC_CLASS_KEY, classes);
         unit.prependClassPath(classes);
         log.debug("Dynamic class root for " + unit.getName() + " is " + dynamicClassRoot);
      }
      catch (Exception e)
      {
         throw new DeploymentException("Error creating dynamic class root", e);
      }
   }

   @Override
   public void undeploy(VFSDeploymentUnit unit)
   {
      log.debug("Removing dynamic class root for " + unit.getName());
      try
      {
         VirtualFile classes = unit.removeAttachment(DYNAMIC_CLASS_KEY, VirtualFile.class);
         if (classes != null)
         {
            List<VirtualFile> classPath = unit.getClassPath();
            if (classPath != null)
            {
               classPath.remove(classes);
               unit.setClassPath(classPath);
            }
         }
      }
      finally
      {
         try
         {
            URL root = unit.removeAttachment(DYNAMIC_CLASS_URL_KEY, URL.class);
            if (root != null)
               MemoryFileFactory.deleteRoot(root);
         }
         catch (Exception e)
         {
            log.warn("Error deleting dynamic class root for " + unit.getName(), e);
         }
      }
   }
}
