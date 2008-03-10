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
package org.jboss.deployers.vfs.spi.structure.helpers;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.jboss.classloading.spi.metadata.ClassLoadingMetaData;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.structure.spi.DeploymentUnitVisitor;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.virtual.VirtualFile;

/**
 * Collects all the classpath elements
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class ClassPathVisitor implements DeploymentUnitVisitor
{
   /** The initial deployment unit */
   private DeploymentUnit initial;
   
   /** The full classpath */
   private Set<VirtualFile> classPath = new LinkedHashSet<VirtualFile>();
   
   /**
    * Create a new ClassPathVisitor.
    */
   @Deprecated // pass the initial unit to weed subdeployments with their own classloader
   public ClassPathVisitor()
   {
      
   }
   
   /**
    * Create a new ClassPathVisitor.
    * 
    * @param initial the initial unit
    */
   public ClassPathVisitor(DeploymentUnit initial)
   {
      if (initial == null)
         throw new IllegalArgumentException("Null initial");
      this.initial = initial;
   }
   
   /**
    * Get the full classpath after the visit
    * 
    * @return the full classpath
    */
   public Set<VirtualFile> getClassPath()
   {
      return classPath;
   }
   
   public void visit(DeploymentUnit unit) throws DeploymentException
   {
      if (unit instanceof VFSDeploymentUnit == false)
         return;
      
      // This part of the deployment wants its own classloader
      if (unit != initial && unit.getAttachment(ClassLoadingMetaData.class) != null)
         return;
      
      VFSDeploymentUnit vfsUnit = (VFSDeploymentUnit) unit;
      List<VirtualFile> paths = vfsUnit.getClassPath();
      if (paths != null)
         classPath.addAll(paths);
   }

   public void error(DeploymentUnit context)
   {
      // nothing
   }
}
