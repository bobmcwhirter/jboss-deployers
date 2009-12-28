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
package org.jboss.deployers.spi.structure;

import java.util.List;

/**
 * StructureFactory.
 * 
 * @author <a href="adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public abstract class StructureMetaDataFactory
{
   /**
    * Create a new StructureMetaData.
    * 
    * @return the structure metadata
    */
   public static StructureMetaData createStructureMetaData()
   {
      return StructureMetaDataBuilder.getInstance().newStructureMetaData();
   }

   /**
    * Create a new ContextInfo.
    * 
    * @return the context info
    * @throws IllegalArgumentException for a null path
    */
   public static ContextInfo createContextInfo()
   {
      return StructureMetaDataBuilder.getInstance().newContextInfo("");
   }

   /**
    * Create a new ContextInfo.
    * 
    * @param path the path
    * @return the context info
    * @throws IllegalArgumentException for a null path
    */
   public static ContextInfo createContextInfo(String path)
   {
      return StructureMetaDataBuilder.getInstance().newContextInfo(path);
   }

   /**
    * Create a new ContextInfo.
    * 
    * @param path the path
    * @param classPath the classpath
    * @return the context info
    * @throws IllegalArgumentException for a null path
    */
   public static ContextInfo createContextInfo(String path, List<ClassPathEntry> classPath)
   {
      return StructureMetaDataBuilder.getInstance().newContextInfo(path, classPath);
   }

   /**
    * Create a new ContextInfo.
    * 
    * @param path the path
    * @param metaDataPath the metadata path
    * @param classPath the class path
    * @return the context info
    * @throws IllegalArgumentException for a null path or metadata path
    */
   public static ContextInfo createContextInfo(String path, MetaDataEntry metaDataPath, List<ClassPathEntry> classPath)
   {
      return StructureMetaDataBuilder.getInstance().newContextInfo(path, metaDataPath, classPath);
   }

   /**
    * Create a new ContextInfo.
    *
    * @param path the path
    * @param metaDataPath the metadata path
    * @param classPath the class path
    * @return the context info
    * @throws IllegalArgumentException for a null path or metadata path
    */
   public static ContextInfo createContextInfo(String path, List<MetaDataEntry> metaDataPath, List<ClassPathEntry> classPath)
   {
      return StructureMetaDataBuilder.getInstance().newContextInfo(path, metaDataPath, classPath);
   }

   /**
    * Create a new MetaData entry.
    *
    * @param metaDataPath the metadata path
    * @return new metadata path entry
    */
   public static MetaDataEntry createMetaDataEntry(String metaDataPath)
   {
      return StructureMetaDataBuilder.getInstance().newMetaDataPathEntry(metaDataPath);
   }

   /**
    * Create a new MetaData entry.
    *
    * @param metaDataPath the metadata path
    * @param type the type
    * @return new metadata path entry
    */
   public static MetaDataEntry createMetaDataEntry(String metaDataPath, MetaDataType type)
   {
      return StructureMetaDataBuilder.getInstance().newMetaDataPathEntry(metaDataPath, type);
   }

   /**
    * Create a new classpath entry
    * 
    * @return the classpath entry
    */
   public static ClassPathEntry createClassPathEntry()
   {
      return StructureMetaDataBuilder.getInstance().newClassPathEntry("", null);
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
      return StructureMetaDataBuilder.getInstance().newClassPathEntry(path, null);
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
      return StructureMetaDataBuilder.getInstance().newClassPathEntry(path, suffixes);
   }

   /**
    * Create a new StructureMetaData.
    * 
    * @return the structure metadata
    */
   protected abstract StructureMetaData newStructureMetaData();

   /**
    * Create a new ContextInfo.
    * 
    * @param path the path
    * @return the context info
    * @throws IllegalArgumentException for a null path
    */
   protected abstract ContextInfo newContextInfo(String path);

   /**
    * Create a new ContextInfo.
    * 
    * @param path the path
    * @param classPath the classpath
    * @return the context info
    * @throws IllegalArgumentException for a null path
    */
   protected abstract ContextInfo newContextInfo(String path, List<ClassPathEntry> classPath);

   /**
    * Create a new ContextInfo.
    * 
    * @param path the path
    * @param metaDataPath the metadata path
    * @param classPath the class path
    * @return the context info
    * @throws IllegalArgumentException for a null path or metadata path
    */
   protected abstract ContextInfo newContextInfo(String path, MetaDataEntry metaDataPath, List<ClassPathEntry> classPath);
   
   /**
    * Create a new ContextInfo.
    *
    * @param path the path
    * @param metaDataPath the metadata path
    * @param classPath the class path
    * @return the context info
    * @throws IllegalArgumentException for a null path or metadata path
    */
   protected abstract ContextInfo newContextInfo(String path, List<MetaDataEntry> metaDataPath, List<ClassPathEntry> classPath);

   /**
    * Create new metadata path entry.
    *
    * @param metaDataPath the metadata path
    * @return new metadata path entry
    * @throws IllegalArgumentException for a null path or metadata path
    */
   protected abstract MetaDataEntry newMetaDataPathEntry(String metaDataPath);

   /**
    * Create new metadata path entry.
    *
    * @param metaDataPath the metadata path
    * @param type the metadata path type
    * @return new metadata path entry
    * @throws IllegalArgumentException for a null path or metadata path
    */
   protected abstract MetaDataEntry newMetaDataPathEntry(String metaDataPath, MetaDataType type);

   /**
    * Create a new classpath entry
    * 
    * @param path the path
    * @param suffixes the suffixes
    * @return the classpath entry
    * @throws IllegalArgumentException for a null path
    */
   protected abstract ClassPathEntry newClassPathEntry(String path, String suffixes);
}
