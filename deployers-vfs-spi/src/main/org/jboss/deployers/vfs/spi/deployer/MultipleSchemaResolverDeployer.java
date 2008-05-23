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

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;

import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.virtual.VirtualFile;
import org.jboss.xb.annotations.JBossXmlConstants;

/**
 * MultipleSchemaResolverDeployer.
 *
 * @param <T> the expected type
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 */
public abstract class MultipleSchemaResolverDeployer<T> extends JBossXBDeployer<T>
{
   private Map<String, Class<?>> mappings;
   private Set<Class<?>> excluded;
   private Set<String> namespaces;

   public MultipleSchemaResolverDeployer(Class<T> output, Map<String, Class<?>> mappings)
   {
      this(output, mappings, null);
   }

   public MultipleSchemaResolverDeployer(Class<T> output, Map<String, Class<?>> mappings, Set<Class<?>> excluded)
   {
      super(output);
      if (mappings == null || mappings.isEmpty())
         throw new IllegalArgumentException("Illegal mappings");
      this.mappings = mappings;
      setNames(mappings.keySet());
      if (excluded == null)
         excluded = Collections.emptySet();
      this.excluded = excluded;
      this.namespaces = new HashSet<String>();
   }

   /**
    * Create a new SchemaResolverDeployer.
    *
    * @param output the output
    * @throws IllegalArgumentException for a null output
    */
   public MultipleSchemaResolverDeployer(Class<T> output)
   {
      super(output);
   }

   /**
    * Check if we need to register schema to jbossxb.
    */
   public void create()
   {
      for (Class<?> metadata : mappings.values())
      {
         if (excluded.contains(metadata) == false)
         {
            String namespace = findNamespace(metadata);
            if (namespace == null || JBossXmlConstants.DEFAULT.equals(namespace))
               throw new IllegalArgumentException(
                     "Registering schema with JBossXB is enabled, but cannot find namespace on class or package: " + metadata +
                     ", perhaps missing @JBossXmlSchema or using default namespace attribute."
               );

            addClassBinding(namespace, metadata);
            namespaces.add(namespace);
         }
      }
   }

   /**
    * Remove registered schema
    */
   public void destroy()
   {
      for (String namespace : namespaces)
         removeClassBinding(namespace);
      namespaces.clear();
   }

   /**
    * Match file to mapping metadata class.
    *
    * @param file the file
    * @return matching metadata class
    */
   protected Class<?> matchFileToClass(VirtualFile file)
   {
      String fileName = file.getName();
      Class<?> result = mappings.get(fileName);
      if (result == null)
      {
         for(Map.Entry<String, Class<?>> entry : mappings.entrySet())
         {
            if (fileName.endsWith(entry.getKey()))
            {
               result = entry.getValue();
               break;
            }
         }
      }

      if (result == null)
         throw new IllegalArgumentException("Should not be here, file '" + file + "' must macth some mapping: " + mappings);

      return result;
   }

   @SuppressWarnings("unchecked")
   protected T parse(VFSDeploymentUnit unit, VirtualFile file, T root) throws Exception
   {
      Class<?> expectedClass = matchFileToClass(file);
      if (getOutput().isAssignableFrom(expectedClass) == false)
         throw new IllegalArgumentException("Matched " + expectedClass + " which is not assignable to output " + getOutput());

      return (T)parse(expectedClass, file);
   }

   protected T mergeFiles(VFSDeploymentUnit unit, T root, Set<VirtualFile> files, Set<String> missingFiles) throws Exception
   {
      Map<Class<?> , Object> metadata = new HashMap<Class<?>, Object>();
      for (VirtualFile file : files)
      {
         Class<?> clazz = matchFileToClass(file);
         Object instance = parse(clazz, file);
         metadata.put(clazz, instance);
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
   protected T mergeMetaData(VFSDeploymentUnit unit, T root, Map<Class<?>, Object> metadata, Set<String> missingFiles) throws Exception
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
   protected abstract T mergeMetaData(VFSDeploymentUnit unit, Map<Class<?>, Object> metadata) throws Exception;
}