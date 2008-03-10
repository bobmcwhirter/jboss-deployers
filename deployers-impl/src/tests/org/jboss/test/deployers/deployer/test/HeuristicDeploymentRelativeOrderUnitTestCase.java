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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.deployers.client.plugins.deployment.AbstractDeployment;
import org.jboss.deployers.client.spi.DeployerClient;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.client.spi.DeploymentFactory;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.structure.ContextInfo;
import org.jboss.test.deployers.AbstractDeployerTest;
import org.jboss.test.deployers.deployer.support.TestDeploymentContextComparator;
import org.jboss.test.deployers.deployer.support.TestSimpleDeployer3;

/**
 * DeployerDependencyUnitTestCase.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class HeuristicDeploymentRelativeOrderUnitTestCase extends AbstractDeployerTest
{
   private static final DeploymentFactory factory = new DeploymentFactory();
   
   private TestSimpleDeployer3 deployer1 = new TestSimpleDeployer3(DeploymentStages.REAL);
   
   private static String P = "Parent";
   private static String C1 = P + "/" + "C1";
   private static String C2 = P + "/" + "C2";
   private static List<String> NONE = Collections.emptyList(); 
   private static List<String> PC1C2 = makeList(P, C1, C2); 
   private static List<String> PC2C1 = makeList(P, C2, C1); 
   private static List<String> C2C1P = makeList(C2, C1, P); 
   private static List<String> C1C2P = makeList(C1, C2, P); 
   
   @SuppressWarnings("unchecked")
   private static <T> List<T> makeList(T... objects)
   {
      List<T> result = new ArrayList<T>();
      for (T object : objects)
         result.add(object);
      return result;
   }
   
   public static Test suite()
   {
      return new TestSuite(HeuristicDeploymentRelativeOrderUnitTestCase.class);
   }
   
   public HeuristicDeploymentRelativeOrderUnitTestCase(String name)
   {
      super(name);
   }

   public void testNoRelativeOrderUsesSimpleName() throws Exception
   {
      DeployerClient main = getMainDeployer();
      
      Deployment parent = createSimpleDeployment("Parent");
      factory.addContext(parent, "C1");
      factory.addContext(parent, "C2");
      main.addDeployment(parent);

      main.process();

      assertEquals(PC1C2, deployer1.deployed);
      assertEquals(NONE, deployer1.undeployed);
      
      main.removeDeployment(parent);
      main.process();

      assertEquals(PC1C2, deployer1.deployed);
      assertEquals(C2C1P, deployer1.undeployed);
   }

   public void testRelativeOrder() throws Exception
   {
      DeployerClient main = getMainDeployer();
      
      Deployment parent = createSimpleDeployment("Parent");
      ContextInfo c1 = factory.addContext(parent, "C1");
      c1.setRelativeOrder(2);
      ContextInfo c2 = factory.addContext(parent, "C2");
      c2.setRelativeOrder(1);
      main.addDeployment(parent);

      main.process();

      assertEquals(PC2C1, deployer1.deployed);
      assertEquals(NONE, deployer1.undeployed);
      
      main.removeDeployment(parent);
      main.process();

      assertEquals(PC2C1, deployer1.deployed);
      assertEquals(C1C2P, deployer1.undeployed);
   }

   public void testComparator() throws Exception
   {
      DeployerClient main = getMainDeployer();
      
      AbstractDeployment parent = new AbstractDeployment("Parent");
      ContextInfo p = factory.addContext(parent, "");
      // Reverse the default comparison
      p.setComparatorClassName(TestDeploymentContextComparator.class.getName());
      factory.addContext(parent, "C1");
      factory.addContext(parent, "C2");
      main.addDeployment(parent);

      main.process();

      assertEquals(PC2C1, deployer1.deployed);
      assertEquals(NONE, deployer1.undeployed);
      
      main.removeDeployment(parent);
      main.process();

      assertEquals(PC2C1, deployer1.deployed);
      assertEquals(C1C2P, deployer1.undeployed);
   }
   
   protected DeployerClient getMainDeployer()
   {
      return createMainDeployer(deployer1);
   }
}
