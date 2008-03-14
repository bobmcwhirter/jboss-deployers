/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.deployers.structure.spi.helpers;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.HashSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

import org.jboss.dependency.spi.ControllerContext;
import org.jboss.dependency.spi.DependencyInfo;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.DeploymentState;
import org.jboss.deployers.spi.attachments.helpers.ManagedObjectsWithTransientAttachmentsImpl;
import org.jboss.deployers.structure.spi.ClassLoaderFactory;
import org.jboss.deployers.structure.spi.DeploymentContext;
import org.jboss.deployers.structure.spi.DeploymentContextVisitor;
import org.jboss.deployers.structure.spi.DeploymentResourceLoader;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.structure.spi.scope.ScopeBuilder;
import org.jboss.deployers.structure.spi.scope.helpers.DefaultScopeBuilder;
import org.jboss.logging.Logger;
import org.jboss.metadata.spi.MetaData;
import org.jboss.metadata.spi.MutableMetaData;
import org.jboss.metadata.spi.context.MetaDataContext;
import org.jboss.metadata.spi.loader.MutableMetaDataLoader;
import org.jboss.metadata.spi.repository.MutableMetaDataRepository;
import org.jboss.metadata.spi.retrieval.MetaDataRetrieval;
import org.jboss.metadata.spi.scope.ScopeKey;

/**
 * AbstractDeploymentContext.
 * 
 * @author <a href="adrian@jboss.org">Adrian Brock</a>
 * @author Scott.Stark@jboss.org
 * @version $Revision: 1.1 $
 */
public class AbstractDeploymentContext extends ManagedObjectsWithTransientAttachmentsImpl implements DeploymentContext
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 7368360479461613969L;

   /** The log */
   private static final Logger log = Logger.getLogger(AbstractDeploymentContext.class);
   
   /** The name */
   private String name;

   /** The controller context names - should be serializable */
   private Set<Object> controllerContextNames;

   /** The simple name */
   private String simpleName;

   /** The relative path */
   private String relativePath;
   
   /** The deployment state */
   private DeploymentState state;

   /** Throwable */
   private Throwable problem;

   /** The deployment */
   private Deployment deployment;
   
   /** The deployment unit */
   private transient DeploymentUnit unit;
   
   /** The class loader */
   private transient ClassLoader classLoader;

   /** The class loader factory for this deployment */
   private transient ClassLoaderFactory classLoaderFactory;

   /** The resource classloader */
   private transient ClassLoader resourceClassLoader;
   
   /** Whether this deployment was processed */
   private boolean deployed;
   
   /** The parent context */
   private DeploymentContext parent;

   /** The types of deployments this has been identified as */
   private Set<String> deploymentTypes = new CopyOnWriteArraySet<String>();

   /** The child contexts */
   private SortedSet<DeploymentContext> children;

   /** The component contexts */
   private List<DeploymentContext> components = new CopyOnWriteArrayList<DeploymentContext>();

   /** The relative order */
   private int relativeOrder;

   /** The context comparator */
   private Comparator<DeploymentContext> comparator = DefaultDeploymentContextComparator.INSTANCE;
   
   /** The scope */
   private ScopeKey scope;
   
   /** The mutable scope */
   private ScopeKey mutableScope;

   /**
    * Get the scope builder for a deployment context
    * 
    * @param deploymentContext the deployment context
    * @return the scope builder
    */
   public static ScopeBuilder getScopeBuilder(DeploymentContext deploymentContext)
   {
      if (deploymentContext == null)
         throw new IllegalArgumentException("Null deployment context");
      ScopeBuilder builder = deploymentContext.getTransientAttachments().getAttachment(ScopeBuilder.class);
      if (builder != null)
         return builder;
      DeploymentContext parent = deploymentContext.getParent();
      if (parent != null)
         return getScopeBuilder(parent);
      return DefaultScopeBuilder.INSTANCE;
   }

   /**
    * Get the repository for a deployment context
    * 
    * @param deploymentContext the deployment context
    * @return the repository
    */
   public static MutableMetaDataRepository getRepository(DeploymentContext deploymentContext)
   {
      if (deploymentContext == null)
         throw new IllegalArgumentException("Null deployment context");

      MutableMetaDataRepository repository = deploymentContext.getTransientAttachments().getAttachment(MutableMetaDataRepository.class);
      if (repository != null)
         return repository;
      DeploymentContext parent = deploymentContext.getParent();
      if (parent == null)
         return null;
      return getRepository(parent);
   }

   /**
    * Cleanup the repository
    * 
    * @param deploymentContext the deployment context
    */
   public static void cleanupRepository(DeploymentContext deploymentContext)
   {
      MutableMetaDataRepository repository = getRepository(deploymentContext);
      if (repository == null)
         return;
      
      try
      {
         ScopeKey scope = deploymentContext.getScope();
         repository.removeMetaDataRetrieval(scope);
      }
      catch (Throwable ignored)
      {
      }

      try
      {
         ScopeKey scope = deploymentContext.getMutableScope();
         repository.removeMetaDataRetrieval(scope);
      }
      catch (Throwable ignored)
      {
      }
   }

   /**
    * Get the metadata for a deployment context
    * 
    * @param deploymentContext the deployment context
    * @return the metaData
    */
   public static MetaData getMetaData(DeploymentContext deploymentContext)
   {
      MutableMetaDataRepository repository = getRepository(deploymentContext);
      if (repository == null)
         return null;
      
      MetaData metaData = repository.getMetaData(deploymentContext.getScope());
      if (metaData == null)
      {
         initMetaDataRetrieval(repository, deploymentContext);
         metaData = repository.getMetaData(deploymentContext.getScope());
      }
      return metaData;
   }

   /**
    * Get the mutable metadata for a deployment context
    * 
    * @param deploymentContext the deployment context
    * @return the metaData
    */
   public static MutableMetaDataLoader getMutableMetaData(DeploymentContext deploymentContext)
   {
      MutableMetaDataRepository repository = getRepository(deploymentContext);
      if (repository == null)
         return null;

      ScopeKey mutableScope = deploymentContext.getMutableScope();
      MetaDataRetrieval retrieval = repository.getMetaDataRetrieval(mutableScope);
      if (retrieval == null)
      {
         initMutableMetaDataRetrieval(repository, deploymentContext);
         retrieval = repository.getMetaDataRetrieval(mutableScope);
      }
      
      // Nothing
      if (retrieval == null)
         return null;

      // This is mutable
      if (retrieval instanceof MutableMetaDataLoader)
         return (MutableMetaDataLoader) retrieval;

      // We have a context, see if there is a mutable in the locals
      if (retrieval instanceof MetaDataContext)
      {
         MetaDataContext context = (MetaDataContext) retrieval;
         List<MetaDataRetrieval> locals = context.getLocalRetrievals();
         if (locals != null)
         {
            for (MetaDataRetrieval local : locals)
            {
               if (local instanceof MutableMetaDataLoader)
                  return (MutableMetaDataLoader) local;
            }
         }
      }
      return null;
   }

   /**
    * Initialise the metadata retrieval for a deployment context
    * 
    * @param repository the meta data repository
    * @param deploymentContext the deployment context
    */
   private static void initMetaDataRetrieval(MutableMetaDataRepository repository, DeploymentContext deploymentContext)
   {
      if (deploymentContext == null)
         throw new IllegalArgumentException("Null deployment context");

      ScopeBuilder builder = deploymentContext.getTransientAttachments().getAttachment(ScopeBuilder.class);
      if (builder == null)
         builder = DefaultScopeBuilder.INSTANCE;
      builder.initMetaDataRetrieval(repository, deploymentContext);
   }

   /**
    * Initialise the metadata retrieval for a deployment context
    *
    * @param repository the meta data repository
    * @param deploymentContext the deployment context
    */
   private static void initMutableMetaDataRetrieval(MutableMetaDataRepository repository, DeploymentContext deploymentContext)
   {
      if (deploymentContext == null)
         throw new IllegalArgumentException("Null deployment context");

      ScopeBuilder builder = deploymentContext.getTransientAttachments().getAttachment(ScopeBuilder.class);
      if (builder == null)
         builder = DefaultScopeBuilder.INSTANCE;
      builder.initMutableMetaDataRetrieval(repository, deploymentContext);
   }
   
   /**
    * For serialization
    */
   public AbstractDeploymentContext()
   {
   }

   /**
    * Create a new AbstractDeploymentContext.
    * 
    * @param name the name
    * @param relativePath the relative path to the top of the deployment
    * @throws IllegalArgumentException if the name is null
    */
   public AbstractDeploymentContext(String name, String relativePath)
   {
      this(name, name, relativePath);
   }

   /**
    * Create a new AbstractDeploymentContext.
    * 
    * @param name the name
    * @param simpleName the simple name
    * @param relativePath the relative path to the top of the deployment
    * @throws IllegalArgumentException if the name is null
    */
   public AbstractDeploymentContext(String name, String simpleName, String relativePath)
   {
      if (name == null)
         throw new IllegalArgumentException("Null name");
      if (relativePath == null)
         throw new IllegalArgumentException("Null relative path");
      this.name = name;
      this.simpleName = simpleName;
      if (simpleName == null)
         this.simpleName = name;
      this.relativePath = relativePath;
   }

   public String getName()
   {
      return name;
   }

   public Set<Object> getControllerContextNames()
   {
      return controllerContextNames != null ? Collections.unmodifiableSet(controllerContextNames) : null;
   }

   public synchronized void addControllerContextName(Object name)
   {
      if (controllerContextNames == null)
         controllerContextNames = new HashSet<Object>();
      controllerContextNames.add(name);
   }

   public synchronized void removeControllerContextName(Object name)
   {
      if (controllerContextNames != null)
      {
         controllerContextNames.remove(name);
         if (controllerContextNames.isEmpty())
            controllerContextNames = null;
      }
      else
         log.warn("Removing name on null names: " + name);
   }

   public String getSimpleName()
   {
      return simpleName;
   }

   public String getRelativePath()
   {
      return relativePath;
   }

   public int getRelativeOrder()
   {
      return relativeOrder;
   }

   public void setRelativeOrder(int relativeOrder)
   {
      this.relativeOrder = relativeOrder;
   }

   public Comparator<DeploymentContext> getComparator()
   {
      return comparator;
   }

   public void setComparator(Comparator<DeploymentContext> comparator)
   {
      if (comparator == null)
         comparator = DefaultDeploymentContextComparator.INSTANCE;
      this.comparator = comparator;
   }

   @Deprecated
   public Set<String> getTypes()
   {
      return deploymentTypes;
   }

   public ScopeKey getScope()
   {
      if (scope == null)
      {
         ScopeBuilder builder = getScopeBuilder(this);
         scope = builder.getDeploymentScope(this);
      }
      return scope;
   }

   public void setScope(ScopeKey scope)
   {
      this.scope = scope;
   }

   public ScopeKey getMutableScope()
   {
      if (mutableScope == null)
      {
         ScopeBuilder builder = getScopeBuilder(this);
         mutableScope = builder.getMutableDeploymentScope(this);
      }
      return mutableScope;
   }

   public void setMutableScope(ScopeKey mutableScope)
   {
      this.mutableScope = mutableScope;
   }

   public MetaData getMetaData()
   {
      return getMetaData(this);
   }

   public MutableMetaData getMutableMetaData()
   {
      return getMutableMetaData(this);
   }

   public DeploymentState getState()
   {
      return state;
   }

   public void setState(DeploymentState state)
   {
      if (state == null)
         throw new IllegalArgumentException("Null state");
      this.state = state;
   }

   public Deployment getDeployment()
   {
      return deployment;
   }
   
   public void setDeployment(Deployment deployment)
   {
      if (deployment == null)
         throw new IllegalArgumentException("Null deployment");
      this.deployment = deployment;
   }

   public DeploymentUnit getDeploymentUnit()
   {
      if (unit == null)
         unit = createDeploymentUnit();
      return unit;
   }

   public void setDeploymentUnit(DeploymentUnit unit)
   {
      this.unit = unit;
   }

   public ClassLoader getClassLoader()
   {
      return classLoader;
   }
   
   public void setClassLoader(ClassLoader classLoader)
   {
      this.classLoader = classLoader;
      if (classLoader != null)
         log.trace("ClassLoader for " + name + " is " + classLoader);
   }

   public boolean createClassLoader(ClassLoaderFactory factory) throws DeploymentException
   {
      if (factory == null)
         throw new IllegalArgumentException("Null factory");
      
      ClassLoader cl = getClassLoader();
      if (cl != null)
         return false;

      try
      {
         cl = factory.createClassLoader(getDeploymentUnit());
         if (cl != null)
         {
            setClassLoader(cl);
            this.classLoaderFactory = factory;
         }
         else
         {
            // No classloader, use the deployer's classloader
            setClassLoader(Thread.currentThread().getContextClassLoader());
         }
      }
      catch (Throwable t)
      {
         throw DeploymentException.rethrowAsDeploymentException("Error creating classloader for " + getName(), t);
      }
      return true;
   }

   public void removeClassLoader()
   {
      if (classLoaderFactory == null)
         return;
      try
      {
         classLoaderFactory.removeClassLoader(getDeploymentUnit());
      }
      catch (Throwable t)
      {
         log.warn("Error removing classloader for " + getName(), t);
      }
      classLoaderFactory = null;
      setClassLoader(null);
   }

   public void removeClassLoader(ClassLoaderFactory factory)
   {
      if (classLoaderFactory == factory)
         removeClassLoader();
   }

   public boolean isTopLevel()
   {
      return parent == null;
   }

   public DeploymentContext getTopLevel()
   {
      DeploymentContext result = this;
      DeploymentContext parent = getParent();
      while (parent != null)
      {
         result = parent;
         parent = parent.getParent();
      }
      return result;
   }
   
   public DeploymentContext getParent()
   {
      return parent;
   }

   public void setParent(DeploymentContext parent)
   {
      if (parent != null && this.parent != null)
         throw new IllegalStateException("Context already has a parent " + getName());
      this.parent = parent;
   }

   public List<DeploymentContext> getChildren()
   {
      if (children == null || children.isEmpty())
         return Collections.emptyList();
      
      return new ArrayList<DeploymentContext>(children);
   }

   public void addChild(DeploymentContext child)
   {
      if (child == null)
         throw new IllegalArgumentException("Null child");
      if (children == null)
         children = new TreeSet<DeploymentContext>(comparator);
      children.add(child);
   }

   public boolean removeChild(DeploymentContext child)
   {
      if (child == null)
         throw new IllegalArgumentException("Null child");
      if (children == null)
         return false;
      return children.remove(child);
   }

   public boolean isComponent()
   {
      return false;
   }
   
   public List<DeploymentContext> getComponents()
   {
      return Collections.unmodifiableList(components);
   }

   public void addComponent(DeploymentContext component)
   {
      if (component == null)
         throw new IllegalArgumentException("Null component");
      deployed();
      components.add(component);
      log.debug("Added component " + component.getName() + " to " + getName());
   }

   public boolean removeComponent(DeploymentContext component)
   {
      if (component == null)
         throw new IllegalArgumentException("Null component");

      List<DeploymentContext> componentComponents = component.getComponents();
      if (componentComponents.isEmpty() == false)
         log.warn("Removing component " + name + " which still has components " + componentComponents);
      boolean result = components.remove(component);
      component.cleanup();
      if (result)
         log.debug("Removed component " + component.getName() + " from " + getName());
      return result;
   }

   public ClassLoader getResourceClassLoader()
   {
      if (resourceClassLoader != null)
         return resourceClassLoader;
      
      DeploymentResourceLoader loader = getResourceLoader();
      resourceClassLoader = new DeploymentResourceClassLoader(loader);
      return resourceClassLoader;
   }

   public DeploymentResourceLoader getResourceLoader()
   {
      return EmptyResourceLoader.INSTANCE;
   }

   public DependencyInfo getDependencyInfo()
   {
      ControllerContext controllerContext = getTransientAttachments().getAttachment(ControllerContext.class);
      if (controllerContext != null)
         return controllerContext.getDependencyInfo();
      else
      {
         DeploymentContext parent = getParent();
         if (parent == null)
            throw new IllegalStateException("Deployment ControllerContext has not been set");
         return parent.getDependencyInfo();
      }
   }

   public void visit(DeploymentContextVisitor visitor) throws DeploymentException
   {
      if (visitor == null)
         throw new IllegalArgumentException("Null visitor");

      visit(this, visitor);
   }
   
   /**
    * Visit a context
    * 
    * @param context the context
    * @param visitor the visitor
    * @throws DeploymentException for any error
    */
   private void visit(DeploymentContext context, DeploymentContextVisitor visitor) throws DeploymentException
   {
      visitor.visit(context);
      try
      {
         List<DeploymentContext> children = context.getChildren();
         if (children.isEmpty())
            return;
         
         DeploymentContext[] childContexts = children.toArray(new DeploymentContext[children.size()]);
         for (int i = 0; i < childContexts.length; ++i)
         {
            if (childContexts[i] == null)
               throw new IllegalStateException("Null child context for " + context.getName() + " children=" + children);
            try
            {
               visit(childContexts[i], visitor);
            }
            catch (Throwable t)
            {
               for (int j = i-1; j >= 0; --j)
                  visitError(childContexts[j], visitor, true);
               throw DeploymentException.rethrowAsDeploymentException("Error visiting: " + childContexts[i].getName(), t);
            }
         }
      }
      catch (Throwable t)
      {
         visitError(context, visitor, false);
         throw DeploymentException.rethrowAsDeploymentException("Error visiting: " + context.getName(), t);
      }
   }

   /**
    * Unwind the visit invoking the previously visited context's error handler
    * 
    * @param context the context
    * @param visitor the visitor
    * @param visitChildren whether to visit the children
    * @throws DeploymentException for any error
    */
   private void visitError(DeploymentContext context, DeploymentContextVisitor visitor, boolean visitChildren) throws DeploymentException
   {
      if (visitChildren)
      {
         List<DeploymentContext> children = context.getChildren();
         if (children.isEmpty())
            return;
         
         for (DeploymentContext child : children)
         {
            try
            {
               visitError(child, visitor, true);
            }
            catch (Throwable t)
            {
               log.warn("Error during visit error: " + child.getName(), t);
            }
         }
         
      }
      try
      {
         visitor.error(context);
      }
      catch (Throwable t)
      {
         log.warn("Error during visit error: " + context.getName(), t);
      }
   }

   public Throwable getProblem()
   {
      return problem;
   }

   public void setProblem(Throwable problem)
   {
      this.problem = problem;
   }
   
   public boolean isDeployed()
   {
      return deployed;
   }
   
   public void deployed()
   {
      deployed = true;
   }

   public void cleanup()
   {
      cleanupRepository(this);
   }
   
   @Override
   public String toString()
   {
      StringBuilder buffer = new StringBuilder();
      buffer.append(getClass().getSimpleName());
      buffer.append('@');
      buffer.append(System.identityHashCode(this));
      buffer.append('{').append(name).append('}');
      return buffer.toString();
   }

   /**
    * Create a deployment unit
    * 
    * @return the deployment unit
    */
   protected DeploymentUnit createDeploymentUnit()
   {
      return new AbstractDeploymentUnit(this);
   }

   @SuppressWarnings("unchecked")
   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
   {
      super.readExternal(in);
      name = in.readUTF();
      simpleName = in.readUTF();
      relativePath = in.readUTF();
      state = (DeploymentState) in.readObject();
      problem = (Throwable) in.readObject();
      deployment = (Deployment) in.readObject();
      deployed = in.readBoolean();
      parent = (DeploymentContext) in.readObject();
      deploymentTypes = (Set) in.readObject();
      children = (SortedSet) in.readObject();
      components = (List) in.readObject();
   }

   /**
    * @serialData name
    * @serialData simpleName
    * @serialData relativePath
    * @serialData state
    * @serialData problem
    * @serialData deployment
    * @serialData deployed
    * @serialData parent
    * @serialData deploymentTypes
    * @serialData children
    * @serialData components
    * @param out the output
    * @throws IOException for any error
    */
   public void writeExternal(ObjectOutput out) throws IOException
   {
      super.writeExternal(out);
      out.writeUTF(name);
      out.writeUTF(simpleName);
      out.writeUTF(relativePath);
      out.writeObject(state);
      out.writeObject(problem);
      out.writeObject(deployment);
      out.writeBoolean(deployed);
      out.writeObject(parent);
      out.writeObject(deploymentTypes);
      out.writeObject(children);
      out.writeObject(components);
   }
}
