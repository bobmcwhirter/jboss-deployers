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
package org.jboss.deployers.plugins.annotations;

import javassist.ClassPool;
import org.jboss.classloading.spi.dependency.Module;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.annotations.AnnotationEnvironment;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractSimpleRealDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;

/**
 * Generic annotation scanner deployer.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class GenericAnnotationDeployer extends AbstractSimpleRealDeployer<Module>
{
   private boolean forceAnnotations;
   private boolean keepAnnotations;

   public GenericAnnotationDeployer()
   {
      super(Module.class);
      setStage(DeploymentStages.PRE_REAL);
      setOutput(AnnotationEnvironment.class);
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
    * Create GenericAnnotationResourceVisitor.
    *
    * Can be used change existing GARV's filter.
    * Or determin if we need to force/keep annotations.
    *
    * @param pool the class pool
    * @param classLoader the classloader
    * @return new generic annotation visitor
    */
   protected GenericAnnotationResourceVisitor createGenericAnnotationResourceVisitor(ClassPool pool, ClassLoader classLoader)
   {
      GenericAnnotationResourceVisitor visitor = new GenericAnnotationResourceVisitor(pool, classLoader);
      visitor.setForceAnnotations(forceAnnotations);
      visitor.setKeepAnnotations(keepAnnotations);
      return visitor;
   }

   public void deploy(DeploymentUnit unit, Module module) throws DeploymentException
   {
      if (log.isTraceEnabled())
         log.trace("Creating AnnotationEnvironment for " + unit + ", module: " + module + ", force annotations: " + forceAnnotations);

      ClassPool pool = ClassPool.getDefault();
      ClassLoader classLoader = unit.getClassLoader();
      GenericAnnotationResourceVisitor visitor = createGenericAnnotationResourceVisitor(pool, classLoader);

      ClassLoader tcl = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(classLoader);
      try
      {
         module.visit(visitor);
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(tcl);
      }

      unit.addAttachment(AnnotationEnvironment.class, visitor.getEnv());
   }
}
