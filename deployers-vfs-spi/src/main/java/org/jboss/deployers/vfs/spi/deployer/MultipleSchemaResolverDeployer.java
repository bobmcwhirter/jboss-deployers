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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jboss.virtual.VirtualFile;
import org.jboss.virtual.VFSInputSource;
import org.jboss.xb.annotations.JBossXmlConstants;
import org.jboss.xb.binding.JBossXBDeployerHelper;
import org.xml.sax.InputSource;

/**
 * MultipleSchemaResolverDeployer.
 *
 * @param <T> the expected type
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 */
public abstract class MultipleSchemaResolverDeployer<T> extends MultipleJBossXBDeployer<T>
{
   private Set<Class<?>> excluded;
   private Set<String> namespaces;

   public MultipleSchemaResolverDeployer(Class<T> output, Map<String, Class<?>> mappings)
   {
      this(output, mappings, null);
   }

   public MultipleSchemaResolverDeployer(Class<T> output, Map<String, Class<?>> mappings, Set<Class<?>> excluded)
   {
      this(output, mappings, null, null, excluded);
   }

   public MultipleSchemaResolverDeployer(Class<T> output, Map<String, Class<?>> mappings, String suffix, Class<?> suffixClass, Set<Class<?>> excluded)
   {
      super(output, mappings, suffix, suffixClass);
      if (excluded == null)
         excluded = Collections.emptySet();
      this.excluded = excluded;
      this.namespaces = new HashSet<String>();
   }

   /**
    * Check if we need to register schema to jbossxb.
    */
   public void create()
   {
      for (Class<?> metadata : getMappings().values())
      {
         registerMetaDataClass(metadata);
      }
      if (getSuffixClass() != null)
         registerMetaDataClass(getSuffixClass());
   }

   /**
    * Register metadata class as class binding,
    * if not excluded.
    *
    * @param metadata the metadata class
    */
   protected void registerMetaDataClass(Class<?> metadata)
   {
      if (excluded.contains(metadata) == false)
      {
         String namespace = JBossXBDeployerHelper.findNamespace(metadata);
         if (namespace == null || JBossXmlConstants.DEFAULT.equals(namespace))
            throw new IllegalArgumentException(
                  "Registering schema with JBossXB is enabled, but cannot find namespace on class or package: " + metadata +
                  ", perhaps missing @JBossXmlSchema or using default namespace attribute."
            );

         JBossXBDeployerHelper.addClassBinding(namespace, metadata);
         namespaces.add(namespace);
      }
   }

   /**
    * Remove registered schema
    */
   public void destroy()
   {
      for (String namespace : namespaces)
         JBossXBDeployerHelper.removeClassBinding(namespace);
      namespaces.clear();
   }

   protected <U> U parse(Class<U> expectedType, VirtualFile file, Object root) throws Exception
   {
      InputSource source = new VFSInputSource(file);
      return getHelper().parse(expectedType, source);
   }
}