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
package org.jboss.test.deployers.main.test;

import junit.framework.Test;
import org.jboss.deployers.client.spi.DeployerClient;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.test.deployers.AbstractDeployerTest;
import org.jboss.test.deployers.main.support.MarkerDeployer;

/**
 * Test dynamic adding / removing of deployer.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class DynamicDeployerUsageTestCase extends AbstractDeployerTest
{
   public DynamicDeployerUsageTestCase(String name)
   {
      super(name);
   }

   public static Test suite()
   {
      return suite(DynamicDeployerUsageTestCase.class);
   }

   public void testAddRemove() throws Exception
   {
      // make sure md1 is before md2
      MarkerDeployer md1 = new MarkerDeployer();
      md1.addOutput(MarkerDeployer.class);
      MarkerDeployer md2 = new MarkerDeployer();
      md2.addInput(MarkerDeployer.class);

      DeployerClient main = createMainDeployer(md1, md2);
      Deployment deployment = createSimpleDeployment("test");
      main.deploy(deployment);
      removeDeployer(main, md2);
      main.undeploy(deployment);
      assertNull(md2.unit);
   }
}
