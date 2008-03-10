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
package org.jboss.test.deployers.vfs.structure.explicit.test;

import java.util.List;

import org.jboss.test.deployers.vfs.structure.AbstractStructureTest;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentContext;
import org.jboss.deployers.vfs.spi.client.VFSDeployment;
import org.jboss.deployers.vfs.plugins.structure.explicit.DeclaredStructure;
import org.jboss.deployers.vfs.plugins.structure.jar.JARStructure;
import org.jboss.deployers.vfs.plugins.structure.file.FileStructure;
import org.jboss.virtual.VirtualFile;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * MultipleMetadataStructure deployer unit tests.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class MultipleMetadataStructureUnitTestCase extends AbstractStructureTest
{
   public static Test suite()
   {
      return new TestSuite(MultipleMetadataStructureUnitTestCase.class);
   }

   public MultipleMetadataStructureUnitTestCase(String name)
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
      return determineStructureWithStructureDeployers(deployment, new DeclaredStructure(), new JARStructure(), new FileStructure());
   }

   public void testMultipleMetadata() throws Throwable
   {
      VFSDeploymentContext root = assertDeploy("/structure/multiple", "metadata.deployer");
      assertChildContexts(root, "jar1.jar");

      // Validate the root context info
      assertMetaDatas(root, "META-INF", "jmx/interceptors", "jmx/interceptors/scripts");
      assertClassPath(root, "jar1.jar");

      VirtualFile bsh = root.getMetaDataFile("myscript.bsh");
      assertNotNull(bsh);
      List<VirtualFile> bshs = root.getMetaDataFiles(null, ".bsh");
      assertNotNull(bshs);
      assertEquals(1, bshs.size());
      VirtualFile security = root.getMetaDataFile("security-config.xml");
      assertNotNull(security);
      List<VirtualFile> cfgs = root.getMetaDataFiles(null, "-config.xml");
      assertNotNull(cfgs);
      assertEquals(1, cfgs.size());
   }
}
