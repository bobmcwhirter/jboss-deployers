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

import javassist.ClassPath;
import javassist.ClassPool;
import javassist.LoaderClassPath;
import org.jboss.classloading.spi.dependency.Module;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.annotations.AnnotationEnvironment;
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

   public GenericAnnotationDeployer()
   {
      super(Module.class);
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

   public void deploy(DeploymentUnit unit, Module module) throws DeploymentException
   {
      ClassPool pool = ClassPool.getDefault();
      ClassLoader classLoader = unit.getClassLoader();
      ClassPath classPath = new LoaderClassPath(classLoader);
      pool.insertClassPath(classPath);
      try
      {
         GenericAnnotationResourceVisitor visitor = new GenericAnnotationResourceVisitor(pool, classLoader);
         visitor.setForceAnnotations(forceAnnotations);

         module.visit(visitor);

         unit.addAttachment(AnnotationEnvironment.class, visitor.getEnv());
      }
      finally
      {
         pool.removeClassPath(classPath);
      }
   }
}
