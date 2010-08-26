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
package org.jboss.deployers.plugins.classloading;

import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jboss.classloading.spi.dependency.Module;
import org.jboss.classloading.spi.visitor.ResourceContext;
import org.jboss.classloading.spi.visitor.ResourceFilter;
import org.jboss.classloading.spi.visitor.ResourceVisitor;
import org.jboss.classloading.spi.visitor.RootAwareResource;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.classloading.ResourceLookupProvider;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractSimpleRealDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;

/**
 * AbstractResourceLookupDeployer.
 *
 * Allow for early resource lookup,
 * and map this against Module / ClassLoader
 *
 * @author <a href="ales.justin@jboss.org">Ales Justin</a>
 */
public class AbstractResourceLookupDeployer extends AbstractSimpleRealDeployer<Module> implements ResourceLookupProvider<Module>
{
   private String resourceName;
   private String[] dir;

   private Map<Module, Set<URL>> matchingModules;
   private AtomicBoolean active = new AtomicBoolean(true);

   public AbstractResourceLookupDeployer(String resourceName)
   {
      super(Module.class);

      if (resourceName == null)
         throw new IllegalArgumentException("Null resource name");

      setStage(DeploymentStages.POST_CLASSLOADER);

      this.resourceName = resourceName;

      int p = resourceName.lastIndexOf("/");
      if (p > 0)
      {
         String dirs = resourceName.substring(0, p);
         dir = dirs.split("/");
      }
      else
      {
         dir = new String[0];
      }
      matchingModules = new ConcurrentHashMap<Module, Set<URL>>();
   }

   public String getResourceName()
   {
      return resourceName;
   }

   public Map<Module, Set<URL>> getMatchingModules()
   {
      return Collections.unmodifiableMap(matchingModules);
   }

   public void deploy(DeploymentUnit unit, Module module) throws DeploymentException
   {
      if (active.get() == false)
         return;

      MapperRV rv = new MapperRV();
      module.visit(rv, rv.getFilter(), new MapperRF());
      if (rv.owners.isEmpty() == false)
      {
         matchingModules.put(module, rv.owners);
      }
   }

   @Override
   public void undeploy(DeploymentUnit unit, Module module)
   {
      // we don't care if it's a member
      matchingModules.remove(module);
   }

   /**
    * Disable or enable this mapper.
    *
    * @param active the active flag
    */
   public void setActive(boolean active)
   {
      this.active.set(active);
   }

   private class MapperRV implements ResourceVisitor
   {
      private Set<URL> owners = new HashSet<URL>();

      public ResourceFilter getFilter()
      {
         return new ResourceFilter()
         {
            public boolean accepts(ResourceContext resource)
            {
               String name = resource.getResourceName();
               return name.endsWith(resourceName);
            }
         };
      }

      public void visit(ResourceContext resource)
      {
         if (resource instanceof RootAwareResource)
         {
            RootAwareResource rar = (RootAwareResource) resource;
            owners.add(rar.getRootUrl());
         }
      }
   }

   // recurse filter
   private class MapperRF implements ResourceFilter
   {
      public boolean accepts(ResourceContext resource)
      {
         if (dir.length == 0)
            return false;

         String name = resource.getResourceName();
         String[] split = name.split("/");
         int sl = split.length;
         int min = Math.min(sl, dir.length);
         for (int i = 1; i <= min; i++)
         {
            boolean match = true;
            for (int j = 0; j < i; j++)
            {
               if (dir[j].equals(split[sl - i + j]) == false)
               {
                  match = false;
                  break;
               }
            }
            if (match)
               return true;
         }
         return false;
      }
   }
}
