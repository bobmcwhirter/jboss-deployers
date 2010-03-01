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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.dependency.spi.DependencyInfo;
import org.jboss.dependency.spi.DependencyItem;
import org.jboss.deployers.client.spi.main.MainDeployer;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStage;
import org.jboss.deployers.spi.attachments.MutableAttachments;
import org.jboss.deployers.spi.attachments.helpers.AbstractMutableAttachments;
import org.jboss.deployers.structure.spi.*;
import org.jboss.metadata.spi.MetaData;
import org.jboss.metadata.spi.MutableMetaData;
import org.jboss.metadata.spi.scope.ScopeKey;

/**
 * AbstractDeploymentUnit.<p>
 * 
 * This is just a wrapper to the deployment context that
 * restricts people from "poking" behind the scenes.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @author Scott.Stark@jboss.org
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 * @version $Revision: 1.1 $
 */
public class AbstractDeploymentUnit extends AbstractMutableAttachments implements DeploymentUnit, DeploymentUnitExt
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1513962148798298768L;
   
   /** The deployment context */
   private DeploymentContext deploymentContext;

   /**
    * For serialization
    */
   public AbstractDeploymentUnit()
   {
   }

   /**
    * Create a new AbstractDeploymentUnit.
    * 
    * @param deploymentContext the deployment context
    * @throws IllegalArgumentException for a null deployment context
    */
   public AbstractDeploymentUnit(DeploymentContext deploymentContext)
   {
      if (deploymentContext == null)
         throw new IllegalArgumentException("Null deployment context");
      this.deploymentContext = deploymentContext;
   }

   public void changeRelativeOrder(int relativeOrder)
   {
      if (deploymentContext instanceof DeploymentContextExt)
      {
         DeploymentContextExt ext = (DeploymentContextExt) deploymentContext;
         ext.changeRelativeOrder(relativeOrder);
      }
   }

   public String getName()
   {
      return deploymentContext.getName();
   }

   public Set<Object> getControllerContextNames()
   {
      return deploymentContext.getControllerContextNames();
   }

   public void addControllerContextName(Object name)
   {
      deploymentContext.addControllerContextName(name);
   }

   public void removeControllerContextName(Object name)
   {
      deploymentContext.removeControllerContextName(name);
   }

   public String getSimpleName()
   {
      return deploymentContext.getSimpleName();
   }

   public String getRelativePath()
   {
      return deploymentContext.getRelativePath();
   }
   
   public ScopeKey getScope()
   {
      return deploymentContext.getScope();
   }

   public void setScope(ScopeKey key)
   {
      deploymentContext.setScope(key);
   }

   public ScopeKey getMutableScope()
   {
      return deploymentContext.getMutableScope();
   }

   public void setMutableScope(ScopeKey key)
   {
      deploymentContext.setMutableScope(key);
   }

   public MetaData getMetaData()
   {
      return deploymentContext.getMetaData();
   }

   public MutableMetaData getMutableMetaData()
   {
      return deploymentContext.getMutableMetaData();
   }

   public ClassLoader getClassLoader()
   {
      ClassLoader cl = deploymentContext.getClassLoader();
      if (cl == null)
         throw new IllegalStateException("ClassLoader has not been set");
      deploymentContext.deployed();
      return cl;
   }

   public boolean createClassLoader(ClassLoaderFactory factory) throws DeploymentException
   {
      return deploymentContext.createClassLoader(factory);
   }

   public void removeClassLoader(ClassLoaderFactory factory)
   {
      deploymentContext.removeClassLoader(factory);
   }

   public boolean isTopLevel()
   {
      return deploymentContext.isTopLevel();
   }
   
   public DeploymentUnit getTopLevel()
   {
      DeploymentContext context = deploymentContext.getTopLevel();
      return context.getDeploymentUnit();
   }

   public DeploymentUnit getParent()
   {
      DeploymentContext parent = deploymentContext.getParent();
      if (parent == null)
         return null;
      return parent.getDeploymentUnit();
   }

   public List<DeploymentUnit> getChildren()
   {
      List<DeploymentContext> children = deploymentContext.getChildren();
      if (children == null || children.isEmpty())
         return Collections.emptyList();
      
      List<DeploymentUnit> result = new ArrayList<DeploymentUnit>(children.size());
      for (DeploymentContext child : children)
      {
         DeploymentUnit unit = child.getDeploymentUnit();
         result.add(unit);
      }
      return result;
   }

   public List<DeploymentUnit> getComponents()
   {
      List<DeploymentContext> components = deploymentContext.getComponents();
      if (components == null || components.isEmpty())
         return Collections.emptyList();
      
      List<DeploymentUnit> result = new ArrayList<DeploymentUnit>(components.size());
      for (DeploymentContext component : components)
      {
         DeploymentUnit unit = component.getDeploymentUnit();
         result.add(unit);
      }
      return result;
   }

   /**
    * Create a component deployment context
    * 
    * @param name the name
    * @param parent the parent
    * @return the deployment context
    */
   protected DeploymentContext createComponentDeploymentContext(String name, DeploymentContext parent)
   {
      return new ComponentDeploymentContext(name, parent);
   }

   /**
    * Create a component deployment unit
    * 
    * @param component the component contextr
    * @return the deployment unit
    */
   protected DeploymentUnit createComponentDeploymentUnit(DeploymentContext component)
   {
      return new AbstractDeploymentUnit(component);
   }
   
   public boolean isComponent()
   {
      return deploymentContext.isComponent();
   }

   public DeploymentUnit addComponent(String name)
   {
      DeploymentContext component = createComponentDeploymentContext(name, deploymentContext);
      DeploymentUnit unit = createComponentDeploymentUnit(component);
      component.setDeploymentUnit(unit);
      deploymentContext.addComponent(component);
      return unit;
   }

   /**
    * Get component deployment context.
    *
    * @param name the name
    * @return component component context or null if no match
    */
   protected DeploymentContext getComponentContext(String name)
   {
      List<DeploymentContext> components = deploymentContext.getComponents();
      if (components == null || components.isEmpty())
         return null;

      for (DeploymentContext component : components)
      {
         if (name.equals(component.getName()))
            return component;
      }
      return null;
   }

   public DeploymentUnit getComponent(String name)
   {
      if (name == null)
         throw new IllegalArgumentException("Null name");

      DeploymentContext component = getComponentContext(name);
      return component != null ? component.getDeploymentUnit() : null;
   }

   public boolean removeComponent(String name)
   {
      if (name == null)
         throw new IllegalArgumentException("Null name");

      DeploymentContext component = getComponentContext(name);
      return component != null && deploymentContext.removeComponent(component);
   }

   public <T> Set<? extends T> getAllMetaData(Class<T> type)
   {
      if (type == null)
         throw new IllegalArgumentException("Null type");
      
      Set<T> result = new HashSet<T>();
      Map<String, Object> attachments = getAttachments();
      for (Object object : attachments.values())
      {
         if (type.isInstance(object))
         {
            T t = type.cast(object);
            result.add(t);
         }
      }
      if (result.isEmpty() == false)
         deploymentContext.deployed();
      return result;
   }

   public MutableAttachments getTransientManagedObjects()
   {
      return getDeploymentContext().getTransientManagedObjects();
   }

   public Object addAttachment(String name, Object attachment)
   {
      DeploymentContext deploymentContext = getDeploymentContext();
      deploymentContext.deployed();
      return deploymentContext.getTransientAttachments().addAttachment(name, attachment);
   }

   public void clear()
   {
      DeploymentContext deploymentContext = getDeploymentContext();
      deploymentContext.getTransientAttachments().clear();
      deploymentContext.getTransientManagedObjects().clear();
   }

   public void clearChangeCount()
   {
      DeploymentContext deploymentContext = getDeploymentContext();
      deploymentContext.getTransientAttachments().clearChangeCount();
      deploymentContext.getTransientManagedObjects().clearChangeCount();
   }

   public int getChangeCount()
   {
      DeploymentContext deploymentContext = getDeploymentContext();
      return deploymentContext.getTransientAttachments().getChangeCount() + deploymentContext.getTransientManagedObjects().getChangeCount();
   }

   public Object removeAttachment(String name)
   {
      return getDeploymentContext().getTransientAttachments().removeAttachment(name);
   }

   public Object getAttachment(String name)
   {
      DeploymentContext deploymentContext = getDeploymentContext();
      DeploymentContext parent = deploymentContext.getParent();
      if (deploymentContext.isComponent() == false)
         parent = null;
      Object result = deploymentContext.getPredeterminedManagedObjects().getAttachment(name);
      if (result != null)
      {
         deploymentContext.deployed();
         return result;
      }
      if (parent != null)
      {
         result = parent.getPredeterminedManagedObjects().getAttachment(name);
         if (result != null)
         {
            deploymentContext.deployed();
            return result;
         }
      }
      result = deploymentContext.getTransientManagedObjects().getAttachment(name);
      if (result != null)
      {
         deploymentContext.deployed();
         return result;
      }
      if (parent != null)
      {
         result = parent.getTransientManagedObjects().getAttachment(name);
         if (result != null)
         {
            deploymentContext.deployed();
            return result;
         }
      }
      result = deploymentContext.getTransientAttachments().getAttachment(name);
      if (result != null)
      {
         deploymentContext.deployed();
         return result;
      }
      if (parent != null)
      {
         result = parent.getTransientAttachments().getAttachment(name);
         if (result != null)
         {
            deploymentContext.deployed();
            return result;
         }
      }
      return null;
   }

   public Map<String, Object> getAttachments()
   {
      DeploymentContext deploymentContext = getDeploymentContext();
      DeploymentContext parent = deploymentContext.getParent();
      if (deploymentContext.isComponent() == false)
         parent = null;
      HashMap<String, Object> result = new HashMap<String, Object>();
      if (parent != null)
         result.putAll(parent.getTransientAttachments().getAttachments());
      result.putAll(deploymentContext.getTransientAttachments().getAttachments());
      if (parent != null)
         result.putAll(parent.getTransientManagedObjects().getAttachments());
      result.putAll(deploymentContext.getTransientManagedObjects().getAttachments());
      if (parent != null)
         result.putAll(parent.getPredeterminedManagedObjects().getAttachments());
      result.putAll(deploymentContext.getPredeterminedManagedObjects().getAttachments());
      if (result.isEmpty() == false)
         deploymentContext.deployed();
      return Collections.unmodifiableMap(result);
   }

   public boolean hasAttachments()
   {
      DeploymentContext deploymentContext = getDeploymentContext();
      if (deploymentContext.getTransientAttachments().hasAttachments())
         return true;
      else if (deploymentContext.getTransientManagedObjects().hasAttachments())
         return true;
      else if (deploymentContext.getPredeterminedManagedObjects().hasAttachments())
         return true;
      
      if (deploymentContext.isComponent())
         return deploymentContext.getParent().getDeploymentUnit().hasAttachments();
      return false;
   }

   public boolean isAttachmentPresent(String name)
   {
      return getAttachment(name) != null;
   }
   
   public ClassLoader getResourceClassLoader()
   {
      return getDeploymentContext().getResourceClassLoader();
   }

   public DeploymentResourceLoader getResourceLoader()
   {
      return getDeploymentContext().getResourceLoader();
   }

   public void addIDependOn(DependencyItem dependency)
   {
      getDependencyInfo().addIDependOn(dependency);
   }

   public void visit(DeploymentUnitVisitor visitor) throws DeploymentException
   {
      UnitVisitorToContextVisitor contextVisitor = new UnitVisitorToContextVisitor(visitor); 
      getDeploymentContext().visit(contextVisitor);
   }

   public Object getControllerContextName()
   {
      return getDeploymentContext().getControllerContextName();
   }

   public DeploymentStage getRequiredStage()
   {
      return getDeploymentContext().getRequiredStage();
   }
   
   public void setRequiredStage(DeploymentStage stage)
   {
      getDeploymentContext().setRequiredStage(stage);
   }

   public DependencyInfo getDependencyInfo()
   {
      return getDeploymentContext().getDependencyInfo();
   }

   public void removeIDependOn(DependencyItem dependency)
   {
      getDependencyInfo().removeIDependOn(dependency);
   }

   public MainDeployer getMainDeployer()
   {
      return getTopLevel().getAttachment(MainDeployer.class);
   }

   /**
    * Get the deployment context
    * 
    * @return the deployment context
    */
   protected DeploymentContext getDeploymentContext()
   {
      return deploymentContext;
   }

   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
   {
      super.readExternal(in);
      deploymentContext = (DeploymentContext) in.readObject();
   }

   /**
    * @serialData deploymentContext
    * @param out the output
    * @throws IOException for any error
    */
   public void writeExternal(ObjectOutput out) throws IOException
   {
      super.writeExternal(out);
      out.writeObject(deploymentContext);
   }

   public String toString()
   {
      return String.valueOf(deploymentContext);
   }
}
