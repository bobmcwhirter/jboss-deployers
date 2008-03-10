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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.deployers.client.spi.DeployerClient;
import org.jboss.deployers.spi.deployer.Deployer;
import org.jboss.test.deployers.AbstractDeployerTest;
import org.jboss.test.deployers.deployer.support.TestSimpleDeployer;

/**
 * DeployersImplUnitTestCase.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class DeployersImplUnitTestCase extends AbstractDeployerTest
{
   public static Test suite()
   {
      return new TestSuite(DeployersImplUnitTestCase.class);
   }
   
   public DeployersImplUnitTestCase(String name)
   {
      super(name);
   }

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
   }
   
   public void testAddNullDeployer() throws Exception
   {
      DeployerClient main = createMainDeployer();
      try
      {
         addDeployer(main, null);
         fail("Should not be here!");
      }
      catch (Throwable t)
      {
         checkThrowable(IllegalArgumentException.class, t);
      }
   }
   
   public void testAddDeployers() throws Exception
   {
      DeployerClient main = createMainDeployer();
      assertEmpty(getDeployers(main));
      
      Deployer deployer = new TestSimpleDeployer();
      HashSet<Deployer> expected = new HashSet<Deployer>();
      expected.add(deployer);

      addDeployer(main, deployer);
      assertEquals(expected, getDeployers(main));
      
      deployer = new TestSimpleDeployer();
      expected.add(deployer);

      addDeployer(main, deployer);
      assertEquals(expected, getDeployers(main));

      // Duplicate
      addDeployer(main, deployer);
      assertEquals(expected, getDeployers(main));
   }
   
   public void testRemoveNullDeployer() throws Exception
   {
      DeployerClient main = createMainDeployer();
      try
      {
         removeDeployer(main, null);
         fail("Should not be here!");
      }
      catch (Throwable t)
      {
         checkThrowable(IllegalArgumentException.class, t);
      }
   }
   
   public void testRemoveDeployers() throws Exception
   {
      DeployerClient main = createMainDeployer();
      assertEmpty(getDeployers(main));
      
      Deployer deployer1 = new TestSimpleDeployer("1");
      Deployer deployer2 = new TestSimpleDeployer("2");
      HashSet<Deployer> expected = new HashSet<Deployer>();
      expected.add(deployer1);
      expected.add(deployer2);
      addDeployer(main, deployer1);
      addDeployer(main, deployer2);
      assertEquals(expected, getDeployers(main));
      
      Deployer notPresent = new TestSimpleDeployer();
      removeDeployer(main, notPresent);
      assertEquals(expected, getDeployers(main));

      removeDeployer(main, deployer1);
      expected.remove(deployer1);
      assertEquals(expected, getDeployers(main));

      removeDeployer(main, deployer2);
      expected.remove(deployer2);
      assertEquals(expected, getDeployers(main));
   }
   
   public void testSetNullDeployers() throws Exception
   {
      DeployerClient main = createMainDeployer();
      try
      {
         setDeployers(main, null);
         fail("Should not be here!");
      }
      catch (Throwable t)
      {
         checkThrowable(IllegalArgumentException.class, t);
      }
   }
   
   public void testSetDeployers() throws Exception
   {
      DeployerClient main = createMainDeployer();
      assertEmpty(getDeployers(main));
      
      Deployer deployer1 = new TestSimpleDeployer();
      Deployer deployer2 = new TestSimpleDeployer();
      Set<Deployer> expected = new HashSet<Deployer>();
      expected.add(deployer1);
      expected.add(deployer2);
      setDeployers(main, expected);
      assertEquals(expected, getDeployers(main));
      
      expected = new HashSet<Deployer>();
      expected.add(deployer1);
      setDeployers(main, expected);
      assertEquals(expected, getDeployers(main));
      
      expected = new HashSet<Deployer>();
      expected.add(deployer1);
      setDeployers(main, expected);
      assertEquals(expected, getDeployers(main));
      
      expected = Collections.emptySet();
      setDeployers(main, expected);
      assertEquals(expected, getDeployers(main));
   }
}
