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

import java.util.Collections;

import javax.management.ObjectName;

import junit.framework.Test;

import org.jboss.dependency.spi.ControllerContext;
import org.jboss.deployers.client.plugins.deployment.AbstractDeployment;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.structure.spi.DeploymentContext;
import org.jboss.deployers.structure.spi.DeploymentRegistry;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.structure.spi.StructuralDeployers;
import org.jboss.deployers.structure.spi.helpers.AbstractDeploymentContext;
import org.jboss.deployers.structure.spi.helpers.AbstractDeploymentRegistry;
import org.jboss.system.metadata.ServiceConstructorMetaData;
import org.jboss.system.metadata.ServiceMetaData;
import org.jboss.test.system.deployers.support.Tester;

/**
 * Test component name usage.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class ServiceDeploymentRegistryTestCase extends AbstractServiceTest
{
   private DeploymentRegistry registry = new AbstractDeploymentRegistry();

   public ServiceDeploymentRegistryTestCase(String name)
   {
      super(name);
   }

   public static Test suite()
   {
      return suite(ServiceDeploymentRegistryTestCase.class);
   }

   @Override
   protected DeploymentRegistry getRegistry()
   {
      return registry;
   }

   public void testDeploymentRegistry() throws Exception
   {
      ServiceMetaData metaData = new ServiceMetaData();
      ObjectName objectName = new ObjectName("jboss.system:service=Tester");
      metaData.setObjectName(objectName);
      metaData.setCode(Tester.class.getName());
      metaData.setConstructor(new ServiceConstructorMetaData());
      addServiceMetaData(metaData);

      setStructureDeployer(new StructuralDeployers()
      {
         public DeploymentContext determineStructure(Deployment deployment) throws DeploymentException
         {
            return new AbstractDeploymentContext("SMD", "");
         }
      });

      ControllerContext context = null;
      Deployment deployment = new AbstractDeployment("SMD");
      DeploymentUnit unit = deploy(deployment);
      try
      {
         context = controller.getInstalledContext(objectName.getCanonicalName());
         assertNotNull(context);
         assertSame(unit, registry.getDeployment(context));
         assertEquals(Collections.singleton(context), registry.getContexts(unit));
      }
      finally
      {
         undeploy(deployment);

         assertNull(registry.getDeployment(context));
         assertEmpty(registry.getContexts(unit));
      }
   }
}