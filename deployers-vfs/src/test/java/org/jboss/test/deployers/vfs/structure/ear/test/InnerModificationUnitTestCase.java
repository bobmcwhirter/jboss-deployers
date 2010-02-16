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
package org.jboss.test.deployers.vfs.structure.ear.test;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.jboss.deployers.vfs.plugins.structure.file.FileStructure;
import org.jboss.deployers.vfs.plugins.structure.jar.JARStructure;
import org.jboss.deployers.vfs.spi.client.VFSDeployment;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentContext;
import org.jboss.test.deployers.vfs.structure.AbstractStructureTest;
import org.jboss.test.deployers.vfs.structure.support.WarUnpackStructure;

/**
 * Inner unpack, with structure deployer
 * setting up the modification info.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class InnerModificationUnitTestCase extends AbstractStructureTest
{
   public static Test suite()
   {
      return new TestSuite(InnerModificationUnitTestCase.class);
   }

   public InnerModificationUnitTestCase(String name)
   {
      super(name);
   }

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      enableTrace("org.jboss.deployers");
   }

   protected VFSDeploymentContext determineStructure(VFSDeployment deployment) throws Exception
   {
      // TODO - check serialization of modifed roots
      return determineStructureWithStructureDeployers(deployment, false, new JARStructure(), new WarUnpackStructure(), new FileStructure());
   }

   public void testExplicitInnerUnpack() throws Throwable
   {
      VFSDeploymentContext root = assertDeploy("/structure/explicit", "explicit.ear");
      assertChildContexts(root, "inner.war");
      assertTrue(root.getRoot().getChild("inner.war").isDirectory());
   }
}
