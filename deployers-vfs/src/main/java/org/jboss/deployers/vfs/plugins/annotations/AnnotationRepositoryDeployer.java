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

import org.jboss.classloading.spi.dependency.Module;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.annotations.ScanningMetaData;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.vfs.plugins.util.ClasspathUtils;
import org.jboss.deployers.vfs.spi.deployer.AbstractOptionalVFSRealDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnitFilter;
import org.jboss.mcann.AnnotationRepository;
import org.jboss.mcann.scanner.DefaultAnnotationScanner;
import org.jboss.mcann.scanner.ModuleAnnotationScanner;

/**
 * A POST_CLASSLOADER deployer which creates AnnotationRepository for sub-deployments.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class AnnotationRepositoryDeployer extends AbstractOptionalVFSRealDeployer<Module>
{
   private boolean forceAnnotations;
   private boolean keepAnnotations;
   private boolean checkInterfaces;

   private VFSDeploymentUnitFilter filter;

   public AnnotationRepositoryDeployer()
   {
      super(Module.class);
      setStage(DeploymentStages.POST_CLASSLOADER);
      addInput(ScanningMetaData.class);
      addInput(AnnotationRepository.class);
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
    * Visit module.
    *
    * Util method to add some behavior to Module
    * before we visit it.
    *
    * @param unit the deployment unit
    * @param module the module
    * @throws DeploymentException for any error
    */
   protected void visitModule(VFSDeploymentUnit unit, Module module) throws DeploymentException
   {
      try
      {
         URL[] urls = ClasspathUtils.getUrls(unit);
         DefaultAnnotationScanner scanner = new ModuleAnnotationScanner(module);
         configureScanner(unit, scanner);
         AnnotationRepository repository = scanner.scan(unit.getClassLoader(), urls);
         unit.addAttachment(AnnotationRepository.class, repository);
      }
      catch (Exception e)
      {
         throw DeploymentException.rethrowAsDeploymentException("Exception visiting module", e);
      }
   }

   /**
    * Configure scanner.
    *
    * @param unit the deployment unit
    * @param scanner the annotation scanner
    */
   protected void configureScanner(VFSDeploymentUnit unit, DefaultAnnotationScanner scanner)
   {
      scanner.setForceAnnotations(forceAnnotations);
      scanner.setKeepAnnotations(keepAnnotations);
      scanner.setCheckInterfaces(checkInterfaces);
   }

   public void deploy(VFSDeploymentUnit unit, Module module) throws DeploymentException
   {
      // we already used McAnn or some other mechanism to create repo
      if (unit.isAttachmentPresent(AnnotationRepository.class))
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
         log.trace("Creating AnnotationRepository for " + unit.getName() + ", module: " + module + ", force annotations: " + forceAnnotations);

      visitModule(unit, module);
   }
}