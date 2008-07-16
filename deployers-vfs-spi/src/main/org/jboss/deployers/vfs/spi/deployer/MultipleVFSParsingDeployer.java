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
package org.jboss.deployers.vfs.spi.deployer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.virtual.VirtualFile;

/**
 * Multiple VFS parsing deployer.
 *
 * @param <T> the expected type
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 */
public abstract class MultipleVFSParsingDeployer<T> extends AbstractVFSParsingDeployer<T>
{
   private Map<String, Class<?>> mappings;
   private Class<?> suffixClass;

   public MultipleVFSParsingDeployer(Class<T> output, Map<String, Class<?>> mappings)
   {
      this(output, mappings, null, null);
   }

   public MultipleVFSParsingDeployer(Class<T> output, Map<String, Class<?>> mappings, String suffix, Class<?> suffixClass)
   {
      super(output);

      if (mappings == null || mappings.isEmpty())
         throw new IllegalArgumentException("Illegal mappings");
      if (suffix != null && suffixClass == null)
         throw new IllegalArgumentException("Null suffix class");

      this.mappings = mappings;
      setNames(mappings.keySet());
      setSuffix(suffix);
      this.suffixClass = suffixClass;
   }

   /**
    * Match file to mapping metadata class.
    *
    * @param unit the deployment unit
    * @param file the file
    * @return matching metadata class
    */
   protected Class<?> matchFileToClass(DeploymentUnit unit, VirtualFile file)
   {
      if (file == null)
         throw new IllegalArgumentException("Null file");

      return matchFileToClass(unit, file.getName(), true);
   }

   protected Class<?> matchFileToClass(DeploymentUnit unit, String fileName)
   {
      return matchFileToClass(unit, fileName, false);
   }

   /**
    * Match file name mappings.
    *
    * @param unit the deployment unit
    * @param fileName the file name
    * @param throwException should we throw an exception if no match found
    * @return match or null or IllegalArgumentException
    */
   protected Class<?> matchFileToClass(DeploymentUnit unit, String fileName, boolean throwException)
   {
      if (fileName == null)
         throw new IllegalArgumentException("Null file name");

      Map<String, Class<?>> altMappingsMap = getAltMappings(unit);
      if (altMappingsMap != null)
      {
         Class<?> result = altMappingsMap.get(fileName);
         if (result != null)
            return result;
      }

      Class<?> result = mappings.get(fileName);
      if (result == null)
      {
         if (getSuffix() != null && fileName.endsWith(getSuffix()))
            result = suffixClass;
      }

      if (result == null && throwException)
         throw new IllegalArgumentException(
               "Should not be here, file name '" + fileName +
               "' must macth some mapping " + mappings + " or suffix " + getSuffix()
         );

      return result;
   }

   @SuppressWarnings("unchecked")
   protected T parse(VFSDeploymentUnit unit, VirtualFile file, T root) throws Exception
   {
      Class<?> expectedType = matchFileToClass(unit, file);
      if (getOutput().isAssignableFrom(expectedType) == false)
         throw new IllegalArgumentException("Matched " + expectedType + " which is not assignable to output " + getOutput());
      if (root != null && expectedType.isInstance(root) == false)
         throw new IllegalArgumentException("Illegal root type: " + root + ", expecting " + expectedType);

      return (T)parse(expectedType, file, root);
   }

   /**
    * Parse file to produce expected class metadata.
    *
    * Root doesn't have U signature, since it conflicts
    * with its usage in the parse(VFSDeployment unit, VirtualFile file, T root) method.
    *
    * @param expectedType the expected class
    * @param file the file to parse
    * @param root the previous root
    * @return new metadata instance
    * @throws Exception for any error
    */
   protected abstract <U> U parse(Class<U> expectedType, VirtualFile file, Object root) throws Exception;

   protected T mergeFiles(VFSDeploymentUnit unit, T root, List<VirtualFile> files, Set<String> missingFiles) throws Exception
   {
      Map<Class<?> , List<Object>> metadata = new HashMap<Class<?>, List<Object>>();
      for (VirtualFile file : files)
      {
         Class<?> clazz = matchFileToClass(unit, file);
         List<Object> instances = metadata.get(clazz);
         if (instances == null)
         {
            instances = new ArrayList<Object>();
            metadata.put(clazz, instances);
         }
         Object instance = parse(clazz, file, root);
         instances.add(instance);
      }
      return mergeMetaData(unit, root, metadata, missingFiles);
   }

   /**
    * Merge metadatas into single piece of metatdata
    *
    * @param unit the unit
    * @param root possibly null pre-existing root
    * @param metadata the metadatas
    * @param missingFiles file names that are missing matching file
    * @return merged metadata
    * @throws Exception for any error
    */
   protected T mergeMetaData(VFSDeploymentUnit unit, T root, Map<Class<?>, List<Object>> metadata, Set<String> missingFiles) throws Exception
   {
      return mergeMetaData(unit, metadata);
   }

   /**
    * Merge metadatas into single piece of metatdata
    *
    * @param unit the unit
    * @param metadata the metadatas
    * @return merged metadata
    * @throws Exception for any error
    */
   protected abstract T mergeMetaData(VFSDeploymentUnit unit, Map<Class<?>, List<Object>> metadata) throws Exception;

   /**
    * Get single metadata instance from metadata.
    *
    * @param metadata the metadatas map
    * @param clazz metadata class
    * @return matching metadata instance
    */
   protected <S> S getInstance(Map<Class<?>, List<Object>> metadata, Class<S> clazz)
   {
      List<Object> instances = metadata.get(clazz);
      if (instances == null || instances.isEmpty())
         return null;
      else if (instances.size() > 1)
         throw new IllegalArgumentException("Expecting single instance: " + metadata);

      return clazz.cast(instances.iterator().next());
   }

   /**
    * Get mappings.
    *
    * @return the mappings
    */
   public Map<String, Class<?>> getMappings()
   {
      return mappings;
   }

   /**
    * Get suffix class.
    *
    * @return the suffix mathing class
    */
   public Class<?> getSuffixClass()
   {
      return suffixClass;
   }
}