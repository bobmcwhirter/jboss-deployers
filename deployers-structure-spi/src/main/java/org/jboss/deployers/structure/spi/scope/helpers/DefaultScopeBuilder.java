/*
* JBoss, Home of Professional Open Source
* Copyright 2007, JBoss Inc., and individual contributors as indicated
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
package org.jboss.deployers.structure.spi.scope.helpers;

import java.util.ArrayList;

import org.jboss.deployers.structure.spi.DeploymentContext;
import org.jboss.deployers.structure.spi.scope.ScopeBuilder;
import org.jboss.metadata.plugins.context.AbstractMetaDataContext;
import org.jboss.metadata.plugins.loader.memory.MemoryMetaDataLoader;
import org.jboss.metadata.spi.repository.MutableMetaDataRepository;
import org.jboss.metadata.spi.retrieval.MetaDataRetrieval;
import org.jboss.metadata.spi.scope.CommonLevels;
import org.jboss.metadata.spi.scope.Scope;
import org.jboss.metadata.spi.scope.ScopeKey;

/**
 * DefaultScopeBuilder.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class DefaultScopeBuilder implements ScopeBuilder
{
   /** The singleton instance */
   public static final DefaultScopeBuilder INSTANCE = new DefaultScopeBuilder();

   public ScopeKey getDeploymentScope(DeploymentContext context)
   {
      if (context == null)
         throw new IllegalArgumentException("Null context");
      
      DeploymentContext top = context.getTopLevel();
      
      ScopeKey result = ScopeKey.DEFAULT_SCOPE.clone();
      result.addScope(CommonLevels.APPLICATION, top.getName());
      result.addScope(CommonLevels.DEPLOYMENT, context.getName());
      return result;
   }

   public ScopeKey getMutableDeploymentScope(DeploymentContext context)
   {
      if (context == null)
         throw new IllegalArgumentException("Null context");

      if (context.isTopLevel())
         return new ScopeKey(CommonLevels.APPLICATION, context.getName());
      else
         return new ScopeKey(CommonLevels.DEPLOYMENT, context.getName());
   }

   public ScopeKey getComponentScope(DeploymentContext context)
   {
      if (context == null)
         throw new IllegalArgumentException("Null context");

      DeploymentContext parent = context;
      while (parent.isComponent())
      {
         parent = context.getParent();
         if (parent == null)
            throw new IllegalStateException("Component has no regular parent?");
      }
      ScopeKey result = parent.getScope().clone();
      result.addScope(CommonLevels.INSTANCE, context.getName());
      return result;
   }

   public ScopeKey getMutableComponentScope(DeploymentContext context)
   {
      if (context == null)
         throw new IllegalArgumentException("Null context");

      return new ScopeKey(CommonLevels.INSTANCE, context.getName());
   }

   public void initMetaDataRetrieval(MutableMetaDataRepository repository, DeploymentContext context)
   {
      if (repository == null)
         throw new IllegalArgumentException("Null repository");
      if (context == null)
         throw new IllegalArgumentException("Null context");

      ScopeKey scopeKey = context.getScope();
      
      ArrayList<MetaDataRetrieval> retrievals = new ArrayList<MetaDataRetrieval>();
      for (Scope scope : scopeKey.getScopes())
      {
         ScopeKey thisScope = new ScopeKey(scope);
         MetaDataRetrieval retrieval = repository.getMetaDataRetrieval(thisScope);
         if (retrieval == null)
         {
            retrieval = new MemoryMetaDataLoader(thisScope);
            repository.addMetaDataRetrieval(retrieval);
         }
         retrievals.add(0, retrieval);
      }
      AbstractMetaDataContext metaDataContext = new AbstractMetaDataContext(null, retrievals);
      repository.addMetaDataRetrieval(metaDataContext);
   }

   public void initMutableMetaDataRetrieval(MutableMetaDataRepository repository, DeploymentContext context)
   {
      if (repository == null)
         throw new IllegalArgumentException("Null repository");
      if (context == null)
         throw new IllegalArgumentException("Null context");

      ScopeKey scopeKey = context.getMutableScope();
      MetaDataRetrieval retrieval = repository.getMetaDataRetrieval(scopeKey);
      if (retrieval == null)
      {
         retrieval = new MemoryMetaDataLoader(scopeKey);
         repository.addMetaDataRetrieval(retrieval);
      }
      AbstractMetaDataContext metaDataContext = new AbstractMetaDataContext(null, retrieval);
      repository.addMetaDataRetrieval(metaDataContext);
   }
}
