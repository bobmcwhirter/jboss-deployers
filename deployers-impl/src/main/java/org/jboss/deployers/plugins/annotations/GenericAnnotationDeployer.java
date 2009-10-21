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

import org.jboss.classloading.spi.dependency.Module;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractSimpleRealDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.mcann.AnnotationRepository;
import org.jboss.mcann.repository.TypeInfoProvider;
import org.jboss.mcann.scanner.DefaultAnnotationScanner;
import org.jboss.mcann.scanner.ModuleAnnotationScanner;

/**
 * Generic annotation scanner deployer.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class GenericAnnotationDeployer extends AbstractSimpleRealDeployer<Module>
{
   private boolean forceAnnotations;
   private boolean keepAnnotations;
   private boolean checkSuper;
   private boolean checkInterfaces;
   private TypeInfoProvider typeInfoProvider;

   public GenericAnnotationDeployer()
   {
      super(Module.class);
      setStage(DeploymentStages.POST_CLASSLOADER);
      setOutput(AnnotationRepository.class);
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
    * Should we check super for annotations as well.
    *
    * @param checkSuper the check super flag
    */
   public void setCheckSuper(boolean checkSuper)
   {
      this.checkSuper = checkSuper;
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
    * Set type info provider.
    *
    * @param typeInfoProvider the type info provider
    */
   public void setTypeInfoProvider(TypeInfoProvider typeInfoProvider)
   {
      this.typeInfoProvider = typeInfoProvider;
   }

   public void deploy(DeploymentUnit unit, Module deployment) throws DeploymentException
   {
      try
      {
         DefaultAnnotationScanner scanner = new ModuleAnnotationScanner(deployment);
         scanner.setForceAnnotations(forceAnnotations);
         scanner.setKeepAnnotations(keepAnnotations);
         scanner.setCheckSuper(checkSuper);
         scanner.setCheckInterfaces(checkInterfaces);
         if (typeInfoProvider != null)
            scanner.setTypeInfoProvider(typeInfoProvider);

         AnnotationRepository repository = scanner.scan(unit.getClassLoader());
         unit.addAttachment(AnnotationRepository.class, repository);
      }
      catch (Exception e)
      {
         throw DeploymentException.rethrowAsDeploymentException("Cannot create AR", e);
      }
   }
}
