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
package org.jboss.test.deployers.vfs.reflect.support;

import javassist.ClassPool;

import org.jboss.classloader.plugins.ClassLoaderUtils;
import org.jboss.classloader.plugins.filter.PatternClassFilter;
import org.jboss.classloader.spi.filter.ClassFilter;
import org.jboss.classloading.spi.metadata.ClassLoadingMetaData10;
import org.jboss.classloading.spi.vfs.metadata.VFSClassLoaderFactory10;
import org.jboss.classpool.base.IsLocalResourcePluginFactoryRegistry;
import org.jboss.classpool.spi.AbstractClassPoolFactory;
import org.jboss.test.deployers.BootstrapDeployersTest;
import org.jboss.test.deployers.BootstrapDeployersTestDelegate;
import org.jboss.util.loading.Translatable;
import org.jboss.xb.binding.resolver.MutableSchemaResolver;
import org.jboss.xb.binding.sunday.unmarshalling.SingletonSchemaResolverFactory;

/**
 * JavassistTestDelegate, sets a FilteredClassPool as the default ClassPool 
 * of AbstractClassPoolFactory.
 * 
 * @author <a href="flavia@jboss.com">Flavia Rainone</a>
 * @version $Revision$
 */
public class JavassistTestDelegate extends BootstrapDeployersTestDelegate
{
   private static ClassPool defaultClassPool;
   
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
      defaultClassPool = new FilteredClassPool(AbstractClassPoolFactory.getSystemClassPool(), classFilter);
      IsLocalResourcePluginFactoryRegistry.addPluginFactory(Translatable.class, new FilteredIsLocalResourcePluginFactory(classFilter));
   }
   
   public JavassistTestDelegate(Class<?> clazz) throws Exception
   {
      super(clazz);
   }

   protected void deploy() throws Exception
   {
      super.deploy();
      AbstractClassPoolFactory.setSystemClassPool(defaultClassPool);
   }
}