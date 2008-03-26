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
package org.jboss.test.deployers.vfs.xb.test;

import java.net.URL;
import java.util.Collections;

import junit.framework.Test;
import org.jboss.deployers.vfs.plugins.structure.AbstractVFSDeploymentContext;
import org.jboss.deployers.vfs.plugins.structure.AbstractVFSDeploymentUnit;
import org.jboss.deployers.vfs.spi.deployer.SchemaResolverDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentContext;
import org.jboss.test.deployers.vfs.xb.XBDeployersTest;
import org.jboss.virtual.VFS;
import org.jboss.virtual.VirtualFile;

/**
 * Abstract schema resolver JBossXB test.
 *
 * @param <T> exact output type
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public abstract class AbstractSchemaResolverXBTest<T> extends XBDeployersTest
{
   public AbstractSchemaResolverXBTest(String name)
   {
      super(name);
   }

   public static Test suite()
   {
      return suite(SchemaResolverXBPackageTestCase.class);
   }

   protected abstract Class<T> getOutput();

   protected abstract String getSuffix();

   protected abstract String getName(T metadata);

   public void testJBossXBParser() throws Throwable
   {
      SchemaResolverDeployer<?> deployer = assertBean("deployer", SchemaResolverDeployer.class);
      assertEquals(getOutput(), deployer.getOutput());
      assertEquals(getSuffix(), deployer.getSuffix());
      assertTrue(deployer.isRegisterWithJBossXB());

      String common = "/org/jboss/test/deployers/vfs/xb/test";
      URL url = getClass().getResource(common);
      assertNotNull(url);
      VirtualFile file = VFS.getRoot(url);
      assertNotNull(file);

      VFSDeploymentContext context = new AbstractVFSDeploymentContext(file, "");
      context.setMetaDataLocations(Collections.singletonList(file));
      AbstractVFSDeploymentUnit unit = new AbstractVFSDeploymentUnit(context);

      deployer.deploy(unit);
      try
      {
         T metaData = unit.getAttachment(getOutput());
         assertNotNull(metaData);
         assertEquals("mymetadata", getName(metaData));
      }
      finally
      {
         deployer.undeploy(unit);
      }
   }
}