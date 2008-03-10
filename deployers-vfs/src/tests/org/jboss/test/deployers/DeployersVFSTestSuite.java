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
package org.jboss.test.deployers;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.jboss.test.deployers.vfs.classloader.ClassLoaderTestSuite;
import org.jboss.test.deployers.vfs.deployer.bean.BeanDeployerTestSuite;
import org.jboss.test.deployers.vfs.deployer.jaxp.VFSDeployerTestSuite;
import org.jboss.test.deployers.vfs.deployer.nonmetadata.NonMetadataDeployersTestSuite;
import org.jboss.test.deployers.vfs.deploymentfactory.VFSDeploymentFactoryTestSuite;
import org.jboss.test.deployers.vfs.metadata.VFSMetaDataTestSuite;
import org.jboss.test.deployers.vfs.structure.VFSStructureTestSuite;
import org.jboss.test.deployers.vfs.structurebuilder.VFSStructureBuilderTestSuite;
import org.jboss.test.deployers.vfs.managed.VFSManagedTestSuite;
import org.jboss.test.deployers.vfs.matchers.VFSMatchersTestSuite;

/**
 * Deployers VFS Test Suite.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 37459 $
 */
public class DeployersVFSTestSuite extends TestSuite
{
   public static void main(String[] args)
   {
      TestRunner.run(suite());
   }

   public static Test suite()
   {
      TestSuite suite = new TestSuite("Deployers VFS Tests");

      suite.addTest(VFSDeploymentFactoryTestSuite.suite());
      suite.addTest(VFSStructureBuilderTestSuite.suite());
      suite.addTest(VFSStructureTestSuite.suite());
      suite.addTest(VFSMetaDataTestSuite.suite());
      suite.addTest(VFSDeployerTestSuite.suite());
      suite.addTest(BeanDeployerTestSuite.suite());
      suite.addTest(VFSManagedTestSuite.suite());
      suite.addTest(NonMetadataDeployersTestSuite.suite());
      suite.addTest(ClassLoaderTestSuite.suite());
      suite.addTest(VFSMatchersTestSuite.suite());

      return suite;
   }
}
