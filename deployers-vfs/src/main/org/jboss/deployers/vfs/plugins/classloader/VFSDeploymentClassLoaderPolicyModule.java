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
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jboss.classloader.spi.filter.ClassFilter;
import org.jboss.classloading.plugins.vfs.PackageVisitor;
import org.jboss.classloading.plugins.vfs.VFSResourceVisitor;
import org.jboss.classloading.spi.metadata.Capability;
import org.jboss.classloading.spi.metadata.ClassLoadingMetaDataFactory;
import org.jboss.classloading.spi.metadata.ExportAll;
import org.jboss.classloading.spi.vfs.policy.VFSClassLoaderPolicy;
import org.jboss.classloading.spi.visitor.ResourceFilter;
import org.jboss.classloading.spi.visitor.ResourceVisitor;
import org.jboss.deployers.plugins.classloading.AbstractDeploymentClassLoaderPolicyModule;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.virtual.VirtualFile;
import org.jboss.virtual.VFS;

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
   
   /** No roots */
   private static final VirtualFile[] NO_ROOTS = new VirtualFile[0];
   
   /** The cached roots */
   private VirtualFile[] vfsRoots;
   
   /** The excluded roots */
   private VirtualFile[] excludedRoots;
   
   /**
    * Create a new VFSDeploymentClassLoaderPolicyModule.
    * 
    * @param unit the deployment unit
    * @throws IllegalArgumentException for a null deployment unit
    */
   @SuppressWarnings("unchecked")
   public VFSDeploymentClassLoaderPolicyModule(DeploymentUnit unit)
   {
      super(unit);
      List<VirtualFile> vfsClassPath = unit.getAttachment(VFSClassLoaderClassPathDeployer.VFS_CLASS_PATH, List.class);
      if (vfsClassPath == null)
         vfsRoots = NO_ROOTS;
      else
         vfsRoots = vfsClassPath.toArray(new VirtualFile[vfsClassPath.size()]);
      Set<VirtualFile> vfsExcludes = unit.getAttachment(VFSClassLoaderClassPathDeployer.VFS_EXCLUDES, Set.class);
      if (vfsExcludes != null)
         excludedRoots = vfsExcludes.toArray(new VirtualFile[vfsExcludes.size()]);
   }

   @Override
   protected List<Capability> determineCapabilities()
   {
      // While we are here, check the roots
      VirtualFile[] roots = vfsRoots;

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
         Set<String> exportedPackages = PackageVisitor.determineAllPackages(roots, excludedRoots, exportAll, included, excluded, excludedExport);
         for (String packageName : exportedPackages)
         {
            capability = factory.createPackage(packageName, version);
            capabilities.add(capability);
         }
      }
      
      return capabilities;
   }

   @Override
   public VFSClassLoaderPolicy getPolicy()
   {
      return (VFSClassLoaderPolicy) super.getPolicy();
   }
   
   @Override
   protected VFSClassLoaderPolicy determinePolicy()
   {
      VFSClassLoaderPolicy policy = VFSClassLoaderPolicy.createVFSClassLoaderPolicy(getContextName(), vfsRoots, excludedRoots);
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
   public void visit(ResourceVisitor visitor, ResourceFilter filter, ResourceFilter recurseFilter, URL... urls)
   {
      ClassLoader classLoader = getClassLoader();
      if (classLoader == null)
         throw new IllegalStateException("ClassLoader has not been constructed for " + getContextName());

      VirtualFile[] roots = vfsRoots;
      if (roots != null && roots.length > 0)
      {
         if (urls != null && urls.length > 0)
            roots = matchUrlsWithRoots(urls, roots);
         
         ClassFilter included = getIncluded();
         ClassFilter excluded = getExcluded();
         VFSResourceVisitor.visit(roots, excludedRoots, included, excluded, classLoader, visitor, filter, recurseFilter);
      }
   }

   /**
    * Match urls with roots.
    *
    * @param urls the urls
    * @param roots the old roots
    * @return new roots
    */
   protected static VirtualFile[] matchUrlsWithRoots(URL[] urls, VirtualFile[] roots)
   {
      try
      {
         String[] rootURLStrings = new String[urls.length];
         List<VirtualFile> newRoots = new ArrayList<VirtualFile>(urls.length);
         for (URL url : urls)
         {
            String urlString = stripProtocol(url);
            for(int i=0; i < roots.length; i++)
            {
               if (rootURLStrings[i] == null)
                  rootURLStrings[i] = stripProtocol(roots[i].toURL());

               if (urlString.startsWith(rootURLStrings[i]))
               {
                  VirtualFile newRoot = VFS.getRoot(url);
                  newRoots.add(newRoot);
                  break;
               }
            }
         }
         return newRoots.toArray(new VirtualFile[newRoots.size()]);
      }
      catch (Exception e)
      {
         throw new RuntimeException("Cannot match urls to roots.", e);
      }
   }

   /**
    * Strip the url protocol.
    *
    * @param url the url
    * @return url external form w/o protocol
    */
   protected static String stripProtocol(URL url)
   {
      if (url == null)
         throw new IllegalArgumentException("Null url");

      String urlString = url.toExternalForm();
      int p = urlString.indexOf(":/");
      return urlString.substring(p + 2);
   }
}
