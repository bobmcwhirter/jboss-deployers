/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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

import java.util.Set;

import org.jboss.dependency.plugins.AbstractController;
import org.jboss.dependency.spi.Controller;
import org.jboss.dependency.spi.ControllerContext;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.deployers.client.plugins.deployment.AbstractDeployment;
import org.jboss.deployers.client.spi.DeployerClient;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.client.spi.DeploymentFactory;
import org.jboss.deployers.plugins.deployers.DeployerWrapper;
import org.jboss.deployers.plugins.deployers.DeployersImpl;
import org.jboss.deployers.plugins.main.MainDeployerImpl;
import org.jboss.deployers.plugins.managed.DefaultManagedDeploymentCreator;
import org.jboss.deployers.spi.attachments.PredeterminedManagedObjectAttachments;
import org.jboss.deployers.spi.deployer.Deployer;
import org.jboss.deployers.spi.deployer.Deployers;
import org.jboss.deployers.spi.deployer.DeploymentStage;
import org.jboss.deployers.spi.deployer.helpers.DefaultManagedObjectCreator;
import org.jboss.deployers.spi.deployer.managed.ManagedDeploymentCreator;
import org.jboss.deployers.spi.deployer.managed.ManagedObjectCreator;
import org.jboss.deployers.spi.structure.ContextInfo;
import org.jboss.deployers.structure.spi.DeploymentContext;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.structure.spi.StructuralDeployers;
import org.jboss.deployers.structure.spi.StructureBuilder;
import org.jboss.deployers.structure.spi.helpers.AbstractStructuralDeployers;
import org.jboss.deployers.structure.spi.helpers.AbstractStructureBuilder;
import org.jboss.test.BaseTestCase;

/**
 * AbstractDeployerTest.
 * 
 * @author <a href="adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public abstract class AbstractDeployerTest extends BaseTestCase
{
   DeploymentFactory factory = new DeploymentFactory();

   public AbstractDeployerTest(String name)
   {
      super(name);
   }
   
   protected DeployerClient createMainDeployer()
   {
      return createMainDeployer((Deployer[]) null);
   }
   
   protected DeployerClient createMainDeployer(Deployer... deployers)
   {
      log.debug("createMainDeployer");
      MainDeployerImpl mainDeployer = new MainDeployerImpl();
      StructuralDeployers structure = createStructuralDeployers();
      mainDeployer.setStructuralDeployers(structure);
      Deployers theDeployers = createDeployers();
      mainDeployer.setDeployers(theDeployers);
      ManagedDeploymentCreator mdc = createManagedDeploymentCreator();
      mainDeployer.setMgtDeploymentCreator(mdc);
      if (deployers != null)
      {
         for (Deployer deployer : deployers)
            addDeployer(mainDeployer, deployer);
      }
      return mainDeployer;
   }

   protected StructureBuilder createStructureBuilder()
   {
      return new AbstractStructureBuilder();
   }
   protected ManagedDeploymentCreator createManagedDeploymentCreator()
   {
      return new DefaultManagedDeploymentCreator();
   }
   protected ManagedObjectCreator createManagedObjectCreator()
   {
      return new DefaultManagedObjectCreator();
   }
   protected StructuralDeployers createStructuralDeployers()
   {
      StructureBuilder builder = createStructureBuilder();
      AbstractStructuralDeployers structure = new AbstractStructuralDeployers();
      structure.setStructureBuilder(builder);
      return structure;
   }

   protected Controller getController()
   {
      return new AbstractController();
   }

   protected Deployers createDeployers()
   {
      log.debug("createDeployers");
      Controller controller = getController();
      ManagedObjectCreator moc = createManagedObjectCreator();
      System.out.println("createDeployers, moc: "+moc);
      DeployersImpl di = new DeployersImpl(controller);
      di.setMgtObjectCreator(moc);
      return di;
   }

   protected void addDeployer(DeployerClient main, Deployer deployer)
   {
      MainDeployerImpl mainDeployerImpl = (MainDeployerImpl) main;
      DeployersImpl deployersImpl = (DeployersImpl) mainDeployerImpl.getDeployers();
      deployersImpl.addDeployer(deployer);
   }
   
   protected void removeDeployer(DeployerClient main, Deployer deployer)
   {
      MainDeployerImpl mainDeployerImpl = (MainDeployerImpl) main;
      DeployersImpl deployersImpl = (DeployersImpl) mainDeployerImpl.getDeployers();
      deployersImpl.removeDeployer(deployer);
   }
   
   protected void setDeployers(DeployerClient main, Set<Deployer> deployers)
   {
      log.debug("setDeployers");
      MainDeployerImpl mainDeployerImpl = (MainDeployerImpl) main;
      DeployersImpl deployersImpl = (DeployersImpl) mainDeployerImpl.getDeployers();
      deployersImpl.setDeployers(deployers);
   }
   
   protected Set<DeployerWrapper> getDeployers(DeployerClient main)
   {
      MainDeployerImpl mainDeployerImpl = (MainDeployerImpl) main;
      DeployersImpl deployersImpl = (DeployersImpl) mainDeployerImpl.getDeployers();
      return deployersImpl.getDeployerWrappers();
   }
   
   @Deprecated
   protected DeploymentContext getDeploymentContext(DeployerClient main, String name)
   {
      MainDeployerImpl mainDeployerImpl = (MainDeployerImpl) main;
      return mainDeployerImpl.getDeploymentContext(name);
   }
   
   @Deprecated
   protected DeploymentContext assertDeploymentContext(DeployerClient main, String name)
   {
      DeploymentContext context = getDeploymentContext(main, name);
      assertNotNull(name + " not found", context);
      return context;
   }
   
   protected DeploymentUnit getDeploymentUnit(DeployerClient main, String name)
   {
      MainDeployerImpl mainDeployerImpl = (MainDeployerImpl) main;
      return mainDeployerImpl.getDeploymentUnit(name);
   }
   
   protected DeploymentUnit assertDeploymentUnit(DeployerClient main, String name)
   {
      DeploymentUnit unit = getDeploymentUnit(main, name);
      assertNotNull(name + " not found", unit);
      return unit;
   }

   @Deprecated
   protected DeploymentUnit deploy(DeployerClient main, Deployment deployment) throws Exception
   {
      main.deploy(deployment);
      return assertDeploymentUnit(main, deployment.getName());
   }
   
   protected DeploymentUnit assertDeploy(DeployerClient main, Deployment deployment) throws Exception
   {
      main.deploy(deployment);
      return assertDeploymentUnit(main, deployment.getName());
   }

   protected DeploymentUnit addDeployment(DeployerClient main, Deployment deployment) throws Exception
   {
      main.addDeployment(deployment);
      main.process();
      return assertDeploymentUnit(main, deployment.getName());
   }

   protected void assertUndeploy(DeployerClient main, Deployment deployment) throws Exception
   {
      main.undeploy(deployment);
   }
   
   protected Deployment createSimpleDeployment(String name)
   {
      AbstractDeployment unit = createAbstractDeployment(name);
      factory.addContext(unit, "");
      return unit;
   }

   protected AbstractDeployment createAbstractDeployment(String name)
   {
      return new AbstractDeployment(name);
   }

   protected ContextInfo addChild(PredeterminedManagedObjectAttachments parent, String name)
   {
      return factory.addContext(parent, name);
   }

   /**
    * This stage check is impl detail.
    *
    * @param unit the deployment unit
    * @param stage expected deployment stage
    */
   protected void assertDeploymentStage(DeploymentUnit unit, DeploymentStage stage)
   {
      ControllerContext context = unit.getAttachment(ControllerContext.class);
      assertNotNull("Expecting controller context attachment: " + unit, context);
      ControllerState state = new ControllerState(stage.getName());
      assertEquals(state, context.getState());
   }

   @Override
   protected void configureLogging()
   {
      //enableTrace("org.jboss.deployers");
   }
}
