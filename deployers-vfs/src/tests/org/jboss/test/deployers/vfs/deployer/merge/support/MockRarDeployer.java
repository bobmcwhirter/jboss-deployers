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
package org.jboss.test.deployers.vfs.deployer.merge.support;

import org.jboss.deployers.vfs.spi.deployer.JBossExtensionDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;

/**
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class MockRarDeployer extends JBossExtensionDeployer<RarMetaData, JBossRarMetaData, RarDeploymentMetaData>
{
   public MockRarDeployer()
   {
      super(RarDeploymentMetaData.class, "rar.xml", RarMetaData.class, "jboss-rar.xml", JBossRarMetaData.class);
   }

   protected RarDeploymentMetaData mergeMetaData(VFSDeploymentUnit unit, RarMetaData spec, JBossRarMetaData jboss) throws Exception
   {
      RarDeploymentMetaData deployment = new RarDeploymentMetaData();
      if (spec != null)
      {
         deployment.setAttribute(spec.getAttribute());
         deployment.setElement(spec.getElement());
      }
      if (jboss != null)
      {
         if (jboss.getAttribute() != null)
            deployment.setAttribute(jboss.getAttribute());
         if (jboss.getElement() != null)
            deployment.setElement(jboss.getElement());
      }
      return deployment;
   }
}