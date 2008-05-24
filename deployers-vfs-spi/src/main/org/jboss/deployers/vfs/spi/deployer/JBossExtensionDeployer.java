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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;

/**
 * JBossExtensionDeployer.
 *
 * @param <U> the spec type
 * @param <V> the jboss type
 * @param <T> the expected type
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 */
public abstract class JBossExtensionDeployer<U, V, T> extends MultipleSchemaResolverDeployer<T>
{
   private String specName;
   private Class<U> specClass;
   private String jbossName;
   private Class<V> jbossClass;

   public JBossExtensionDeployer(Class<T> output, String specName, Class<U> specClass, String jbossName, Class<V> jbossClass)
   {
      this(output, specName, specClass, jbossName, jbossClass, null);
   }

   public JBossExtensionDeployer(Class<T> output, String specName, Class<U> specClass, String jbossName, Class<V> jbossClass, Set<Class<?>> excluded)
   {
      super(output, toMap(specName, specClass, jbossName, jbossClass), excluded);
      if (specClass == null)
         throw new IllegalArgumentException("Null spec class");
      if (jbossClass == null)
         throw new IllegalArgumentException("Null jboss class");
      this.specName = specName;
      this.specClass = specClass;
      this.jbossName = jbossName;
      this.jbossClass = jbossClass;
   }

   protected static Map<String, Class<?>> toMap(String specName, Class specClass, String jbossName, Class jbossClass)
   {
      Map<String, Class<?>> map = new HashMap<String, Class<?>>();
      map.put(specName, specClass);
      map.put(jbossName, jbossClass);
      return map;
   }

   protected T mergeMetaData(VFSDeploymentUnit unit, T root, Map<Class<?>, List<Object>> metadata, Set<String> missingFiles) throws Exception
   {
      if (specClass.equals(jbossClass))
      {
         List<Object> instances = metadata.get(specClass);
         if (instances == null || instances.isEmpty())
            return mergeMetaData(unit, null, null);
         else if (instances.size() == 1)
         {
            if (missingFiles.contains(jbossName))
               return mergeMetaData(unit, specClass.cast(instances.iterator().next()), null);
            else if (missingFiles.contains(specName))
               return mergeMetaData(unit, null, jbossClass.cast(instances.iterator().next()));
            else
               throw new IllegalArgumentException("Should be either missing spec or jboss: " + missingFiles);
         }
         else
            return mergeMetaData(unit, specClass.cast(instances.get(0)), jbossClass.cast(instances.get(1)));

      }
      else
         return super.mergeMetaData(unit, root, metadata, missingFiles);
   }

   protected T mergeMetaData(VFSDeploymentUnit unit, Map<Class<?>, List<Object>> metadata) throws Exception
   {
      return mergeMetaData(unit, getInstance(metadata, specClass), getInstance(metadata, jbossClass));
   }

   /**
    * Get metadata instance from metadata.
    *
    * @param metadata the metadatas map
    * @param clazz metadata class
    * @return matching metadata instance
    */
   protected <S> S getInstance(Map<Class<?>, List<Object>> metadata, Class<S> clazz)
   {
      List<Object> instances = metadata.get(clazz);
      if (instances == null)
         return null;
      else if (instances.size() > 1)
         throw new IllegalArgumentException("Expecting single instance: " + metadata);

      return clazz.cast(instances.iterator().next());
   }

   /**
    * Merge spec and extension.
    *
    * @param unit deployment unit
    * @param spec the spec metadata instance
    * @param jboss the jboss metadata instance
    * @return merged metadata
    * @throws Exception for any error
    */
   protected abstract T mergeMetaData(VFSDeploymentUnit unit, U spec, V jboss) throws Exception;
}