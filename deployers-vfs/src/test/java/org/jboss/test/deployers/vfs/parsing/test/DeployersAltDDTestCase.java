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
package org.jboss.test.deployers.vfs.parsing.test;

import junit.framework.Test;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.plugins.structure.AbstractVFSDeploymentContext;
import org.jboss.deployers.vfs.spi.deployer.AbstractVFSParsingDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.test.BaseTestCase;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;

/**
 * alt-dd tests.
 *
 * @author <a href="mailto:alex@jboss.com">Alexey Loubyansky</a>
 */
public class DeployersAltDDTestCase extends BaseTestCase
{
   public static Test suite()
   {
      return suite(DeployersAltDDTestCase.class);
   }

   public DeployersAltDDTestCase(String test)
   {
      super(test);
   }

   public void testAltDD() throws Exception
   {
      final VirtualFile altDD = getMockVF();
      AbstractVFSDeploymentContext context = new AbstractVFSDeploymentContext();
      DeploymentUnit unit = context.getDeploymentUnit();
      unit.addAttachment(Object.class.getName() + ".altDD", altDD);

      final boolean pickedUpAltDD[] = new boolean[1];
      AbstractVFSParsingDeployer<Object> deployer = new AbstractVFSParsingDeployer<Object>(Object.class)
      {
         @Override
         protected Object parse(VFSDeploymentUnit unit, VirtualFile file, Object root) throws Exception
         {
            if(file == altDD)
               pickedUpAltDD[0] = true;
            return null;
         }
      };
      
      deployer.setName("dd");
      deployer.deploy(unit);
      
      assertTrue("Picked up alt-dd", pickedUpAltDD[0]);
   }

   private static VirtualFile getMockVF()
   {
      return VFS.getChild("/mock/file");
   }
}
