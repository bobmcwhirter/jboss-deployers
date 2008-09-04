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
package org.jboss.test.deployers.scope.test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.deployers.client.spi.DeployerClient;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.client.spi.DeploymentFactory;
import org.jboss.deployers.plugins.deployers.DeployersImpl;
import org.jboss.deployers.spi.attachments.MutableAttachments;
import org.jboss.deployers.spi.attachments.PredeterminedManagedObjectAttachments;
import org.jboss.deployers.spi.deployer.Deployers;
import org.jboss.deployers.spi.structure.ContextInfo;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.metadata.plugins.repository.basic.BasicMetaDataRepository;
import org.jboss.metadata.spi.MetaData;
import org.jboss.metadata.spi.MutableMetaData;
import org.jboss.metadata.spi.scope.CommonLevels;
import org.jboss.metadata.spi.scope.ScopeKey;
import org.jboss.test.deployers.AbstractDeployerTest;
import org.jboss.test.deployers.scope.support.TestComponentDeployer;
import org.jboss.test.deployers.scope.support.TestComponentMetaData;
import org.jboss.test.deployers.scope.support.TestComponentMetaDataContainer;

/**
 * DeployerScopeUnitTestCase.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class DeployerScopeUnitTestCase extends AbstractDeployerTest
{
   private static final DeploymentFactory factory = new DeploymentFactory();

   private TestComponentDeployer deployer = new TestComponentDeployer();

   private BasicMetaDataRepository repository;
   
   private Set<ScopeKey> scopes = new HashSet<ScopeKey>();
   
   public static ScopeKey SCOPE_A = createAppScope("A");
   public static ScopeKey SCOPE_AA = createScope("A", "A");

   public static ScopeKey createAppScope(String app)
   {
      return new ScopeKey(CommonLevels.APPLICATION, app);
   }

   public static ScopeKey createDepScope(String dep)
   {
      return new ScopeKey(CommonLevels.DEPLOYMENT, dep);
   }

   public static ScopeKey createInstanceScope(String name)
   {
      return new ScopeKey(CommonLevels.INSTANCE, name);
   }

   public static ScopeKey createScope(String app, String dep)
   {
      ScopeKey result = ScopeKey.DEFAULT_SCOPE.clone();
      result.addScope(CommonLevels.APPLICATION, app);
      result.addScope(CommonLevels.DEPLOYMENT, dep);
      return result;
   }

   public static ScopeKey createComponentScope(String app, String dep, String comp)
   {
      ScopeKey result = createScope(app, dep);
      result.addScope(CommonLevels.INSTANCE, comp);
      return result;
   }
   
   public static Test suite()
   {
      return new TestSuite(DeployerScopeUnitTestCase.class);
   }
   
   public DeployerScopeUnitTestCase(String name)
   {
      super(name);
   }

   public void testSimpleScope() throws Exception
   {
      DeployerClient main = getMainDeployer();
      
      Deployment a = createSimpleDeployment("A");
      main.addDeployment(a);
      main.process();
      
      DeploymentUnit unit = getDeploymentUnit(main, "A");
      assertScope(SCOPE_AA, SCOPE_A, unit);
      
      main.removeDeployment(a);
      main.process();
      assertNoScopes();
   }

   public void testSubDeploymentScope() throws Exception
   {
      DeployerClient main = getMainDeployer();
      
      Deployment a = createSimpleDeployment("A");
      factory.addContext(a, "A1");
      factory.addContext(a, "A2");
      main.addDeployment(a);
      main.process();

      DeploymentUnit unit = getDeploymentUnit(main, "A");
      assertScope(SCOPE_AA, SCOPE_A, unit);

      List<DeploymentUnit> children = unit.getChildren();
      assertEquals(2, children.size());
      for (DeploymentUnit child : children)
      {
         ScopeKey scope = createScope("A", child.getName());
         ScopeKey mutable = createDepScope(child.getName());
         assertScope(scope, mutable, child);
      }

      main.removeDeployment(a);
      main.process();
      assertNoScopes();
   }

   public void testSimpleComponents() throws Exception
   {
      DeployerClient main = getMainDeployer();
      
      Deployment a = createSimpleDeployment("A");
      TestComponentMetaData c1 = new TestComponentMetaData("C1");
      TestComponentMetaData c2 = new TestComponentMetaData("C2");
      TestComponentMetaDataContainer md = new TestComponentMetaDataContainer(c1, c2);
      addMetaData(a, md);
      main.addDeployment(a);
      main.process();

      DeploymentUnit unit = getDeploymentUnit(main, "A");
      assertScope(SCOPE_AA, SCOPE_A, unit);

      List<DeploymentUnit> components = unit.getComponents();
      assertEquals(2, components.size());
      for (DeploymentUnit component : components)
      {
         ScopeKey scope = createComponentScope("A", "A", component.getName());
         ScopeKey mutable = createInstanceScope(component.getName());
         assertScope(scope, mutable, component);
      }

      main.removeDeployment(a);
      main.process();
      assertNoScopes();
   }

   public void testSubDeploymentScopeWithComponents() throws Exception
   {
      DeployerClient main = getMainDeployer();
      
      Deployment a = createSimpleDeployment("A");
      ContextInfo a1 = factory.addContext(a, "A1");
      TestComponentMetaData a1c1 = new TestComponentMetaData("A1C1");
      TestComponentMetaData a1c2 = new TestComponentMetaData("A1C2");
      TestComponentMetaDataContainer md1 = new TestComponentMetaDataContainer(a1c1, a1c2);
      addMetaData(a1, md1);
      ContextInfo a2 = factory.addContext(a, "A2");
      TestComponentMetaData a2c1 = new TestComponentMetaData("A2C1");
      TestComponentMetaData a2c2 = new TestComponentMetaData("A2C2");
      TestComponentMetaDataContainer md2 = new TestComponentMetaDataContainer(a2c1, a2c2);
      addMetaData(a2, md2);
      main.addDeployment(a);
      main.process();

      DeploymentUnit unit = getDeploymentUnit(main, "A");
      assertScope(SCOPE_AA, SCOPE_A, unit);

      List<DeploymentUnit> children = unit.getChildren();
      assertEquals(2, children.size());
      for (DeploymentUnit child : children)
      {
         ScopeKey scope = createScope("A", child.getName());
         ScopeKey mutable = createDepScope(child.getName());
         assertScope(scope, mutable, child);

         List<DeploymentUnit> components = child.getComponents();
         assertEquals(2, components.size());
         for (DeploymentUnit component : components)
         {
            scope = createComponentScope("A", child.getName(), component.getName());
            mutable = createInstanceScope(component.getName());
            assertScope(scope, mutable, component);
         }
      }

      main.removeDeployment(a);
      main.process();
      assertNoScopes();
   }

   protected void assertScope(ScopeKey scope, ScopeKey mutable, DeploymentUnit unit)
   {
      scopes.add(scope);
      scopes.add(mutable);
      
      assertEquals(scope, unit.getScope());
      assertEquals(mutable, unit.getMutableScope());
      
      MetaData metaData = unit.getMetaData();
      assertNotNull("Should have metadata for " + unit.getName(), metaData);
      String expected = null;
      DeploymentUnit parent = unit.getParent();
      if (parent != null)
         expected = parent.getName();
      assertEquals(expected, metaData.getMetaData("test"));
      
      MutableMetaData mutableMetaData = unit.getMutableMetaData();
      mutableMetaData.addMetaData("test", unit.getName(), String.class);
      
      assertEquals(unit.getName(), metaData.getMetaData("test"));
   }
   
   protected void assertNoScopes()
   {
      for (ScopeKey scope : scopes)
      {
         assertNull(scope + " should have been removed", repository.getMetaData(scope));
         assertNull(scope + " should have been removed", repository.getMetaDataRetrieval(scope));
      }
   }
   
   protected static void addMetaData(PredeterminedManagedObjectAttachments attachments, TestComponentMetaDataContainer md)
   {
      MutableAttachments mutable = (MutableAttachments) attachments.getPredeterminedManagedObjects();
      mutable.addAttachment(TestComponentMetaDataContainer.class, md);
   }
   
   protected DeployerClient getMainDeployer()
   {
      return createMainDeployer(deployer);
   }

   @Override
   protected Deployers createDeployers()
   {
      DeployersImpl deployers = (DeployersImpl) super.createDeployers();
      repository = new BasicMetaDataRepository();
      deployers.setRepository(repository);
      return deployers;
   }
}
