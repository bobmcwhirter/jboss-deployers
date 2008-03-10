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

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Collections;

import org.jboss.test.deployers.AbstractDeployerTest;
import org.jboss.deployers.client.spi.IncompleteDeployments;
import org.jboss.deployers.client.spi.MissingDependency;
import junit.framework.Test;

/**
 * Incomplete deployments API test case.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class DeployerIncompleteDeploymentsTestCase extends AbstractDeployerTest
{
   public DeployerIncompleteDeploymentsTestCase(String name)
   {
      super(name);
   }

   public static Test suite()
   {
      return suite(DeployerIncompleteDeploymentsTestCase.class);
   }

   public void testMessage() throws Exception
   {
      Map<String, Throwable> deploymentsInError = new HashMap<String, Throwable>();
      Collection<String> deploymentsMissingDeployer = new HashSet<String>();
      Map<String, Throwable> contextsInError = new HashMap<String, Throwable>();
      Map<String, Set<MissingDependency>> contextsMissingDependencies = new HashMap<String, Set<MissingDependency>>();

      deploymentsInError.put("deployment1", new Throwable("sd1"));
      deploymentsMissingDeployer.add("deployment2");
      contextsInError.put("context1", new Throwable("sc1"));
      contextsMissingDependencies.put("context2", Collections.singleton(new MissingDependency()));

      IncompleteDeployments deployments = new IncompleteDeployments(
            deploymentsInError,
            deploymentsMissingDeployer,
            contextsInError,
            contextsMissingDependencies
      );

      assertTrue(deployments.isIncomplete());
      assertTrue(deployments.isInvalidDeployment("deployment1"));
      assertTrue(deployments.isInvalidDeployment("deployment2"));
      assertTrue(deployments.isInvalidContext("context1"));
      assertTrue(deployments.isInvalidContext("context2"));

      String deInfo = deployments.getDeploymentsInErrorInfo();
      assertNotNull(deInfo);
      assertSame(deInfo, deployments.getDeploymentsInErrorInfo());

      String dmInfo = deployments.getDeploymentsMissingDeployerInfo();
      assertNotNull(dmInfo);
      assertSame(dmInfo, deployments.getDeploymentsMissingDeployerInfo());

      String ceInfo = deployments.getContextsInErrorInfo();
      assertNotNull(ceInfo);
      assertSame(ceInfo, deployments.getContextsInErrorInfo());

      String cmInfo = deployments.getContextsMissingDependenciesInfo();
      assertNotNull(cmInfo);
      assertSame(cmInfo, deployments.getContextsMissingDependenciesInfo());
   }
}
