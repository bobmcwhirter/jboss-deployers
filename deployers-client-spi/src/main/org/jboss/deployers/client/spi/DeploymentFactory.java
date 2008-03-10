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
package org.jboss.deployers.client.spi;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jboss.deployers.spi.attachments.Attachments;
import org.jboss.deployers.spi.attachments.AttachmentsFactory;
import org.jboss.deployers.spi.attachments.MutableAttachments;
import org.jboss.deployers.spi.attachments.PredeterminedManagedObjectAttachments;
import org.jboss.deployers.spi.structure.ClassPathEntry;
import org.jboss.deployers.spi.structure.ContextInfo;
import org.jboss.deployers.spi.structure.StructureMetaData;
import org.jboss.deployers.spi.structure.StructureMetaDataFactory;

/**
 * DeploymentFactory.
 * 
 * @author <a href="adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class DeploymentFactory
{
   /**
    * Add a context to a deployment
    * 
    * @param context the context
    * @param path the path
    * @return the context info
    * @throws IllegalArgumentException for a null parameter
    */
   public ContextInfo addContext(PredeterminedManagedObjectAttachments context, String path)
   {
      StructureMetaData structure = assureStructure(context);
      ContextInfo result = StructureMetaDataFactory.createContextInfo(path);
      structure.addContext(result);
      return result;
   }

   /**
    * Add a context to a deployment
    * 
    * @param context the context
    * @param path the path
    * @param classPath the classpath
    * @return the context info
    * @throws IllegalArgumentException for a null parameter
    */
   public ContextInfo addContext(PredeterminedManagedObjectAttachments context, String path, List<ClassPathEntry> classPath)
   {
      StructureMetaData structure = assureStructure(context);
      ContextInfo result = StructureMetaDataFactory.createContextInfo(path, classPath);
      structure.addContext(result);
      return result;
   }

   /**
    * Add a context to a deployment
    * 
    * @param context the context
    * @param path the path
    * @param metaDataPath the meta data path
    * @param classPath the classpath
    * @return the context info
    * @throws IllegalArgumentException for a null parameter
    */
   public ContextInfo addContext(PredeterminedManagedObjectAttachments context, String path, String metaDataPath, List<ClassPathEntry> classPath)
   {
      StructureMetaData structure = assureStructure(context);
      ContextInfo result = StructureMetaDataFactory.createContextInfo(path, metaDataPath, classPath);
      structure.addContext(result);
      return result;
   }
   
   /**
    * Add a context to a deployment
    *
    * @param context the context
    * @param path the path
    * @param metaDataPath the meta data path
    * @param classPath the classpath
    * @return the context info
    * @throws IllegalArgumentException for a null parameter
    */
   public ContextInfo addContext(PredeterminedManagedObjectAttachments context, String path, List<String> metaDataPath, List<ClassPathEntry> classPath)
   {
      StructureMetaData structure = assureStructure(context);
      ContextInfo result = StructureMetaDataFactory.createContextInfo(path, metaDataPath, classPath);
      structure.addContext(result);
      return result;
   }

   /**
    * Create a new classpath entry
    * 
    * @param path the path
    * @return the classpath entry
    * @throws IllegalArgumentException for a null path
    */
   public static ClassPathEntry createClassPathEntry(String path)
   {
      return StructureMetaDataFactory.createClassPathEntry(path, null);
   }
   
   /**
    * Create a new classpath
    * 
    * @param path the path
    * @return the classpath
    * @throws IllegalArgumentException for a null path
    */
   public static List<ClassPathEntry> createClassPath(String path)
   {
      return Collections.singletonList(createClassPathEntry(path));
   }
   
   /**
    * Create a new classpath entry
    * 
    * @param path the path
    * @param suffixes the suffixes
    * @return the classpath entry
    * @throws IllegalArgumentException for a null path
    */
   public static ClassPathEntry createClassPathEntry(String path, String suffixes)
   {
      return StructureMetaDataFactory.createClassPathEntry(path, suffixes);
   }
   
   /**
    * Create a new classpath 
    * 
    * @param path the path
    * @param suffixes the suffixes
    * @return the classpath
    * @throws IllegalArgumentException for a null path
    */
   public static List<ClassPathEntry> createClassPath(String path, String suffixes)
   {
      return Collections.singletonList(createClassPathEntry(path, suffixes));
   }
   
   /**
    * Assure the context has a predetermined structure
    * 
    * @param context the context
    * @return the structure
    */
   protected static StructureMetaData assureStructure(PredeterminedManagedObjectAttachments context)
   {
      if (context == null)
         throw new IllegalArgumentException("Null context");

      MutableAttachments mutable;
      
      Attachments attachments = context.getPredeterminedManagedObjects();

      // Nothing predetermined yet
      if (attachments == null)
      {
         mutable = AttachmentsFactory.createMutableAttachments();;
         context.setPredeterminedManagedObjects(mutable);
      }
      // Some predetermined but needs to be made mutable
      else if (attachments instanceof MutableAttachments == false)
      {
         mutable = AttachmentsFactory.createMutableAttachments();
         Map<String, Object> map = attachments.getAttachments();
         if (map != null)
            mutable.setAttachments(map);
         context.setPredeterminedManagedObjects(mutable);
      }
      else
      {
         mutable = (MutableAttachments) attachments;
      }
      
      StructureMetaData structure = mutable.getAttachment(StructureMetaData.class);
      // No previous structure
      if (structure == null)
      {
         structure = StructureMetaDataFactory.createStructureMetaData();
         mutable.addAttachment(StructureMetaData.class, structure);
      }
      return structure;
   }
}
