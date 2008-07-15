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
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
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
 * @author <a href="ales.justin@jboss.org">Ales Justin</a>
 * @version $Revision: 1.1 $
 */
public abstract class AbstractVFSParsingDeployer<T> extends AbstractParsingDeployerWithOutput<T> implements FileMatcher
{
   /** The allow multiple fiels flag */
   private boolean allowMultipleFiles;

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
         return getNames() != null && getNames().contains(fileName);
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

   /**
    * Get metadata file.
    * First try altDD, then fallback to original name.
    *
    * @param unit the vfs deployment unit
    * @param altPrefix altDD prefix
    * @param originalName the original file name
    * @return metadata file or null if it doesn't exist
    */
   protected VirtualFile getMetadataFile(VFSDeploymentUnit unit, String altPrefix, String originalName)
   {
      VirtualFile file = unit.getAttachment(altPrefix + ".altDD", VirtualFile.class);
      if(file == null && originalName != null)
         file = unit.getMetaDataFile(originalName);

      return file;
   }

   /**
    * Match file name to metadata class.
    *
    * @param fileName the file name
    * @return matching metadata class
    */
   protected Class<?> matchFileToClass(String fileName)
   {
      return null;
   }

   /**
    * Get altDD prefix from file name.
    *
    * First look into matching metadata classes.
    * If no match found, fall back to file name.
    *
    * @param fileName the file name
    * @return altDD prefix
    */
   protected String getAltDDPrefix(String fileName)
   {
      Class<?> expectedClass = matchFileToClass(fileName);
      if (expectedClass != null)
         return expectedClass.getName();
      else
         return fileName;
   }

   @Override
   protected T parse(DeploymentUnit unit, String name, T root) throws Exception
   {
      // Try to find the metadata
      VFSDeploymentUnit vfsDeploymentUnit = (VFSDeploymentUnit) unit;

      VirtualFile file = getMetadataFile(vfsDeploymentUnit, getOutput().getName(), name);
      if(file == null)
            return null;

      return parseAndInit(vfsDeploymentUnit, file, root);
   }

   protected T parse(DeploymentUnit unit, Set<String> names, T root) throws Exception
   {
      if (names == null || names.isEmpty())
         throw new IllegalArgumentException("Null or empty names.");

      VFSDeploymentUnit  vfsDeploymentUnit = (VFSDeploymentUnit)unit;

      List<VirtualFile> files = new ArrayList<VirtualFile>();
      Set<String> missingFiles = new HashSet<String>();

      for (String name : names)
      {
         VirtualFile file = getMetadataFile(vfsDeploymentUnit, getAltDDPrefix(name), name);
         if (file != null)
            files.add(file);
         else
            missingFiles.add(name);
      }

      if (missingFiles.size() == names.size())
         return null;

      return mergeFiles(vfsDeploymentUnit, root, files, missingFiles);
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

      // let's check altDD first
      VirtualFile file = getMetadataFile(vfsDeploymentUnit, getOutput().getName(), null);
      if (file != null)
         return parseAndInit(vfsDeploymentUnit, file, root);

      // try all name+suffix matches
      List<VirtualFile> files = vfsDeploymentUnit.getMetaDataFiles(name, suffix);
      switch (files.size())
      {
         case 0 :
            return null;
         case 1 :
            return parseAndInit(vfsDeploymentUnit, files.get(0), root);
         default :
            return handleMultipleFiles(vfsDeploymentUnit, root, files);
      }
   }

   /**
    * Parse the file, initialize the result if exists.
    *
    * @param unit the deployment unit
    * @param file the file
    * @param root the root
    * @return parsed result
    * @throws Exception for any error
    */
   protected T parseAndInit(VFSDeploymentUnit unit, VirtualFile file, T root) throws Exception
   {
      T result = parse(unit, file, root);
      if (result != null)
         init(unit, result, file);
      return result;
   }

   protected T parse(DeploymentUnit unit, Set<String> names, String suffix, T root) throws Exception
   {
      if (names == null || names.isEmpty())
         throw new IllegalArgumentException("Null or empty names.");

      VFSDeploymentUnit  vfsDeploymentUnit = (VFSDeploymentUnit)unit;

      List<VirtualFile> files = new ArrayList<VirtualFile>();
      Set<String> missingFiles = new HashSet<String>();

      for (String name : names)
      {
         // try finding altDD file
         VirtualFile file = getMetadataFile(vfsDeploymentUnit, getAltDDPrefix(name), null);
         if (file == null)
         {
            List<VirtualFile> matched = vfsDeploymentUnit.getMetaDataFiles(name, suffix);
            if (matched != null && matched.isEmpty() == false)
               files.addAll(matched);
            else
               missingFiles.add(name);
         }
         else
         {
            files.add(file);
         }
      }

      if (missingFiles.size() == names.size())
         return null;

      return mergeFiles(vfsDeploymentUnit, root, files, missingFiles);
   }


   /**
    * Merge files into one piece of metatdata
    *
    * @param unit the unit
    * @param root possibly null pre-existing root
    * @param files matching meta files
    * @param missingFiles file names that are missing matching file
    * @return merged metadata
    * @throws Exception for any error
    */
   protected T mergeFiles(VFSDeploymentUnit unit, T root, List<VirtualFile> files, Set<String> missingFiles) throws Exception
   {
      return null;
   }

   /**
    * Handle multiple files.
    *
    * @param unit the vfs deployment unit
    * @param root possibly null pre-existing root
    * @param files the matching files
    * @return null or merged single result
    * @throws Exception for any error
    */
   protected T handleMultipleFiles(VFSDeploymentUnit unit, T root, List<VirtualFile> files) throws Exception
   {
      if (allowsMultipleFiles(files) == false)
         throw new IllegalArgumentException("Multiple matching files not allowed: " + files);

      for (VirtualFile file : files)
      {
         T result = parse(unit, file, root);
         if (result != null)
         {
            init(unit, result, file);
            unit.addAttachment(file.toURL().toString(), result, getOutput());
         }
      }

      return null;
   }

   /**
    * Check if we allow multiple files.
    *
    * Make sure you have deployers down
    * the chain that will handle generated
    * multiple attachments if this method returns true.
    * 
    * @param files the matching files
    * @return true if we allow, false otherwise
    */
   protected boolean allowsMultipleFiles(List<VirtualFile> files)
   {
      return allowMultipleFiles;
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

   /**
    * Set allow multiple files.
    *
    * @param allowMultipleFiles the allow multiple files flag
    */
   public void setAllowMultipleFiles(boolean allowMultipleFiles)
   {
      this.allowMultipleFiles = allowMultipleFiles;
   }
}
