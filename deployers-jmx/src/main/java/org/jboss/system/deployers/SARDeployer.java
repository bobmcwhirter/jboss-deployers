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
package org.jboss.system.deployers;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.vfs.spi.deployer.JAXPDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.system.metadata.ServiceDeployment;
import org.jboss.system.metadata.ServiceDeploymentClassPath;
import org.jboss.system.metadata.ServiceDeploymentParser;
import org.jboss.system.metadata.ServiceMetaData;
import org.jboss.system.metadata.ServiceMetaDataParser;
import org.jboss.util.xml.DOMWriter;
import org.jboss.virtual.VFS;
import org.jboss.virtual.VirtualFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * SARDeployer.<p>
 * 
 * This deployer is responsible for looking for -service.xml
 * and creating the metadata object.<p>
 * 
 * The {@link ServiceClassLoaderDeployer} and {@link ServiceDeployer} does the
 * real work of deployment.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @author Scott.Stark@jboss.org
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 * @version $Revision$
 */
public class SARDeployer extends JAXPDeployer<ServiceDeployment>
{
   /** The default codebase root */
   private URL defaultCodeBaseRoot;
   
   /**
    * Create a new SARDeployer.
    * 
    * @throws IllegalArgumentException for a null kernel
    */
   public SARDeployer()
   {
      super(ServiceDeployment.class);
      setSuffix("-service.xml");
      // Enable the super class ManagedObjectCreator implementation
      setBuildManagedObject(true);
      setAllowMultipleFiles(true);
   }

   /**
    * Get the defaultCodeBaseRoot.
    * 
    * @return the defaultCodeBaseRoot.
    */
   public URL getDefaultCodeBaseRoot()
   {
      return defaultCodeBaseRoot;
   }

   /**
    * Set the defaultCodeBaseRoot.
    * 
    * @param defaultCodeBaseRoot the defaultCodeBaseRoot.
    */
   public void setDefaultCodeBaseRoot(URL defaultCodeBaseRoot)
   {
      this.defaultCodeBaseRoot = defaultCodeBaseRoot;
   }

   /**
    * @param unit - the deployment unit
    * @param file - the vf for the jboss-service.xml descriptor
    * @param document - the jaxp document for the jboss-service.xml descriptor
    */
   @Override
   protected ServiceDeployment parse(VFSDeploymentUnit unit, VirtualFile file, Document document) throws Exception
   {
      ServiceDeploymentParser parser = new ServiceDeploymentParser(document);
      ServiceDeployment parsed = parser.parse();
      String name = file.toURI().toString();
      parsed.setName(name);

      List<ServiceDeploymentClassPath> classPaths = parsed.getClassPaths();
      if (classPaths != null)
         processXMLClasspath(unit, classPaths);
      
      List<ServiceMetaData> services = parsed.getServices();
      if (services == null)
      {
         Element config = parsed.getConfig();
         if (config == null)
         {
            log.debugf("Service deployment has no services: %1s", parsed.getName());
            return parsed;
         }
         if (log.isDebugEnabled())
         {
            String docStr = DOMWriter.printNode(config, true);
            int index = docStr.toLowerCase().indexOf("password"); 
            if (index != -1)
            {
               docStr = maskPasswords(docStr, index);
            }
            log.debug(docStr);
         }
         ServiceMetaDataParser SMDparser = new ServiceMetaDataParser(config);
         services = SMDparser.parse();
         parsed.setServices(services);
      }

      return parsed;
   }

   /**
    * Process the xml classpath
    * 
    * @param unit the unit
    * @param classpaths the classpaths
    * @throws Exception for any error
    */
   private void processXMLClasspath(VFSDeploymentUnit unit, List<ServiceDeploymentClassPath> classpaths) throws Exception
   {
      ArrayList<VirtualFile> classpath = new ArrayList<VirtualFile>();

      for (ServiceDeploymentClassPath path : classpaths)
      {
         String codebase = path.getCodeBase();
         String archives = path.getArchives();

         log.debugf("Processing classpath: %1s codebase=%2s archives=%3s", unit.getName(), codebase, archives);
         VirtualFile codebaseFile = unit.getRoot();
         if (".".equals(codebase) == false)
         {
            if (defaultCodeBaseRoot == null)
               throw new DeploymentException("No default codebase root for " + codebase + " in " + unit.getName());
            URL codeBaseURL = new URL(defaultCodeBaseRoot, codebase);
            codebaseFile = VFS.getRoot(codeBaseURL);
         }

         if (codebaseFile == null)
            throw new DeploymentException("Cannot use classpath without a root: " + unit.getName());

         if (archives == null)
         {
            classpath.add(codebaseFile);
            log.debugf("Using codebase as classpath: %1s", unit.getName());
         }
         else
         {
            SARArchiveFilter filter = new SARArchiveFilter(archives);
            List<VirtualFile> archiveFiles = codebaseFile.getChildren(filter);
            classpath.addAll(archiveFiles);
         }
      }

      unit.prependClassPath(classpath);
   }
   
   private String maskPasswords(String original, int index)
   {
      StringBuilder sb = new StringBuilder(original);
      String modified;
      int startPasswdStringIndex = sb.indexOf(">", index);
      if (startPasswdStringIndex != -1)
      {
         // checks if the keyword 'password' was not in a comment
         if (sb.charAt(startPasswdStringIndex - 1) != '-')
         {
            int endPasswdStringIndex = sb.indexOf("<", startPasswdStringIndex);
            if (endPasswdStringIndex != -1) // shouldn't happen, but check anyway
            {
               sb.replace(startPasswdStringIndex + 1, endPasswdStringIndex, "****");
            }
         }
         modified = sb.toString();
         // unlikely event of more than one password
         index = modified.toLowerCase().indexOf("password", startPasswdStringIndex);
         if (index != -1)
            return maskPasswords(modified, index);
         return modified;
      }
      return original;
   }
}
