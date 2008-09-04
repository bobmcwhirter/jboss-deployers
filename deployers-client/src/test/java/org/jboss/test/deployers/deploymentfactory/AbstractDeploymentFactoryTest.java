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
package org.jboss.test.deployers.deploymentfactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.client.spi.DeploymentFactory;
import org.jboss.deployers.spi.attachments.Attachments;
import org.jboss.deployers.spi.attachments.PredeterminedManagedObjectAttachments;
import org.jboss.deployers.spi.structure.ClassPathEntry;
import org.jboss.deployers.spi.structure.ContextInfo;
import org.jboss.deployers.spi.structure.StructureMetaData;
import org.jboss.test.BaseTestCase;

/**
 * AbstractDeploymentFactoryTest.
 * 
 * @author <a href="adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public abstract class AbstractDeploymentFactoryTest extends BaseTestCase
{
   public AbstractDeploymentFactoryTest(String name)
   {
      super(name);
   }

   protected static void assertDefault(ContextInfo contextInfo)
   {
      assertDefaultPath(contextInfo);
      assertDefaultMetaDataPath(contextInfo);
      assertDefaultClassPath(contextInfo.getClassPath());
   }

   protected static void assertDefaultPath(ContextInfo contextInfo)
   {
      assertEquals("", contextInfo.getPath());
   }

   protected static void assertDefaultNonPath(ContextInfo contextInfo)
   {
      assertDefaultMetaDataPath(contextInfo);
      assertDefaultClassPath(contextInfo.getClassPath());
   }

   protected static void assertDefaultMetaDataPath(ContextInfo contextInfo)
   {
      assertNotNull(contextInfo);
      assertNotNull(contextInfo.getMetaDataPath());
      assertTrue(contextInfo.getMetaDataPath().isEmpty());
   }

   protected static void assertDefaultMetaDataPath(List<String> metaDataPath)
   {
      assertEquals(1, metaDataPath.size());
      assertEquals("metaDataPath", metaDataPath.get(0));
   }

   protected static void assertDefaultClassPath(List<ClassPathEntry> classPath)
   {
      assertNotNull(classPath);
      assertEquals(1, classPath.size());
      ClassPathEntry entry = classPath.get(0);
      assertNotNull(entry);
      assertEquals("", entry.getPath());
      assertNull(entry.getSuffixes());
   }
   
   protected StructureMetaData assertStructureMetaData(PredeterminedManagedObjectAttachments attachments)
   {
      assertNotNull(attachments);
      Attachments x = attachments.getPredeterminedManagedObjects();
      assertNotNull(x);
      StructureMetaData structure = x.getAttachment(StructureMetaData.class);
      assertNotNull(structure);
      return structure;
   }
   
   protected void assertContexts(PredeterminedManagedObjectAttachments attachments, ContextInfo... expected)
   {
      StructureMetaData structure = assertStructureMetaData(attachments);
      assertContexts(structure, expected);
   }
   
   protected void assertContexts(StructureMetaData structure, ContextInfo... expected)
   {
      assertNotNull(structure);
      List<ContextInfo> contexts = new ArrayList<ContextInfo>();
      if (expected != null)
      {
         for (ContextInfo context : expected)
            contexts.add(context);
      }
      
      assertEquals(contexts, structure.getContexts());
   }

   protected abstract DeploymentFactory createDeploymentFactory();
   
   protected abstract Deployment createDeployment();
   
   public void testCreateClassPathEntryPath()
   {
      DeploymentFactory factory = createDeploymentFactory();
      ClassPathEntry entry = factory.createClassPathEntry("path");
      assertEquals("path", entry.getPath());
      assertNull(entry.getSuffixes());
   }
   
   public void testCreateClassPathEntryPathErrors()
   {
      DeploymentFactory factory = createDeploymentFactory();
      try
      {
         factory.createClassPathEntry(null);
         fail("Should not be here!");
      }
      catch (Exception e)
      {
         checkThrowable(IllegalArgumentException.class, e);
      }
   }
   
   public void testCreateClassPathEntryPathAndSuffixes()
   {
      DeploymentFactory factory = createDeploymentFactory();
      ClassPathEntry entry = factory.createClassPathEntry("path", "suffixes");
      assertEquals("path", entry.getPath());
      assertEquals("suffixes", entry.getSuffixes());

      entry = factory.createClassPathEntry("path", null);
      assertEquals("path", entry.getPath());
      assertNull(entry.getSuffixes());
   }
   
   public void testCreateClassPathEntryPathAndSuffixesErrors()
   {
      DeploymentFactory factory = createDeploymentFactory();
      try
      {
         factory.createClassPathEntry(null, "suffixes");
         fail("Should not be here!");
      }
      catch (Exception e)
      {
         checkThrowable(IllegalArgumentException.class, e);
      }
   }
   
   public void testCreateClassPathPath()
   {
      DeploymentFactory factory = createDeploymentFactory();
      List<ClassPathEntry> expected = Collections.singletonList(factory.createClassPathEntry("path"));
      List<ClassPathEntry> classPath = factory.createClassPath("path");
      assertEquals(expected, classPath);
   }
   
   public void testCreateClassPathPathErrors()
   {
      DeploymentFactory factory = createDeploymentFactory();
      try
      {
         factory.createClassPath(null);
         fail("Should not be here!");
      }
      catch (Exception e)
      {
         checkThrowable(IllegalArgumentException.class, e);
      }
   }
   
   public void testCreateClassPathPathAndSuffixes()
   {
      DeploymentFactory factory = createDeploymentFactory();
      List<ClassPathEntry> expected = Collections.singletonList(factory.createClassPathEntry("path", "suffixes"));
      List<ClassPathEntry> classPath = factory.createClassPath("path", "suffixes");
      assertEquals(expected, classPath);
   }
   
   public void testCreateClassPathPathAndSuffixesErrors()
   {
      DeploymentFactory factory = createDeploymentFactory();
      try
      {
         factory.createClassPath(null, "suffixes");
         fail("Should not be here!");
      }
      catch (Exception e)
      {
         checkThrowable(IllegalArgumentException.class, e);
      }
   }

   public void testAddContextPath()
   {
      DeploymentFactory factory = createDeploymentFactory();
      Deployment deployment = createDeployment();
      ContextInfo context = factory.addContext(deployment, "path");
      assertEquals("path", context.getPath());
      assertDefaultNonPath(context);
      
      assertContexts(deployment, context);
   }
   
   public void testAddContextPathErrors()
   {
      DeploymentFactory factory = createDeploymentFactory();
      Deployment deployment = createDeployment();
      try
      {
         factory.addContext(null, "path");
         fail("Should not be here");
      }
      catch (Exception e)
      {
         checkThrowable(IllegalArgumentException.class, e);
      }
      try
      {
         factory.addContext(deployment, null);
         fail("Should not be here");
      }
      catch (Exception e)
      {
         checkThrowable(IllegalArgumentException.class, e);
      }
   }

   public void testAddContextPathAndClassPath()
   {
      DeploymentFactory factory = createDeploymentFactory();
      Deployment deployment = createDeployment();
      List<ClassPathEntry> classPath = factory.createClassPath("ClassPath");
      ContextInfo context = factory.addContext(deployment, "path", classPath);
      assertEquals("path", context.getPath());
      assertDefaultMetaDataPath(context);
      assertEquals(classPath, context.getClassPath());

      assertContexts(deployment, context);
      
      deployment = createDeployment();
      classPath = null;
      context = factory.addContext(deployment, "path", classPath);
      assertEquals("path", context.getPath());
      assertDefaultMetaDataPath(context);
      assertNull(context.getClassPath());

      assertContexts(deployment, context);
   }
   
   public void testAddContextPathAndClassPathErrors()
   {
      DeploymentFactory factory = createDeploymentFactory();
      Deployment deployment = createDeployment();
      List<ClassPathEntry> classPath = factory.createClassPath("ClassPath");
      try
      {
         factory.addContext(null, "path", classPath);
         fail("Should not be here");
      }
      catch (Exception e)
      {
         checkThrowable(IllegalArgumentException.class, e);
      }
      try
      {
         factory.addContext(deployment, null, classPath);
         fail("Should not be here");
      }
      catch (Exception e)
      {
         checkThrowable(IllegalArgumentException.class, e);
      }
   }

   public void testAddContextPathAndMetaDataAndClassPath()
   {
      DeploymentFactory factory = createDeploymentFactory();
      Deployment deployment = createDeployment();
      List<ClassPathEntry> classPath = factory.createClassPath("ClassPath");
      ContextInfo context = factory.addContext(deployment, "path", "metaDataPath", classPath);
      assertEquals("path", context.getPath());
      assertDefaultMetaDataPath(context.getMetaDataPath());
      assertEquals(classPath, context.getClassPath());

      assertContexts(deployment, context);

      classPath = null;
      deployment = createDeployment();
      context = factory.addContext(deployment, "path", "metaDataPath", classPath);
      assertEquals("path", context.getPath());
      assertDefaultMetaDataPath(context.getMetaDataPath());
      assertNull(context.getClassPath());

      assertContexts(deployment, context);
   }
   
   public void testAddContextPathAndMetaDataPathAndAndClassPathErrors()
   {
      DeploymentFactory factory = createDeploymentFactory();
      Deployment deployment = createDeployment();
      List<ClassPathEntry> classPath = factory.createClassPath("ClassPath");
      try
      {
         factory.addContext(null, "path", "metaData", classPath);
         fail("Should not be here");
      }
      catch (Exception e)
      {
         checkThrowable(IllegalArgumentException.class, e);
      }

      try
      {
         factory.addContext(deployment, null, "metaData", classPath);
         fail("Should not be here");
      }
      catch (Exception e)
      {
         checkThrowable(IllegalArgumentException.class, e);
      }

      try
      {
         factory.addContext(deployment, "path", (String)null, classPath);
         fail("Should not be here");
      }
      catch (Exception e)
      {
         checkThrowable(IllegalArgumentException.class, e);
      }
   }

   public void testAddContexts()
   {
      DeploymentFactory factory = createDeploymentFactory();
      Deployment deployment = createDeployment();
      ContextInfo context1 = factory.addContext(deployment, "path1");
      assertEquals("path1", context1.getPath());
      assertDefaultNonPath(context1);

      ContextInfo context2 = factory.addContext(deployment, "path2");
      assertEquals("path2", context2.getPath());
      assertDefaultNonPath(context1);

      ContextInfo context3 = factory.addContext(deployment, "path3");
      assertEquals("path3", context3.getPath());
      assertDefaultNonPath(context1);
      
      assertContexts(deployment, context1, context2, context3);
   }

   public void testAddContextsToContext()
   {
      DeploymentFactory factory = createDeploymentFactory();
      Deployment deployment = createDeployment();

      ContextInfo parent = factory.addContext(deployment, "parent");
      assertEquals("parent", parent.getPath());
      assertDefaultNonPath(parent);
      
      ContextInfo context1 = factory.addContext(parent, "path1");
      assertEquals("path1", context1.getPath());
      assertDefaultNonPath(context1);

      ContextInfo context2 = factory.addContext(parent, "path2");
      assertEquals("path2", context2.getPath());
      assertDefaultNonPath(context2);

      ContextInfo context3 = factory.addContext(parent, "path3");
      assertEquals("path3", context3.getPath());
      assertDefaultNonPath(context3);
      
      assertContexts(deployment, parent);
      assertContexts(parent, context1, context2, context3);
   }
   
   public void testSerialization() throws Exception
   {
      DeploymentFactory factory = createDeploymentFactory();
      Deployment deployment = createDeployment();

      List<ClassPathEntry> classPath = factory.createClassPath("ClassPath");
      ContextInfo context1 = factory.addContext(deployment, "path1", "metaDataPath", classPath);
      assertEquals("path1", context1.getPath());
      assertDefaultMetaDataPath(context1.getMetaDataPath());
      assertEquals(classPath, context1.getClassPath());
      
      StructureMetaData structure = assertStructureMetaData(deployment);
      assertContexts(structure, context1);
      
      deployment = serializeDeserialize(deployment, Deployment.class);
      structure = assertStructureMetaData(deployment);
      assertContexts(structure, context1);
      
      ContextInfo context2 = factory.addContext(deployment, "path2");
      assertEquals("path2", context2.getPath());
      assertDefaultNonPath(context2);
      structure = assertStructureMetaData(deployment);
      assertContexts(structure, context1, context2);

      deployment = serializeDeserialize(deployment, Deployment.class);
      structure = assertStructureMetaData(deployment);
      assertContexts(structure, context1, context2);

      ContextInfo testContext = structure.getContext("path2");
      
      ContextInfo child1 = factory.addContext(testContext, "child1");
      assertEquals("child1", child1.getPath());
      assertDefaultNonPath(child1);

      ContextInfo child2 = factory.addContext(testContext, "child2");
      assertEquals("child2", child2.getPath());
      assertDefaultNonPath(child2);
      
      ContextInfo child3 = factory.addContext(testContext, "child3");
      assertEquals("child3", child3.getPath());
      assertDefaultNonPath(child3);

      assertContexts(structure, context1, context2);
      StructureMetaData childStructure = assertStructureMetaData(testContext);
      assertContexts(childStructure, child1, child2, child3);

      structure = serializeDeserialize(structure, StructureMetaData.class);
      assertContexts(structure, context1, context2);
      testContext = structure.getContext("path2");
      assertEquals(context2, testContext);
      
      childStructure = assertStructureMetaData(testContext);
      assertContexts(childStructure, child1, child2, child3);
   }
}
