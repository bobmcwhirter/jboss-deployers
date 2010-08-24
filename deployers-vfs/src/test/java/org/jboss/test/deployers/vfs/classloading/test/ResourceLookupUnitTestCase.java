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

import java.net.URL;
import java.util.Map;
import java.util.Set;

import org.jboss.classloading.spi.dependency.Module;
import org.jboss.deployers.plugins.classloading.AbstractResourceLookupDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.test.deployers.BootstrapDeployersTest;

import junit.framework.Test;

/**
 * Deployment metadata test case.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class ResourceLookupUnitTestCase extends BootstrapDeployersTest
{
   public ResourceLookupUnitTestCase(String name)
   {
      super(name);
   }

   public static Test suite()
   {
      return suite(ResourceLookupUnitTestCase.class);
   }

   public void testLookup() throws Exception
   {
      DeploymentUnit du = addDeployment("/classloading", "lookup");
      try
      {
         AbstractResourceLookupDeployer deployer = assertBean("RLDeployer", AbstractResourceLookupDeployer.class);
         Map<Module, Set<URL>> map = deployer.getMatchingModules();
         assertNotNull(map);
         assertFalse(map.isEmpty());
      }
      finally
      {
         undeploy(du);
      }
   }
}