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

import java.util.Collections;

import junit.framework.Test;
import org.jboss.deployers.spi.deployer.Deployer;
import org.jboss.deployers.spi.structure.ContextInfo;
import org.jboss.deployers.spi.structure.StructureMetaData;
import org.jboss.deployers.vfs.plugins.structure.AbstractVFSDeploymentContext;
import org.jboss.deployers.vfs.plugins.structure.AbstractVFSDeploymentUnit;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentContext;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.test.deployers.BaseDeployersVFSTest;
import org.jboss.test.deployers.vfs.deployer.validate.support.StructureOMFDeployer;
import org.jboss.virtual.VirtualFile;

/**
 * Validate omfd deployer.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class ObjectMFDTestCase extends BaseDeployersVFSTest
{
   public ObjectMFDTestCase(String name)
   {
      super(name);
   }

   public static Test suite()
   {
      return suite(ObjectMFDTestCase.class);
   }

   public void testDeployer() throws Exception
   {
      VirtualFile file = getVirtualFile("/structure/explicit", "complex.deployer");
      VFSDeploymentContext deployment = new AbstractVFSDeploymentContext(file, "");
      deployment.setMetaDataPath(Collections.singletonList("META-INF"));
      VFSDeploymentUnit unit = new AbstractVFSDeploymentUnit(deployment);
      Deployer deployer = new StructureOMFDeployer();

      deployer.deploy(unit);
      try
      {
         StructureMetaData metaData = unit.getAttachment(StructureMetaData.class);
         assertNotNull(metaData);
      }
      finally
      {
         deployer.undeploy(unit);
      }
   }

   public void testComparator() throws Exception
   {
      VirtualFile file = getVirtualFile("/structure/explicit", "comparator.jar");
      VFSDeploymentContext deployment = new AbstractVFSDeploymentContext(file, "");
      deployment.setMetaDataPath(Collections.singletonList("META-INF"));
      VFSDeploymentUnit unit = new AbstractVFSDeploymentUnit(deployment);
      Deployer deployer = new StructureOMFDeployer();

      deployer.deploy(unit);
      try
      {
         StructureMetaData metaData = unit.getAttachment(StructureMetaData.class);
         assertNotNull(metaData);
         assertComparator(metaData, "", "org.jboss.test.deployment.test.SomeDeploymentComparatorTop");
         assertComparator(metaData, "sub.jar", "org.jboss.test.deployment.test.SomeDeploymentComparatorSub");
         assertComparator(metaData, "x.war", "org.jboss.test.deployment.test.SomeDeploymentComparatorX");
      }
      finally
      {
         deployer.undeploy(unit);
      }
   }

   protected void assertComparator(StructureMetaData metaData, String path, String comparator)
   {
      ContextInfo ci = metaData.getContext(path);
      assertNotNull(ci);
      assertEquals(comparator, ci.getComparatorClassName());
   }
}