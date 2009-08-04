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

import org.jboss.deployers.vfs.spi.structure.VFSDeploymentContext;
import org.jboss.test.deployers.vfs.structure.AbstractStructureTest;

/**
 * Test sub dir handling.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public abstract class SubDirectoryStructureTest extends AbstractStructureTest
{
   protected SubDirectoryStructureTest(String name)
   {
      super(name);
   }

   protected abstract boolean shouldFlattenContext();

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
      assertChildContexts(context, shouldFlattenContext(), "test-in-lib.sar", "test-in-lib.sar/lib/test.jar");
   }

   public void testEarSarWithNestedLib() throws Throwable
   {
      VFSDeploymentContext context = assertDeploy("/structure/dir", "nested.ear");
      assertChildContexts(context, shouldFlattenContext(), "test-in-lib-nested.sar", "test-in-lib-nested.sar/lib/nested/test.jar");
   }
}