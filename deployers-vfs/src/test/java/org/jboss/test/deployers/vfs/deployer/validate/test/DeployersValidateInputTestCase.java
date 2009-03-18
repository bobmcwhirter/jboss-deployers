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
package org.jboss.test.deployers.vfs.deployer.validate.test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.deployer.kernel.Properties2BeansDeployer;
import org.jboss.deployers.vfs.plugins.structure.AbstractVFSDeploymentContext;
import org.jboss.deployers.vfs.spi.deployer.AbstractVFSParsingDeployer;
import org.jboss.deployers.vfs.spi.deployer.SchemaResolverDeployer;
import org.jboss.test.BaseTestCase;
import org.jboss.test.deployers.vfs.deployer.nonmetadata.support.MockBshDeployer;
import org.jboss.test.deployers.vfs.deployer.validate.support.MyVFSDeploymentContext;
import org.jboss.test.deployers.vfs.deployer.validate.support.MyVirtualFile;
import org.jboss.test.deployers.vfs.deployer.validate.support.TestXmlDeployer;
import org.jboss.virtual.VirtualFile;
import org.jboss.xb.binding.JBossXBException;

/**
 * Validate deployers.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class DeployersValidateInputTestCase extends BaseTestCase
{
   public DeployersValidateInputTestCase(String name)
   {
      super(name);
   }

   public static Test suite()
   {
      return suite(DeployersValidateInputTestCase.class);
   }

   public void testNullStream() throws Exception
   {
      // this one needs to be created first
      TestXmlDeployer xmlDeployer = new TestXmlDeployer();
      xmlDeployer.create();

      Map<AbstractVFSParsingDeployer<?>, Class<? extends Exception>> map = new HashMap<AbstractVFSParsingDeployer<?>, Class<? extends Exception>>();
      map.put(new Properties2BeansDeployer(), IOException.class);
      map.put(new MockBshDeployer(), IOException.class);
      map.put(xmlDeployer, RuntimeException.class);
      map.put(new SchemaResolverDeployer<Object>(Object.class), JBossXBException.class);

      VirtualFile root = new MyVirtualFile();
      AbstractVFSDeploymentContext context = new MyVFSDeploymentContext(root, "");
      DeploymentUnit unit = context.getDeploymentUnit();

      for(AbstractVFSParsingDeployer<?> deployer : map.keySet())
      {
         // set name to "" to match in deployment
         deployer.setName("");
         try
         {
            deployer.deploy(unit);
            fail("Should not be here: " + deployer);
         }
         catch(Exception e)
         {
            assertInstanceOf(e, DeploymentException.class);
            Throwable cause = e.getCause();
            if (map.get(deployer).isInstance(cause) == false)
            {
               fail("Illegal exception cause: " + cause);
            }
         }
      }
   }
}
