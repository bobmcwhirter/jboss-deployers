/*
* JBoss, Home of Professional Open Source
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
package org.jboss.deployers.structure.spi;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.management.ObjectName;

import org.jboss.dependency.spi.DependencyInfo;
import org.jboss.deployers.spi.DeploymentState;
import org.jboss.metadata.spi.scope.ScopeKey;

/**
 * DeploymentMBean.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public interface DeploymentMBean
{
   /**
    * Get the deployment name
    * 
    * @return the name
    */
   String getName();

   /**
    * Get the object name
    * 
    * @return the object name
    */
   ObjectName getObjectName();
   
   /**
    * Get the controller context names.
    *
    * @return the names
    */
   Set<Object> getControllerContextNames();

   /**
    * Get the simple vfs name of the deployment unit. This is the simple
    * name of the virtual file .
    * 
    * vfs path ------------------- relative path
    * deploy/some.ear              "some.ear"
    * deploy/some.ear/x.ejb        "x.ejb"
    * deploy/some.ear/y.sar        "y.sar"
    * deploy/some.ear/y.sar/z.rar  "z.rar"
    * @return the deployment unit simple path
    */
   String getSimpleName();

   /**
    * Get the path of this deployment relative to the top of the deployment
    * 
    * vfs path ------------------- relative path
    * deploy/some.ear              ""
    * deploy/some.ear/x.ejb        "/x.ejb"
    * deploy/some.ear/y.sar        "/y.sar"
    * deploy/some.ear/y.sar/z.rar  "/y.sar/z.rar"
    * 
    * @return the top-level deployment relative path
    */
   String getRelativePath();

   /**
    * Get the relative order
    * 
    * @return the relative order
    */
   int getRelativeOrder();

   /**
    * Get the comparator.
    * 
    * @return the comparator.
    */
   Comparator<DeploymentContext> getComparator();

   /**
    * Get the scope
    * 
    * @return the scope
    */
   ScopeKey getScope();
   
   /**
    * Get the mutable scope
    * 
    * @return the mutable scope
    */
   ScopeKey getMutableScope();
   
   /**
    * Set the mutable scope
    * 
    * @param key the mutable scope key
    */
   void setMutableScope(ScopeKey key);

   /**
    * Get the deployment state
    * 
    * @return the state
    */
   DeploymentState getState();
   
   /**
    * Gets the classloader for this deployment unit
    * 
    * @return the classloader
    */
   ObjectName getClassLoaderName();

   /**
    * Whether this is a top level deployment
    * 
    * @return true when top level
    */
   boolean isTopLevel();
   
   /**
    * Get the top level deployment
    * 
    * @return the top level deployment
    */
   ObjectName getTopLevelName();
   
   /**
    * The parent
    * 
    * @return the parent
    */
   ObjectName getParentName();
   
   /**
    * The children
    * 
    * @return the children
    */
   List<ObjectName> getChildNames();
   
   /**
    * Whether this is a component
    * 
    * @return true when a component
    */
   boolean isComponent();
   
   /**
    * The components
    * 
    * @return the components
    */
   List<ObjectName> getComponentNames();
   
   /**
    * Get the dependency info
    * 
    * @return the dependency
    */
   DependencyInfo getDependencyInfo();

   /**
    * Whether the deployment was processed
    * 
    * @return true when processed
    */
   boolean isDeployed();
   
   /**
    * Get the problem for this context
    * 
    * @return the problem
    */
   Throwable getProblem();
}
