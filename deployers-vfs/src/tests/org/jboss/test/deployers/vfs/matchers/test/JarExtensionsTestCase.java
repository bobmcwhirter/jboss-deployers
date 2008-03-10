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
package org.jboss.test.deployers.vfs.matchers.test;

import java.util.Set;

import junit.framework.Test;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.dependency.spi.ControllerContext;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.plugins.main.MainDeployerImpl;
import org.jboss.deployers.spi.deployer.Deployers;
import org.jboss.deployers.vfs.spi.deployer.FileMatcher;
import org.jboss.kernel.spi.dependency.KernelController;
import org.jboss.test.deployers.vfs.matchers.support.ExposedFileStructure;
import org.jboss.test.deployers.vfs.matchers.support.ExposedJARStructure;
import org.jboss.test.deployers.vfs.matchers.support.FeedbackDeployer;
import org.jboss.test.deployers.vfs.matchers.support.TestExtensionDeployer;

/**
 * Jar extensions tests.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class JarExtensionsTestCase extends KernelHolderDeployersTest
{
   public JarExtensionsTestCase(String name)
   {
      super(name);
   }

   public static Test suite()
   {
      return suite(JarExtensionsTestCase.class);
   }

   public void testExtensionProviders() throws Throwable
   {
      KernelController controller = getController();

      MainDeployerImpl main = new MainDeployerImpl();
      main.setStructuralDeployers(createStructuralDeployers());

      BeanMetaDataBuilder builder = BeanMetaDataBuilder.createBuilder("JARStructure", ExposedJARStructure.class.getName());
      controller.install(builder.getBeanMetaData());
      builder = BeanMetaDataBuilder.createBuilder("FileStructure", ExposedFileStructure.class.getName());
      controller.install(builder.getBeanMetaData());

      ControllerContext jarContext = controller.getInstalledContext("JARStructure");
      assertNotNull(jarContext);
      ExposedJARStructure jarStructure = (ExposedJARStructure)jarContext.getTarget();
      assertNotNull(jarStructure);
      addStructureDeployer(main, jarStructure);

      ControllerContext fileContext = controller.getInstalledContext("FileStructure");
      assertNotNull(fileContext);
      ExposedFileStructure fileStructure = (ExposedFileStructure)fileContext.getTarget();
      assertNotNull(fileStructure);
      addStructureDeployer(main, fileStructure);

      Set<FileMatcher> matchers = fileStructure.getMatchers();
      assertNotNull(matchers);
      assertEmpty(matchers);

      builder = BeanMetaDataBuilder.createBuilder("RhtDeployer", TestExtensionDeployer.class.getName());
      builder.addConstructorParameter(String.class.getName(), ".rht");
      controller.install(builder.getBeanMetaData());
      assertEquals(1, matchers.size());

      builder = BeanMetaDataBuilder.createBuilder("JbsDeployer", TestExtensionDeployer.class.getName());
      builder.addConstructorParameter(String.class.getName(), ".jbs");
      controller.install(builder.getBeanMetaData());
      assertEquals(2, matchers.size());

      Set<String> suffixes = jarStructure.getSuffixes();
      assertTrue(suffixes.contains(".rhts"));
      assertTrue(suffixes.contains(".jbss"));

      Deployers deployers = createDeployers();
      main.setDeployers(deployers);

      FeedbackDeployer rhtDeployer = addDeployer(main, "RhtDeployer");
      FeedbackDeployer jbsDeployer = addDeployer(main, "JbsDeployer");

      Deployment deploymentRHT = createDeployment("/matchers", "some.rhts");
      assertDeploy(main, deploymentRHT);
      assertTrue(jarStructure.getRecognized().contains("some.rhts"));

      Deployment deploymentJBS = createDeployment("/matchers", "some.jbss");
      assertDeploy(main, deploymentJBS);
      assertTrue(jarStructure.getRecognized().contains("some.jbss"));

      Deployment deploymentXYZ = createDeployment("/matchers", "some.xyz");
      assertDeploy(main, deploymentXYZ);
      assertFalse(jarStructure.getRecognized().contains("some.xyz"));

      assertNotNull(rhtDeployer.getFiles());
      assertEquals(new String[]{"some.rht"}, rhtDeployer.getFiles().toArray());
      assertNotNull(jbsDeployer.getFiles());
      assertEquals(new String[]{"some.jbs"}, jbsDeployer.getFiles().toArray());
   }
}

