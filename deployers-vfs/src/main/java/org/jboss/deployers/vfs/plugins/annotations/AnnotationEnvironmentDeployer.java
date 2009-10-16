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

import org.jboss.classloading.spi.dependency.Module;
import org.jboss.deployers.plugins.annotations.GenericAnnotationResourceVisitor;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.annotations.AnnotationEnvironment;
import org.jboss.deployers.spi.annotations.ScanningMetaData;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.plugins.util.ClasspathUtils;
import org.jboss.deployers.vfs.spi.deployer.AbstractOptionalVFSRealDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnitFilter;

import java.net.URL;

import javassist.ClassPath;
import javassist.ClassPool;
import javassist.LoaderClassPath;

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

   private VFSDeploymentUnitFilter filter;

   public AnnotationEnvironmentDeployer()
   {
      super(Module.class);
      setStage(DeploymentStages.POST_CLASSLOADER);
      addInput(ScanningMetaData.class);
      addInput(AnnotationEnvironment.class);
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
    * Set vfs deployment filter.
    *
    * @param filter the vfs deployment filter.
    */
   public void setFilter(VFSDeploymentUnitFilter filter)
   {
      this.filter = filter;
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
   @Deprecated
   protected ClassPool createClassPool(ClassLoader classLoader)
   {
      ClassPool pool = new ClassPool();
      ClassPath classPath = new LoaderClassPath(classLoader);
      pool.insertClassPath(classPath);
      return pool;
   }

   /**
    * Create class pool.
    *
    * @param unit the deployment unit
    * @return javassist class pool
    */
   protected ClassPool createClassPool(VFSDeploymentUnit unit)
   {
      ClassPool pool = new ClassPool();
      ClassPath deploymentPath = new DeploymentUnitClassPath(unit);
      pool.appendClassPath(deploymentPath);
      // fall back to classloader classpath
      ClassPath classPath = new LoaderClassPath(unit.getClassLoader());
      pool.appendClassPath(classPath);
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
         URL[] urls = ClasspathUtils.getUrls(unit);
         module.visit(visitor, visitor.getFilter(), null, urls);
      }
      catch (Exception e)
      {
         throw DeploymentException.rethrowAsDeploymentException("Exception visiting module", e);
      }
   }

   public void deploy(VFSDeploymentUnit unit, Module module) throws DeploymentException
   {
      // we already used Papaki or some other mechanism to create env
      if (unit.isAttachmentPresent(AnnotationEnvironment.class))
         return;

      // running this deployer is costly, check if it should be run
      if (filter != null && filter.accepts(unit) == false)
         return;

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
      ClassPool pool = createClassPool(unit);
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
