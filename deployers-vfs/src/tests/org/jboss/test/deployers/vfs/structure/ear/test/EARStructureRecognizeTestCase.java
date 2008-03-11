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
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentContext;
import org.jboss.deployers.vfs.spi.structure.StructureDeployer;
import org.jboss.test.deployers.vfs.structure.ear.support.WrapperMockEarStructureDeployer;

/**
 * Mock ear structure deployer tests
 *
 * @author ales.justin@jboss.org
 */
public class EARStructureRecognizeTestCase extends AbstractEARStructureTest
{
   private WrapperMockEarStructureDeployer earStructureDeployer;

   public static Test suite()
   {
      return new TestSuite(EARStructureRecognizeTestCase.class);
   }

   public EARStructureRecognizeTestCase(String name)
   {
      super(name);
   }

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      earStructureDeployer = new WrapperMockEarStructureDeployer();
   }

   protected void tearDown() throws Exception
   {
      earStructureDeployer = null;
      super.tearDown();
   }

   protected StructureDeployer createEarStructureDeployer()
   {
      return earStructureDeployer;
   }

   protected void reset()
   {
      if (earStructureDeployer != null)
         earStructureDeployer.reset();
   }

   public void testNotAnEAR() throws Throwable
   {
      try
      {
         VFSDeploymentContext context = assertDeployNoChildren("/structure/ear", "notanear");
         assertNoMetaDataFile(context, "META-INF");
         assertClassPath(context, "");
         assertTrue(earStructureDeployer.isTouched());
         assertFalse(earStructureDeployer.isValid());
      }
      finally
      {
         reset();
      }
   }
}
