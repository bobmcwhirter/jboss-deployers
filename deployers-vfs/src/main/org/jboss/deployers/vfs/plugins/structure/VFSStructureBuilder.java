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
package org.jboss.deployers.vfs.plugins.structure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.structure.ClassPathEntry;
import org.jboss.deployers.spi.structure.ContextInfo;
import org.jboss.deployers.structure.spi.DeploymentContext;
import org.jboss.deployers.structure.spi.helpers.AbstractStructureBuilder;
import org.jboss.deployers.vfs.spi.client.VFSDeployment;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentContext;
import org.jboss.logging.Logger;
import org.jboss.virtual.VFSUtils;
import org.jboss.virtual.VirtualFile;
import org.jboss.virtual.VisitorAttributes;
import org.jboss.virtual.plugins.vfs.helpers.SuffixMatchFilter;

/**
 * VFSStructureBuilder.
 * 
 * @author <a href="adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class VFSStructureBuilder extends AbstractStructureBuilder
{
   /** The log */
   private static final Logger log = Logger.getLogger(VFSStructureBuilder.class);
   
   protected DeploymentContext createRootDeploymentContext(Deployment deployment) throws Exception
   {
      if (deployment instanceof VFSDeployment)
      {
         VFSDeployment vfsDeployment = (VFSDeployment) deployment;
         String name = deployment.getName();
         String simpleName = deployment.getSimpleName();
         if (name == null)
         {
            return new AbstractVFSDeploymentContext(vfsDeployment.getRoot(), "");
         }
         else
         {
            if (simpleName == null)
               return new AbstractVFSDeploymentContext(name, name, vfsDeployment.getRoot(), "");
            else
               return new AbstractVFSDeploymentContext(name, simpleName, vfsDeployment.getRoot(), "");
         }
      }
      return super.createRootDeploymentContext(deployment);
   }

   protected DeploymentContext createChildDeploymentContext(DeploymentContext parent, ContextInfo child) throws Exception
   {
      if (parent instanceof VFSDeploymentContext)
      {
         VFSDeploymentContext vfsParent = (VFSDeploymentContext) parent;
         String path = child.getPath();
         try
         {
            VirtualFile parentFile = vfsParent.getRoot();
            VirtualFile file = parentFile.findChild(path); // leaving the findChild usage
            return new AbstractVFSDeploymentContext(file, path);
         }
         catch (Throwable t)
         {
            throw DeploymentException.rethrowAsDeploymentException("Unable to determine child " + path + " from parent " + vfsParent.getRoot().getName(), t);
         }
      }
      return super.createChildDeploymentContext(parent, child);
   }

   protected void applyContextInfo(DeploymentContext context, ContextInfo contextInfo) throws Exception
   {
      super.applyContextInfo(context, contextInfo);
      
      if (context instanceof VFSDeploymentContext)
      {
         boolean trace = log.isTraceEnabled();
         log.trace("Apply context: " + context.getName() + " " + contextInfo);
         
         VFSDeploymentContext vfsContext = (VFSDeploymentContext) context;
         List<String> metaDataPath = contextInfo.getMetaDataPath();
         if (metaDataPath != null && metaDataPath.isEmpty() == false)
            vfsContext.setMetaDataPath(contextInfo.getMetaDataPath());
         
         boolean classPathHadVF = false;

         List<ClassPathEntry> classPathEntries = contextInfo.getClassPath();
         VFSDeploymentContext top = vfsContext.getTopLevel();
         VirtualFile root = top.getRoot();
         List<VirtualFile> classPath = new ArrayList<VirtualFile>();

         if (classPathEntries != null)
         {
            for (ClassPathEntry entry : classPathEntries)
            {
               if (trace)
                  log.trace("Resolving classpath entry " + entry + " for " + context.getName());
               String suffixes = entry.getSuffixes();
               VirtualFile child;
               if (entry.getPath().length() == 0)
                  child = root;
               else
               {
                  try
                  {
                     child = root.findChild(entry.getPath()); // leaving the findChild
                  }
                  catch (Throwable t)
                  {
                     throw DeploymentException.rethrowAsDeploymentException("Unable to find class path entry " + entry + " from " + root.getName(), t);
                  }
               }
               if (suffixes == null)
               {
                  classPath.add(child);
                  if (classPathHadVF == false)
                     classPathHadVF = child.equals(root);
               }
               else
               {
                  String[] suffs = suffixes.split(",");
                  SuffixMatchFilter filter = new SuffixMatchFilter(Arrays.asList(suffs), VisitorAttributes.DEFAULT);
                  List<VirtualFile> matches = child.getChildren(filter);
                  if( matches != null )
                  {
                     classPath.addAll(matches);
                     if (trace)
                        log.trace("Added classpath matches: " + matches);
                     // Process any Manifest Class-Path refs
                     for (VirtualFile file : matches)
                     {
                        VFSUtils.addManifestLocations(file, classPath);
                        if (classPathHadVF == false)
                           classPathHadVF = file.equals(root);
                     }
                  }
               }
            }
         }
         
         VirtualFile file = vfsContext.getRoot();
         if (classPathHadVF == false && SecurityActions.isLeaf(file) == false)
            VFSUtils.addManifestLocations(file, classPath);

         if (classPath.isEmpty() == false)
            vfsContext.setClassPath(classPath);
      }
   }
}
