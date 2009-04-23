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
package org.jboss.test.deployers.vfs.redeploy.test;

import java.lang.reflect.Method;

import junit.framework.Test;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.DeploymentState;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.deployers.vfs.spi.client.VFSDeployment;
import org.jboss.deployers.vfs.plugins.structure.jar.JARStructure;
import org.jboss.deployers.client.spi.DeployerClient;
import org.jboss.test.deployers.BaseDeployersVFSTest;
import org.jboss.virtual.VFS;
import org.jboss.virtual.VirtualFile;
import org.jboss.virtual.plugins.cache.LRUVFSCache;
import org.jboss.virtual.spi.VirtualFileHandler;
import org.jboss.virtual.spi.cache.VFSCacheFactory;

/**
 * Tests that a deployer sees the same underlying child during redeployment.
 *
 * See JBAS-6715
 *
 * @author Jason T. Greene
 * @author <a href="ales.justin@jboss.org">Ales Justin</a>
 */
public class VFSRedeployTestCase extends BaseDeployersVFSTest
{
   /** The deployer */
   private VerifyVirtualFileDeployer deployer = new VerifyVirtualFileDeployer();

   public VFSRedeployTestCase(String name) throws Throwable
   {
      super(name);
   }

   public static Test suite()
   {
      return suite(VFSRedeployTestCase.class);
   }

   public void setUp() throws Exception
   {
      super.setUp();

      LRUVFSCache cache = new LRUVFSCache(50, 100);
      cache.start();
      VFSCacheFactory.setInstance(cache);
   }

   public void tearDown() throws Exception
   {
      VFSCacheFactory.setInstance(null);

      super.tearDown();
   }

   @SuppressWarnings("deprecation")
   public static class VerifyVirtualFileDeployer extends org.jboss.deployers.vfs.spi.deployer.AbstractVFSRealDeployer
   {
      public void deploy(VFSDeploymentUnit unit) throws DeploymentException
      {
         VirtualFile deploymentRoot = unit.getRoot();
         VirtualFile registryRoot;
         try
         {
            registryRoot = VFS.getRoot(deploymentRoot.toURL());
         }
         catch (Exception e)
         {
            throw new RuntimeException(e);
         }

         VirtualFileHandler deploymentHandler = getHandler(deploymentRoot);
         VirtualFileHandler registryHandler = getHandler(registryRoot);
         assertSame(deploymentHandler, registryHandler);
      }

      public VirtualFileHandler getHandler(VirtualFile file)
      {
         try
         {
            Method method = VirtualFile.class.getDeclaredMethod("getHandler");
            method.setAccessible(true);
            return (VirtualFileHandler)method.invoke(file);
         }
         catch (Exception e)
         {
            throw new RuntimeException(e);
         }
      }
   }

   public void testDeploy() throws Exception
   {
      DeployerClient main = createMainDeployer(deployer);
      addStructureDeployer(main, new JARStructure());

      VFSDeployment context = createDeployment("/bean", "/toplevel/test.jar");
      try
      {
         assertAddDeployment(main, context);
      }
      finally
      {
         assertUndeploy(main, context);
      }
   }

   public void testRedeploy() throws Exception
   {
      DeployerClient main = createMainDeployer(deployer);
      addStructureDeployer(main, new JARStructure());

      VFSDeployment context = createDeployment("/bean", "/toplevel/test.jar");
      try
      {
         assertAddDeployment(main, context);
         assertAddDeployment(main, context);
      }
      finally
      {
         assertUndeploy(main, context);
      }
   }

   protected void assertAddDeployment(DeployerClient main, VFSDeployment context) throws Exception
   {
      addDeployment(main, context);
      assertEquals("Should be Deployed " + context, DeploymentState.DEPLOYED, main.getDeploymentState(context.getName()));
   }
}
