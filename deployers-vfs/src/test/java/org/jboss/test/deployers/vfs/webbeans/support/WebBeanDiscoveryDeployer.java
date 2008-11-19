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
package org.jboss.test.deployers.vfs.webbeans.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jboss.classloading.spi.dependency.Module;
import org.jboss.classloading.spi.visitor.ClassFilter;
import org.jboss.classloading.spi.visitor.ResourceContext;
import org.jboss.classloading.spi.visitor.ResourceFilter;
import org.jboss.classloading.spi.visitor.ResourceVisitor;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.vfs.spi.deployer.AbstractOptionalVFSRealDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.virtual.VirtualFile;

/**
 * WBD deployer.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class WebBeanDiscoveryDeployer extends AbstractOptionalVFSRealDeployer<WebBeansMetaData>
{
   public WebBeanDiscoveryDeployer()
   {
      super(WebBeansMetaData.class);
      addOutput(WebBeanDiscovery.class);
   }

   public void deploy(VFSDeploymentUnit unit, WebBeansMetaData deployment) throws DeploymentException
   {
      VFSDeploymentUnit topUnit = unit.getTopLevel();
      WebBeanDiscoveryImpl wbdi = topUnit.getAttachment(WebBeanDiscovery.class.getName(), WebBeanDiscoveryImpl.class);
      if (wbdi == null)
      {
         wbdi = new WebBeanDiscoveryImpl();
         topUnit.addAttachment(WebBeanDiscovery.class.getName(), wbdi);
      }

      try
      {
         Module module = unit.getAttachment(Module.class);
         if (module == null)
         {
            VFSDeploymentUnit parent = unit.getParent();
            while(parent != null && module == null)
            {
               module = parent.getAttachment(Module.class);
               parent = parent.getParent();
            }
            if (module == null)
               throw new IllegalArgumentException("No module in deployment unit's hierarchy: " + unit.getName());
         }

         if (deployment != null) // do some more
            wbdi.addWebBeansXmlURL(deployment.getURL());

         WBDiscoveryVisitor visitor = new WBDiscoveryVisitor(wbdi, unit.getClassLoader());

         Iterable<VirtualFile> classpaths = getClassPaths(unit);
         for (VirtualFile cp : classpaths)
         {
            VirtualFile wbXml = cp.getChild("META-INF/web-beans.xml");
            if (wbXml != null)
            {
               // add url
               wbdi.addWebBeansXmlURL(wbXml.toURL());
               // add classes
               module.visit(visitor, ClassFilter.INSTANCE, null, cp.toURL());
            }
         }

         // handle war slightly different
         VirtualFile warWbXml = unit.getFile("WEB-INF/web-beans.xml");
         if (warWbXml != null)
         {
            VirtualFile classes = unit.getFile("WEB-INF/classes");
            if (classes != null)
               module.visit(visitor, ClassFilter.INSTANCE, null, classes.toURL());
         }
      }
      catch (Exception e)
      {
         throw DeploymentException.rethrowAsDeploymentException("Cannot deploy WBD.", e);
      }
   }

   /**
    * Get the matching class paths that belong to this deployment unit.
    *
    * @param unit the deployment unit
    * @return matching class paths
    * @throws Exception for any error
    */
   protected Iterable<VirtualFile> getClassPaths(VFSDeploymentUnit unit) throws Exception
   {
      List<VirtualFile> classpath = unit.getClassPath();
      if (classpath != null && classpath.isEmpty() == false)
      {
         List<VirtualFile> matching = new ArrayList<VirtualFile>();
         VirtualFile root = unit.getRoot();
         for (VirtualFile cp : classpath)                        
         {
            VirtualFile check = cp;
            while(check != null && check.equals(root) == false)
               check = check.getParent();

            if (check != null)
               matching.add(cp);
         }
         return matching;
      }
      return Collections.emptySet();
   }

   private class WBDiscoveryVisitor implements ResourceVisitor
   {
      private WebBeanDiscoveryImpl wbdi;
      private ClassLoader cl;

      private WBDiscoveryVisitor(WebBeanDiscoveryImpl wbdi, ClassLoader cl)
      {
         this.wbdi = wbdi;
         this.cl = cl;
      }

      public ResourceFilter getFilter()
      {
         return ClassFilter.INSTANCE;
      }

      public void visit(ResourceContext resource)
      {
         try
         {
            wbdi.addWebBeanClass(cl.loadClass(resource.getClassName()));
         }
         catch (ClassNotFoundException e)
         {
            throw new RuntimeException(e);
         }
      }
   }
}