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
package org.jboss.test.deployers.vfs.deployer.bean.test;

import java.lang.reflect.Method;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.deployer.kernel.BeanDeployer;
import org.jboss.deployers.vfs.deployer.kernel.BeanMetaDataDeployer;
import org.jboss.deployers.vfs.deployer.kernel.KernelDeploymentDeployer;
import org.jboss.deployers.vfs.spi.client.VFSDeployment;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.kernel.Kernel;
import org.jboss.test.deployers.support.TCCLClassLoaderDeployer;
import org.jboss.test.deployers.vfs.deployer.AbstractDeployerUnitTest;
import org.jboss.virtual.VFS;
import org.jboss.virtual.VFSUtils;
import org.jboss.virtual.VirtualFile;
import org.jboss.virtual.plugins.cache.LRUVFSCache;
import org.jboss.virtual.spi.VirtualFileHandler;
import org.jboss.virtual.spi.cache.VFSCacheFactory;
import org.jboss.virtual.spi.cache.helpers.NoopVFSCache;

/**
 * Tests that a deployer sees the same underlying child during redeployment.
 *
 * See JBAS-6715
 *
 * @author Jason T. Greene
 */
public class VFSRedeployTestCase extends AbstractDeployerUnitTest
{
   public static Test suite()
   {
      return new TestSuite(VFSRedeployTestCase.class);
   }

   public void setUp() throws Exception
   {
      LRUVFSCache cache = new LRUVFSCache(50, 100);
      cache.start();
      VFSCacheFactory.setInstance(cache);

      super.setUp();
   }

   public void tearDown() throws Exception
   {
      VFSCacheFactory.setInstance(null);
   }

   public static class VerifyVirtualFileDeployer extends AbstractDeployer {

      public void deploy(DeploymentUnit unit) throws DeploymentException
      {
         VirtualFile deploymentRoot = ((VFSDeploymentUnit)unit).getRoot();
         VirtualFile registryRoot;
         try
         {
            registryRoot = VFS.getRoot(deploymentRoot.toURL());
         }
         catch (Exception e)
         {
            throw new RuntimeException(e);
         }

         assertSame(getHandler(deploymentRoot), getHandler(registryRoot));
      }

      public VirtualFileHandler getHandler(VirtualFile file)
      {
         try
         {
            Method method = file.getClass().getDeclaredMethod("getHandler");
            method.setAccessible(true);
            return (VirtualFileHandler)method.invoke(file);
         }
         catch (Exception e)
         {
            throw new RuntimeException(e);
         }

      }

   }

   public VFSRedeployTestCase(String name) throws Throwable
   {
      super(name);
   }

   protected void addDeployers(Kernel kernel)
   {
      BeanDeployer beanDeployer = new BeanDeployer();
      KernelDeploymentDeployer kernelDeploymentDeployer = new KernelDeploymentDeployer();
      BeanMetaDataDeployer beanMetaDataDeployer = new BeanMetaDataDeployer(kernel);
      addDeployer(main, new TCCLClassLoaderDeployer());
      addDeployer(main, beanDeployer);
      addDeployer(main, kernelDeploymentDeployer);
      addDeployer(main, beanMetaDataDeployer);
      addDeployer(main, new VerifyVirtualFileDeployer());
   }

   public void testDeploy() throws Exception
   {
      VFSDeployment context = createDeployment("/bean", "/toplevel/test.jar");

      assertDeploy(context);
      assertUndeploy(context);
   }

   public void testRedeploy() throws Exception
   {
      VFSDeployment context = createDeployment("/bean", "/toplevel/test.jar");

      assertDeploy(context);
      assertDeploy(context);
      assertUndeploy(context);
   }

}
