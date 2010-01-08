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
package org.jboss.test.deployers.vfs.matchers.test;

import junit.framework.Test;

import org.jboss.deployers.client.spi.DeployerClient;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.spi.structure.MetaDataTypeFilter;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.plugins.structure.explicit.DeclaredStructure;
import org.jboss.test.deployers.BaseDeployersVFSTest;
import org.jboss.test.deployers.vfs.matchers.support.FeedbackDeployer;

/**
 * MetaDataType filter tests.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class MetaDataTypeFilterTestCase extends BaseDeployersVFSTest
{
   public MetaDataTypeFilterTestCase(String name)
   {
      super(name);
   }

   public static Test suite()
   {
      return suite(MetaDataTypeFilterTestCase.class);
   }

   public void testAlternative() throws Exception
   {
      FeedbackDeployer fbd = new FeedbackDeployer();
      fbd.setAllowMultipleFiles(true);
      fbd.setSuffix(".txt");
      fbd.setFilter(MetaDataTypeFilter.ALL);

      DeployerClient main = createMainDeployer(fbd);
      addStructureDeployer(main, new DeclaredStructure());

      Deployment deployment = createDeployment("/matchers", "mdtf");
      main.deploy(deployment);

      assertEquals(3, fbd.getFiles().size());
   }

   public void testFilterFromUnit() throws Exception
   {
      AbstractDeployer pre = new AbstractDeployer()
      {
         public void deploy(DeploymentUnit unit) throws DeploymentException
         {
            unit.addAttachment(MetaDataTypeFilter.class, MetaDataTypeFilter.ALL);
         }
      };
      pre.setStage(DeploymentStages.PRE_PARSE);
      FeedbackDeployer fbd = new FeedbackDeployer();
      fbd.setAllowMultipleFiles(true);
      fbd.setSuffix(".txt");
      fbd.setFilter(null); // disable default

      DeployerClient main = createMainDeployer(fbd, pre);
      addStructureDeployer(main, new DeclaredStructure());

      Deployment deployment = createDeployment("/matchers", "mdtf");
      main.deploy(deployment);

      assertEquals(3, fbd.getFiles().size());
   }
}