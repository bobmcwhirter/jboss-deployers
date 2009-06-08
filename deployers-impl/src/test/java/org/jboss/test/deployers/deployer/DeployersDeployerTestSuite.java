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
package org.jboss.test.deployers.deployer;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.jboss.test.deployers.deployer.test.ComponentUnitTestCase;
import org.jboss.test.deployers.deployer.test.DeployerClassLoaderUnitTestCase;
import org.jboss.test.deployers.deployer.test.DeployerContextClassLoaderUnitTestCase;
import org.jboss.test.deployers.deployer.test.DeployerFlowUnitTestCase;
import org.jboss.test.deployers.deployer.test.DeployerOrderingUnitTestCase;
import org.jboss.test.deployers.deployer.test.DeployerProtocolUnitTestCase;
import org.jboss.test.deployers.deployer.test.DeployerRequiredStageUnitTestCase;
import org.jboss.test.deployers.deployer.test.DeployerWidthFirstUnitTestCase;
import org.jboss.test.deployers.deployer.test.DeployersImplUnitTestCase;
import org.jboss.test.deployers.deployer.test.HeuristicAllOrNothingUnitTestCase;
import org.jboss.test.deployers.deployer.test.HeuristicRussionDollUnitTestCase;
import org.jboss.test.deployers.deployer.test.MultipleComponentTypeUnitTestCase;

/**
 * Deployers Deployer Test Suite.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 37459 $
 */
public class DeployersDeployerTestSuite extends TestSuite
{
   public static void main(String[] args)
   {
      TestRunner.run(suite());
   }

   public static Test suite()
   {
      TestSuite suite = new TestSuite("Deployers Deployer Tests");

      suite.addTest(DeployerProtocolUnitTestCase.suite());
      suite.addTest(DeployerOrderingUnitTestCase.suite());
      suite.addTest(DeployerFlowUnitTestCase.suite());
      suite.addTest(DeployerWidthFirstUnitTestCase.suite());
      suite.addTest(DeployerClassLoaderUnitTestCase.suite());
      suite.addTest(DeployersImplUnitTestCase.suite());
      suite.addTest(ComponentUnitTestCase.suite());
      suite.addTest(MultipleComponentTypeUnitTestCase.suite());
      suite.addTest(HeuristicAllOrNothingUnitTestCase.suite());
      suite.addTest(HeuristicRussionDollUnitTestCase.suite());
      suite.addTest(DeployerContextClassLoaderUnitTestCase.suite());
      suite.addTest(DeployerRequiredStageUnitTestCase.suite());

      return suite;
   }
}
