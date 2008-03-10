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
import java.util.Collections;
import java.util.List;

import org.jboss.deployers.spi.structure.ClassPathEntry;

/**
 * ClassPathEntry.
 * 
 * @author <a href="adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class ClassPathEntryImpl implements ClassPathEntry, Externalizable
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -6463413458528845538L;

   /** The default classpath */
   static final List<ClassPathEntry> DEFAULT = Collections.singletonList((ClassPathEntry) new ClassPathEntryImpl()); 

   /** The path */
   private String path;
   
   /** The suffixes */
   private String suffixes;

   /**
    * Create a new ClassPathEntryImpl.
    */
   public ClassPathEntryImpl()
   {
      setPath("");
   }

   /**
    * Create a new ClassPathEntryImpl.
    * 
    * @param path the path
    * @throws IllegalArgumentException for a null path
    */
   public ClassPathEntryImpl(String path)
   {
      setPath(path);
   }

   /**
    * Create a new ClassPathEntryImpl.
    * 
    * @param path the path
    * @param suffixes the suffixes
    * @throws IllegalArgumentException for a null path
    */
   public ClassPathEntryImpl(String path, String suffixes)
   {
      setPath(path);
      setSuffixes(suffixes);
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

   public String getSuffixes()
   {
      return suffixes;
   }

   /**
    * Set the suffixes.
    * 
    * @param suffixes the suffixes.
    */
   public void setSuffixes(String suffixes)
   {
      this.suffixes = suffixes;
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
      String suffixes = getSuffixes();
      if (suffixes != null)
         builder.append(" suffixes=").append(suffixes);
   }
   
   @Override
   public boolean equals(Object obj)
   {
      if (obj == this)
         return true;
      if (obj == null || obj instanceof ClassPathEntry == false)
         return false;
      
      ClassPathEntry other = (ClassPathEntry) obj;
      if (getPath().equals(other.getPath()) == false)
         return false;

      String thisSuffixes = getSuffixes();
      String otherSuffixes = other.getSuffixes();
      if (thisSuffixes == null)
         return otherSuffixes == null;
      return thisSuffixes.equals(otherSuffixes);
   }
   
   @Override
   public int hashCode()
   {
      return getPath().hashCode();
   }

   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
   {
      setPath(in.readUTF());
      boolean hasSuffixes = in.readBoolean();
      if (hasSuffixes)
         setSuffixes(in.readUTF());
   }

   /**
    * @serialData path from {@link #getPath()}
    * @serialData suffixes from {@link #getSuffixes()}
    * @param out the output
    * @throws IOException for any error
    */
   public void writeExternal(ObjectOutput out) throws IOException
   {
      out.writeUTF(getPath());
      String suffixes = getSuffixes();
      out.writeBoolean(suffixes != null);
      if (suffixes != null)
         out.writeUTF(suffixes);
   }
}
