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
package org.jboss.test.deployers.vfs.reflect.test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jboss.classloader.plugins.jdk.AbstractJDKChecker;
import org.jboss.classloading.spi.metadata.ClassLoadingMetaData;
import org.jboss.classloading.spi.metadata.ExportAll;
import org.jboss.classloading.spi.version.Version;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.spi.attachments.MutableAttachments;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.deployer.kernel.BeanMetaDataFactoryVisitor;
import org.jboss.deployers.vfs.spi.client.VFSDeploymentFactory;
import org.jboss.reflect.spi.ClassInfo;
import org.jboss.reflect.spi.MethodInfo;
import org.jboss.reflect.spi.TypeInfo;
import org.jboss.reflect.spi.TypeInfoFactory;
import org.jboss.test.deployers.BootstrapDeployersTest;
import org.jboss.test.deployers.vfs.reflect.support.crm.CrmFacade;
import org.jboss.test.deployers.vfs.reflect.support.ejb.MySLSBean;
import org.jboss.test.deployers.vfs.reflect.support.ext.External;
import org.jboss.test.deployers.vfs.reflect.support.jar.PlainJavaBean;
import org.jboss.test.deployers.vfs.reflect.support.jsf.JsfBean;
import org.jboss.test.deployers.vfs.reflect.support.service.SomeMBean;
import org.jboss.test.deployers.vfs.reflect.support.ui.UIBean;
import org.jboss.test.deployers.vfs.reflect.support.util.SomeUtil;
import org.jboss.test.deployers.vfs.reflect.support.web.AnyServlet;
import org.jboss.virtual.AssembledDirectory;
import org.jboss.virtual.VirtualFile;

/**
 * Abstract test for Reflect.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 *
 * @version $Revision$
 */
public abstract class ReflectTest extends BootstrapDeployersTest
{
   protected ReflectTest(String name)
   {
      super(name);
   }

   @Override
   protected void setUp() throws Exception
   {
      // excluding class that knows hot to load from system classloader
      Set<Class<?>> excluded = AbstractJDKChecker.getExcluded();
      excluded.add(BeanMetaDataFactoryVisitor.class);

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

   protected abstract TypeInfoFactory createTypeInfoFactory();

   protected TypeInfo assertReturnType(TypeInfo ti, String method)
   {
      ClassInfo ci = assertInstanceOf(ti, ClassInfo.class);
      MethodInfo mi = ci.getDeclaredMethod(method);
      assertNotNull("No such '" + method + "' method on " + ci, mi);
      return mi.getReturnType();      
   }

   protected void assertTypeInfo(VirtualFile file, Class<?> ... classes) throws Exception
   {
      Map<Class<?>, String> map = new HashMap<Class<?>, String>();
      for (Class<?> clazz : classes)
      {
         map.put(clazz, null);
      }
      assertTypeInfo(file, map);
   }

   protected void assertTypeInfo(VirtualFile file, Map<Class<?>, String> classes) throws Exception
   {
      DeploymentUnit unit = assertDeploy(file);
      try
      {
         TypeInfoFactory typeInfoFactory = createTypeInfoFactory();
         for (Map.Entry<Class<?>, String> entry : classes.entrySet())
         {
            DeploymentUnit du = getDeploymentUnit(unit, entry.getValue());
            ClassLoader classLoader = getClassLoader(du);
            Class<?> clazz = entry.getKey();
            String className = clazz.getName();
            assertLoadClass(className, classLoader);
            TypeInfo typeInfo = typeInfoFactory.getTypeInfo(className, classLoader);
            assertEquals(className, typeInfo.getName());
            ClassLoader cl = typeInfo.getClassLoader();
            assertEquals(classLoader, cl);
         }
      }
      finally
      {
         undeploy(unit);
      }
   }

   protected void assertEquals(TypeInfo ti1, TypeInfo ti2)
   {
      assertSame(ti1, ti2);
   }

   protected void assertNotEquals(TypeInfo ti1, TypeInfo ti2)
   {
      assertNotSame(ti1, ti2);
   }

   protected DeploymentUnit getDeploymentUnit(DeploymentUnit parent, String name)
   {
      if (name == null || "".equals(name))
         return parent;

      return assertChild(parent, name);
   }

   protected AssembledDirectory createJar() throws Exception
   {
      return createJar("simple.jar", PlainJavaBean.class);
   }

   protected AssembledDirectory createJar(String name, Class<?> reference) throws Exception
   {
      AssembledDirectory jar = createAssembledDirectory(name, name);
      addPackage(jar, reference);
      return jar;
   }

   protected AssembledDirectory createEjbJar() throws Exception
   {
      AssembledDirectory jar = createAssembledDirectory("ejbs.jar", "ejbs.jar");
      addPackage(jar, MySLSBean.class);

      addPath(jar, "/reflect/ejb", "META-INF");
      return jar;
   }

   protected AssembledDirectory createWar() throws Exception
   {
      AssembledDirectory war = createAssembledDirectory("simple.war", "simple.war");
      AssembledDirectory webinf = war.mkdir("WEB-INF");
      AssembledDirectory classes = webinf.mkdir("classes");
      addPackage(classes, AnyServlet.class);
      addPath(war, "/reflect/web", "WEB-INF");
      return war;
   }

   protected AssembledDirectory createSar() throws Exception
   {
      AssembledDirectory sar = createAssembledDirectory("simple.sar", "simple.sar");
      addPackage(sar, SomeMBean.class);
      return sar;
   }

   protected AssembledDirectory createBasicEar() throws Exception
   {
      AssembledDirectory ear = createTopLevelWithUtil("/reflect/simple");

      AssembledDirectory jar = ear.mkdir("simple.jar");
      addPackage(jar, PlainJavaBean.class);

      AssembledDirectory ejbs = ear.mkdir("ejbs.jar");
      addPackage(ejbs, MySLSBean.class);
      addPath(ejbs, "/reflect/ejb", "META-INF");

      AssembledDirectory war = ear.mkdir("simple.war");
      AssembledDirectory webinf = war.mkdir("WEB-INF");
      AssembledDirectory classes = webinf.mkdir("classes");
      addPackage(classes, AnyServlet.class);
      addPath(war, "/reflect/web", "WEB-INF");

      AssembledDirectory lib = webinf.mkdir("lib");

      AssembledDirectory uijar = lib.mkdir("ui.jar");
      addPackage(uijar, UIBean.class);

      // another war
      war = ear.mkdir("jsfapp.war");
      webinf = war.mkdir("WEB-INF");
      addPath(war, "/reflect/web", "WEB-INF");
      classes = webinf.mkdir("classes");
      addPackage(classes, JsfBean.class);

      lib = webinf.mkdir("lib");

      uijar = lib.mkdir("ui_util.jar");
      addPackage(uijar, CrmFacade.class);

      // a sar
      AssembledDirectory sar = ear.mkdir("simple.sar");
      addPackage(sar, SomeMBean.class);
      addPath(war, "/reflect/sar", "META-INF");

      enableTrace("org.jboss.deployers");

      return ear;
   }

   protected AssembledDirectory createTopLevelWithUtil(String path) throws Exception
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

   protected AssembledDirectory createWarInEar() throws Exception
   {
      AssembledDirectory ear = createAssembledDirectory("war-in-ear.ear", "war-in-ear.ear");
      addPath(ear, "/reflect/warinear", "META-INF");

      AssembledDirectory war = ear.mkdir("simple.war");
      AssembledDirectory webinf = war.mkdir("WEB-INF");
      AssembledDirectory classes = webinf.mkdir("classes");
      addPackage(classes, AnyServlet.class);
      addPath(war, "/reflect/web", "WEB-INF");

      return ear;
   }

   protected AssembledDirectory createJarInEar() throws Exception
   {
      AssembledDirectory ear = createAssembledDirectory("jar-in-ear.ear", "jar-in-ear.ear");
      addPath(ear, "/reflect/jarinear", "META-INF");

      AssembledDirectory jar = ear.mkdir("simple.jar");
      addPackage(jar, PlainJavaBean.class);

      return ear;
   }

   protected Deployment createIsolatedDeployment(String name) throws Exception
   {
      return createIsolatedDeployment(name, null, PlainJavaBean.class);
   }

   protected Deployment createIsolatedDeployment(String name, String parentDomain, Class<?> reference) throws Exception
   {
      AssembledDirectory jar = createJar(name, reference);
      Deployment deployment = VFSDeploymentFactory.getInstance().createVFSDeployment(jar);

      ClassLoadingMetaData clmd = new ClassLoadingMetaData();
      clmd.setDomain(name + "_Domain");
      clmd.setParentDomain(parentDomain);
      clmd.setImportAll(true);
      clmd.setExportAll(ExportAll.NON_EMPTY);
      clmd.setVersion(Version.DEFAULT_VERSION);

      MutableAttachments attachments = (MutableAttachments)deployment.getPredeterminedManagedObjects();
      attachments.addAttachment(ClassLoadingMetaData.class, clmd);

      return deployment;
   }
}