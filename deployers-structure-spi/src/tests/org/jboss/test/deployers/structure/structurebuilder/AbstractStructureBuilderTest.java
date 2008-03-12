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
package org.jboss.test.deployers.structure.structurebuilder;

import java.util.List;
import java.util.Map;

import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.client.spi.DeploymentFactory;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.attachments.MutableAttachments;
import org.jboss.deployers.spi.attachments.PredeterminedManagedObjectAttachments;
import org.jboss.deployers.spi.structure.ContextInfo;
import org.jboss.deployers.spi.structure.StructureMetaData;
import org.jboss.deployers.spi.structure.StructureMetaDataFactory;
import org.jboss.deployers.structure.spi.DeploymentContext;
import org.jboss.test.BaseTestCase;

/**
 * AbstractStructureBuilderTest.
 * 
 * @author <a href="ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public abstract class AbstractStructureBuilderTest extends BaseTestCase
{
   public AbstractStructureBuilderTest(String name)
   {
      super(name);
   }
   
   protected abstract DeploymentContext build(Deployment deployment) throws DeploymentException;
   
   protected abstract Deployment createDeployment();
   
   protected abstract DeploymentFactory getDeploymentFactory();

   protected abstract String getDeploymentName();

   protected Deployment createDeployment(DeploymentFactory factory)
   {
      Deployment deployment = createDeployment();
      factory.addContext(deployment, "");
      return deployment;
   }

   public void testSimple() throws Exception
   {
      Deployment deployment = createSimple();
      DeploymentContext context = build(deployment);
      checkDeployment(context, deployment);
   }

   protected Deployment createSimple() throws Exception
   {
      Deployment deployment = createDeployment();
      StructureMetaData structure = StructureMetaDataFactory.createStructureMetaData();
      MutableAttachments attachments = (MutableAttachments) deployment.getPredeterminedManagedObjects();
      attachments.addAttachment(StructureMetaData.class, structure);

      DeploymentFactory factory = getDeploymentFactory();
      factory.addContext(deployment, "");
      return deployment;
   }

   public void testSimpleWithAttachment() throws Exception
   {
      Deployment deployment = createSimpleWithAttachment();
      DeploymentContext context = build(deployment);
      checkDeployment(context, deployment);
   }

   protected Deployment createSimpleWithAttachment() throws Exception
   {
      Deployment deployment = createDeployment();
      StructureMetaData structure = StructureMetaDataFactory.createStructureMetaData();
      MutableAttachments attachments = (MutableAttachments) deployment.getPredeterminedManagedObjects();
      attachments.addAttachment(StructureMetaData.class, structure);
      attachments.addAttachment("test", "hello");

      DeploymentFactory factory = getDeploymentFactory();
      factory.addContext(deployment, "");
      return deployment;
   }

   public void testOneChild() throws Exception
   {
      Deployment deployment = createOneChild();
      DeploymentContext context = build(deployment);
      checkDeployment(context, deployment);
   }

   protected Deployment createOneChild() throws Exception
   {
      DeploymentFactory factory = getDeploymentFactory();
      Deployment deployment = createDeployment(factory);
      factory.addContext(deployment, "child1");
      return deployment;
   }

   public void testManyChildren() throws Exception
   {
      Deployment deployment = createManyChildren();
      DeploymentContext context = build(deployment);
      checkDeployment(context, deployment);
   }

   protected Deployment createManyChildren() throws Exception
   {
      DeploymentFactory factory = getDeploymentFactory();
      Deployment deployment = createDeployment(factory);
      factory.addContext(deployment, "child1");
      factory.addContext(deployment, "child2");
      factory.addContext(deployment, "child3");
      return deployment;
   }

   public void testMetaDataLocation() throws Exception
   {
      Deployment deployment = createMetaDataLocation();
      DeploymentContext context = build(deployment);
      checkDeployment(context, deployment);
   }

   protected Deployment createMetaDataLocation() throws Exception
   {
      DeploymentFactory factory = getDeploymentFactory();
      Deployment deployment = createDeployment();
      factory.addContext(deployment, "", ContextInfo.DEFAULT_METADATA_PATH, DeploymentFactory.createClassPath(""));
      return deployment;
   }

   public void testClasspathEntries() throws Exception
   {
      Deployment deployment = createClasspathEntries();
      DeploymentContext context = build(deployment);
      checkDeployment(context, deployment);
   }

   protected Deployment createClasspathEntries() throws Exception
   {
      DeploymentFactory factory = getDeploymentFactory();
      Deployment deployment = createDeployment();
      ContextInfo contextInfo = factory.addContext(deployment, "");
      contextInfo.addClassPathEntry(DeploymentFactory.createClassPathEntry("cp1.txt"));
      contextInfo.addClassPathEntry(DeploymentFactory.createClassPathEntry("cp2.txt"));
      return deployment;
   }

   protected void checkDeployment(DeploymentContext context, Deployment deployment) throws Exception
   {
      assertNotNull(context);
      
      assertEquals(deployment.getName(), context.getName());
      
      MutableAttachments attachments = (MutableAttachments) deployment.getPredeterminedManagedObjects();
      StructureMetaData structure = attachments.getAttachment(StructureMetaData.class);
      checkAttachments(context, deployment);

      assertNotNull(structure);
      checkDeployment(context, structure);

      ContextInfo contextInfo = structure.getContext("");
      assertNotNull(contextInfo);
      checkContextInfo(context, contextInfo);
   }
   
   protected void checkDeployment(String parentName, DeploymentContext context, ContextInfo contextInfo) throws Exception
   {
      assertNotNull(context);
      
      String expectedName;
      if (parentName.endsWith("/"))
         expectedName = parentName + contextInfo.getPath();
      else
         expectedName = parentName + "/" + contextInfo.getPath();
      
      assertEquals(expectedName, context.getName());
      
      StructureMetaData structure = contextInfo.getPredeterminedManagedObjects().getAttachment(StructureMetaData.class);
      checkAttachments(context, contextInfo);
      if (structure != null)
         checkDeployment(context, structure);

      checkContextInfo(context, contextInfo);
   }
   
   protected void checkDeployment(DeploymentContext context, StructureMetaData structure) throws Exception
   {
      assertNotNull(context);
      assertNotNull(structure);
      
      List<ContextInfo> contextInfos = structure.getContexts();
      int numContexts = contextInfos.size();
      // Ignore the parent
      if (structure.getContext("") != null)
        --numContexts; 
      List<DeploymentContext> children = context.getChildren();
      int numChildren = children.size();

      assertEquals("Should have the same number contexts=" + contextInfos + " children=" + children, numContexts, numChildren);
      
      for (DeploymentContext childContext : children)
      {
         ContextInfo contextInfo = structure.getContext(childContext.getRelativePath());
         assertNotNull("Should have a context info " + childContext + " in " + contextInfos, contextInfo);
         checkDeployment(context.getName(), childContext, contextInfo);
      }
   }
   
   protected void checkAttachments(DeploymentContext context, PredeterminedManagedObjectAttachments predetermined) throws Exception
   {
      Map<String, Object> expected = predetermined.getPredeterminedManagedObjects().getAttachments();
      Map<String, Object> actual = context.getPredeterminedManagedObjects().getAttachments();
      assertEquals(expected, actual);
   }
   
   protected void checkContextInfo(DeploymentContext context, ContextInfo contextInfo) throws Exception
   {
   }
}
