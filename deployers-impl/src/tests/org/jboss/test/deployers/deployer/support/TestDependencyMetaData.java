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
package org.jboss.test.deployers.deployer.support;

import java.util.HashSet;
import java.util.Set;

import org.jboss.dependency.plugins.AbstractDependencyItem;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.dependency.spi.DependencyItem;
import org.jboss.deployers.spi.deployer.DeploymentStage;
import org.jboss.deployers.spi.deployer.DeploymentStages;

/**
 * TestDependencyMetaData.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class TestDependencyMetaData
{
   private static final ControllerState CLASSLOADER = new ControllerState(DeploymentStages.CLASSLOADER.getName());
   
   public String name;
   
   public Set<DependencyItem> iDependOn = new HashSet<DependencyItem>();

   public TestDependencyMetaData(String name)
   {
      this.name = name;
   }
   
   public DependencyItem addDependencyItem(Object iDependOn)
   {
      return addDependencyItem(iDependOn, null);
   }
   
   public DependencyItem addDependencyItem(Object iDependOn, DeploymentStage whenRequired)
   {
      return addDependencyItem(iDependOn, whenRequired, null);
   }

   public DependencyItem addDependencyItem(Object iDependOn, DeploymentStage whenRequired, DeploymentStage dependentState)
   {
      ControllerState whenState = CLASSLOADER;
      if (whenRequired != null)
         whenState = new ControllerState(whenRequired.getName());
      ControllerState depState = ControllerState.INSTALLED;
      if (dependentState != null)
         depState = new ControllerState(dependentState.getName()); 
      DependencyItem item = new AbstractDependencyItem(name, iDependOn, whenState, depState);
      this.iDependOn.add(item);
      return item;
   }
}
