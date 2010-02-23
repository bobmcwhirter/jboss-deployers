/*
* JBoss, Home of Professional Open Source
* Copyright 2008, JBoss Inc., and individual contributors as indicated
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
package org.jboss.test.deployers;

import java.net.URL;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.classloader.plugins.ClassLoaderUtils;
import org.jboss.classloader.plugins.filter.PatternClassFilter;
import org.jboss.classloader.spi.ClassLoaderDomain;
import org.jboss.classloader.spi.ClassLoaderSystem;
import org.jboss.classloader.spi.ParentPolicy;
import org.jboss.classloader.spi.filter.ClassFilter;
import org.jboss.classloader.spi.filter.ClassFilterUtils;
import org.jboss.classloading.spi.metadata.ClassLoadingMetaData10;
import org.jboss.classloading.spi.vfs.metadata.VFSClassLoaderFactory10;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.deployers.plugins.main.MainDeployerImpl;
import org.jboss.deployers.spi.deployer.Deployers;
import org.jboss.test.kernel.junit.MicrocontainerTestDelegate;
import org.jboss.xb.binding.resolver.MutableSchemaResolver;
import org.jboss.xb.binding.sunday.unmarshalling.SingletonSchemaResolverFactory;

/**
 * BootstrapDeployersTestDelegate.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class BootstrapDeployersTestDelegate extends MicrocontainerTestDelegate
{
   private static ParentPolicy parentPolicy;
   
   private MainDeployerImpl mainDeployer;

   private MBeanServer server = null;
   
   static
   {
      MutableSchemaResolver resolver = SingletonSchemaResolverFactory.getInstance().getSchemaBindingResolver();
      resolver.mapURIToClass("urn:jboss:classloader:1.0", VFSClassLoaderFactory10.class);
      resolver.mapURIToClass("urn:jboss:classloading:1.0", ClassLoadingMetaData10.class);

      // TODO add a negating class filter to jboss-classloader
      ClassFilter classFilter = new ClassFilter()
      {
         /** The serialVersionUID */
         private static final long serialVersionUID = 1L;
         
         String packageName = BootstrapDeployersTest.class.getPackage().getName();
         String packagePath = ClassLoaderUtils.packageNameToPath(BootstrapDeployersTest.class.getName());
         ClassFilter patternFilter = new PatternClassFilter(
               new String[] { packageName + "\\..+" }, 
               new String[] { packagePath + "/.+" },
               new String[] { packageName, packageName + "\\..*"}
         ); 
         public boolean matchesClassName(String className)
         {
            return patternFilter.matchesClassName(className) == false;
         }

         public boolean matchesPackageName(String packageName)
         {
            return patternFilter.matchesPackageName(packageName) == false;
         }

         public boolean matchesResourcePath(String resourcePath)
         {
            return patternFilter.matchesResourcePath(resourcePath) == false;
         }
         
         public String toString()
         {
            return "EXCLUDE " + patternFilter;
         }
      };
      
      parentPolicy = new ParentPolicy(classFilter, ClassFilterUtils.NOTHING, "BEFORE");
   }
   
   public BootstrapDeployersTestDelegate(Class<?> clazz) throws Exception
   {
      super(clazz);
   }

   public MBeanServer getMBeanServer()
   {
      return server;
   }

   public void setMBeanServer(MBeanServer server)
   {
      this.server = server;
   }

   protected void deploy() throws Exception
   {
      String common = "/bootstrap/bootstrap.xml";
      URL url = getClass().getResource(common);
      if (url == null)
         throw new IllegalStateException(common + " not found");
      deploy(url);

      ClassLoaderSystem system = getBean("ClassLoaderSystem", ControllerState.INSTALLED, ClassLoaderSystem.class);
      ClassLoaderDomain domain = system.getDefaultDomain();
      domain.setParentPolicy(parentPolicy);
      
      if (server != null)
      {
         Deployers deployers = getBean("Deployers", ControllerState.INSTALLED, Deployers.class);
         server.registerMBean(deployers, new ObjectName("test:type=Deployers"));
      }
      
      super.deploy();
   }
   
   protected MainDeployerImpl getMainDeployer()
   {
      if (mainDeployer == null)
         mainDeployer = getBean("MainDeployer", ControllerState.INSTALLED, MainDeployerImpl.class);
      return mainDeployer;
   }
}
