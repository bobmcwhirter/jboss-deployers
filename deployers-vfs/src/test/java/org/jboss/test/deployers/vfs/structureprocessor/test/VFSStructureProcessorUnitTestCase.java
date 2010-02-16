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
import org.jboss.deployers.spi.structure.ModificationType;
import org.jboss.deployers.structure.spi.DeploymentContext;
import org.jboss.deployers.structure.spi.StructureProcessor;
import org.jboss.deployers.structure.spi.main.MainDeployerInternals;
import org.jboss.deployers.vfs.plugins.structure.jar.JARStructure;
import org.jboss.deployers.vfs.plugins.structure.modify.FileModificationTypeMatcher;
import org.jboss.deployers.vfs.plugins.structure.modify.ModificationTypeMatcher;
import org.jboss.deployers.vfs.plugins.structure.modify.ModificationTypeStructureProcessor;
import org.jboss.deployers.vfs.spi.client.VFSDeployment;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentContext;
import org.jboss.vfs.VirtualFile;
import org.jboss.vfs.util.automount.Automounter;

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

      ModificationTypeMatcher topAndChildren = createTempMatcher(ModificationType.TEMP, true, true, true, false, "child.xml");
      ModificationTypeMatcher directTop = createTempMatcher(ModificationType.TEMP, true, false, true, false, "top.xml");
      ModificationTypeMatcher justChildren = createTempMatcher(ModificationType.UNPACK, true, false, false, true, "sub.xml");

      mtsp.setMatchers(Arrays.asList(topAndChildren, directTop, justChildren));
      return mtsp;
   }

   protected ModificationTypeMatcher createTempMatcher(
         ModificationType type,
         boolean metadataOnly,
         boolean checkChildren,
         boolean topLevelOnly,
         boolean childrenOnly,
         String... paths)
   {
      FileModificationTypeMatcher matcher = new FileModificationTypeMatcher(paths);
      matcher.setModificationType(type);
      matcher.setMetadataOnly(metadataOnly);
      matcher.setCheckChildren(checkChildren);
      matcher.setTopLevelOnly(topLevelOnly);
      matcher.setChildrenOnly(childrenOnly);
      return matcher;
   }

   protected VFSDeploymentContext getTopDeploymentContext(DeployerClient main, String name)
   {
      MainDeployerInternals mdi = (MainDeployerInternals)main;
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
         assertTrue("Should be temp", root.isDirectory());
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

      VFSDeployment deployment = createDeployment("/structureprocessor", "childmod.jar");
      main.deploy(deployment);
      try
      {
         VFSDeploymentContext vdc = getTopDeploymentContext(main, "childmod.jar");
         VirtualFile root = vdc.getRoot();
         VirtualFile file = root.getChild("tempchild.jar");
         try
         {
            assertTrue("Should be temp", file.isDirectory());
         }
         finally
         {
            Automounter.cleanup(root);
         }
      }
      finally
      {
         main.undeploy(deployment);
      }
   }
}