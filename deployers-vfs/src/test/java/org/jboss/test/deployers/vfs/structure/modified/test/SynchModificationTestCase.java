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

import junit.framework.Test;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.deployers.vfs.spi.structure.modified.OverrideSynchAdapter;
import org.jboss.deployers.vfs.spi.structure.modified.StructureModificationChecker;
import org.jboss.deployers.vfs.spi.structure.modified.SynchAdapter;
import org.jboss.test.deployers.vfs.structure.modified.support.XmlIncludeVirtualFileFilter;
import org.jboss.virtual.VirtualFile;
import org.jboss.virtual.VirtualFileFilter;

/**
 * Test file synch.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class SynchModificationTestCase extends AbstractSynchTest
{
   public SynchModificationTestCase(String name)
   {
      super(name);
   }

   public static Test suite()
   {
      return suite(SynchModificationTestCase.class);
   }

   protected VirtualFileFilter createFilter()
   {
      return new XmlIncludeVirtualFileFilter();
   }

   protected VirtualFileFilter createRecurseFilter()
   {
      return new VirtualFileFilter()
      {
         public boolean accepts(VirtualFile file)
         {
            String path = file.getPathName();
            // only wars, but not its classes
            return (path.contains(".war") && path.contains("/WEB-INF") == false);
         }
      };
   }

   protected SynchAdapter createSynchAdapter()
   {
      return new OverrideSynchAdapter();
   }

   public void testWAR() throws Exception
   {
      VFSDeploymentUnit deploymentUnit = assertDeploy("/synch/war", "simple.war");
      try
      {
         VirtualFile root = deploymentUnit.getRoot();
         StructureModificationChecker checker = createStructureModificationChecker();
         assertFalse(checker.hasStructureBeenModified(root));
      }
      finally
      {
         undeploy(deploymentUnit);
      }
   }

   public void testEAR() throws Exception
   {
      VFSDeploymentUnit deploymentUnit = assertDeploy("/synch/ear", "simple.ear");
      try
      {
         VirtualFile root = deploymentUnit.getRoot();
         StructureModificationChecker checker = createStructureModificationChecker();
         assertFalse(checker.hasStructureBeenModified(root));
      }
      finally
      {
         undeploy(deploymentUnit);
      }
   }
}