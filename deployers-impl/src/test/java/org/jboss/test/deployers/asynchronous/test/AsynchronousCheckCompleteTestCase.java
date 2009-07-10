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
package org.jboss.test.deployers.asynchronous.test;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;

import junit.framework.Test;

import org.jboss.dependency.plugins.AbstractController;
import org.jboss.dependency.plugins.AbstractDependencyItem;
import org.jboss.dependency.spi.ControllerMode;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.deployers.client.spi.DeployerClient;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.client.spi.IncompleteDeploymentException;
import org.jboss.deployers.client.spi.IncompleteDeployments;
import org.jboss.deployers.client.spi.MissingAsynchronousDependency;
import org.jboss.deployers.client.spi.MissingDependency;
import org.jboss.deployers.plugins.deployers.DeployersImpl;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.Deployers;
import org.jboss.deployers.spi.deployer.managed.ManagedObjectCreator;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.structure.spi.main.MainDeployerStructure;
import org.jboss.test.deployers.asynchronous.support.TestControllerContext;
import org.jboss.test.deployers.main.test.AbstractMainDeployerTest;

/**
 * Check complete deployment test case.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class AsynchronousCheckCompleteTestCase extends AbstractMainDeployerTest
{
   AbstractController controller;
   
   public AsynchronousCheckCompleteTestCase(String name)
   {
      super(name);
   }

   public static Test suite()
   {
      return suite(AsynchronousCheckCompleteTestCase.class);
   }

   public void testAsynchronousContextInProgressNotReported() throws Throwable
   {
      controller = (AbstractController)getController();
      controller.setExecutor(Executors.newFixedThreadPool(2));
      
      DeployerClient main = getMainDeployer();

      TestControllerContext bean = new TestControllerContext("Bean");
      bean.getDependencyInfo().addIDependOn(new AbstractDependencyItem("Bean", "Dependency", ControllerState.CONFIGURED, ControllerState.INSTALLED));
      controller.install(bean);
      
      TestControllerContext dependency = new TestControllerContext("Dependency", ControllerMode.ASYNCHRONOUS);
      controller.install(dependency);

      Deployment dA = createSimpleDeployment("A");
      main.addDeployment(dA);
      
      DeploymentUnit depUnit = ((MainDeployerStructure)main).getDeploymentUnit("A");
      assertNotNull(depUnit);
      depUnit.addControllerContextName("Bean");
      depUnit.addControllerContextName("Dependency");
      
      main.process();
      try
      {
         main.checkComplete(dA);
         fail("Should not be complete");
      }
      catch (DeploymentException e)
      {
         assertInstanceOf(e, IncompleteDeploymentException.class);
         IncompleteDeployments id = ((IncompleteDeploymentException)e).getIncompleteDeployments();
         
         assertTrue(id.getContextsInError() == null || id.getContextsInError().size() == 0);
         assertTrue(id.getDeploymentsInError() == null || id.getDeploymentsInError().size() == 0);
         assertTrue(id.getDeploymentsMissingDeployer() == null || id.getDeploymentsMissingDeployer().size() == 0);
         
         Map<String, Set<MissingDependency>> missingDeps = id.getContextsMissingDependencies();
         assertEquals(1, missingDeps.size());
         Set<MissingDependency> missing = missingDeps.get("Bean");
         assertNotNull(missing);
         assertEquals(1, missing.size());
         MissingDependency dep = missing.toArray(new MissingDependency[1])[0];
         assertNotNull(dep);
         assertInstanceOf(dep, MissingAsynchronousDependency.class);
         
         String msg = e.getMessage();
         assertTrue(0 > msg.indexOf("DEPLOYMENTS IN ERROR"));
         e.printStackTrace();
      }
   }

   protected Deployers createDeployers()
   {
      log.debug("createDeployers");
      if (controller == null)
      {
         throw new IllegalStateException("Controller not initialised");
      }
      ManagedObjectCreator moc = createManagedObjectCreator();
      System.out.println("createDeployers, moc: "+moc);
      DeployersImpl di = new DeployersImpl(controller);
      di.setMgtObjectCreator(moc);
      return di;
   }


}
