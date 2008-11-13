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
package org.jboss.test.deployers.vfs.annotations.test;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import org.jboss.deployers.plugins.annotations.AbstractScanningMetaData;
import org.jboss.deployers.spi.annotations.PathMetaData;
import org.jboss.deployers.spi.annotations.ScanningMetaData;
import org.jboss.deployers.spi.annotations.PathEntryMetaData;
import org.jboss.deployers.vfs.plugins.annotations.ScanningMetaDataDeployer;
import org.jboss.deployers.vfs.plugins.structure.AbstractVFSDeploymentContext;
import org.jboss.deployers.vfs.plugins.structure.AbstractVFSDeploymentUnit;
import org.jboss.deployers.vfs.spi.deployer.SchemaResolverDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentContext;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.test.deployers.BaseDeployersVFSTest;
import org.jboss.virtual.VirtualFile;

/**
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class ScanningMetaDataTestCase extends BaseDeployersVFSTest
{
   public ScanningMetaDataTestCase(String name)
   {
      super(name);
   }

   public static Test suite()
   {
      return suite(ScanningMetaDataTestCase.class);
   }

   public void testSMDRead() throws Exception
   {
      SchemaResolverDeployer<AbstractScanningMetaData> deployer = new ScanningMetaDataDeployer();
      deployer.create();
      try
      {
         VirtualFile file = getVirtualFile("/scanning", "smoke");
         VFSDeploymentContext deployment = new AbstractVFSDeploymentContext(file, "");
         deployment.setMetaDataPath(Collections.singletonList("META-INF"));
         VFSDeploymentUnit unit = new AbstractVFSDeploymentUnit(deployment);
         deployer.deploy(unit);
         try
         {
            ScanningMetaData metaData = unit.getAttachment(ScanningMetaData.class);
            assertNotNull(metaData);
            List<PathMetaData> paths = metaData.getPaths();
            assertNotNull(paths);
            assertEquals(2, paths.size());

            PathMetaData pmd = paths.get(0);
            assertNotNull(pmd);
            assertEquals("myejbs.jar", pmd.getPathName());
            Set<PathEntryMetaData> includes = pmd.getIncludes();
            assertNotNull(includes);
            assertEquals(1, includes.size());
            PathEntryMetaData pemd = includes.iterator().next();
            assertNotNull(pemd);
            assertEquals("com.acme.foo", pemd.getName());
            Set<PathEntryMetaData> excludes = pmd.getExcludes();
            assertNotNull(excludes);
            pemd = excludes.iterator().next();
            assertNotNull(pemd);
            assertEquals("com.acme.foo.bar", pemd.getName());
            assertEquals(1, excludes.size());

            pmd = paths.get(1);
            assertNotNull(pmd);
            assertEquals("my.war/WEB-INF/classes", pmd.getPathName());
            includes = pmd.getIncludes();
            assertNotNull(includes);
            assertEquals(1, includes.size());
            pemd = includes.iterator().next();
            assertNotNull(pemd);
            assertEquals("com.acme.foo", pemd.getName());
            assertNull(pmd.getExcludes());
         }
         finally
         {
            deployer.undeploy(unit);
         }
      }
      finally
      {
         deployer.destroy();
      }
   }
}
