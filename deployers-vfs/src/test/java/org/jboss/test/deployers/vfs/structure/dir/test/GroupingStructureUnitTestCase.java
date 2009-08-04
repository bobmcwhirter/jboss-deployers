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
package org.jboss.test.deployers.vfs.structure.dir.test;

import junit.framework.Test;
import org.jboss.deployers.vfs.plugins.structure.dir.GroupingStructure;
import org.jboss.deployers.vfs.plugins.structure.jar.JARStructure;
import org.jboss.deployers.vfs.spi.client.VFSDeployment;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentContext;
import org.jboss.test.deployers.vfs.structure.ear.support.MockEarStructureDeployer;
import org.jboss.virtual.VirtualFileFilter;
import org.jboss.virtual.VirtualFile;

/**
 * Test grouping examples.
 * Substitute for legacy DirectoryStrucutre.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class GroupingStructureUnitTestCase extends SubDirectoryStructureTest
{
   public GroupingStructureUnitTestCase(String name)
   {
      super(name);
   }

   public static Test suite()
   {
      return suite(GroupingStructureUnitTestCase.class);
   }

   protected boolean shouldFlattenContext()
   {
      return true;
   }

   protected VFSDeploymentContext determineStructure(VFSDeployment deployment) throws Exception
   {
      GroupingStructure gs = new GroupingStructure();
      VirtualFileFilter top = new VirtualFileFilter()
      {
         public boolean accepts(VirtualFile file)
         {
            return file.getName().endsWith(".sar");
         }
      };
      gs.setShortCircuitFilter(top);
      gs.addGroup("lib");
      gs.addGroup("lib/nested");
      return determineStructureWithStructureDeployers(deployment, new MockEarStructureDeployer(), new JARStructure(), gs);
   }
}