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
package org.jboss.deployers.structure.spi.helpers;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.jboss.dependency.spi.ControllerContext;
import org.jboss.deployers.structure.spi.DeploymentRegistry;
import org.jboss.deployers.structure.spi.DeploymentUnit;

/**
 * Simple deployment registry
 *
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 */
public class AbstractDeploymentRegistry implements DeploymentRegistry
{
   /** The read/write lock */
   private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

   /** The context 2 deployment mapping */
   private final Map<ControllerContext, DeploymentUnit> contextMapping = new ConcurrentHashMap<ControllerContext, DeploymentUnit>();

   /** The deployment 2 context mapping */
   private final Map<DeploymentUnit, Set<ControllerContext>> deploymentMapping = new HashMap<DeploymentUnit, Set<ControllerContext>>();

   private void check(ControllerContext context, DeploymentUnit unit)
   {
      if (context == null || unit == null)
         throw new IllegalArgumentException("Null context or unit: context=" + context + ", unit=" + unit);
   }

   public DeploymentUnit putContext(ControllerContext context, DeploymentUnit unit)
   {
      check(context, unit);
      lock.writeLock().lock();
      try
      {
         Set<ControllerContext> contexts = deploymentMapping.get(unit);
         if (contexts == null)
         {
            contexts = new HashSet<ControllerContext>();
            deploymentMapping.put(unit, contexts);
         }
         contexts.add(context);
      }
      finally
      {
         lock.writeLock().unlock();
      }
      return contextMapping.put(context, unit);
   }

   public DeploymentUnit removeContext(ControllerContext context, DeploymentUnit unit)
   {
      check(context, unit);
      lock.writeLock().lock();
      try
      {
         Set<ControllerContext> contexts = deploymentMapping.get(unit);
         if (contexts != null)
         {
            contexts.remove(context);

            if (contexts.isEmpty())
               deploymentMapping.remove(unit);
         }
      }
      finally
      {
         lock.writeLock().unlock();
      }
      return contextMapping.remove(context);
   }

   public DeploymentUnit getDeployment(ControllerContext context)
   {
      return contextMapping.get(context);
   }

   public Set<ControllerContext> getContexts(DeploymentUnit unit)
   {
      lock.readLock().lock();
      try
      {
         Set<ControllerContext> contexts = deploymentMapping.get(unit);
         return (contexts != null) ? new HashSet<ControllerContext>(contexts) : Collections.<ControllerContext>emptySet(); 
      }
      finally
      {
         lock.readLock().unlock();
      }
   }
}