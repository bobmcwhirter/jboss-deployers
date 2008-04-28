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

import org.jboss.classloading.spi.dependency.Module;
import org.jboss.classloading.spi.visitor.ResourceFilter;
import org.jboss.classloading.spi.visitor.ResourceVisitor;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractSimpleRealDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;

/**
 * Abstract resource visitor deployer.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class AbstractResourceVisitorDeployer extends AbstractSimpleRealDeployer<Module>
{
   private ResourceVisitor visitor;
   private ResourceFilter filter;

   public AbstractResourceVisitorDeployer()
   {
      super(Module.class);
      setStage(DeploymentStages.POST_CLASSLOADER);
   }

   public AbstractResourceVisitorDeployer(ResourceVisitor visitor)
   {
      this();
      this.visitor = visitor;
   }

   public AbstractResourceVisitorDeployer(ResourceVisitor visitor, ResourceFilter filter)
   {
      this(visitor);
      this.filter = filter;
   }

   public void deploy(DeploymentUnit unit, Module module) throws DeploymentException
   {
      ResourceVisitor currentVisitor = visitor;
      if (currentVisitor == null)
         currentVisitor = createVisitor(unit);
      ResourceFilter currentFilter = filter;
      if (currentFilter == null)
         currentFilter = createFilter(unit);

      if (currentFilter != null)
         module.visit(currentVisitor, currentFilter);
      else
         module.visit(currentVisitor);
   }

   /**
    * Create resource visitor from unit.
    *
    * @param unit the deployment unit
    * @return new resource visitor
    */
   protected ResourceVisitor createVisitor(DeploymentUnit unit)
   {
      throw new UnsupportedOperationException("Missing resource visitor");
   }

   /**
    * Create resource filter from unit.
    *
    * @param unit the deployment unit
    * @return new resource filter
    */
   protected ResourceFilter createFilter(DeploymentUnit unit)
   {
      return null;
   }
}