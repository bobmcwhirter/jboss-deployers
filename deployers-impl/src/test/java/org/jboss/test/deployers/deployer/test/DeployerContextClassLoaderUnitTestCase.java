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
package org.jboss.test.deployers.deployer.test;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.deployers.client.spi.DeployerClient;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.test.deployers.AbstractDeployerTest;
import org.jboss.test.deployers.deployer.support.TestDummyClassLoader;
import org.jboss.test.deployers.deployer.support.TestDummyClassLoaderDeployer;

/**
 * DeployerClassLoaderUnitTestCase.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class DeployerContextClassLoaderUnitTestCase extends AbstractDeployerTest
{
   public static Test suite()
   {
      return new TestSuite(DeployerContextClassLoaderUnitTestCase.class);
   }
   
   public DeployerContextClassLoaderUnitTestCase(String name)
   {
      super(name);
   }

   public void testContextClassLoader() throws Exception
   {
      TestDummyClassLoader dummy = new TestDummyClassLoader();
      ClassLoader previous = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(dummy);
      DeployerClient main = null;
      try
      {
         TestDummyClassLoaderDeployer deployer = new TestDummyClassLoaderDeployer();
         main = createMainDeployer(deployer);
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(previous);
      }
      
      Deployment deployment = createSimpleDeployment("x");
      main.addDeployment(deployment);
      main.process();
      assertEquals(dummy, TestDummyClassLoaderDeployer.getAndResetClassLoader());
      
      main.removeDeployment(deployment);
      main.process();
      assertEquals(dummy, TestDummyClassLoaderDeployer.getAndResetClassLoader());
   }
}
