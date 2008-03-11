/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.deployers.vfs.spi.deployer;

import java.util.List;
import java.io.InputStream;
import java.io.IOException;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.helpers.AbstractParsingDeployerWithOutput;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.virtual.VirtualFile;

/**
 * AbstractVFSParsingDeployer.
 *
 * @param <T> the type of output
 * @author <a href="adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public abstract class AbstractVFSParsingDeployer<T> extends AbstractParsingDeployerWithOutput<T> implements FileMatcher
{
   /**
    * Create a new AbstractVFSParsingDeployer.
    * 
    * @param output the type of output
    * @throws IllegalArgumentException for null output
    */
   public AbstractVFSParsingDeployer(Class<T> output)
   {
      super(output);
   }

   public boolean isDeployable(VirtualFile file)
   {
      String fileName = file.getName();
      String suffix = getSuffix();
      if (suffix == null)
         return fileName.equals(getName());
      else
         return fileName.endsWith(suffix);
   }

   /**
    * Callback to do prechecking on the deployment
    * 
    * @param unit the unit
    * @return true by default
    * @throws DeploymentException for any error
    */
   protected boolean accepts(VFSDeploymentUnit unit) throws DeploymentException
   {
      return true;
   }

   @Override
   protected boolean accepts(DeploymentUnit unit) throws DeploymentException
   {
      // Ignore non-vfs deployments
      if (unit instanceof VFSDeploymentUnit == false)
      {
         log.trace("Not a vfs deployment: " + unit.getName());
         return false;
      }
      return accepts((VFSDeploymentUnit) unit);
   }

   /**
    * Open stream and validate if not null.
    *
    * @param file the virtual file
    * @return non-null input stream
    * @throws Exception for any error or if file's stream is null
    */
   protected InputStream openStreamAndValidate(VirtualFile file) throws Exception
   {
      if (file == null)
         throw new IllegalArgumentException("Null file");

      InputStream inputStream = SecurityActions.openStream(file);
      if (inputStream == null)
         throw new IOException("Null file stream: " + file);

      return inputStream;
   }

   @Override
   protected T parse(DeploymentUnit unit, String name, T root) throws Exception
   {
      // Try to find the metadata
      VFSDeploymentUnit vfsDeploymentUnit = (VFSDeploymentUnit) unit;

      VirtualFile file = vfsDeploymentUnit.getMetaDataFile(name);
      if (file == null)
         return null;
      
      T result = parse(vfsDeploymentUnit, file, root);
      if (result != null)
         init(vfsDeploymentUnit, result, file);
      return result;
   }

   @Override
   protected T parse(DeploymentUnit unit, String name, String suffix, T root) throws Exception
   {
      // Should we include the deployment
      // The infrastructure will only check leafs anyway so no need to check here
      if (name == null && isIncludeDeploymentFile())
         name = unit.getName();
      
      // Try to find the metadata
      VFSDeploymentUnit vfsDeploymentUnit = (VFSDeploymentUnit) unit;
      List<VirtualFile> files = vfsDeploymentUnit.getMetaDataFiles(name, suffix);
      if (files.size() == 0)
         return null;
      
      // TODO JBMICROCONT-184 remove this limitation
      if (files.size() > 1)
         throw new DeploymentException("Only one file is allowed, found=" + files);

      VirtualFile file = files.get(0);
      
      T result = parse(vfsDeploymentUnit, file, root);
      if (result != null)
         init(vfsDeploymentUnit, result, file);
      return result;
   }

   /**
    * Parse a deployment
    * 
    * @param unit the deployment unit
    * @param file the metadata file
    * @param root - possibly null pre-existing root
    * @return the metadata
    * @throws Exception for any error
    */
   protected abstract T parse(VFSDeploymentUnit unit, VirtualFile file, T root) throws Exception;
   
   /**
    * Initialise the metadata
    * 
    * @param unit the unit
    * @param metaData the metadata
    * @param file the metadata file
    * @throws Exception for any error
    */
   protected void init(VFSDeploymentUnit unit, T metaData, VirtualFile file) throws Exception
   {
   }
}
