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
package org.jboss.test.deployers.vfs.classloading.test;

import java.util.List;

import junit.framework.Test;
import org.jboss.classloader.spi.ClassLoaderDomain;
import org.jboss.classloader.spi.ClassLoaderPolicy;
import org.jboss.classloader.spi.ClassLoaderSystem;
import org.jboss.classloading.spi.dependency.Module;
import org.jboss.classloading.spi.dependency.policy.ClassLoaderPolicyModule;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.test.deployers.BootstrapDeployersTest;
import org.jboss.util.loading.Translator;

/**
 * ClassLoadingTranslators metadata test case.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class ClassLoadingTranslatorsMetaDataUnitTestCase extends BootstrapDeployersTest
{
   public ClassLoadingTranslatorsMetaDataUnitTestCase(String name)
   {
      super(name);
   }

   public static Test suite()
   {
      return suite(ClassLoadingTranslatorsMetaDataUnitTestCase.class);
   }

   public void testDeployer() throws Exception
   {
      DeploymentUnit du = addDeployment("/classloading", "translator");
      try
      {
         ClassLoaderSystem system = (ClassLoaderSystem) getBean("ClassLoaderSystem", ControllerState.INSTALLED);
         assertTranslators(system.getTranslators(), 1);

         ClassLoaderDomain domain = system.getDefaultDomain();
         assertTranslators(domain.getTranslators(), 1);

         ClassLoaderPolicyModule module = du.getAttachment(Module.class.getName(), ClassLoaderPolicyModule.class);
         ClassLoaderPolicy policy = module.getPolicy();
         assertTranslators(policy.getTranslators(), 1);
      }
      finally
      {
         undeploy(du);
      }
   }

   protected void assertTranslators(List<Translator> translators, int size)
   {
      assertNotNull(translators);
      assertEquals(size, translators.size());
   }
}