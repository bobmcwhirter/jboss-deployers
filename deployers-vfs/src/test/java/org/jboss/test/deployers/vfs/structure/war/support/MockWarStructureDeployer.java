/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.deployers.vfs.structure.war.support;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.structure.ContextInfo;
import org.jboss.deployers.vfs.plugins.structure.AbstractVFSStructureDeployer;
import org.jboss.deployers.vfs.spi.structure.StructureContext;
import org.jboss.vfs.VirtualFile;
import org.jboss.vfs.VirtualFileFilter;
import org.jboss.vfs.VisitorAttributes;
import org.jboss.vfs.util.automount.Automounter;
import org.jboss.vfs.util.SuffixMatchFilter;

/**
 * MockWarStructure.
 *
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class MockWarStructureDeployer extends AbstractVFSStructureDeployer
{
   /** The default filter which allows jars/jar directories */
   public static final VirtualFileFilter DEFAULT_WEB_INF_LIB_FILTER = new SuffixMatchFilter(".jar", VisitorAttributes.DEFAULT);

   /** The web-inf/lib filter */
   private VirtualFileFilter webInfLibFilter = DEFAULT_WEB_INF_LIB_FILTER;

   /** The web-inf/lib/[some-archive]/META-INF filter */
   private VirtualFileFilter webInfLibMetaDataFilter;

   /**
    * Sets the default relative order 1000.
    *
    */
   public MockWarStructureDeployer()
   {
      setRelativeOrder(1000);
   }

   /**
    * Get the webInfLibFilter.
    *
    * @return the webInfLibFilter.
    */
   public VirtualFileFilter getWebInfLibFilter()
   {
      return webInfLibFilter;
   }

   /**
    * Set the webInfLibFilter.
    *
    * @param webInfLibFilter the webInfLibFilter.
    * @throws IllegalArgumentException for a null filter
    */
   public void setWebInfLibFilter(VirtualFileFilter webInfLibFilter)
   {
      if (webInfLibFilter == null)
         throw new IllegalArgumentException("Null filter");
      this.webInfLibFilter = webInfLibFilter;
   }

   /**
    * Get webInfLibMetaDataFilter
    *
    * @return the webInfLibMetaDataFilter
    */
   public VirtualFileFilter getWebInfLibMetaDataFilter()
   {
      return webInfLibMetaDataFilter;
   }

   /**
    * Set the webInfLibMetaDataFilter.
    *
    * @param webInfLibMetaDataFilter the webInfLibFilter.
    */
   public void setWebInfLibMetaDataFilter(VirtualFileFilter webInfLibMetaDataFilter)
   {
      this.webInfLibMetaDataFilter = webInfLibMetaDataFilter;
   }

   public boolean determineStructure(StructureContext structureContext) throws DeploymentException
   {
      ContextInfo context = null;
      VirtualFile file = structureContext.getFile();
      try
      {
         boolean trace = log.isTraceEnabled();

         if (isLeaf(file) == false)
         {
            // We require either a WEB-INF or the name ends in .war
            if (file.getName().endsWith(".war") == false)
            {
               VirtualFile child = file.getChild("WEB-INF");
               if (child.exists())
               {
                  if (trace)
                     log.trace("... ok - directory has a WEB-INF subdirectory");
               }
               else
               {
                  if (trace)
                     log.trace("... no - doesn't look like a war and no WEB-INF subdirectory.");
                  return false;
               }
            }
            else if (trace)
            {
               log.trace("... ok - name ends in .war.");
            }

            List<String> metaDataLocations = new ArrayList<String>();
            metaDataLocations.add("WEB-INF");

            // Check for WEB-INF/classes
            VirtualFile classes = null;
            // The classpath contains WEB-INF/classes
            classes = file.getChild("WEB-INF/classes");
            // Check for a META-INF for metadata
            if (classes.exists())
               metaDataLocations.add("WEB-INF/classes/META-INF");
            // Check for jars in WEB-INF/lib
            List<VirtualFile> archives = null;
            try
            {
               VirtualFile webinfLib = file.getChild("WEB-INF/lib");
               if (webinfLib.exists())
               {
                  archives = webinfLib.getChildren(webInfLibFilter);
                  // Add the jars' META-INF for metadata
                  for (VirtualFile jar : archives)
                  {
                     // either same as plain lib filter, null or accepts the jar
                     if (webInfLibMetaDataFilter == null || webInfLibMetaDataFilter == webInfLibFilter || webInfLibMetaDataFilter.accepts(jar))
                        metaDataLocations.add("WEB-INF/lib/" + jar.getName() + "/META-INF");
                  }
               }
            }
            catch (IOException e)
            {
               log.warn("Exception looking for WEB-INF/lib, " + file.getPathName() + ", " + e);
            }

            // Create a context for this war file and all its metadata locations
            context = createContext(structureContext, metaDataLocations.toArray(new String[metaDataLocations.size()]));

            // Add the war manifest classpath entries
            addClassPath(structureContext, file, false, true, context);

            // Add WEB-INF/classes if present
            if (classes.exists())
               addClassPath(structureContext, classes, true, false, context);
            else if (trace)
               log.trace("No WEB-INF/classes for: " + file.getPathName());

            // and the top level jars in WEB-INF/lib
            if (archives != null)
            {
               for (VirtualFile jar : archives)
               {
                  Automounter.mount(file, jar);
                  addClassPath(structureContext, jar, true, true, context);
               }
            }
            else if (trace)
            {
               log.trace("No WEB-INF/lib for: " + file.getPathName());
            }

            // There are no subdeployments for wars
            return true;
         }
         else
         {
            if (trace)
               log.trace("... no - not a directory or an archive.");
            return false;
         }
      }
      catch (Exception e)
      {
         // Remove the invalid context
         if (context != null)
            structureContext.removeChild(context);

         throw DeploymentException.rethrowAsDeploymentException("Error determining structure: " + file.getName(), e);
      }
   }
}
