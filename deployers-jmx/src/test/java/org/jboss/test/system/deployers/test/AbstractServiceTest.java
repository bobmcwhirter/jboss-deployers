/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.system.deployers.test;

import java.lang.reflect.Method;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import org.jboss.dependency.spi.Controller;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.plugins.deployers.DeployersImpl;
import org.jboss.deployers.plugins.main.MainDeployerImpl;
import org.jboss.deployers.spi.deployer.Deployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.structure.spi.StructuralDeployers;
import org.jboss.deployers.structure.spi.DeploymentRegistry;
import org.jboss.kernel.Kernel;
import org.jboss.kernel.plugins.bootstrap.basic.BasicBootstrap;
import org.jboss.mx.server.ServerConstants;
import org.jboss.mx.util.MBeanServerLocator;
import org.jboss.system.ServiceController;
import org.jboss.system.deployers.ServiceDeployer;
import org.jboss.system.deployers.ServiceDeploymentDeployer;
import org.jboss.system.metadata.ServiceMetaData;
import org.jboss.test.AbstractSystemTest;
import org.jboss.test.AbstractTestDelegate;
import org.jboss.test.system.deployers.support.CLDeployer;
import org.jboss.test.system.deployers.support.JmxCL;
import org.jboss.test.system.deployers.support.SMDParsingDeployer;

/**
 * Abstract service/jmx test.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public abstract class AbstractServiceTest extends AbstractSystemTest
{
   protected Controller controller;
   protected ServiceController serviceController;
   protected MainDeployerImpl main;

   protected AbstractServiceTest(String name)
   {
      super(name);
   }

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();

      MBeanServer mbeanServer = createMBeanServer();
      try
      {
         BasicBootstrap bootstrap = new BasicBootstrap();
         bootstrap.run();
         Kernel kernel = bootstrap.getKernel();
         controller = kernel.getController();

         serviceController = new ServiceController();
         ObjectName objectName = new ObjectName("jboss.system:service=ServiceController");
         serviceController.setKernel(kernel);
         serviceController.setMBeanServer(mbeanServer);
         mbeanServer.registerMBean(serviceController, objectName);

         ClassLoader loader = Thread.currentThread().getContextClassLoader();
         ObjectName clON = new ObjectName("jboss:service=defaultClassLoader");
         mbeanServer.registerMBean(new JmxCL(loader), clON);

         MainDeployerImpl mainDeployer = new MainDeployerImpl();
         DeployersImpl deployersImpl = new DeployersImpl(controller);
         mainDeployer.setDeployers(deployersImpl);

         // default deployers
         ServiceDeployer serviceDeployer = new ServiceDeployer(serviceController);
         serviceDeployer.setDeploymentRegistry(getRegistry());
         deployersImpl.addDeployer(serviceDeployer);
         deployersImpl.addDeployer(new ServiceDeploymentDeployer());
         deployersImpl.addDeployer(new CLDeployer());

         main = mainDeployer;
      }
      catch (Exception e)
      {
         cleanup(mbeanServer);

         super.tearDown();

         throw e;
      }
   }

   protected DeploymentRegistry getRegistry()
   {
      return null;
   }

   private void cleanup(MBeanServer mbeanServer)
   {
      try
      {
         ObjectName objectName = new ObjectName("jboss.system:service=ServiceController");
         mbeanServer.unregisterMBean(objectName);

         ObjectName clON = new ObjectName("jboss:service=defaultClassLoader");
         mbeanServer.unregisterMBean(clON);
      }
      catch (Exception ignored)
      {
      }
      finally
      {
         controller = null;
         serviceController = null;
         main = null;

         MBeanServerLocator.setJBoss(null);
         if (MBeanServerFactory.findMBeanServer("JBoss").isEmpty() == false)
            MBeanServerFactory.releaseMBeanServer(mbeanServer);
      }
   }

   @Override
   protected void tearDown() throws Exception
   {
      try
      {
         MBeanServer mbeanServer = MBeanServerLocator.locateJBoss();

         cleanup(mbeanServer);
      }
      finally
      {
         super.tearDown();
      }
   }

   protected void setStructureDeployer(StructuralDeployers deployers)
   {
      main.setStructuralDeployers(deployers);
   }

   protected void addDeployers(Deployer... deployers)
   {
      if (deployers != null)
      {
         DeployersImpl dc = (DeployersImpl)main.getDeployers();
         for (Deployer deployer : deployers)
            dc.addDeployer(deployer);
      }
   }

   protected DeploymentUnit deploy(Deployment deployment) throws Exception
   {
      main.addDeployment(deployment);
      main.process();
      main.checkComplete();
      return main.getDeploymentUnit(deployment.getName());
   }

   protected void undeploy(Deployment deployment) throws Exception
   {
      main.removeDeployment(deployment);
      main.process();
      main.checkComplete();
   }

   protected void addServiceMetaData(ServiceMetaData smd)
   {
      SMDParsingDeployer deployer = new SMDParsingDeployer(smd);
      addDeployers(deployer);
   }

   private MBeanServer createMBeanServer() throws Exception
   {
      MBeanServer server;

      String builder = System.getProperty(ServerConstants.MBEAN_SERVER_BUILDER_CLASS_PROPERTY, ServerConstants.DEFAULT_MBEAN_SERVER_BUILDER_CLASS);
      System.setProperty(ServerConstants.MBEAN_SERVER_BUILDER_CLASS_PROPERTY, builder);

      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      Class<?> clazz = cl.loadClass("java.lang.management.ManagementFactory");
      Method method = clazz.getMethod("getPlatformMBeanServer");
      Object[] args = null;
      server = (MBeanServer)method.invoke(null, args);
      // Tell the MBeanServerLocator to point to this mbeanServer
      MBeanServerLocator.setJBoss(server);
      return server;
   }

   /**
    * Default setup with security manager enabled
    *
    * @param clazz the class
    * @return the delegate
    * @throws Exception for any error
    */
   public static AbstractTestDelegate getDelegate(Class<?> clazz) throws Exception
   {
      AbstractTestDelegate delegate = new AbstractTestDelegate(clazz);
      delegate.enableSecurity = false; // security
      return delegate;
   }
}