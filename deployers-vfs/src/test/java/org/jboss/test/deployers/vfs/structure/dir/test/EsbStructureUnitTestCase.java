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
import org.jboss.deployers.vfs.plugins.structure.war.WARStructure;
import org.jboss.deployers.vfs.spi.client.VFSDeployment;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentContext;
import org.jboss.test.deployers.vfs.structure.AbstractStructureTest;
import org.jboss.vfs.VirtualFile;
import org.jboss.vfs.VirtualFileFilter;

/**
 * Esb example test.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class EsbStructureUnitTestCase extends AbstractStructureTest
{
   public EsbStructureUnitTestCase(String name)
   {
      super(name);
   }

   public static Test suite()
   {
      return suite(EsbStructureUnitTestCase.class);
   }
   
   protected VFSDeploymentContext determineStructure(VFSDeployment deployment) throws Exception
   {
      GroupingStructure gs = new GroupingStructure();
      VirtualFileFilter top = new VirtualFileFilter()
      {
         public boolean accepts(VirtualFile file)
         {
            return file.getName().endsWith(".esb");
         }
      };
      gs.setShortCircuitFilter(top);
      gs.addGroup("jars");
      gs.addGroup("wars");
      return determineStructureWithStructureDeployers(deployment, new JARStructure(), new WARStructure(), gs);
   }

   public void testEsbStructure() throws Throwable
   {
      VFSDeploymentContext context = assertDeploy("/structure/dir", "beve.esb");
      assertChildContexts(context, "jars/j1.jar", "wars/w1.war");
   }
}