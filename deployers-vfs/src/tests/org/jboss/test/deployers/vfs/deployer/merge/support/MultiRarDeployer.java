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
package org.jboss.test.deployers.vfs.deployer.merge.support;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import org.jboss.deployers.vfs.spi.deployer.MultipleSchemaResolverDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;

/**
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class MultiRarDeployer extends MultipleSchemaResolverDeployer<RarDeploymentMetaData>
{
   private static Map<String, Class<?>> getCustomMappings()
   {
      Map<String, Class<?>> mappings = new HashMap<String, Class<?>>();
      mappings.put("rar.xml", RarMetaData.class);
      mappings.put("jboss-rar.xml", JBossRarMetaData.class);
      mappings.put("alias.xml", AliasMetaData.class);
      mappings.put("alias-ext.xml", AliasMetaData.class);
      return mappings;
   }

   public MultiRarDeployer()
   {
      super(RarDeploymentMetaData.class, getCustomMappings());
   }

   protected RarDeploymentMetaData mergeMetaData(VFSDeploymentUnit unit, Map<Class<?>, List<Object>> metadata) throws Exception
   {
      RarDeploymentMetaData deployment = new RarDeploymentMetaData();
      RarMetaData spec = getInstance(metadata, RarMetaData.class);
      JBossRarMetaData jboss = getInstance(metadata, JBossRarMetaData.class);
      if (spec != null)
      {
         deployment.setAttribute(spec.getAttribute());
         deployment.setElement(spec.getElement());
      }
      if (jboss != null)
      {
         if (jboss.getAttribute() != null)
            deployment.setAttribute(jboss.getAttribute());
         if (jboss.getElement() != null)
            deployment.setElement(jboss.getElement());
      }

      List<Object> aliases = metadata.get(AliasMetaData.class);
      if (aliases != null)
      {
         Set<Object> strings = new HashSet<Object>();
         for (Object md : aliases)
         {
            AliasMetaData amd = AliasMetaData.class.cast(md);
            strings.add(amd.getAlias());
         }
         deployment.setAliases(strings);
      }

      return deployment;
   }
}
