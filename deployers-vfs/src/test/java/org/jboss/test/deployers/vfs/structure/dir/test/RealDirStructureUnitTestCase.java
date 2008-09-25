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
package org.jboss.test.deployers.vfs.structure.dir.test;

import junit.framework.Test;
import org.jboss.beans.metadata.plugins.AbstractBeanMetaData;
import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.deployers.vfs.plugins.structure.dir.DirectoryStructure;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.kernel.spi.dependency.KernelControllerContext;
import org.jboss.test.deployers.BootstrapDeployersTest;
import org.jboss.test.deployers.vfs.structure.ear.support.MockEarStructureDeployer;

/**
 * Legacy directory structure tests.
 * See JBAS-5900 for more information.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class RealDirStructureUnitTestCase extends BootstrapDeployersTest
{
   public RealDirStructureUnitTestCase(String name)
   {
      super(name);
   }

   public static Test suite()
   {
      return suite(RealDirStructureUnitTestCase.class);
   }

   @SuppressWarnings("deprecation")
   protected void assertDeploy(String file) throws Throwable
   {
      BeanMetaData earStructureBMD = new AbstractBeanMetaData("EarStructure", MockEarStructureDeployer.class.getName());
      KernelControllerContext earContext = deploy(earStructureBMD);
      try
      {
         BeanMetaData dirStructureBMD = new AbstractBeanMetaData("DirStructure", DirectoryStructure.class.getName());
         KernelControllerContext dirContext = deploy(dirStructureBMD);
         try
         {
            VFSDeploymentUnit unit = assertDeploy("/structure/dir", file);
            try
            {
               assertNotNull(getBean("test:service=TestMessage"));
            }
            finally
            {
               undeploy(unit);
            }
         }
         finally
         {
            undeploy(dirContext);
         }
      }
      finally
      {
         undeploy(earContext);
      }
   }

   public void testSarWithLib() throws Throwable
   {
      assertDeploy("test-in-lib.sar");
   }

   public void testSarWithNestedLib() throws Throwable
   {
      assertDeploy("test-in-lib-nested.sar");
   }

   public void testEarSarWithLib() throws Throwable
   {
      assertDeploy("simple.ear");
   }

   public void testEarSarWithNestedLib() throws Throwable
   {
      assertDeploy("nested.ear");
   }
}