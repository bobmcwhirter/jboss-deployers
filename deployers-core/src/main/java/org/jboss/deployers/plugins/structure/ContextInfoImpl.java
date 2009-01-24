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
package org.jboss.deployers.plugins.structure;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jboss.deployers.spi.attachments.helpers.PredeterminedManagedObjectAttachmentsImpl;
import org.jboss.deployers.spi.structure.ClassPathEntry;
import org.jboss.deployers.spi.structure.ContextInfo;
import org.jboss.deployers.spi.structure.ModificationType;

/**
 * ContextInfoImpl.
 * 
 * @author <a href="adrian@jboss.org">Adrian Brock</a>
 * @author <a href="ales.justin@jboss.org">Ales Justin</a>
 * @author Scott.Stark@jboss.org
 * @version $Revision: 1.1 $
 */
public class ContextInfoImpl extends PredeterminedManagedObjectAttachmentsImpl
   implements ContextInfo, Externalizable
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -4384869824260284607L;

   /** The logical path */
   private String path;
   
   /** The metadata path */
   private List<String> metaDataPath;
   
   /** The class path entries */
   private List<ClassPathEntry> classPath = ClassPathEntryImpl.DEFAULT;

   private int relativeOrder = 0;
   
   /** The comparator class name */
   private String comparatorClassName;

   /** The modification type */
   private ModificationType modificationType;

   /**
    * Create a new ContextInfoImpl.
    */
   public ContextInfoImpl()
   {
      setPath("");
   }

   /**
    * Create a new ContextInfoImpl.
    * 
    * @param path the path
    * @throws IllegalArgumentException for a null path
    */
   public ContextInfoImpl(String path)
   {
      setPath(path);
   }

   /**
    * Create a new ContextInfoImpl.
    * 
    * @param path the path
    * @param classPath the classpath
    * @throws IllegalArgumentException for a null path
    */
   public ContextInfoImpl(String path, List<ClassPathEntry> classPath)
   {
      setPath(path);
      setClassPath(classPath);
   }

   /**
    * Create a new ContextInfoImpl.
    * 
    * @param path the path
    * @param metaDataPath a single metadata path
    * @param classPath the class path
    * @throws IllegalArgumentException for a null path or metadata path
    */
   public ContextInfoImpl(String path, String metaDataPath, List<ClassPathEntry> classPath)
   {
      setPath(path);
      if (metaDataPath == null)
         throw new IllegalArgumentException("Null metadata path.");
      setMetaDataPath(Collections.singletonList(metaDataPath));
      setClassPath(classPath);
   }
   
   /**
    * Create a new ContextInfoImpl.
    *
    * @param path the path
    * @param metaDataPath the metadata paths
    * @param classPath the class path
    * @throws IllegalArgumentException for a null path or metadata path
    */
   public ContextInfoImpl(String path, List<String> metaDataPath, List<ClassPathEntry> classPath)
   {
      setPath(path);
      setMetaDataPath(metaDataPath);
      setClassPath(classPath);
   }

   public String getPath()
   {
      return path;
   }

   /**
    * Set the path.
    * 
    * @param path the path.
    * @throws IllegalArgumentException for a null path
    */
   public void setPath(String path)
   {
      if (path == null)
         throw new IllegalArgumentException("Null path");
      this.path = path;
   }

   public List<String> getMetaDataPath()
   {
      if (metaDataPath == null)
         return Collections.emptyList();
      return metaDataPath;
   }

   /**
    * Set the meta data paths.
    *
    * @param metaDataPath the meta data paths
    */
   public void setMetaDataPath(List<String> metaDataPath)
   {
      this.metaDataPath = metaDataPath;
   }

   public void addMetaDataPath(String path)
   {
      if (path == null)
         throw new IllegalArgumentException("Null path");

      if (metaDataPath == null)
         metaDataPath = Collections.singletonList(path);
      else if (metaDataPath.size() == 1)
      {
         List<String> paths = new ArrayList<String>();
         paths.addAll(metaDataPath);
         paths.add(path);
         metaDataPath = paths;
      }
      else
         metaDataPath.add(path);
   }

   public List<ClassPathEntry> getClassPath()
   {
      return classPath;
   }

   /**
    * Set the classPath.
    * 
    * @param classPath the classPath.
    */
   public void setClassPath(List<ClassPathEntry> classPath)
   {
      this.classPath = classPath;
   }
   
   public void addClassPathEntry(ClassPathEntry entry)
   {
      if (entry == null)
         throw new IllegalArgumentException("Null entry");

      if (classPath == null || classPath == ClassPathEntryImpl.DEFAULT)
      {
         List<ClassPathEntry> old = classPath;
         classPath = new ArrayList<ClassPathEntry>();
         if (old != null)
            classPath.addAll(old);
      }
      classPath.add(entry);
   }

   public String getComparatorClassName()
   {
      return comparatorClassName;
   }

   public void setComparatorClassName(String className)
   {
      this.comparatorClassName = className;
   }

   public int getRelativeOrder()
   {
      return relativeOrder;
   }

   public void setRelativeOrder(int relativeOrder)
   {
      this.relativeOrder = relativeOrder;
   }

   public ModificationType getModificationType()
   {
      return modificationType;
   }

   public void setModificationType(ModificationType modificationType)
   {
      this.modificationType = modificationType;
   }

   @Override
   public String toString()
   {
      StringBuilder builder = new StringBuilder();
      builder.append(getClass().getSimpleName());
      builder.append("{");
      toString(builder);
      builder.append("}");
      return builder.toString();
   }
   
   /**
    * For subclasses to override toString()
    * 
    * @param builder the builder
    */
   protected void toString(StringBuilder builder)
   {
      builder.append("path=").append(getPath());
      builder.append(" metaData=").append(getMetaDataPath());
      builder.append(" classPath=").append(getClassPath());
      if (relativeOrder != 0)
         builder.append(" relativeOrder=").append(getRelativeOrder());
      if (comparatorClassName != null)
         builder.append(" comparator=").append(getComparatorClassName());
      if (modificationType != null)
         builder.append(" modification=").append(modificationType);
   }
   
   @Override
   public boolean equals(Object obj)
   {
      if (obj == this)
         return true;
      if (obj == null || obj instanceof ContextInfo == false)
         return false;
      
      ContextInfo other = (ContextInfo) obj;
      if (getPath().equals(other.getPath()) == false)
         return false;

      List<String> thisMetaDataPath = getMetaDataPath();
      List<String> otherMetaDataPath = other.getMetaDataPath();
      if (thisMetaDataPath.equals(otherMetaDataPath) == false)
         return false;

      List<ClassPathEntry> thisClassPath = getClassPath();
      List<ClassPathEntry> otherClassPath = other.getClassPath();
      if (thisClassPath == null)
         return otherClassPath == null;
      return thisClassPath.equals(otherClassPath);
   }
   
   @Override
   public int hashCode()
   {
      return getPath().hashCode();
   }

   @SuppressWarnings("unchecked")
   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
   {
      super.readExternal(in);
      setPath(in.readUTF());
      boolean isEmptyMetaDataPath = in.readBoolean();
      if (isEmptyMetaDataPath == false)
         setMetaDataPath((List<String>)in.readObject());
      setClassPath((List) in.readObject());
      setRelativeOrder(in.readInt());
      boolean isNullComparator = in.readBoolean();
      if (isNullComparator == false)
         setComparatorClassName(in.readUTF());
   }

   /**
    * @serialData path from {@link #getPath()}
    * @serialData metaDataPath from {@link #getMetaDataPath()}
    * @serialData classPath from {@link #getClassPath()}
    * @param out the output 
    * @throws IOException for any error
    */
   public void writeExternal(ObjectOutput out) throws IOException
   {
      super.writeExternal(out);
      out.writeUTF(getPath());
      List<String> metaDataPath = getMetaDataPath();
      boolean isEmptyMetaDataPath = metaDataPath.isEmpty();
      out.writeBoolean(isEmptyMetaDataPath);
      if (isEmptyMetaDataPath == false)
         out.writeObject(metaDataPath);
      out.writeObject(getClassPath());
      out.writeInt(getRelativeOrder());
      String comparator = getComparatorClassName();
      boolean isNullComparator = (comparator == null);
      out.writeBoolean(isNullComparator);
      if (isNullComparator == false)
         out.writeUTF(comparator);
   }
}
