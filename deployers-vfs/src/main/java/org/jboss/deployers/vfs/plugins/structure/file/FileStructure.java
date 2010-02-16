/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.deployers.vfs.plugins.structure.file;

import java.util.Set;
import java.util.Arrays;
import java.util.concurrent.CopyOnWriteArraySet;

import org.jboss.beans.metadata.api.annotations.Install;
import org.jboss.beans.metadata.api.annotations.Uninstall;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.structure.ContextInfo;
import org.jboss.deployers.vfs.plugins.structure.AbstractVFSStructureDeployer;
import org.jboss.deployers.vfs.spi.deployer.FileMatcher;
import org.jboss.deployers.vfs.spi.structure.StructureContext;
import org.jboss.vfs.VirtualFile;
import org.jboss.vfs.VirtualFileFilter;

/**
 * FileStructure is a simple suffix recognition structure deployer.
 * 
 * It can also act as a file filter, only recognizing those which
 * match any of the FileMatcher implementations.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 * @version $Revision: 1.1 $
 */
public class FileStructure extends AbstractVFSStructureDeployer implements VirtualFileFilter
{
   /** The file suffixes */
   private static Set<String> fileSuffixes = new CopyOnWriteArraySet<String>(Arrays.asList(new String[] {
      "-service.xml",
      "-beans.xml",
      "-ds.xml",
      "-aop.xml",
   }));

   /** The file matchers */
   private Set<FileMatcher> fileMatchers = new CopyOnWriteArraySet<FileMatcher>();

   /**
    * Create a new FileStructure.
    */
   public FileStructure()
   {
   }

   /**
    * Create a new FileStructure.
    * 
    * @param suffixes the recognised suffixes
    * @throws IllegalArgumentException for null suffixes
    */
   public FileStructure(Set<String> suffixes)
   {
      if (suffixes == null)
         throw new IllegalArgumentException("Null suffixes");
      fileSuffixes.clear();
      fileSuffixes.addAll(suffixes);
   }

   /**
    * Gets the list of suffixes recognised as files
    * 
    * @return the list of suffixes
    */
   public Set<String> getSuffixes()
   {
      return fileSuffixes;      
   }
   
   
   /**
    * Add a file suffix
    * 
    * @param suffix the suffix
    * @return true when added
    * @throws IllegalArgumentException for a null suffix
    */
   public static boolean addFileSuffix(String suffix)
   {
      if (suffix == null)
         throw new IllegalArgumentException("Null suffix");
      return fileSuffixes.add(suffix);
   }

   /**
    * Remove a file suffix
    * 
    * @param suffix the suffix
    * @return true when removed
    * @throws IllegalArgumentException for a null suffix
    */
   public static boolean removeFileSuffix(String suffix)
   {
      if (suffix == null)
         throw new IllegalArgumentException("Null suffix");
      return fileSuffixes.remove(suffix);
   }
   
   @Install
   public boolean addFileMatcher(FileMatcher fm)
   {
      return fileMatchers.add(fm);
   }

   @Uninstall
   public boolean removeFileMatcher(FileMatcher fm)
   {
      return fileMatchers.remove(fm);   
   }

   /**
    * Whether this is an archive
    *
    * @param name the name
    * @return true when an archive
    * @throws IllegalArgumentException for a null name
    */
   public static boolean isKnownFile(String name)
   {
      if (name == null)
         throw new IllegalArgumentException("Null name");

      for(String suffix : fileSuffixes)
      {
         if (name.endsWith(suffix))
            return true;
      }
      return false;
   }

   /**
    * Check is some file matcher recognizes the file.
    *
    * @param file the virtual file
    * @return true if recognized, false otherwise
    */
   protected boolean checkFileMatchers(VirtualFile file)
   {
      for(FileMatcher fm : fileMatchers)
      {
         if (fm.isDeployable(file))
            return true;
      }
      return false;
   }

   public boolean accepts(VirtualFile file)
   {
      return checkFileMatchers(file);
   }

   public boolean determineStructure(StructureContext structureContext) throws DeploymentException
   {
      ContextInfo context = null;
      VirtualFile file = structureContext.getFile();
      try
      {
         boolean trace = log.isTraceEnabled();
         if (isLeaf(file) == true)
         {
            boolean isFile = false;
            if( trace )
               log.trace(file + " is a leaf");
            // See if this is a top-level by checking the parent
            if (structureContext.isTopLevel() == false)
            {
               if (isKnownFile(file.getName()) == false && checkFileMatchers(file) == false)
               {
                  if (trace)
                     log.trace("... no - it is not a top level file and not a known name");
               }
               else
               {
                  if (trace)
                     log.trace("... ok - not a top level file but it is a known name");
                  isFile = true;
               }
            }
            else
            {
               if (trace)
                  log.trace("... ok - it is a top level file");
               isFile = true;
            }

            // Create a context info for this file
            if (isFile)
               context = createContext(structureContext);
            // There are no subdeployments for files
            if (trace)
               log.trace(file + " isFile: " + isFile);
            return isFile;
         }
         else
         {
            if (trace)
               log.trace("... no - not a file.");
            return false;
         }
      }
      catch (Exception e)
      {
         if (context != null)
            structureContext.removeChild(context);

         throw DeploymentException.rethrowAsDeploymentException("Error determining structure: " + file.getName(), e);
      }
   }
}
