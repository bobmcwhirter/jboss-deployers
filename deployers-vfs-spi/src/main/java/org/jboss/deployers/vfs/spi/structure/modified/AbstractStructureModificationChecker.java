/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.deployers.vfs.spi.structure.modified;

import java.io.IOException;
import java.net.URISyntaxException;

import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.structure.spi.DeploymentContext;
import org.jboss.deployers.structure.spi.main.MainDeployerStructure;
import org.jboss.deployers.vfs.spi.client.VFSDeployment;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentContext;
import org.jboss.logging.Logger;
import org.jboss.virtual.VirtualFile;

/**
 * AbstractStructureModificationChecker.
 *
 * @param <T> exact cache value type
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class AbstractStructureModificationChecker<T> implements StructureModificationChecker
{
   /** The log */
   protected Logger log = Logger.getLogger(getClass());

   /** The main deployer structure */
   private MainDeployerStructure mainDeployer;

   /** The structure cache */
   private StructureCache<T> cache;

   protected AbstractStructureModificationChecker()
   {
   }

   protected AbstractStructureModificationChecker(MainDeployerStructure mainDeployer)
   {
      if (mainDeployer == null)
         throw new IllegalArgumentException("Null main deployer");
      
      this.mainDeployer = mainDeployer;
   }

   /**
    * Get the structure cache.
    *
    * @return the structure cache
    */
   protected StructureCache<T> getCache()
   {
      if (cache == null)
         cache = new DefaultStructureCache<T>();

      return cache;
   }

   /**
    * Set the structure cache.
    *
    * @param cache the structure cache
    */
   public void setCache(StructureCache<T> cache)
   {
      this.cache = cache;
   }

   /**
    * Get main deployer structure.
    *
    * @return the main deployer structure
    */
   protected MainDeployerStructure getMainDeployerStructure()
   {
      if (mainDeployer == null)
         throw new IllegalArgumentException("Null main deployer structure");

      return mainDeployer;
   }

   /**
    * Get deployment context.
    *
    * @param name the deployment context name
    * @return vfs deployment context or null if doesn't exist or not vfs based
    */
   @SuppressWarnings("deprecation")
   protected VFSDeploymentContext getDeploymentContext(String name)
   {
      DeploymentContext deploymentContext = getMainDeployerStructure().getDeploymentContext(name);
      if (deploymentContext == null || deploymentContext instanceof VFSDeploymentContext == false)
         return null;

      return (VFSDeploymentContext) deploymentContext;
   }

   public boolean hasStructureBeenModified(VirtualFile root) throws IOException
   {
      if (root == null)
         throw new IllegalArgumentException("Null root");

      // skip vfs deployment context lookup if archive or file
      if (root.isArchive() || root.isLeaf())
         return root.hasBeenModified();

      VFSDeploymentContext deploymentContext;
      try
      {
         String name = root.toURI().toString();
         deploymentContext = getDeploymentContext(name);
         if (deploymentContext != null)
            return hasStructureBeenModified(deploymentContext, false);
      }
      catch (URISyntaxException ignore)
      {
      }

      log.trace("Falling back to root name: " + root);
      deploymentContext = getDeploymentContext(root.getName());
      if (deploymentContext != null)
         return hasStructureBeenModified(deploymentContext, false);

      return false;
   }

   public boolean hasStructureBeenModified(VFSDeployment deployment) throws IOException
   {
      if (deployment == null)
         throw new IllegalArgumentException("Null deployment");

      VFSDeploymentContext deploymentContext = getDeploymentContext(deployment.getName());
      return deploymentContext != null && hasStructureBeenModified(deploymentContext);
   }

   public boolean hasStructureBeenModified(VFSDeploymentContext deploymentContext) throws IOException
   {
      return hasStructureBeenModified(deploymentContext, true);
   }

   /**
    * Has structure been modified.
    *
    * @param deploymentContext the deployment context
    * @param checkRoot should we check root
    * @return true if modifed, false otherwise
    * @throws IOException for any error
    */
   protected boolean hasStructureBeenModified(VFSDeploymentContext deploymentContext, boolean checkRoot) throws IOException
   {
      Deployment deployment = deploymentContext.getDeployment();
      if (deployment == null || deployment instanceof VFSDeployment == false)
      {
         log.warn("Deployment is not VFS or not top level.");
         return false;
      }

      VFSDeployment vfsDeployment = VFSDeployment.class.cast(deployment);
      VirtualFile root = vfsDeployment.getRoot();
      if (checkRoot && (root.isArchive() || root.isLeaf()))
         return root.hasBeenModified();

      boolean result = hasStructureBeenModifed(root, deploymentContext);
      if (result)
      {
         String pathName = root.getPathName();
         getCache().invalidateCache(pathName);
      }
      return result;
   }

   /**
    * Has structure been modified.
    *
    * @param root the client root
    * @param deploymentContext the deployment context
    * @return true if modifed, false otherwise
    * @throws IOException for any error
    */
   protected abstract boolean hasStructureBeenModifed(VirtualFile root, VFSDeploymentContext deploymentContext) throws IOException;

   public void addStructureRoot(VirtualFile root)
   {
      if (root == null)
         throw new IllegalArgumentException("Null root");

      String pathName = root.getPathName();
      getCache().initializeCache(pathName);
   }

   public void removeStructureRoot(VirtualFile root)
   {
      if (root == null)
         throw new IllegalArgumentException("Null root");

      String pathName = root.getPathName();
      getCache().removeCache(pathName);
   }
}
