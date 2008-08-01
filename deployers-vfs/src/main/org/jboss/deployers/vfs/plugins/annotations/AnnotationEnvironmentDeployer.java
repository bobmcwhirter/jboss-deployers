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
package org.jboss.deployers.vfs.plugins.annotations;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javassist.ClassPath;
import javassist.ClassPool;
import javassist.LoaderClassPath;
import org.jboss.classloading.spi.dependency.Module;
import org.jboss.deployers.plugins.annotations.GenericAnnotationResourceVisitor;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.annotations.AnnotationEnvironment;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.deployer.AbstractOptionalVFSRealDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.virtual.VirtualFile;

/**
 * A POST_CLASSLOADER deployer which creates AnnotationEnvironment for sub-deployments.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class AnnotationEnvironmentDeployer extends AbstractOptionalVFSRealDeployer<Module>
{
   private boolean forceAnnotations;
   private boolean keepAnnotations;
   private boolean checkInterfaces;

   public AnnotationEnvironmentDeployer()
   {
      super(Module.class);
      setStage(DeploymentStages.POST_CLASSLOADER);
      setOutput(AnnotationEnvironment.class);
      checkInterfaces = true;
   }

   /**
    * Should we force all annotations to be available.
    *
    * @param forceAnnotations the force annotations flag
    */
   public void setForceAnnotations(boolean forceAnnotations)
   {
      this.forceAnnotations = forceAnnotations;
   }

   /**
    * Set the keep annotations flag.
    *
    * @param keepAnnotations the keep annotations flag
    */
   public void setKeepAnnotations(boolean keepAnnotations)
   {
      this.keepAnnotations = keepAnnotations;
   }

   /**
    * Should we check interfaces for annotations as well.
    *
    * @param checkInterfaces the check interfaces flag
    */
   public void setCheckInterfaces(boolean checkInterfaces)
   {
      this.checkInterfaces = checkInterfaces;
   }

   /**
    * Create GenericAnnotationResourceVisitor.
    *
    * Can be used change existing GARV's filter.
    * Or determin if we need to force/keep annotations.
    *
    * @param unit the deployment unit
    * @param pool the class pool
    * @param classLoader the classloader
    * @return new generic annotation visitor
    */
   protected GenericAnnotationResourceVisitor createGenericAnnotationResourceVisitor(DeploymentUnit unit, ClassPool pool, ClassLoader classLoader)
   {
      GenericAnnotationResourceVisitor visitor = new GenericAnnotationResourceVisitor(pool, classLoader);
      visitor.setForceAnnotations(forceAnnotations);
      visitor.setKeepAnnotations(keepAnnotations);
      visitor.setCheckInterfaces(checkInterfaces);
      return visitor;
   }

   /**
    * Create class pool.
    *
    * @param classLoader the class loader
    * @return javassist class pool
    */
   protected ClassPool createClassPool(ClassLoader classLoader)
   {
      ClassPool pool = new ClassPool();
      ClassPath classPath = new LoaderClassPath(classLoader);
      pool.insertClassPath(classPath);
      return pool;
   }

   /**
    * Visit module.
    *
    * Util method to add some behavior to Module
    * before we visit it.
    *
    * @param unit the deployment unit
    * @param module the module
    * @param visitor the resource visitor
    * @throws DeploymentException for any error
    */
   protected void visitModule(VFSDeploymentUnit unit, Module module, GenericAnnotationResourceVisitor visitor) throws DeploymentException
   {
      try
      {
         module.visit(visitor, null, null, getURL(unit));
      }
      catch (Exception e)
      {
         throw DeploymentException.rethrowAsDeploymentException("Exception visiting module", e);
      }
   }

   /**
    * Get the matching urls.
    *
    * @param unit the deployment unit
    * @return matching urls
    * @throws Exception for any error
    */
   protected static URL[] getURL(VFSDeploymentUnit unit) throws Exception
   {
      List<VirtualFile> classpath = unit.getClassPath();
      if (classpath != null && classpath.isEmpty() == false)
      {
         List<URL> urls = new ArrayList<URL>();
         VirtualFile root = unit.getRoot();
         for (VirtualFile cp : classpath)
         {
            VirtualFile check = cp;
            while(check != null && check.equals(root) == false)
               check = check.getParent();

            if (check != null)
               urls.add(cp.toURL());
         }
         return urls.toArray(new URL[urls.size()]);
      }
      return new URL[0];
   }

   public void deploy(VFSDeploymentUnit unit, Module module) throws DeploymentException
   {
      if (module == null)
      {
         VFSDeploymentUnit parent = unit.getParent();
         while(parent != null && module == null)
         {
            module = parent.getAttachment(Module.class);
            parent = parent.getParent();
         }
         if (module == null)
            throw new IllegalArgumentException("No module in deployment unit's hierarchy: " + unit.getName());
      }

      if (log.isTraceEnabled())
         log.trace("Creating AnnotationEnvironment for " + unit.getName() + ", module: " + module + ", force annotations: " + forceAnnotations);

      ClassLoader classLoader = unit.getClassLoader();
      ClassPool pool = createClassPool(classLoader);
      GenericAnnotationResourceVisitor visitor = createGenericAnnotationResourceVisitor(unit, pool, classLoader);

      // something in javassist uses TCL
      ClassLoader tcl = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(classLoader);
      try
      {
         visitModule(unit, module, visitor);
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(tcl);
      }

      unit.addAttachment(AnnotationEnvironment.class, visitor.getEnv());
   }
}
