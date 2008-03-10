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
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jboss.dependency.spi.DependencyInfo;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.DeploymentState;
import org.jboss.deployers.spi.attachments.Attachments;
import org.jboss.deployers.spi.attachments.AttachmentsFactory;
import org.jboss.deployers.spi.attachments.MutableAttachments;
import org.jboss.deployers.structure.spi.ClassLoaderFactory;
import org.jboss.deployers.structure.spi.DeploymentContext;
import org.jboss.deployers.structure.spi.DeploymentContextVisitor;
import org.jboss.deployers.structure.spi.DeploymentResourceLoader;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.structure.spi.scope.ScopeBuilder;
import org.jboss.logging.Logger;
import org.jboss.metadata.spi.MetaData;
import org.jboss.metadata.spi.MutableMetaData;
import org.jboss.metadata.spi.scope.ScopeKey;

/**
 * AbstractDeploymentContext.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @author Scott.Stark@jboss.org
 * @version $Revision: 59630 $
 */
public class ComponentDeploymentContext implements DeploymentContext
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -5105972679660633071L;

   /** The log */
   protected Logger log = Logger.getLogger(getClass());
   
   /** The name */
   private String name;

   /** The controller context names - should be serializable */
   private Set<Object> controllerContextNames;

   /** The deployment unit */
   private DeploymentUnit unit;

   /** The parent context */
   private DeploymentContext parent;

   /** The component contexts */
   private List<DeploymentContext> components = new CopyOnWriteArrayList<DeploymentContext>();
   
   /** The attachments */
   private transient MutableAttachments transientAttachments = AttachmentsFactory.createMutableAttachments();
   
   /** The managed objects */
   private transient MutableAttachments transientManagedObjects = AttachmentsFactory.createMutableAttachments();
   
   /** The scope */
   private ScopeKey scope;
   
   /** The mutable scope */
   private ScopeKey mutableScope;
   
   /**
    * For serialization
    */
   public ComponentDeploymentContext()
   {
   }

   /**
    * Create a new ComponentDeploymentContext.
    * 
    * @param name the name
    * @param parent the parent
    * @throws IllegalArgumentException if the name or parent is null
    */
   public ComponentDeploymentContext(String name, DeploymentContext parent)
   {
      if (name == null)
         throw new IllegalArgumentException("Null name");
      if (parent == null)
         throw new IllegalArgumentException("Null parent");
      this.name = name;
      this.parent = parent;
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
      return parent.getSimpleName();
   }

   public String getRelativePath()
   {
      return parent.getRelativePath();
   }

   public int getRelativeOrder()
   {
      return 0;
   }

   public void setRelativeOrder(int relativeOrder)
   {
      // No relative ordering of components?
   }

   public Comparator<DeploymentContext> getComparator()
   {
      return null;
   }

   public void setComparator(Comparator<DeploymentContext> comparator)
   {
      // No relative ordering of components?
   }

   public Set<String> getTypes()
   {
      return parent.getTypes();
   }

   public ScopeKey getScope()
   {
      if (scope == null)
      {
         ScopeBuilder builder = AbstractDeploymentContext.getScopeBuilder(this);
         scope = builder.getComponentScope(this);
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
         ScopeBuilder builder = AbstractDeploymentContext.getScopeBuilder(this);
         mutableScope = builder.getMutableComponentScope(this);
      }
      return mutableScope;
   }

   public void setMutableScope(ScopeKey mutableScope)
   {
      this.mutableScope = mutableScope;
   }

   public MetaData getMetaData()
   {
      return AbstractDeploymentContext.getMetaData(this);
   }

   public MutableMetaData getMutableMetaData()
   {
      return AbstractDeploymentContext.getMutableMetaData(this);
   }

   public DeploymentState getState()
   {
      return parent.getState();
   }

   public void setState(DeploymentState state)
   {
      parent.setState(state);
   }

   public DeploymentUnit getDeploymentUnit()
   {
      if (unit == null)
         throw new IllegalStateException("Deployment unit has not been set");
      return unit;
   }

   public void setDeploymentUnit(DeploymentUnit unit)
   {
      this.unit = unit;
   }
   
   public void setMetaDataPath(String path)
   {
      throw new UnsupportedOperationException("Not supported for components");
   }

   public ClassLoader getClassLoader()
   {
      return parent.getClassLoader();
   }
   
   public void setClassLoader(ClassLoader classLoader)
   {
      throw new UnsupportedOperationException("Not supported for components");
   }
   
   public boolean createClassLoader(ClassLoaderFactory factory) throws DeploymentException
   {
      return false;
   }

   public void removeClassLoader()
   {
   }

   public void removeClassLoader(ClassLoaderFactory factory)
   {
   }

   public boolean isTopLevel()
   {
      return false;
   }

   public DeploymentContext getTopLevel()
   {
      return parent.getTopLevel();
   }
   
   public DeploymentContext getParent()
   {
      return parent;
   }

   public void setParent(DeploymentContext parent)
   {
      throw new UnsupportedOperationException("Not supported for components");
   }

   public List<DeploymentContext> getChildren()
   {
      return Collections.emptyList();
   }

   public void addChild(DeploymentContext child)
   {
      throw new UnsupportedOperationException("Not supported for components");
   }

   public boolean removeChild(DeploymentContext child)
   {
      throw new UnsupportedOperationException("Not supported for components");
   }

   public Deployment getDeployment()
   {
      return parent.getDeployment();
   }

   public void setDeployment(Deployment deployment)
   {
      throw new UnsupportedOperationException("Not supported for components");
   }

   public boolean isComponent()
   {
      return true;
   }

   public List<DeploymentContext> getComponents()
   {
      return Collections.unmodifiableList(components);
   }

   public void addComponent(DeploymentContext component)
   {
      if (component == null)
         throw new IllegalArgumentException("Null component");
      components.add(component);
   }

   public boolean removeComponent(DeploymentContext component)
   {
      if (component == null)
         throw new IllegalArgumentException("Null component");
      boolean result = components.remove(component);
      component.cleanup();
      return result;
   }

   public ClassLoader getResourceClassLoader()
   {
      return parent.getResourceClassLoader();
   }

   public DeploymentResourceLoader getResourceLoader()
   {
      return parent.getResourceLoader();
   }

   public DependencyInfo getDependencyInfo()
   {
      return parent.getDependencyInfo();
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

   public Attachments getPredeterminedManagedObjects()
   {
      return parent.getPredeterminedManagedObjects();
   }
   
   public void setPredeterminedManagedObjects(Attachments objects)
   {
      throw new UnsupportedOperationException("Not supported for components");
   }
   
   public MutableAttachments getTransientManagedObjects()
   {
      return transientManagedObjects;
   }
   
   public MutableAttachments getTransientAttachments()
   {
      return transientAttachments;
   }

   public Throwable getProblem()
   {
      return parent.getProblem();
   }

   public void setProblem(Throwable problem)
   {
      parent.setProblem(problem);
   }
   
   public void deployed()
   {
      parent.deployed();
   }

   public boolean isDeployed()
   {
      return parent.isDeployed();
   }

   public void cleanup()
   {
      AbstractDeploymentContext.cleanupRepository(this);
   }

   public String toString()
   {
      StringBuilder buffer = new StringBuilder();
      buffer.append(getClass().getSimpleName());
      buffer.append('@');
      buffer.append(System.identityHashCode(this));
      buffer.append('{').append(name).append('}');
      return buffer.toString();
   }
}
