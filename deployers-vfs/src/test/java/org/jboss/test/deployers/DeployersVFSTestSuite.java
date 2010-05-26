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

import org.jboss.test.deployers.vfs.classloader.ClassLoaderTestSuite;
import org.jboss.test.deployers.vfs.classloading.ClassLoadingTestSuite;
import org.jboss.test.deployers.vfs.dependency.DependencyTestSuite;
import org.jboss.test.deployers.vfs.deployer.bean.BeanDeployerTestSuite;
import org.jboss.test.deployers.vfs.deployer.facelets.FaceletsTestSuite;
import org.jboss.test.deployers.vfs.deployer.jaxp.JAXPDeployerTestSuite;
import org.jboss.test.deployers.vfs.deployer.merge.MergeDeployerTestSuite;
import org.jboss.test.deployers.vfs.deployer.nonmetadata.NonMetadataDeployersTestSuite;
import org.jboss.test.deployers.vfs.deployer.validate.ValidateDeployerTestSuite;
import org.jboss.test.deployers.vfs.deploymentfactory.VFSDeploymentFactoryTestSuite;
import org.jboss.test.deployers.vfs.jmx.JMXTestSuite;
import org.jboss.test.deployers.vfs.managed.VFSManagedTestSuite;
import org.jboss.test.deployers.vfs.matchers.VFSMatchersTestSuite;
import org.jboss.test.deployers.vfs.metadata.VFSMetaDataTestSuite;
import org.jboss.test.deployers.vfs.parsing.test.ParsingTestSuite;
import org.jboss.test.deployers.vfs.reflect.ReflectTestSuite;
import org.jboss.test.deployers.vfs.structure.VFSStructureTestSuite;
import org.jboss.test.deployers.vfs.structurebuilder.VFSStructureBuilderTestSuite;
import org.jboss.test.deployers.vfs.structureprocessor.VFSStructureProcessorTestSuite;
import org.jboss.test.deployers.vfs.webbeans.WebBeansTestSuite;
import org.jboss.test.deployers.vfs.xb.JBossXBDeployersTestSuite;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Deployers VFS Test Suite.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
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
      suite.addTest(VFSStructureProcessorTestSuite.suite());
      suite.addTest(VFSStructureTestSuite.suite());
      suite.addTest(VFSMetaDataTestSuite.suite());
      suite.addTest(ParsingTestSuite.suite());
      suite.addTest(JAXPDeployerTestSuite.suite());
      suite.addTest(BeanDeployerTestSuite.suite());
      suite.addTest(VFSManagedTestSuite.suite());
      suite.addTest(NonMetadataDeployersTestSuite.suite());
      suite.addTest(ClassLoaderTestSuite.suite());
      suite.addTest(ClassLoadingTestSuite.suite());
      suite.addTest(VFSMatchersTestSuite.suite());
      suite.addTest(JBossXBDeployersTestSuite.suite());
      suite.addTest(MergeDeployerTestSuite.suite());
      suite.addTest(FaceletsTestSuite.suite());
      suite.addTest(ValidateDeployerTestSuite.suite());
      suite.addTest(DependencyTestSuite.suite());
      suite.addTest(JMXTestSuite.suite());
      suite.addTest(WebBeansTestSuite.suite()); // now Weld
      suite.addTest(ReflectTestSuite.suite());

      return suite;
   }
}
