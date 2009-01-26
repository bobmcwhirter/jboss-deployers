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
package org.jboss.test.deployers.vfs.structureprocessor.test;

import java.util.Arrays;
import java.util.Collection;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.jboss.deployers.client.spi.DeployerClient;
import org.jboss.deployers.plugins.main.MainDeployerImpl;
import org.jboss.deployers.structure.spi.DeploymentContext;
import org.jboss.deployers.structure.spi.StructureProcessor;
import org.jboss.deployers.vfs.plugins.structure.jar.JARStructure;
import org.jboss.deployers.vfs.plugins.structure.modify.FileModificationTypeMatcher;
import org.jboss.deployers.vfs.plugins.structure.modify.ModificationTypeMatcher;
import org.jboss.deployers.vfs.plugins.structure.modify.ModificationTypeStructureProcessor;
import org.jboss.deployers.vfs.spi.client.VFSDeployment;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentContext;
import org.jboss.deployers.spi.structure.ModificationType;
import org.jboss.virtual.VFSUtils;
import org.jboss.virtual.VirtualFile;

/**
 * VFSStructureProcessorUnitTestCase.
 *
 * @author <a href="ales.justin@jboss.org">Ales Justin</a>
 */
public class VFSStructureProcessorUnitTestCase extends StructureProcessorUnitTest
{
   public static Test suite()
   {
      return new TestSuite(VFSStructureProcessorUnitTestCase.class);
   }

   public VFSStructureProcessorUnitTestCase(String name)
   {
      super(name);
   }

   protected StructureProcessor createStructureProcessor()
   {
      ModificationTypeStructureProcessor mtsp = new ModificationTypeStructureProcessor();

      ModificationTypeMatcher topAndChildren = createTempMatcher(true, true, false, true, "child.xml");
      ModificationTypeMatcher directTop = createTempMatcher(true, false, true, false, "top.xml");
      ModificationTypeMatcher justChildren = createTempMatcher(true, false, false, true, "sub.xml");

      mtsp.setMatchers(Arrays.asList(directTop, topAndChildren, justChildren));
      return mtsp;
   }

   protected ModificationTypeMatcher createTempMatcher(
         boolean metadataOnly,
         boolean checkChildren,
         boolean topLevelOnly,
         boolean childrenOnly,
         String... paths)
   {
      FileModificationTypeMatcher matcher = new FileModificationTypeMatcher(paths);
      matcher.setModificationType(ModificationType.TEMP);
      matcher.setMetadataOnly(metadataOnly);
      matcher.setCheckChildren(checkChildren);
      matcher.setTopLevelOnly(topLevelOnly);
      matcher.setChildrenOnly(childrenOnly);
      return matcher;
   }

   protected VFSDeploymentContext getTopDeploymentContext(DeployerClient main, String name)
   {
      MainDeployerImpl mdi = (MainDeployerImpl)main;
      Collection<DeploymentContext> all = mdi.getAll();
      for (DeploymentContext dc : all)
      {
         if (dc.getSimpleName().equals(name))
            return VFSDeploymentContext.class.cast(dc);
      }
      throw new IllegalArgumentException("No such deployment context: " + name + ", all: " + all);
   }

   protected void testTopModification(String path) throws Exception
   {
      DeployerClient main = createMainDeployer();
      addStructureDeployer(main, new JARStructure());

      VFSDeployment deployment = createDeployment("/structureprocessor", path);
      main.deploy(deployment);
      try
      {
         VFSDeploymentContext vdc = getTopDeploymentContext(main, path);
         VirtualFile root = vdc.getRoot();
         assertTrue("Should be temp", VFSUtils.isTemporaryFile(root));
      }
      finally
      {
         main.undeploy(deployment);
      }
   }

   public void testDirectTopLevelModification() throws Exception
   {
      testTopModification("directtop");
   }

   public void testTopFromChildModification() throws Exception
   {
      testTopModification("topfromchild");
   }

   public void testChildModification() throws Exception
   {
      DeployerClient main = createMainDeployer();
      addStructureDeployer(main, new JARStructure());

      VFSDeployment deployment = createDeployment("/structureprocessor", "childmod");
      main.deploy(deployment);
      try
      {
         VFSDeploymentContext vdc = getTopDeploymentContext(main, "childmod");
         VirtualFile root = vdc.getRoot();
         VirtualFile file = root.getChild("tempchild");
         try
         {
            assertTrue("Should be temp", VFSUtils.isTemporaryFile(file));
         }
         finally
         {
            file.cleanup();
         }
      }
      finally
      {
         main.undeploy(deployment);
      }
   }
}