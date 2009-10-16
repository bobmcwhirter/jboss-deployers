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

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.vfs.plugins.util.ClasspathUtils;
import org.jboss.deployers.vfs.spi.deployer.AbstractOptionalVFSRealDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.papaki.AnnotationRepository;
import org.jboss.papaki.AnnotationScanner;
import org.jboss.papaki.AnnotationScannerCreator;
import org.jboss.papaki.Configuration;
import org.jboss.papaki.ScanStrategy;

import java.net.URL;

/**
 * Papaki scanning deployer.
 *
 * This deployer creates Papaki's AnnotationRepository based on .ser file.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class PapakiScannerDeployer extends AbstractOptionalVFSRealDeployer<Configuration>
{
   /**
    * The scanner creator.
    */
   private AnnotationScannerCreator creator = ScanStrategy.JAVASSIST_INPUT_STREAM;

   public PapakiScannerDeployer()
   {
      super(Configuration.class);
      setOutput(AnnotationRepository.class);
      setStage(DeploymentStages.POST_CLASSLOADER);
   }

   public void deploy(VFSDeploymentUnit unit, Configuration configuration) throws DeploymentException
   {
      // let's first verify creator
      creator.verify();

      try
      {
         AnnotationScanner scanner = creator.create(configuration);
         URL[] urls = ClasspathUtils.getUrls(unit);
         ClassLoader cl = unit.getClassLoader();
         // TODO - this needs filtering notion
         AnnotationRepository repository = scanner.scan(urls, cl);
         unit.addAttachment(AnnotationRepository.class, repository);
      }
      catch (Exception e)
      {
         throw DeploymentException.rethrowAsDeploymentException("Cannot create AnnotationRepository.", e);
      }
   }

   /**
    * Set the annotation scanner creator.
    *
    * @param creator the annotation scanner creator
    */
   public void setCreator(AnnotationScannerCreator creator)
   {
      if (creator == null)
         throw new IllegalArgumentException("Null creator");

      this.creator = creator;
   }
}