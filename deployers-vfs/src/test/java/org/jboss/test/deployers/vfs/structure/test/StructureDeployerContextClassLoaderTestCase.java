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
package org.jboss.test.deployers.vfs.structure.test;

import junit.framework.Test;

import org.jboss.deployers.vfs.plugins.structure.VFSStructuralDeployersImpl;
import org.jboss.deployers.vfs.plugins.structure.VFSStructureBuilder;
import org.jboss.deployers.vfs.plugins.structure.file.FileStructure;
import org.jboss.deployers.vfs.plugins.structure.jar.JARStructure;
import org.jboss.deployers.vfs.spi.client.VFSDeployment;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentContext;
import org.jboss.test.deployers.vfs.structure.AbstractStructureTest;
import org.jboss.test.deployers.vfs.structure.support.TestDummyClassLoader;
import org.jboss.test.deployers.vfs.structure.support.TestDummyClassLoaderStructureDeployer;

/**
 * StructureDeployerContextClassLoaderTestCase.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class StructureDeployerContextClassLoaderTestCase extends AbstractStructureTest
{
   public StructureDeployerContextClassLoaderTestCase(String name)
   {
      super(name);
   }

   public static Test suite()
   {
      return suite(StructureDeployerContextClassLoaderTestCase.class);
   }

   public void testContextClassLoader() throws Exception
   {
      TestDummyClassLoader dummy = new TestDummyClassLoader();
      VFSStructuralDeployersImpl structuralDeployers = new VFSStructuralDeployersImpl();
      VFSStructureBuilder builder = new VFSStructureBuilder();
      structuralDeployers.setStructureBuilder(builder);
      structuralDeployers.addDeployer(new JARStructure());
      structuralDeployers.addDeployer(new FileStructure());

      ClassLoader previous = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(dummy);
      try
      {
         TestDummyClassLoaderStructureDeployer deployer = new TestDummyClassLoaderStructureDeployer();
         structuralDeployers.addDeployer(deployer);
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(previous);
      }

      VFSDeployment deployment = createDeployment("/structure/file", "simple");
      structuralDeployers.determineStructure(deployment);
      assertEquals(dummy, TestDummyClassLoaderStructureDeployer.getAndResetClassLoader());
   }

   protected VFSDeploymentContext determineStructure(VFSDeployment deployment) throws Exception
   {
      throw new UnsupportedOperationException("not used");
   }
}
