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
package org.jboss.test.deployers.vfs.structure.modified.test;

import org.jboss.deployers.structure.spi.main.MainDeployerStructure;
import org.jboss.deployers.vfs.spi.structure.modified.StructureModificationChecker;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.test.deployers.vfs.webbeans.test.AbstractWebBeansTest;
import org.jboss.virtual.AssembledDirectory;
import org.jboss.virtual.VirtualFileFilter;

/**
 * Test StructureModificationChecker.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class StructureModificationTest extends AbstractWebBeansTest
{
   protected StructureModificationTest(String name)
   {
      super(name);
   }

   protected abstract StructureModificationChecker createStructureModificationChecker(MainDeployerStructure mainDeployerStructure, VirtualFileFilter filter);

   protected abstract VirtualFileFilter createFilter();

   public void testDoBasicTest() throws Exception
   {
      AssembledDirectory ear = createBasicEar();

      MainDeployerStructure mainDeployer = assertBean("MainDeployer", MainDeployerStructure.class);
      VirtualFileFilter filter = createFilter();      
      StructureModificationChecker checker = createStructureModificationChecker(mainDeployer, filter);

      VFSDeploymentUnit deploymentUnit = assertDeploy(ear);
      try
      {
         testStructureModified(checker, deploymentUnit);
      }
      finally
      {
         undeploy(deploymentUnit);
      }
   }

   protected abstract void testStructureModified(StructureModificationChecker checker, VFSDeploymentUnit deploymentUnit) throws Exception;
}
