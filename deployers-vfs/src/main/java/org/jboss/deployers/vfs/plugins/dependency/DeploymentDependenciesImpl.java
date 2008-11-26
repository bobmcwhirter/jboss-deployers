/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.deployers.vfs.plugins.dependency;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import org.jboss.dependency.plugins.AbstractDependencyItem;
import org.jboss.dependency.spi.DependencyItem;

/**
 * DeploymentDependenciesimpl.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class DeploymentDependenciesImpl implements DeploymentDependencies
{
   private Object name;
   private DependenciesMetaData dmd;
   private Set<DependencyItem> dependencies;

   public DeploymentDependenciesImpl(Object name, DependenciesMetaData dmd)
   {
      if (name == null)
         throw new IllegalArgumentException("Null name");
      if (dmd == null)
         throw new IllegalArgumentException("Null dmd");

      this.name = name;
      this.dmd = dmd;
   }

   /**
    * Create dependency item.
    *
    * @param dimd the dependency metadata item
    * @return new dependency item
    */
   protected DependencyItem createDependencyItem(DependencyItemMetaData dimd)
   {
      return new AbstractDependencyItem(name, dimd.getValue(), dimd.getWhenRequired(), dimd.getDependentState());
   }

   public Set<DependencyItem> getDependencies()
   {
      if (dependencies == null)
      {
         List<DependencyItemMetaData> dimds = dmd.getItems();
         if (dimds != null && dimds.isEmpty() == false)
         {
            dependencies = new HashSet<DependencyItem>();
            for (DependencyItemMetaData dimd : dimds)
               dependencies.add(createDependencyItem(dimd));
         }
         else
         {
            dependencies = Collections.emptySet();
         }
      }
      return dependencies;
   }
}