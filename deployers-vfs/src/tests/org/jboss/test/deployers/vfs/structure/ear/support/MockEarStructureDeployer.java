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
package org.jboss.test.deployers.vfs.structure.ear.support;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.jboss.deployers.spi.structure.ContextInfo;
import org.jboss.deployers.spi.structure.StructureMetaData;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.vfs.spi.structure.VFSStructuralDeployers;
import org.jboss.deployers.vfs.spi.structure.helpers.AbstractStructureDeployer;
import org.jboss.virtual.VirtualFile;
import org.jboss.virtual.VirtualFileFilter;
import org.jboss.virtual.plugins.vfs.helpers.SuffixMatchFilter;

/**
 * A mock ear structure deployer that illustrates concepts involved with an ear
 * type of deployer.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision:$
 */
public class MockEarStructureDeployer extends AbstractStructureDeployer
{
   /**
    * The default ear/lib filter
    */
   public static final VirtualFileFilter DEFAULT_EAR_LIB_FILTER = new SuffixMatchFilter(".jar");

   /**
    * The ear/lib filter
    */
   private VirtualFileFilter earLibFilter = DEFAULT_EAR_LIB_FILTER;

   @Override
   public int getRelativeOrder()
   {
      return 1000;
   }

   /**
    * Get the earLibFilter.
    * 
    * @return the earLibFilter.
    */
   public VirtualFileFilter getEarLibFilter()
   {
      return earLibFilter;
   }

   /**
    * Set the earLibFilter.
    * 
    * @param earLibFilter
    * @throws IllegalArgumentException for a null filter
    */
   public void setEarLibFilter(VirtualFileFilter earLibFilter)
   {
      if (earLibFilter == null)
         throw new IllegalArgumentException("Null filter");
      this.earLibFilter = earLibFilter;
   }

   public boolean determineStructure(VirtualFile root, VirtualFile parent, VirtualFile file, StructureMetaData metaData, VFSStructuralDeployers deployers) throws DeploymentException
   {
      ContextInfo context = null;
      boolean valid = false;
      try
      {
         if (file.isLeaf() == true || file.getName().endsWith(".ear") == false)
            return false;

         context = createContext(file, "META-INF", metaData);

         VirtualFile applicationProps = getMetaDataFile(file, "META-INF/application.properties");
         boolean scan = true;
         List<EarModule> modules = new ArrayList<EarModule>();
         if (applicationProps != null)
         {
            // This is a simple module-name=earPath properties file
            InputStream in = applicationProps.openStream();
            Properties props = new Properties();
            props.load(in);
            in.close();
            scan = false;
            for(Object key : props.keySet())
            {
               String name = (String) key;
               String fileName = props.getProperty(name);
               EarModule module = new EarModule(name, fileName);
               modules.add(module);
            }
         }
         // Add the ear lib contents to the classpath
         String libDir = "lib";
         try
         {
            VirtualFile lib = file.findChild(libDir);
            if (lib != null)
            {
               List<VirtualFile> archives = lib.getChildren(earLibFilter);
               for (VirtualFile archive : archives)
                  super.addClassPath(root, archive, true, true, context);
            }
         }
         catch (IOException ignored)
         {
            // lib directory does not exist
         }

         // Add the ear manifest locations?
         super.addClassPath(root, file, false, true, context);

         // TODO JBMICROCONT-185 need to scan for annotationss
         if (scan)
         {
            throw new RuntimeException("Scanning not implemented: "+file.getName());
         }
         else
         {
            // Create subdeployments for the ear modules
            for(EarModule mod : modules)
            {
               String fileName = mod.getFileName();
               if (fileName != null && (fileName = fileName.trim()).length() > 0)
               {
                  try
                  {
                     VirtualFile module = file.findChild(fileName);
                     if (module == null)
                     {
                        throw new RuntimeException(fileName
                                    + " module listed in application.xml does not exist within .ear "
                                    + file.getName());
                     }
                     // Ask the deployers to analyze this
                     if (deployers.determineStructure(root, file, module, metaData) == false)
                     {
                        throw new RuntimeException(fileName
                              + " module listed in application.xml is not a recognized deployment, .ear: "
                              + file.getName());
                     }
                  }
                  catch (IOException e)
                  {
                     throw new RuntimeException(fileName
                                 + " module listed in application.xml does not exist within .ear "
                                 + file.getName(), e);
                  }
               }
            }
         }

         valid = true;
      }
      catch (Exception e)
      {
         throw new RuntimeException("Error determining structure: "+ file.getName(), e);
      }

      return valid;
   }

   private VirtualFile getMetaDataFile(VirtualFile file, String path)
   {
      VirtualFile metaFile = null;
      try
      {
         metaFile = file.findChild(path);
      }
      catch (IOException e)
      {
      }
      return metaFile;
   }
}
