/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.deployers.annotations.support;

import org.jboss.deployers.plugins.classloading.AbstractLevelClassLoaderSystemDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.classloading.spi.dependency.Module;
import org.jboss.classloader.spi.ClassLoaderSystem;
import org.jboss.classloader.spi.ClassLoaderDomain;

/**
 * InterceptionClassLoaderSystemDeployer.
 *
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 */
public class InterceptionClassLoaderSystemDeployer extends AbstractLevelClassLoaderSystemDeployer
{
   @Override
   public void removeClassLoader(DeploymentUnit unit) throws Exception
   {
      Module module = unit.getAttachment(Module.class);
      if (module == null)
         return;

      ClassLoader classLoader = unit.getClassLoader();
      if (classLoader instanceof InterceptionClassLoader)
      {
         InterceptionClassLoader icl = (InterceptionClassLoader)classLoader;
         classLoader = icl.getDelegate();
      }

      try
      {
         try
         {
            // Remove the classloader
            getSystem().unregisterClassLoader(classLoader);
         }
         finally
         {
            // Try to tidy up empty domains
            String domainName = module.getDeterminedDomainName();
            if (ClassLoaderSystem.DEFAULT_DOMAIN_NAME.equals(domainName) == false)
            {
               ClassLoaderDomain domain = getSystem().getDomain(domainName);
               if (domain.hasClassLoaders() == false)
                  getSystem().unregisterDomain(domain);
            }
         }
      }
      finally
      {
         cleanup(unit, module);
         module.reset();
      }
   }
}