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
package org.jboss.test.deployers.vfs.deploymentfactory.test;

import java.io.IOException;
import java.net.URL;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.vfs.plugins.client.AbstractVFSDeployment;
import org.jboss.deployers.vfs.spi.client.VFSDeployment;
import org.jboss.deployers.vfs.spi.client.VFSDeploymentFactory;
import org.jboss.test.deployers.deploymentfactory.AbstractDeploymentFactoryTest;
import org.jboss.virtual.VFS;
import org.jboss.virtual.VirtualFile;

/**
 * VFSDeploymentFactoryUnitTestCase.
 * 
 * @author <a href="adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class VFSDeploymentFactoryUnitTestCase extends AbstractDeploymentFactoryTest
{
   public static Test suite()
   {
      return new TestSuite(VFSDeploymentFactoryUnitTestCase.class);
   }
   
   public VFSDeploymentFactoryUnitTestCase(String name)
   {
      super(name);
   }

   public void testConstruct()
   {
      VFSDeploymentFactory factory = createDeploymentFactory();
      VirtualFile root = getVirtualFile("/dummy");
      VFSDeployment deployment = factory.createVFSDeployment(root);
      assertEquals(root, deployment.getRoot());
   }

   public void testConstructErrors()
   {
      VFSDeploymentFactory factory = createDeploymentFactory();
      try
      {
         factory.createVFSDeployment(null);
         fail("Should not be here!");
      }
      catch (Exception e)
      {
         checkThrowable(IllegalArgumentException.class, e);
      }
   }
   
   @Override
   protected VFSDeploymentFactory createDeploymentFactory()
   {
      return VFSDeploymentFactory.getInstance();
   }

   @Override
   protected Deployment createDeployment()
   {
      return new AbstractVFSDeployment(getVirtualFile("/dummy"));
   }
   
   protected VirtualFile getVirtualFile(String resourceName)
   {
      URL resource = null;
      try
      {
         resource = getResource("/dummy");
         assertNotNull(resource);
         return VFS.getRoot(resource);
      }
      catch (IOException e)
      {
         throw new RuntimeException("Failed to get resource: " + resource, e);
      }
   }
}
