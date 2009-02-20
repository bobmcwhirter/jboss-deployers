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
package org.jboss.deployers.vfs.plugins.structure.jar;

import java.io.IOException;
import java.util.Set;

import org.jboss.beans.metadata.api.annotations.Install;
import org.jboss.beans.metadata.api.annotations.Uninstall;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.matchers.JarExtensionProvider;
import org.jboss.deployers.spi.structure.ContextInfo;
import org.jboss.deployers.vfs.plugins.structure.AbstractVFSStructureDeployer;
import org.jboss.deployers.vfs.spi.structure.StructureContext;
import org.jboss.virtual.VirtualFile;
import org.jboss.virtual.plugins.context.jar.JarUtils;

/**
 * JARStructure.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 * @version $Revision: 1.1 $
 */
public class JARStructure extends AbstractVFSStructureDeployer
{
   /**
    * Create a new JARStructure. with the default suffixes
    */
   public JARStructure()
   {
      this(null);
   }

   /**
    * Sets the default relative order 10000.
    *
    * @param suffixes the suffixes
    */
   public JARStructure(Set<String> suffixes)
   {
      if (suffixes != null)
         setSuffixes(suffixes);
      setRelativeOrder(10000);
   }

   /**
    * Gets the set of suffixes recognised as jars
    * 
    * @return the set of suffixes
    */
   public Set<String> getSuffixes()
   {
      return JarUtils.getSuffixes();
   }
   /**
    * Gets the set of suffixes recognised as jars
    * 
    * @param suffixes - the set of suffixes
    */
   public void setSuffixes(Set<String> suffixes)
   {
      JarUtils.setJarSuffixes(suffixes);
   }

   @Install
   public void addJarExtension(JarExtensionProvider provider)
   {
      String extension = provider.getJarExtension();
      if (extension != null)
         JarUtils.addJarSuffix(extension);
   }

   @Uninstall
   public void removeJarExtension(JarExtensionProvider provider)
   {
      String extension = provider.getJarExtension();
      if (extension != null)
         JarUtils.removeJarSuffix(extension);
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
            // For non top level directories that don't look like jars
            // we require a META-INF otherwise each subdirectory would be a subdeployment
            if (JarUtils.isArchive(file.getName()) == false)
            {
               if (structureContext.isTopLevel() == false)
               {
                  try
                  {
                     VirtualFile child = file.getChild("META-INF");
                     if (child != null)
                     {
                        if (trace)
                           log.trace("... ok - non top level directory has a META-INF subdirectory");
                     }
                     else
                     {
                        if (trace)
                           log.trace("... no - doesn't look like a jar and no META-INF subdirectory.");
                        return false;
                     }
                  }
                  catch (IOException e)
                  {
                     log.warn("Exception while checking if file is a jar: " + e);
                     return false;
                  }
               }
               else if (trace)
               {
                  log.trace("... ok - doesn't look like a jar but it is a top level directory.");
               }
            }
            else
            {
               log.trace("... ok - its an archive or at least pretending to be");
            }
         }
         else if (JarUtils.isArchive(file.getName()))
         {
            if (trace)
               log.trace("... ok - its an archive or at least pretending to be.");
         }
         else
         {
            if (trace)
               log.trace("... no - not a directory or an archive.");
            return false;
         }

         boolean valid = true;

         if (isSupportsCandidateAnnotations())
         {
            StructureContext parentContext = structureContext.getParentContext();
            if (parentContext != null && parentContext.isCandidateAnnotationScanning())
            {
               valid = checkCandidateAnnotations(structureContext, file);
               if (trace)
                  log.trace("... candidate annotations for " + file + " returned " + valid);
            }
         }

         if (valid)
         {
            // Create a context for this jar file with META-INF as the location for metadata
            context = createContext(structureContext, "META-INF");

            // The classpath is the root
            addClassPath(structureContext, file, true, true, context);

            // We try all the children as potential subdeployments
            addAllChildren(structureContext);
         }
         return valid;
      }
      catch (Exception e)
      {
         // Remove the invalid context
         if(context != null)
            structureContext.removeChild(context);

         throw DeploymentException.rethrowAsDeploymentException("Error determining structure: " + file.getName(), e);
      }
   }
}
