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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.classloading.spi.metadata.ClassLoadingMetaData;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.vfs.spi.deployer.AbstractOptionalVFSRealDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.deployers.vfs.spi.structure.helpers.ClassPathVisitor;
import org.jboss.virtual.VirtualFile;

/**
 * VFSClassLoaderClassPathDeployer.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class VFSClassLoaderClassPathDeployer extends AbstractOptionalVFSRealDeployer<ClassLoadingMetaData>
{
   /** The vfs classpath */
   public static String VFS_CLASS_PATH = "org.jboss.deployers.vfs.plugins.classloader.VFS_CLASS_PATH";  

   /** The vfs excluded */
   public static String VFS_EXCLUDES = "org.jboss.deployers.vfs.plugins.classloader.VFS_EXCLUDES";  
   
   /**
    * Create a new VFSClassLoaderClassPathDeployer.
    */
   public VFSClassLoaderClassPathDeployer()
   {
      super(ClassLoadingMetaData.class);
      setOutput(ClassLoadingMetaData.class);
      setStage(DeploymentStages.DESCRIBE);
   }

   @Override
   @SuppressWarnings("unchecked")
   public void deploy(VFSDeploymentUnit unit, ClassLoadingMetaData deployment) throws DeploymentException
   {
      // We aren't creating a classloader
      if (unit.isTopLevel() == false && deployment == null)
         return;
      
      // Locate the parent class path
      List<VirtualFile> parentClassPath = null;
      Set<VirtualFile> parentExcludes = null;

      VFSDeploymentUnit parent = unit.getParent();
      while (parent != null)
      {
         parentClassPath = parent.getAttachment(VFS_CLASS_PATH, List.class);
         if (parentClassPath != null)
         {
            parentExcludes = parent.getAttachment(VFS_EXCLUDES, Set.class);
            break;
         }
         else
            parent = parent.getParent();
      }
      
      // Get our classpath
      ClassPathVisitor visitor = new ClassPathVisitor(unit);
      unit.visit(visitor);
      Set<VirtualFile> rawClassPath = visitor.getClassPath();

      // We're creating the classpath
      List<VirtualFile> vfsClassPath = new ArrayList<VirtualFile>();
      unit.addAttachment(VFS_CLASS_PATH, vfsClassPath, List.class);
      Set<VirtualFile> vfsExcludes = new HashSet<VirtualFile>();
      unit.addAttachment(VFS_EXCLUDES, vfsExcludes, Set.class);
      
      // Whether our classloader can see the parent
      boolean canSeeParent = parentClassPath != null && deployment.getParentDomain() == null;
      
      // Add all the classpath elements unless we can see the parent and its already in the parent
      if (rawClassPath != null)
      {
         for (VirtualFile file : rawClassPath)
         {
            if (vfsClassPath.contains(file) == false)
            {
               if (canSeeParent == false || (canSeeParent && parentClassPath.contains(file) == false))
                  vfsClassPath.add(file);
            }
         }
      }
      
      // Exclude those elements from the parent that it doesn't explicitly have
      if (parentClassPath != null)
      {
         VirtualFile root = unit.getRoot();
         if (root != null && parentClassPath.contains(root) == false)
            parentExcludes.add(root);

         for (VirtualFile file : vfsClassPath)
         {
            if (parentClassPath.contains(file) == false)
            {
               parentExcludes.add(file);
            }
         }
      }
   }

   @Override
   public void undeploy(VFSDeploymentUnit unit, ClassLoadingMetaData deployment)
   {
      unit.removeAttachment(VFS_CLASS_PATH);
      unit.removeAttachment(VFS_EXCLUDES);
   }
}
