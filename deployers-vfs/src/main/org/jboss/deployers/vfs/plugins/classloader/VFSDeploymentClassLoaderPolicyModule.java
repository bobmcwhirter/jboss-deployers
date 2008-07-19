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
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jboss.classloader.spi.filter.ClassFilter;
import org.jboss.classloading.plugins.vfs.PackageVisitor;
import org.jboss.classloading.plugins.vfs.VFSResourceVisitor;
import org.jboss.classloading.spi.dependency.Module;
import org.jboss.classloading.spi.metadata.Capability;
import org.jboss.classloading.spi.metadata.ClassLoadingMetaDataFactory;
import org.jboss.classloading.spi.metadata.ExportAll;
import org.jboss.classloading.spi.vfs.policy.VFSClassLoaderPolicy;
import org.jboss.classloading.spi.visitor.ResourceFilter;
import org.jboss.classloading.spi.visitor.ResourceVisitor;
import org.jboss.deployers.plugins.classloading.AbstractDeploymentClassLoaderPolicyModule;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.structure.helpers.ClassPathVisitor;
import org.jboss.logging.Logger;
import org.jboss.virtual.VirtualFile;

/**
 * VFSDeploymentClassLoaderPolicyModule.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class VFSDeploymentClassLoaderPolicyModule extends AbstractDeploymentClassLoaderPolicyModule
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;

   /** The log */
   private static final Logger log  = Logger.getLogger(VFSDeploymentClassLoaderPolicyModule.class);
   
   /** The attachment containing the final classpath */
   public static final String VFS_CLASS_PATH = "VFSClassPath";
   
   /** The cached roots */
   private VirtualFile[] vfsRoots;
   
   /**
    * Create a new VFSDeploymentClassLoaderPolicyModule.
    * 
    * @param unit the deployment unit
    * @throws IllegalArgumentException for a null deployment unit
    */
   public VFSDeploymentClassLoaderPolicyModule(DeploymentUnit unit)
   {
      super(unit);
   }

   @Override
   protected List<Capability> determineCapabilities()
   {
      // While we are here, check the roots
      VirtualFile[] roots = determineVFSRoots();

      List<Capability> capabilities = super.determineCapabilities();
      if (capabilities != null)
         return capabilities;
         
      // We need to work it out
      ClassLoadingMetaDataFactory factory = ClassLoadingMetaDataFactory.getInstance();
      capabilities = new CopyOnWriteArrayList<Capability>();

      // We have a module capability
      Object version = getVersion();
      Capability capability = factory.createModule(getName(), version);
      capabilities.add(capability);
      
      // Do we determine package capabilities
      ClassFilter included = getIncluded();
      ClassFilter excluded = getExcluded();
      ClassFilter excludedExport = getExcludedExport();
      ExportAll exportAll = getExportAll();
      if (exportAll != null)
      {
         Set<String> exportedPackages = PackageVisitor.determineAllPackages(roots, exportAll, included, excluded, excludedExport);
         for (String packageName : exportedPackages)
         {
            capability = factory.createPackage(packageName, version);
            capabilities.add(capability);
         }
      }
      
      return capabilities;
   }
   
   /**
    * Get the virtual file roots
    * 
    * @return the roots
    */
   @SuppressWarnings("unchecked")
   protected VirtualFile[] determineVFSRoots()
   {
      if (vfsRoots != null)
         return vfsRoots;

      DeploymentUnit unit = getDeploymentUnit();
      Set<VirtualFile> classPath = determineClassPath(unit, this);
      vfsRoots = classPath.toArray(new VirtualFile[classPath.size()]);
      return vfsRoots;
   }

   /**
    * Determine classpath.
    *
    * @param unit the deployment unit we check
    * @param module the unit's module
    * @return unit's classpath
    */
   @SuppressWarnings("unchecked")
   protected static Set<VirtualFile> determineClassPath(DeploymentUnit unit, Module module)
   {
      ClassPathVisitor visitor = new ClassPathVisitor(unit);
      try
      {
         unit.visit(visitor);
      }
      catch (DeploymentException e)
      {
         throw new RuntimeException("Error visiting deployment: " + e);
      }
      Set<VirtualFile> classPath = visitor.getClassPath();
      // Weed out parent classpaths
      if (module != null && module.getParentDomainName() == null)
      {
         DeploymentUnit parent = unit.getParent();
         while (parent != null)
         {
            Set<VirtualFile> parentClassPath = parent.getAttachment(VFS_CLASS_PATH, Set.class);
            if (parentClassPath == null)
               parentClassPath = determineClassPath(parent, parent.getAttachment(Module.class));

            if (parentClassPath != null && parentClassPath.isEmpty() == false)
            {
               if (log.isTraceEnabled())
               {
                  for (VirtualFile parentFile : parentClassPath)
                  {
                     if (classPath.contains(parentFile))
                        log.trace(unit + " weeding duplicate entry " + parentFile + " from classpath already in parent " + parent);
                  }
               }
               classPath.removeAll(parentClassPath);
            }
            parent = parent.getParent();
         }
      }
      unit.addAttachment(VFS_CLASS_PATH, classPath);
      return classPath;
   }

   @Override
   public VFSClassLoaderPolicy getPolicy()
   {
      return (VFSClassLoaderPolicy) super.getPolicy();
   }
   
   @Override
   protected VFSClassLoaderPolicy determinePolicy()
   {
      VirtualFile[] roots = determineVFSRoots();
      VFSClassLoaderPolicy policy = VFSClassLoaderPolicy.createVFSClassLoaderPolicy(getContextName(), roots);
      String[] packageNames = getPackageNames();
      policy.setExportedPackages(packageNames);
      policy.setIncluded(getIncluded());
      policy.setExcluded(getExcluded());
      policy.setExcludedExport(getExcludedExport());
      policy.setExportAll(getExportAll());
      policy.setImportAll(isImportAll());
      policy.setCacheable(isCacheable());
      policy.setBlackListable(isBlackListable());
      policy.setDelegates(getDelegates());
      return policy;
   }

   @Override
   public URL getDynamicClassRoot()
   {
      return getDeploymentUnit().getAttachment(InMemoryClassesDeployer.DYNAMIC_CLASS_URL_KEY, URL.class);
   }

   @Override
   public void reset()
   {
      super.reset();
      vfsRoots = null;
   }

   @Override
   public void visit(ResourceVisitor visitor, ResourceFilter filter, ResourceFilter recurseFilter)
   {
      ClassLoader classLoader = getClassLoader();
      if (classLoader == null)
         throw new IllegalStateException("ClassLoader has not been constructed for " + getContextName());

      VirtualFile[] roots = determineVFSRoots();
      if (roots != null)
      {
         ClassFilter included = getIncluded();
         ClassFilter excluded = getExcluded();
         VFSResourceVisitor.visit(roots, included, excluded, classLoader, visitor, filter, recurseFilter);
      }
   }
}
