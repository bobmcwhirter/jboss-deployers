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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.deployers.vfs.plugins.structure.explicit.DeclaredStructure;
import org.jboss.deployers.vfs.spi.client.VFSDeployment;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentContext;
import org.jboss.test.deployers.vfs.structure.AbstractStructureTest;

/**
 * DeclaredStructure deployer unit tests.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 1.1 $
 */
public class DeclaredStructureUnitTestCase extends AbstractStructureTest
{
   public static Test suite()
   {
      return new TestSuite(DeclaredStructureUnitTestCase.class);
   }
   
   public DeclaredStructureUnitTestCase(String name)
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
      return determineStructureWithStructureDeployer(deployment, new DeclaredStructure());
   }

   public void testComplex() throws Throwable
   {
      VFSDeploymentContext root = assertDeploy("/structure/explicit", "complex.deployer");
      assertChildContexts(root, "sub.jar", "x.war");

      // Validate the root context info
      assertMetaData(root, "META-INF");
      assertClassPath(root, "cp-mf.jar", "jar1.jar", "lib-dir/jar0.jar", "lib-dir/jar2.jar");

      // Validate the sub.jar
      VFSDeploymentContext subJar = assertChildContext(root, "sub.jar");
      assertMetaData(subJar, "META-INF");
      assertClassPath(subJar, root, "cp-mf.jar");

      // Validate the x.war context info
      VFSDeploymentContext xwar = assertChildContext(root, "x.war");
      assertMetaData(xwar, "WEB-INF");
      assertClassPath(xwar, "WEB-INF/classes", "WEB-INF/lib/w0.jar");
   }
}
