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
package org.jboss.test.deployers.vfs.structure.jar.test;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.deployers.vfs.spi.client.VFSDeployment;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentContext;

/**
 * CombinedJARStructureUnitTestCase.
 * 
 * @author <a href="adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class CombinedJARStructureUnitTestCase extends AbstractJARStructureTest
{
   public static Test suite()
   {
      return new TestSuite(CombinedJARStructureUnitTestCase.class);
   }
   
   public CombinedJARStructureUnitTestCase(String name)
   {
      super(name);
   }

   public void testRootNotAnArchive() throws Throwable
   {
      // should not be valid anymore!
      assertNotValid("/structure/jar/notanarchive", "NotAnArchive.jar");
      assertNotValid("/structure/jar/notanarchive", "NotAnArchive.zip");
   }

   public void testSubdeploymentIsKnownFile() throws Throwable
   {
      // The file structure recognises the subdeployer for the combined test
      VFSDeploymentContext context = assertDeploy("/structure/file", "simple");
      assertChildContexts(context, "simple-service.xml");
   }

   protected VFSDeploymentContext determineStructure(VFSDeployment deployment) throws Exception
   {
      return determineStructureWithAllStructureDeployers(deployment);
   }
}
