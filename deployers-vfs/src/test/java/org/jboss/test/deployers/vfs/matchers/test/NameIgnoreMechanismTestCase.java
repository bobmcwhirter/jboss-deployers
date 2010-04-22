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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.jboss.deployers.client.spi.DeployerClient;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.vfs.plugins.structure.jar.JARStructure;
import org.jboss.deployers.vfs.spi.deployer.AbstractIgnoreFilesDeployer;
import org.jboss.test.deployers.BaseDeployersVFSTest;
import org.jboss.test.deployers.vfs.matchers.support.FeedbackDeployer;
import org.jboss.test.deployers.vfs.matchers.support.NIMDeployer;
import org.jboss.test.deployers.vfs.matchers.support.SingleNIM;

import junit.framework.Test;

/**
 * Name ignore mechanism tests.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class NameIgnoreMechanismTestCase extends BaseDeployersVFSTest
{
   public NameIgnoreMechanismTestCase(String name)
   {
      super(name);
   }

   public static Test suite()
   {
      return suite(NameIgnoreMechanismTestCase.class);
   }

   protected void testNameIgnoreMechanism(FeedbackDeployer fbd, int size) throws Throwable
   {
      NIMDeployer nimd = new NIMDeployer(new SingleNIM("fst.txt"));

      DeployerClient main = createMainDeployer(fbd, nimd);
      addStructureDeployer(main, new JARStructure());

      Deployment deployment = createDeployment("/matchers", "ignore");
      main.deploy(deployment);

      assertEquals(size, fbd.getFiles().size());
      assertFalse(fbd.getFiles().contains("fst.txt"));
   }

   public void testSingleNameNoSuffix() throws Throwable
   {
      FeedbackDeployer fbd1 = new FeedbackDeployer();
      fbd1.setName("empty.txt");
      FeedbackDeployer fbd2 = new FeedbackDeployer();
      fbd2.setName("fst.txt");
      NIMDeployer nimd = new NIMDeployer(new SingleNIM("fst.txt"));

      DeployerClient main = createMainDeployer(fbd1, fbd2, nimd);
      addStructureDeployer(main, new JARStructure());

      Deployment deployment = createDeployment("/matchers", "ignore");
      main.deploy(deployment);

      assertFalse(fbd1.getFiles().isEmpty());
      assertEmpty(fbd2.getFiles());
   }

   public void testMultipleNamesNoSuffix() throws Throwable
   {
      FeedbackDeployer fbd = new FeedbackDeployer();
      Set<String> names = new HashSet<String>(Arrays.asList("empty.txt", "fst.txt", "snd.txt"));
      fbd.setNames(names);

      testNameIgnoreMechanism(fbd, 2);
   }

   public void testNoNameJustSuffix() throws Throwable
   {
      FeedbackDeployer fbd = new FeedbackDeployer();
      fbd.setSuffix(".txt");
      fbd.setAllowMultipleFiles(true);

      testNameIgnoreMechanism(fbd, 3);
   }

   public void testNamesWithSuffix() throws Throwable
   {
      FeedbackDeployer fbd = new FeedbackDeployer();
      Set<String> names = new HashSet<String>(Arrays.asList("empty.txt", "fst.txt", "snd.txt"));
      fbd.setNames(names);
      fbd.setSuffix(".tmp");

      testNameIgnoreMechanism(fbd, 3);
   }

   public void testRealNIMDeployer() throws Throwable
   {
      FeedbackDeployer fbd = new FeedbackDeployer();
      fbd.setSuffix(".txt");
      fbd.setAllowMultipleFiles(true);

      AbstractIgnoreFilesDeployer nimd = new AbstractIgnoreFilesDeployer();

      DeployerClient main = createMainDeployer(fbd, nimd);
      addStructureDeployer(main, new JARStructure());

      Deployment deployment = createDeployment("/matchers", "ignore");
      main.deploy(deployment);

      assertEquals(2, fbd.getFiles().size());
      assertFalse(fbd.getFiles().contains("fst.txt"));
   }
}