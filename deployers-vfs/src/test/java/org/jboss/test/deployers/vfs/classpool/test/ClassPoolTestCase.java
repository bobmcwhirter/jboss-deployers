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
package org.jboss.test.deployers.vfs.classpool.test;

import java.util.Set;

import javassist.scopedpool.ScopedClassPoolFactory;
import junit.framework.Test;

import org.jboss.classloader.plugins.jdk.AbstractJDKChecker;
import org.jboss.classpool.plugins.as5.RegisterModuleCallback;
import org.jboss.classpool.plugins.as5.VFSClassLoaderDomainRegistry;
import org.jboss.classpool.plugins.as5.jbosscl.JBossClDelegatingClassPoolFactory;
import org.jboss.classpool.spi.ClassPoolRepository;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.deployer.kernel.BeanMetaDataFactoryVisitor;
import org.jboss.reflect.plugins.javassist.JavassistTypeInfoFactoryImpl;
import org.jboss.reflect.plugins.javassist.classpool.ClassPoolFactory;
import org.jboss.reflect.plugins.javassist.classpool.RepositoryClassPoolFactory;
import org.jboss.reflect.spi.TypeInfo;
import org.jboss.reflect.spi.TypeInfoFactory;
import org.jboss.test.deployers.BootstrapDeployersTest;
import org.jboss.test.deployers.vfs.classpool.support.crm.CrmFacade;
import org.jboss.test.deployers.vfs.classpool.support.ejb.MySLSBean;
import org.jboss.test.deployers.vfs.classpool.support.ext.External;
import org.jboss.test.deployers.vfs.classpool.support.jar.PlainJavaBean;
import org.jboss.test.deployers.vfs.classpool.support.jsf.JsfBean;
import org.jboss.test.deployers.vfs.classpool.support.service.SomeMBean;
import org.jboss.test.deployers.vfs.classpool.support.ui.UIBean;
import org.jboss.test.deployers.vfs.classpool.support.util.SomeUtil;
import org.jboss.test.deployers.vfs.classpool.support.web.AnyServlet;
import org.jboss.virtual.AssembledDirectory;

/**
 * Test case for ClassPool.
 * 
 * @author <a href="mailto:flavia.rainone@jboss.com">Flavia Rainone</a>
 *
 * @version $Revision$
 */
public class ClassPoolTestCase extends BootstrapDeployersTest
{
   public ClassPoolTestCase(String name)
   {
      super(name);
   }
   
   public static Test suite()
   {
      return suite(ClassPoolTestCase.class);
   }

   @Override
   protected void setUp() throws Exception
   {
      // excluding class that knows hot to load from system classloader
      Set<Class<?>> excluded = AbstractJDKChecker.getExcluded();
      excluded.add(BeanMetaDataFactoryVisitor.class);
      
      ScopedClassPoolFactory scopedClassPoolFactory = new JBossClDelegatingClassPoolFactory(new VFSClassLoaderDomainRegistry(), new RegisterModuleCallback());
      ClassPoolRepository repository = ClassPoolRepository.getInstance();
      repository.setClassPoolFactory(scopedClassPoolFactory);
      ClassPoolFactory classPoolFactory = new RepositoryClassPoolFactory(repository);
      JavassistTypeInfoFactoryImpl.setPoolFactory(classPoolFactory);
      super.setUp();
   }
   
   @Override
   protected void tearDown() throws Exception
   {
      super.tearDown();
      // reverting exclusion performed on setUp
      Set<Class<?>> excluded = AbstractJDKChecker.getExcluded();
      excluded.remove(BeanMetaDataFactoryVisitor.class);
   }

   public void testJar() throws Exception
   {
      AssembledDirectory directory = createJar();
      assertClassPool(directory, PlainJavaBean.class);
   }

   public void testEjbJar() throws Exception
   {
      AssembledDirectory directory = createEjbJar();
      assertClassPool(directory, MySLSBean.class);
   }
   
   public void testWar() throws Exception
   {
      AssembledDirectory directory = createWar();
      assertClassPool(directory, AnyServlet.class);
   }
   
   public void testSar() throws Exception
   {
      AssembledDirectory directory = createSar();
      assertClassPool(directory, SomeMBean.class);
   }
      
   public void testBasicEar() throws Exception
   {
      AssembledDirectory directory = createBasicEar();
      assertClassPool(directory, SomeUtil.class, PlainJavaBean.class,
            MySLSBean.class, AnyServlet.class, UIBean.class,
            JsfBean.class, CrmFacade.class, SomeMBean.class);
   }
   
   public void testTopLevelWithUtil() throws Exception 
   {
      AssembledDirectory directory = createTopLevelWithUtil("/classpool/earutil");
      assertClassPool(directory, SomeUtil.class, External.class);
   }
   
   public void testWarInEar() throws Exception 
   {
      AssembledDirectory directory = createWarInEar();
      assertClassPool(directory, AnyServlet.class);
   }
   
   public void testJarInEar() throws Exception 
   {
      AssembledDirectory directory = createJarInEar();
      assertClassPool(directory, PlainJavaBean.class);
   }
   
   private AssembledDirectory createJar() throws Exception
   {
      AssembledDirectory jar = createAssembledDirectory("simple.jar", "simple.jar");
      addPackage(jar, PlainJavaBean.class);
      return jar;
   }
   
   private AssembledDirectory createEjbJar() throws Exception
   {
      AssembledDirectory jar = createAssembledDirectory("ejbs.jar", "ejbs.jar");
      addPackage(jar, MySLSBean.class);
      
      addPath(jar, "/classpool/ejb", "META-INF");
      return jar;
   }
   
   private AssembledDirectory createWar() throws Exception
   {
      AssembledDirectory war = createAssembledDirectory("simple.war", "simple.war");
      AssembledDirectory webinf = war.mkdir("WEB-INF");
      AssembledDirectory classes = webinf.mkdir("classes");
      addPackage(classes, AnyServlet.class);
      addPath(war, "/classpool/web", "WEB-INF");
      return war;
   }

   private AssembledDirectory createSar() throws Exception
   {
      AssembledDirectory sar = createAssembledDirectory("simple.sar", "simple.sar");
      addPackage(sar, SomeMBean.class);
      return sar;
   }
   
   private AssembledDirectory createBasicEar() throws Exception
   {
      AssembledDirectory ear = createTopLevelWithUtil("/classpool/simple");
      
      AssembledDirectory jar = ear.mkdir("simple.jar");
      addPackage(jar, PlainJavaBean.class);
      
      AssembledDirectory ejbs = ear.mkdir("ejbs.jar");
      addPackage(ejbs, MySLSBean.class);
      addPath(ejbs, "/classpool/ejb", "META-INF");
      
      AssembledDirectory war = ear.mkdir("simple.war");
      AssembledDirectory webinf = war.mkdir("WEB-INF");
      AssembledDirectory classes = webinf.mkdir("classes");
      addPackage(classes, AnyServlet.class);
      addPath(war, "/classpool/web", "WEB-INF");
      
      AssembledDirectory lib = webinf.mkdir("lib");
      
      AssembledDirectory uijar = lib.mkdir("ui.jar");
      addPackage(uijar, UIBean.class);
      
      // another war
      war = ear.mkdir("jsfapp.war");
      webinf = war.mkdir("WEB-INF");
      addPath(war, "/classpool/web", "WEB-INF");
      classes = webinf.mkdir("classes");
      addPackage(classes, JsfBean.class);
      
      lib = webinf.mkdir("lib");
      
      uijar = lib.mkdir("ui_util.jar");
      addPackage(uijar, CrmFacade.class);
      
      // a sar
      AssembledDirectory sar = ear.mkdir("simple.sar");
      addPackage(sar, SomeMBean.class);
      addPath(war, "/classpool/sar", "META-INF");
            
      enableTrace("org.jboss.deployers");
      
      return ear;
   }

   private AssembledDirectory createTopLevelWithUtil(String path) throws Exception
   {
      AssembledDirectory topLevel = createAssembledDirectory("top-level.ear", "top-level.ear");
      addPath(topLevel, path, "META-INF");

      AssembledDirectory earLib = topLevel.mkdir("lib");

      AssembledDirectory util = earLib.mkdir("util.jar");
      addPackage(util, SomeUtil.class);

      AssembledDirectory ext = earLib.mkdir("ext.jar");
      addPackage(ext, External.class);

      return topLevel;
   }
   
   private AssembledDirectory createWarInEar() throws Exception
   {
      AssembledDirectory ear = createAssembledDirectory("war-in-ear.ear", "war-in-ear.ear");
      addPath(ear, "/classpool/warinear", "META-INF");

      AssembledDirectory war = ear.mkdir("simple.war");
      AssembledDirectory webinf = war.mkdir("WEB-INF");
      AssembledDirectory classes = webinf.mkdir("classes");
      addPackage(classes, AnyServlet.class);
      addPath(war, "/classpool/web", "WEB-INF");

      return ear;
   }

   private AssembledDirectory createJarInEar() throws Exception
   {
      AssembledDirectory ear = createAssembledDirectory("jar-in-ear.ear", "jar-in-ear.ear");
      addPath(ear, "/classpool/jarinear", "META-INF");

      AssembledDirectory jar = ear.mkdir("simple.jar");
      addPackage(jar, PlainJavaBean.class);
      
      return ear;
   }
   
   @SuppressWarnings("deprecation")
   private void assertClassPool(AssembledDirectory directory, Class<?> ... classes)
      throws Exception
   {
      DeploymentUnit unit = assertDeploy(directory);
      try
      {
         ClassLoader classLoader = getClassLoader(unit);
         for (Class<?> clazz: classes)
         {
            String className = clazz.getName();
            assertLoadClass(className, classLoader);
            TypeInfoFactory typeInfoFactory = new JavassistTypeInfoFactoryImpl();
            TypeInfo typeInfo = typeInfoFactory.getTypeInfo(className, classLoader);
            assertEquals(className, typeInfo.getName());
            assertEquals(classLoader, typeInfo.getType().getClassLoader());
         }
      }
      finally
      {
         undeploy(unit);
      }
   }
}