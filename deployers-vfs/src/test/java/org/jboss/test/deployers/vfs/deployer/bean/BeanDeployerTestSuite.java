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
package org.jboss.test.deployers.vfs.deployer.bean;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.jboss.test.deployers.vfs.deployer.bean.test.AliasDeployerUnitTestCase;
import org.jboss.test.deployers.vfs.deployer.bean.test.AnnotatedBeansUnitTestCase;
import org.jboss.test.deployers.vfs.deployer.bean.test.BeanDeployerClassLoaderUnitTestCase;
import org.jboss.test.deployers.vfs.deployer.bean.test.BeanDeployerUnitTestCase;
import org.jboss.test.deployers.vfs.deployer.bean.test.BeanManagedDeploymentUnitTestCase;
import org.jboss.test.deployers.vfs.deployer.bean.test.BeanMetaDataFactoryDeployerUnitTestCase;
import org.jboss.test.deployers.vfs.deployer.bean.test.BeanScanningUnitTestCase;
import org.jboss.test.deployers.vfs.deployer.bean.test.BuilderBeansUnitTestCase;
import org.jboss.test.deployers.vfs.deployer.bean.test.KernelDeployerUnitTestCase;
import org.jboss.test.deployers.vfs.deployer.bean.test.KernelScopeUnitTestCase;
import org.jboss.test.deployers.vfs.deployer.bean.test.AutowireAnnotationBeansTestCase;

/**
 * BeanDeployerTestSuite.
 * 
 * @author <a href="adrian@jboss.org">Adrian Brock</a>
 * @author <a href="ales.justin@jboss.org">Ales Justin</a>
 * @version $Revision: 1.1 $
 */
public class BeanDeployerTestSuite extends TestSuite
{
   public static void main(String[] args)
   {
      TestRunner.run(suite());
   }

   public static Test suite()
   {
      TestSuite suite = new TestSuite("VFS Bean Deployer Tests");

      suite.addTest(BeanDeployerUnitTestCase.suite());
      suite.addTest(KernelDeployerUnitTestCase.suite());
      suite.addTest(AliasDeployerUnitTestCase.suite());
      suite.addTest(BeanDeployerClassLoaderUnitTestCase.suite());
      suite.addTest(BeanManagedDeploymentUnitTestCase.suite());
      suite.addTest(BeanScanningUnitTestCase.suite());
      suite.addTest(BeanMetaDataFactoryDeployerUnitTestCase.suite());
      suite.addTest(KernelScopeUnitTestCase.suite());
      suite.addTest(AnnotatedBeansUnitTestCase.suite());
      suite.addTest(BuilderBeansUnitTestCase.suite());
      suite.addTest(AutowireAnnotationBeansTestCase.suite());

      return suite;
   }
}
