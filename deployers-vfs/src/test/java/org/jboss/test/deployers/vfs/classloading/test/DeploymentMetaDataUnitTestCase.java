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

import java.util.Set;

import org.jboss.deployers.spi.classloading.DeploymentMetaData;
import org.jboss.deployers.spi.classloading.FilterMetaData;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.test.deployers.BootstrapDeployersTest;

import junit.framework.Test;

/**
 * Deployment metadata test case.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class DeploymentMetaDataUnitTestCase extends BootstrapDeployersTest
{
   public DeploymentMetaDataUnitTestCase(String name)
   {
      super(name);
   }

   public static Test suite()
   {
      return suite(DeploymentMetaDataUnitTestCase.class);
   }

   public void testXmlParsing() throws Exception
   {
      DeploymentUnit du = addDeployment("/classloading", "smoke");
      try
      {
         DeploymentMetaData dmd = du.getAttachment(DeploymentMetaData.class);
         assertNotNull(dmd);
         assertEquals(DeploymentStages.PRE_DESCRIBE, dmd.getRequiredStage());
         assertTrue(dmd.isLazyResolve());
         assertTrue(dmd.isLazyStart());
         Set<FilterMetaData> filters = dmd.getFilters();
         assertNotNull(filters);
         assertEquals(2, filters.size());
         for (FilterMetaData fmd : filters)
         {
            if (fmd.isRecurse())
               assertEquals("com.acme.somepackage", fmd.getValue());
            else
               assertEquals("org.foo.bar", fmd.getValue());
         }
      }
      finally
      {
         undeploy(du);
      }
   }
}