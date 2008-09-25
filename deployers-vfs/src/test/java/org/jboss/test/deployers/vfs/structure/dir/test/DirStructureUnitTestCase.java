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
import org.jboss.deployers.vfs.spi.client.VFSDeployment;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentContext;
import org.jboss.deployers.vfs.plugins.structure.jar.JARStructure;
import org.jboss.deployers.vfs.plugins.structure.dir.DirectoryStructure;
import org.jboss.test.deployers.vfs.structure.AbstractStructureTest;
import org.jboss.test.deployers.vfs.structure.ear.support.MockEarStructureDeployer;

/**
 * Legacy directory structure tests.
 * See JBAS-5900 for more information.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class DirStructureUnitTestCase extends AbstractStructureTest
{
   public DirStructureUnitTestCase(String name)
   {
      super(name);
   }

   public static Test suite()
   {
      return suite(DirStructureUnitTestCase.class);
   }

   @SuppressWarnings("deprecation")
   protected VFSDeploymentContext determineStructure(VFSDeployment deployment) throws Exception
   {
      return determineStructureWithStructureDeployers(deployment, new MockEarStructureDeployer(), new JARStructure(), new DirectoryStructure());
   }

   public void testSarWithLib() throws Throwable
   {
      VFSDeploymentContext context = assertDeploy("/structure/dir", "test-in-lib.sar");
      assertChildContexts(context, "lib/test.jar");
   }

   public void testSarWithNestedLib() throws Throwable
   {
      VFSDeploymentContext context = assertDeploy("/structure/dir", "test-in-lib-nested.sar");
      assertChildContexts(context, "lib/nested/test.jar");
   }

   public void testEarSarWithLib() throws Throwable
   {
      VFSDeploymentContext context = assertDeploy("/structure/dir", "simple.ear");
      assertChildContexts(context, "test-in-lib.sar", "test-in-lib.sar/lib/test.jar");
   }

   public void testEarSarWithNestedLib() throws Throwable
   {
      VFSDeploymentContext context = assertDeploy("/structure/dir", "nested.ear");
      assertChildContexts(context, "test-in-lib-nested.sar", "test-in-lib-nested.sar/lib/nested/test.jar");
   }
}
