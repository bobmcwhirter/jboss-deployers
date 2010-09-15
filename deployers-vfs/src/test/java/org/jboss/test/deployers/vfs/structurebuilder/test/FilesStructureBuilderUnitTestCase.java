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
package org.jboss.test.deployers.vfs.structurebuilder.test;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.attachments.MutableAttachments;
import org.jboss.deployers.structure.spi.DeploymentContext;
import org.jboss.deployers.vfs.plugins.structure.VFSStructuralDeployersImpl;
import org.jboss.deployers.vfs.plugins.structure.VFSStructureBuilder;
import org.jboss.deployers.vfs.plugins.structure.explicit.DeclaredStructure;
import org.jboss.deployers.vfs.plugins.structure.jar.JARStructure;
import org.jboss.deployers.vfs.spi.client.VFSDeployment;
import org.jboss.deployers.vfs.spi.structure.StructureDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentContext;
import org.jboss.test.deployers.vfs.structurebuilder.support.ChildFileStructure;
import org.jboss.test.deployers.vfs.structurebuilder.support.TestStructuralDeployers;

/**
 * FilesStructureBuilderUnitTestCase.
 *
 * @author <a href="ales.justin@jboss.org">Ales Justin</a>
 */
public class FilesStructureBuilderUnitTestCase extends VFSStructureBuilderUnitTestCase
{
   public static Test suite()
   {
      return new TestSuite(FilesStructureBuilderUnitTestCase.class);
   }

   public FilesStructureBuilderUnitTestCase(String name)
   {
      super(name);
   }

   @Override
   protected DeploymentContext build(Deployment deployment) throws DeploymentException
   {
      return deployment.getPredeterminedManagedObjects().getAttachment(DeploymentContext.class);
   }

   protected Deployment createDeployment(StructureDeployer... deployers) throws Exception
   {
      VFSDeployment deployment = createDeployment();
      VFSDeploymentContext context = determineStructureWithStructureDeployers(deployment, deployers);
      ((MutableAttachments)deployment.getPredeterminedManagedObjects()).addAttachment(DeploymentContext.class, context);
      return deployment;
   }

   protected VFSDeploymentContext determineStructureWithStructureDeployers(VFSDeployment deployment, StructureDeployer... deployers) throws Exception
   {
      VFSStructuralDeployersImpl structuralDeployers = new TestStructuralDeployers();
      VFSStructureBuilder builder = getStructureBuilder();
      structuralDeployers.setStructureBuilder(builder);

      for (StructureDeployer deployer : deployers)
         structuralDeployers.addDeployer(deployer);

      return (VFSDeploymentContext) structuralDeployers.determineStructure(deployment);
   }

   protected Deployment createDefaultDeployment() throws Exception
   {
      return createDeployment(new DeclaredStructure(), new ChildFileStructure(), new JARStructure());
   }

   @Override
   protected Deployment createSimple() throws Exception
   {
      return createDefaultDeployment();
   }

   @Override
   protected Deployment createSimpleWithAttachment() throws Exception
   {
      return createDefaultDeployment();
   }

   @Override
   protected Deployment createOneChild() throws Exception
   {
      return createDefaultDeployment();
   }

   @Override
   protected Deployment createManyChildren() throws Exception
   {
      return createDefaultDeployment();
   }

   @Override
   protected Deployment createMetaDataLocation() throws Exception
   {
      return createDefaultDeployment();
   }

   @Override
   protected Deployment createClasspathEntries() throws Exception
   {
      return createDefaultDeployment();
   }

   @Override
   protected Deployment createClasspathEntriesWithExternalJar() throws Exception
   {
      return createDefaultDeployment();
   }

   @Override
   public void testOrderedChildren() throws Exception
   {
      // ignore this test
   }
}
