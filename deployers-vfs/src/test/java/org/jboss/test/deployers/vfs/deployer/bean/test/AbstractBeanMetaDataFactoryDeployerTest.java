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
package org.jboss.test.deployers.vfs.deployer.bean.test;

import org.jboss.dependency.spi.ControllerContext;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.spi.attachments.MutableAttachments;
import org.jboss.test.deployers.vfs.deployer.AbstractDeployerUnitTest;
import org.jboss.test.deployers.vfs.deployer.bean.support.CustomBMDF;

/**
 * AbstractBeanMetaDataFactoryDeployerTest.
 *
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 */
public abstract class AbstractBeanMetaDataFactoryDeployerTest extends AbstractDeployerUnitTest
{
   protected AbstractBeanMetaDataFactoryDeployerTest(String name) throws Throwable
   {
      super(name);
   }

   public void testCustomBeanMetaData() throws Throwable
   {
      Deployment context = createSimpleDeployment(getClass().getName());
      MutableAttachments attachments = (MutableAttachments)context.getPredeterminedManagedObjects();
      attachments.addAttachment(CustomBMDF.class, new CustomBMDF("Test"));

      assertDeploy(context);

      ControllerContext testCC = controller.getInstalledContext("Test");
      assertNotNull(testCC);

      assertUndeploy(context);
      assertNull(controller.getContext("Test", null));
   }
}