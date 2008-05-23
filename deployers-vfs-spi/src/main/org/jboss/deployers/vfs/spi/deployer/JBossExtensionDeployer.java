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
   private Class<U> specClass;
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
      this.specClass = specClass;
      this.jbossClass = jbossClass;
   }

   protected static Map<String, Class<?>> toMap(String specName, Class specClass, String jbossName, Class jbossClass)
   {
      Map<String, Class<?>> map = new HashMap<String, Class<?>>();
      map.put(specName, specClass);
      map.put(jbossName, jbossClass);
      return map;
   }

   protected T mergeMetaData(VFSDeploymentUnit unit, Map<Class<?>, Object> metadata) throws Exception
   {
      U spec = specClass.cast(metadata.get(specClass));
      V jboss = jbossClass.cast(metadata.get(jbossClass));
      return mergeMetaData(unit, spec, jboss);
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