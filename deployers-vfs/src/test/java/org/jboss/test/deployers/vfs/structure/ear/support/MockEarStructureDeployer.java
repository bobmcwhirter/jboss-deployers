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
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.annotations.AnnotationEnvironment;
import org.jboss.deployers.spi.structure.ContextInfo;
import org.jboss.deployers.vfs.plugins.structure.AbstractVFSStructureDeployer;
import org.jboss.deployers.vfs.spi.structure.CandidateAnnotationsCallback;
import org.jboss.deployers.vfs.spi.structure.StructureContext;
import org.jboss.logging.Logger;
import org.jboss.virtual.VFSUtils;
import org.jboss.virtual.VirtualFile;
import org.jboss.virtual.VirtualFileFilter;
import org.jboss.virtual.plugins.vfs.helpers.SuffixMatchFilter;

/**
 * A mock ear structure deployer that illustrates concepts involved with an ear
 * type of deployer.
 *
 * @author Scott.Stark@jboss.org
 * @author Ales.Justin@jboss.org
 * @version $Revision:$
 */
public class MockEarStructureDeployer extends AbstractVFSStructureDeployer
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
    * @param earLibFilter the filter
    * @throws IllegalArgumentException for a null filter
    */
   public void setEarLibFilter(VirtualFileFilter earLibFilter)
   {
      if (earLibFilter == null)
         throw new IllegalArgumentException("Null filter");
      this.earLibFilter = earLibFilter;
   }

   public boolean determineStructure(StructureContext structureContext) throws DeploymentException
   {
      ContextInfo context;
      boolean valid;
      VirtualFile file = structureContext.getFile();
      try
      {
         if (file.isLeaf() == true || file.getName().endsWith(".ear") == false)
            return false;

         context = createContext(structureContext, "META-INF");

         VirtualFile applicationProps = getMetaDataFile(file, "META-INF/application.properties");
         VirtualFile jbossProps = getMetaDataFile(file, "META-INF/jboss-application.properties");
         boolean scan = true;
         List<EarModule> modules = new ArrayList<EarModule>();
         if (applicationProps != null)
         {
            scan = false;
            readAppXml(applicationProps, modules);
         }
         if (jbossProps != null)
         {
            readAppXml(jbossProps, modules);
         }
         // Add the ear lib contents to the classpath
         try
         {
            VirtualFile lib = file.getChild("lib");
            if (lib != null)
            {
               List<VirtualFile> archives = lib.getChildren(earLibFilter);
               for (VirtualFile archive : archives)
                  super.addClassPath(structureContext, archive, true, true, context);
            }
         }
         catch (IOException ignored)
         {
            // lib directory does not exist
         }

         // Add the ear manifest locations?
         addClassPath(structureContext, file, false, true, context);

         if (scan)
            scanEar(structureContext, file, modules);

         // Create subdeployments for the ear modules
         for (EarModule mod : modules)
         {
            String fileName = mod.getFileName();
            if (fileName != null && (fileName = fileName.trim()).length() > 0)
            {
               try
               {
                  VirtualFile module = file.getChild(fileName);
                  if (module == null)
                  {
                     throw new RuntimeException(fileName
                           + " module listed in application.xml does not exist within .ear "
                           + file.getName());
                  }
                  // Ask the deployers to analyze this
                  if (structureContext.determineChildStructure(module) == false)
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

         valid = true;
      }
      catch (Exception e)
      {
         throw new RuntimeException("Error determining structure: " + file.getName(), e);
      }

      return valid;
   }

   protected void readAppXml(VirtualFile file, List<EarModule> modules)
         throws IOException
   {
      InputStream in = file.openStream();
      try
      {
         Properties props = new Properties();
         props.load(in);
         for (Object key : props.keySet())
         {
            String name = (String)key;
            String fileName = props.getProperty(name);
            EarModule module = new EarModule(name, fileName);
            modules.add(module);
         }
      }
      finally
      {
         in.close();
      }
   }

   private void scanEar(StructureContext context, VirtualFile root, List<EarModule> modules) throws Exception
   {
      List<VirtualFile> archives = root.getChildren();
      if (archives != null && archives.isEmpty() == false)
      {
         // enable candidate annotations
         context.setCandidateAnnotationScanning(true);
         EarCandidateAnnotationsCallback callback = new EarCandidateAnnotationsCallback();
         context.addCallback(callback);

         String earPath = root.getPathName();
         int counter = 0;
         for (VirtualFile vfArchive : archives)
         {
            String filename = earRelativePath(earPath, vfArchive.getPathName());
            // Check if the module already exists, i.e. it is declared in jboss-app.xml
            EarModule moduleMetaData = getModule(modules, filename);
            if (moduleMetaData == null)
            {
               // reset callback result
               callback.reset();

               int type = typeFromSuffix(context, filename, vfArchive);
               Integer callbackResult = null;
               if (type < 0)
               {
                  callbackResult = callback.getResult();
                  if (callbackResult != null)
                     type = callbackResult;
               }

               if (type >= 0)
               {
                  String typeString = null;
                  switch(type)
                  {
                     case J2eeModuleMetaData.EJB:
                        typeString = "Ejb";
                        break;
                     case J2eeModuleMetaData.CLIENT:
                        typeString = "Java";
                        break;
                     case J2eeModuleMetaData.CONNECTOR:
                        typeString = "Connector";
                        break;
                     case J2eeModuleMetaData.SERVICE:
                     case J2eeModuleMetaData.HAR:
                        typeString = "Service";
                        break;
                     case J2eeModuleMetaData.WEB:
                        typeString = "Web";
                        break;
                  }
                  // we didin't do full recognition yet
                  if (callbackResult == null)
                  {
                     moduleMetaData = new EarModule(typeString + "Module" + counter, filename);
                     modules.add(moduleMetaData);
                     counter++;
                  }
               }
            }
         }

         // reset candidate annotation scanning flag
         context.setCandidateAnnotationScanning(false);
      }
   }

   private EarModule getModule(List<EarModule> modules, String filename)
   {
      for(EarModule em : modules)
         if (filename.endsWith(em.getFileName()))
            return em;
      return null;
   }

   private int typeFromSuffix(StructureContext context, String path, VirtualFile archive) throws Exception
   {
      int type = -1;

      if (path.endsWith(".war"))
         type = J2eeModuleMetaData.WEB;
      else if (path.endsWith(".rar"))
         type = J2eeModuleMetaData.CONNECTOR;
      else if (path.endsWith(".har"))
         type = J2eeModuleMetaData.HAR;
      else if (path.endsWith(".sar"))
         type = J2eeModuleMetaData.SERVICE;
      else if (path.endsWith(".jar"))
      {
         // Look for a META-INF/application-client.xml
         VirtualFile mfFile = getMetaDataFile(archive, "META-INF/MANIFEST.MF");
         VirtualFile clientXml = getMetaDataFile(archive, "META-INF/application-client.xml");
         VirtualFile ejbXml = getMetaDataFile(archive, "META-INF/ejb-jar.xml");
         VirtualFile jbossXml = getMetaDataFile(archive, "META-INF/jboss.xml");
         //VirtualFile seamXml = getMetaDataFile(archive, "META-INF/components.xml");

         if (clientXml != null)
         {
            type = J2eeModuleMetaData.CLIENT;
         }
         else if (mfFile != null)
         {
            Manifest mf = VFSUtils.readManifest(mfFile);
            Attributes attrs = mf.getMainAttributes();
            if (attrs.containsKey(Attributes.Name.MAIN_CLASS))
            {
               type = J2eeModuleMetaData.CLIENT;
            }
            else
            {
               determineType(context, archive);
            }
         }
         else if (ejbXml != null || jbossXml != null) // || seamXml != null)
         {
            type = J2eeModuleMetaData.EJB;
         }
         else
         {
            determineType(context, archive);
         }
      }

      return type;
   }

   private void determineType(StructureContext context, VirtualFile archive) throws Exception
   {
      context.determineChildStructure(archive);
   }

   private String earRelativePath(String earPath, String pathName)
   {
      StringBuilder tmp = new StringBuilder(pathName);
      tmp.delete(0, earPath.length());
      return tmp.toString();
   }

   private VirtualFile getMetaDataFile(VirtualFile file, String path)
   {
      VirtualFile metaFile = null;
      try
      {
         metaFile = file.getChild(path);
      }
      catch (IOException ignored)
      {
      }
      return metaFile;
   }

   private static class EarCandidateAnnotationsCallback implements CandidateAnnotationsCallback
   {
      private static final Logger log = Logger.getLogger(EarCandidateAnnotationsCallback.class);
      private Integer result;

      private final static Map<Class<? extends Annotation>, Integer> map;

      static
      {
         map = new HashMap<Class<? extends Annotation>, Integer>();
         map.put(Stateless.class, J2eeModuleMetaData.EJB);
         map.put(Service.class, J2eeModuleMetaData.SERVICE);
         map.put(AppClient.class, J2eeModuleMetaData.CLIENT);
         map.put(Servlet.class, J2eeModuleMetaData.WEB);
      }

      public void executeCallback(VirtualFile root, StructureContext currentContext, AnnotationEnvironment env, Class<? extends Annotation> annotationClass)
      {
         if (result == null)
         {
            result = map.get(annotationClass);
         }
         else
         {
            log.warn("Result already set: " + result);
         }
      }

      public void reset()
      {
         result = null;
      }

      public Integer getResult()
      {
         return result;
      }
   }
}
