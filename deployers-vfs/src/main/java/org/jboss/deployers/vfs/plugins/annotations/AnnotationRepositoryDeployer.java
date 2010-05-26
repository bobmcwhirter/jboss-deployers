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
import org.jboss.scanning.annotations.plugins.AnnotationsScanningPlugin;
import org.jboss.scanning.annotations.spi.AnnotationRepository;
import org.jboss.scanning.plugins.DeploymentUnitScanner;

/**
 * A POST_CLASSLOADER deployer which creates AnnotationRepository for sub-deployments.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
@Deprecated
public class AnnotationRepositoryDeployer extends AbstractOptionalVFSRealDeployer<Module>
{
   private VFSDeploymentUnitFilter filter;

   public AnnotationRepositoryDeployer()
   {
      super(Module.class);
      setStage(DeploymentStages.POST_CLASSLOADER);
      addInput(ScanningMetaData.class);
      addInput(AnnotationRepository.class);
      setOutput(AnnotationRepository.class);
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
    * @throws DeploymentException for any error
    */
   protected void visitModule(VFSDeploymentUnit unit) throws DeploymentException
   {
      try
      {
         URL[] urls = ClasspathUtils.getUrls(unit);
         DeploymentUnitScanner scanner = new DeploymentUnitScanner(unit, urls);
         AnnotationsScanningPlugin plugin = createPlugin(unit);

         configureScanner(scanner);

         scanner.scan();

         AnnotationRepository repository = unit.getAttachment(plugin.getAttachmentKey(), AnnotationRepository.class);
         unit.addAttachment(AnnotationRepository.class, repository);
      }
      catch (Exception e)
      {
         throw DeploymentException.rethrowAsDeploymentException("Exception visiting module", e);
      }
   }

   /**
    * Configure scanner and plugin.
    *
    * @param scanner the annotation scanner
    */
   protected void configureScanner(DeploymentUnitScanner scanner)
   {
   }

   /**
    * Create and configure annotation plugin.
    *
    * @param unit the deployment unit
    * @return new annotation plugin
    */
   protected AnnotationsScanningPlugin createPlugin(VFSDeploymentUnit unit)
   {
      return new AnnotationsScanningPlugin(unit.getClassLoader());
   }

   public void deploy(VFSDeploymentUnit unit, Module module) throws DeploymentException
   {
      // we already used mc scanning or some other mechanism to create repo
      if (unit.isAttachmentPresent(AnnotationRepository.class))
         return;

      // running this deployer is costly, check if it should be run
      if (filter != null && filter.accepts(unit) == false)
         return;

      if (log.isTraceEnabled())
         log.trace("Creating AnnotationRepository for " + unit.getName() + ", module: " + module);

      visitModule(unit);
   }
}
