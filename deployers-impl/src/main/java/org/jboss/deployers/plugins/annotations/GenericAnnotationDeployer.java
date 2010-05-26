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
import org.jboss.scanning.annotations.plugins.AnnotationsScanningPlugin;
import org.jboss.scanning.annotations.spi.AnnotationRepository;
import org.jboss.scanning.plugins.DeploymentUnitScanner;
import org.jboss.scanning.spi.helpers.AbstractScanner;

/**
 * Generic annotation scanner deployer.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class GenericAnnotationDeployer extends AbstractSimpleRealDeployer<Module>
{
   public GenericAnnotationDeployer()
   {
      super(Module.class);
      setStage(DeploymentStages.POST_CLASSLOADER);
      setOutput(AnnotationRepository.class);
   }

   public void deploy(DeploymentUnit unit, Module deployment) throws DeploymentException
   {
      try
      {
         AbstractScanner scanner = new DeploymentUnitScanner(unit);
         AnnotationsScanningPlugin plugin = new AnnotationsScanningPlugin(unit.getClassLoader());
         scanner.addPlugin(plugin);

         scanner.scan();

         AnnotationRepository repository = unit.getAttachment(plugin.getAttachmentKey(), AnnotationRepository.class);
         unit.addAttachment(AnnotationRepository.class, repository);
      }
      catch (Exception e)
      {
         throw DeploymentException.rethrowAsDeploymentException("Cannot create AR", e);
      }
   }
}
