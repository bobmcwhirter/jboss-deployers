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
package org.jboss.test.deployers.vfs.deployer.nonmetadata.test;

import junit.framework.Test;
import org.jboss.dependency.plugins.AbstractController;
import org.jboss.deployers.plugins.deployers.DeployersImpl;
import org.jboss.deployers.plugins.main.MainDeployerImpl;
import org.jboss.deployers.spi.deployer.helpers.DefaultManagedObjectCreator;
import org.jboss.deployers.vfs.plugins.structure.file.FileStructure;
import org.jboss.deployers.vfs.plugins.structure.jar.JARStructure;
import org.jboss.deployers.vfs.spi.client.VFSDeployment;
import org.jboss.test.deployers.BaseDeployersVFSTest;
import org.jboss.test.deployers.vfs.deployer.nonmetadata.support.MockBshDeployer;
import org.jboss.test.deployers.vfs.deployer.nonmetadata.support.BshScript;

/**
 * Mock .bsh deploy.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class MockBshDeployerTestCase extends BaseDeployersVFSTest
{
   public MockBshDeployerTestCase(String name)
   {
      super(name);
   }

   public static Test suite()
   {
      return suite(MockBshDeployerTestCase.class);
   }

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      enableTrace("org.jboss.deployers");
   }

   public void testBshNonMetadataDeploy() throws Throwable
   {
      MainDeployerImpl main = new MainDeployerImpl();
      main.setStructuralDeployers(createStructuralDeployers());
      addStructureDeployer(main, new JARStructure());
      MockBshDeployer bshDeployer = new MockBshDeployer();
      FileStructure fileStructure = new FileStructure();
      fileStructure.addFileMatcher(bshDeployer);
      addStructureDeployer(main, fileStructure);
      DeployersImpl deployers = new DeployersImpl(new AbstractController());
      deployers.setMgtObjectCreator(new DefaultManagedObjectCreator());
      deployers.addDeployer(bshDeployer);
      main.setDeployers(deployers);
      VFSDeployment deployment = createDeployment("/nonmetadata", "nmd.jar");
      main.deploy(deployment);
      assertNotNull(bshDeployer.getScipts());
      assertEquals(1, bshDeployer.getScipts().size());
      BshScript script = bshDeployer.getScipts().iterator().next();
      assertNotNull(script);
      assertNotNull(script.getProperties());
      assertEquals("root", script.getName());
   }
}

