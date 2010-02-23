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

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jboss.classloader.plugins.jdk.AbstractJDKChecker;
import org.jboss.classloading.spi.metadata.ClassLoadingMetaData;
import org.jboss.classloading.spi.metadata.ExportAll;
import org.jboss.classloading.spi.version.Version;
import org.jboss.deployers.client.spi.DeployerClient;
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
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;

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

   public void testJar() throws Exception
   {
      VirtualFile directory = createJar();
      assertReflect(directory, PlainJavaBean.class);
   }

   public void testEjbJar() throws Exception
   {
      VirtualFile directory = createEjbJar();
      assertReflect(directory, MySLSBean.class);
   }

   public void testWar() throws Exception
   {
      VirtualFile directory = createWar();
      assertReflect(directory, AnyServlet.class);
   }

   public void testSar() throws Exception
   {
      VirtualFile directory = createSar();
      assertReflect(directory, SomeMBean.class);
   }

   public void testBasicEar() throws Exception
   {
      VirtualFile directory = createBasicEar();
      Map<Class<?>, String> classes = new HashMap<Class<?>, String>();
      classes.put(SomeUtil.class, null);
      classes.put(PlainJavaBean.class, null);
      classes.put(MySLSBean.class, null);
      classes.put(AnyServlet.class, "simple.war");
      classes.put(UIBean.class, "simple.war");
      classes.put(JsfBean.class, "jsfapp.war");
      classes.put(CrmFacade.class, "jsfapp.war");
      classes.put(SomeMBean.class, null);
      assertReflect(directory, classes);
   }

   public void testTopLevelWithUtil() throws Exception
   {
      VirtualFile directory = createTopLevelWithUtil("/reflect/earutil");
      assertReflect(directory, SomeUtil.class, External.class);
   }

   public void testWarInEar() throws Exception
   {
      VirtualFile directory = createWarInEar();
      assertReflect(directory, Collections.<Class<?>, String>singletonMap(AnyServlet.class, "simple.war"));
   }

   public void testJarInEar() throws Exception
   {
      VirtualFile directory = createJarInEar();
      assertReflect(directory, PlainJavaBean.class);
   }

   public void testHierarchyCLUsage() throws Exception
   {
      VirtualFile directory = createBasicEar();
      DeploymentUnit unit = assertDeploy(directory);
      try
      {
         ClassLoader topCL = getClassLoader(unit);
         DeploymentUnit child = getDeploymentUnit(unit, "simple.war");
         ClassLoader childCL = getClassLoader(child);
         assertSimpleHierarchy(topCL, childCL);
      }
      finally
      {
         undeploy(unit);
      }
   }

   public void testIsolatedJars() throws Exception
   {
      Deployment d1 = createIsolatedDeployment("j1.jar");
      Deployment d2 = createIsolatedDeployment("j2.jar");
      testIsolatedJars(d1, d2);
   }

   public void testHierarchyJarsChildFirst() throws Exception
   {
      Deployment d1 = createIsolatedDeployment("j1.jar");
      ClassLoadingMetaData clmd = createDefaultClassLoadingMetaData("j2.jar", "j1.jar_Domain");
      clmd.setJ2seClassLoadingCompliance(false);
      Deployment d2 = createIsolatedDeployment("j2.jar", "j1.jar_Domain", PlainJavaBean.class, clmd);
      testIsolatedJars(d1, d2);
   }

   public void testDomainHierarchy() throws Exception
   {
      Deployment top = createIsolatedDeployment("top.jar", null, PlainJavaBean.class);
      Deployment left = createIsolatedDeployment("left.jar", "top.jar_Domain", AnyServlet.class);
      Deployment right = createIsolatedDeployment("right.jar", "top.jar_Domain", AnyServlet.class);
      testDomainHierarchy(top.getName(), left.getName(), right.getName(), top, left, right);
   }

   public void testEar2War() throws Exception
   {
      VirtualFile earFile = VFS.getChild(getName()).getChild("ptd-ear-1.0-SNAPSHOT.ear");
      createAssembledDirectory(earFile)
         .addPath("META-INF", "/reflect/ear2war/META-INF")
         .addPackage("lib/common.jar", PlainJavaBean.class)
         .addPath("ptd-jsf-1.0-SNAPSHOT.war", "/reflect/ear2war/war1")
         .addPackage("ptd-jsf-1.0-SNAPSHOT.war/WEB-INF/lib/wj1.jar", AnyServlet.class)
         .addPath("ptd-jsf-1.0-SNAPSHOT.war/WEB-INF/lib/wj1.jar", "/reflect/ear2war/manifest")
         .addPath("ptd-ws-1.0-SNAPSHOT.war", "/reflect/ear2war/war2")
         .addPackage("ptd-ws-1.0-SNAPSHOT.war/WEB-INF/lib/wj2.jar", AnyServlet.class)
         .addPath("ptd-ws-1.0-SNAPSHOT.war/WEB-INF/lib/wj2.jar", "/reflect/ear2war/manifest");

      Deployment deployment = createVFSDeployment(earFile);
      String top = deployment.getName();
      String left = top + "ptd-jsf-1.0-SNAPSHOT.war/";
      String right = top + "ptd-ws-1.0-SNAPSHOT.war/";
      testDomainHierarchy(top, left, right, deployment);
   }

   public void testNonDeploymentModule() throws Exception
   {
      URL location = AnyServlet.class.getProtectionDomain().getCodeSource().getLocation();
      System.setProperty("jboss.tests.url", location.toExternalForm());
      try
      {
         VirtualFile jarFile = VFS.getChild(getName()).getChild("simple.jar");
         createAssembledDirectory(jarFile)
            .addPackage(PlainJavaBean.class)
            .addPath("/reflect/module");

         Deployment deployment = createVFSDeployment(jarFile);
         DeployerClient main = getDeployerClient();
         main.deploy(deployment);
         try
         {
            Object anys = assertBean("AnyServlet", Object.class);
            Class<?> anysClass = anys.getClass();
            ClassLoader anysCL = anysClass.getClassLoader();

            DeploymentUnit du = getMainDeployerStructure().getDeploymentUnit(deployment.getName(), true);
            ClassLoader cl = getClassLoader(du);

            assertNotSame(cl, anysCL);

            assertNonDeploymentModule(cl, anysClass);
         }
         finally
         {
            main.undeploy(deployment);
         }
      }
      finally
      {
         System.clearProperty("jboss.tests.url");
      }
   }

   public void testHierarchyNonDeploymentModule() throws Exception
   {
      testHierarchy("tif");
   }

   public void testClassLoadingMetaDataModule() throws Exception
   {
      testHierarchy("clmd");
   }

   //-------------------- helpers --------------------

   protected abstract void assertSimpleHierarchy(ClassLoader topCL, ClassLoader childCL) throws Exception;

   protected abstract void assertNonDeploymentModule(ClassLoader cl, Class<?> anysClass) throws Exception;

   protected  abstract void assertNonDeploymentModule(ClassLoader cl, Class<?> anysClass, Class<?> tifClass) throws Exception;

   protected abstract void assertIsolated(ClassLoader cl1, ClassLoader cl2, Class<?> clazz1, Class<?> clazz2) throws Exception;

   protected abstract void assertDomainHierarchy(ClassLoader topCL, ClassLoader leftCL, ClassLoader rightCL) throws Exception;

   protected void testHierarchy(String name) throws Exception
   {
      URL location = AnyServlet.class.getProtectionDomain().getCodeSource().getLocation();
      System.setProperty("jboss.tests.url", location.toExternalForm());
      try
      {
         VirtualFile jarFile = VFS.getChild(getName()).getChild("simple.jar");
         createAssembledDirectory(jarFile)
            .addPackage(PlainJavaBean.class)
            .addPath("/reflect/module")
            .addPath("/reflect/" + name);

         Deployment deployment = createVFSDeployment(jarFile);
         DeployerClient main = getDeployerClient();
         main.deploy(deployment);
         try
         {
            Object anys = assertBean("AnyServlet", Object.class);
            Class<?> anysClass = anys.getClass();
            ClassLoader anysCL = anysClass.getClassLoader();

            Object tif = assertBean("TifTester", Object.class);
            Class<?> tifClass = tif.getClass();
            ClassLoader tifCL = tifClass.getClassLoader();

            DeploymentUnit du = getMainDeployerStructure().getDeploymentUnit(deployment.getName(), true);
            ClassLoader cl = getClassLoader(du);

            assertNotSame(cl, anysCL);
            assertNotSame(cl, tifCL);
            assertNotSame(anysCL, tifCL);

            assertNonDeploymentModule(cl, anysClass, tifClass);
         }
         finally
         {
            main.undeploy(deployment);
         }
      }
      finally
      {
         System.clearProperty("jboss.tests.url");
      }
   }

   protected void testIsolatedJars(Deployment d1, Deployment d2) throws Exception
   {
      DeployerClient main = getDeployerClient();
      main.deploy(d1, d2);
      try
      {
         DeploymentUnit du1 = getMainDeployerStructure().getDeploymentUnit(d1.getName(), true);
         DeploymentUnit du2 = getMainDeployerStructure().getDeploymentUnit(d2.getName(), true);
         ClassLoader cl1 = getClassLoader(du1);
         ClassLoader cl2 = getClassLoader(du2);
         assertFalse(cl1.equals(cl2));
         Class<?> clazz1 = assertLoadClass(PlainJavaBean.class.getName(), cl1, cl1);
         Class<?> clazz2 = assertLoadClass(PlainJavaBean.class.getName(), cl2, cl2);
         assertNoClassEquality(clazz1, clazz2);

         assertIsolated(cl1, cl2, clazz1, clazz2);
      }
      finally
      {
         main.undeploy(d1, d2);
      }
   }

   protected void testDomainHierarchy(String top, String left, String right, Deployment... deployments) throws Exception
   {
      DeployerClient main = getDeployerClient();
      main.deploy(deployments);
      try
      {
         DeploymentUnit duTop = getMainDeployerStructure().getDeploymentUnit(top, true);
         DeploymentUnit duLeft = getMainDeployerStructure().getDeploymentUnit(left, true);
         DeploymentUnit duRight = getMainDeployerStructure().getDeploymentUnit(right, true);
         ClassLoader topCL = getClassLoader(duTop);
         ClassLoader leftCL = getClassLoader(duLeft);
         ClassLoader rightCL = getClassLoader(duRight);
         Class<?> asL = assertLoadClass(AnyServlet.class.getName(), leftCL);
         Class<?> asR = assertLoadClass(AnyServlet.class.getName(), rightCL);
         assertFalse(asL.equals(asR));
         Class<?> pjbL = assertLoadClass(PlainJavaBean.class.getName(), leftCL, topCL);
         Class<?> pjbR = assertLoadClass(PlainJavaBean.class.getName(), rightCL, topCL);
         assertEquals(pjbL, pjbR);

         assertDomainHierarchy(topCL, leftCL, rightCL);
      }
      finally
      {
         main.undeploy(deployments);
      }
   }

   protected abstract TypeInfoFactory createTypeInfoFactory();

   protected TypeInfo assertReturnType(TypeInfo ti, String method)
   {
      ClassInfo ci = assertInstanceOf(ti, ClassInfo.class);
      MethodInfo mi = ci.getDeclaredMethod(method);
      assertNotNull("No such '" + method + "' method on " + ci, mi);
      return mi.getReturnType();      
   }

   protected void assertReflect(VirtualFile file, Class<?> ... classes) throws Exception
   {
      Map<Class<?>, String> map = new HashMap<Class<?>, String>();
      for (Class<?> clazz : classes)
      {
         map.put(clazz, null);
      }
      assertReflect(file, map);
   }

   protected abstract void assertReflect(VirtualFile file, Map<Class<?>, String> classes) throws Exception;

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

   protected VirtualFile createJar() throws Exception
   {
      return createJar("simple.jar", PlainJavaBean.class);
   }

   protected VirtualFile createJar(String name, Class<?> reference) throws Exception
   {
      VirtualFile jarFile = VFS.getChild(getName()).getChild(name);
      createAssembledDirectory(jarFile)
         .addPackage(reference);
      return jarFile;
   }

   protected VirtualFile createEjbJar() throws Exception
   {
      VirtualFile jarFile = VFS.getChild(getName()).getChild("ejbs.jar");
      createAssembledDirectory(jarFile)
         .addPackage(MySLSBean.class)
         .addPath("/reflect/ejb");
      return jarFile;
   }

   protected VirtualFile createWar() throws Exception
   {
      VirtualFile warFile = VFS.getChild(getName()).getChild("simple.war");
      createAssembledDirectory(warFile)
         .addPackage("WEB-INF/classes", AnyServlet.class)
         .addPath("/reflect/web");
      return warFile;
   }

   protected VirtualFile createSar() throws Exception
   {
      VirtualFile sarFile = VFS.getChild(getName()).getChild( "simple.sar");
      createAssembledDirectory(sarFile)
         .addPackage(SomeMBean.class);
      return sarFile;
   }

   protected VirtualFile createBasicEar() throws Exception
   {
      VirtualFile ear = createTopLevelWithUtil("/reflect/simple");
      
      VirtualFile jarFile = ear.getChild("simple.jar");
      createAssembledDirectory(jarFile)
         .addPackage(PlainJavaBean.class);

      VirtualFile ejbsFile = ear.getChild("ejbs.jar");
      createAssembledDirectory(ejbsFile)
         .addPackage(MySLSBean.class)
         .addPath("/reflect/ejb");

      
      VirtualFile warFile = ear.getChild("simple.war");
      createAssembledDirectory(warFile)
         .addPackage("WEB-INF/classes", AnyServlet.class)
         .addPath("/reflect/web")
         .addPackage("WEB-INF/lib/ui.jar", UIBean.class);

      // another war
      warFile = ear.getChild("jsfapp.war");
      createAssembledDirectory(warFile)
         .addPath("/reflect/web")
         .addPackage("WEB-INF/classes", JsfBean.class)
         .addPackage("WEB-INF/lib/ui_util.jar", CrmFacade.class);

      // a sar
      VirtualFile sarFile = ear.getChild("simple.sar");
      createAssembledDirectory(sarFile)
         .addPackage(SomeMBean.class)
         .addPath("/reflect/sar");

      enableTrace("org.jboss.deployers");

      return ear;
   }

   protected VirtualFile createTopLevelWithUtil(String path) throws Exception
   {
      VirtualFile earFile = VFS.getChild(getName()).getChild("top-level.ear");
      createAssembledDirectory(earFile)
         .addPath(path)
         .addPackage("lib/util.jar", SomeUtil.class)
         .addPackage("lib/ext.jar", External.class);
      return earFile;
   }

   protected VirtualFile createWarInEar() throws Exception
   {
      VirtualFile earFile = VFS.getChild(getName()).getChild("war-in-ear.ear");
      createAssembledDirectory(earFile)
         .addPath("/reflect/warinear")
         .addPackage("simple.war/WEB-INF/classes", AnyServlet.class)
         .addPath("simple.war", "/reflect/web");
      return earFile;
   }

   protected VirtualFile createJarInEar() throws Exception
   {
      VirtualFile earFile = VFS.getChild(getName()).getChild("jar-in-ear.ear"); 
      createAssembledDirectory(earFile)
         .addPath("/reflect/jarinear")
         .addPackage("simple.jar", PlainJavaBean.class);
      return earFile;
   }

   protected Deployment createIsolatedDeployment(String name) throws Exception
   {
      return createIsolatedDeployment(name, null, PlainJavaBean.class);
   }

   protected Deployment createIsolatedDeployment(String name, String parentDomain, Class<?> reference) throws Exception
   {
      return createIsolatedDeployment(name, parentDomain, reference, null);
   }

   protected Deployment createIsolatedDeployment(String name, String parentDomain, Class<?> reference, ClassLoadingMetaData clmd) throws Exception
   {
      VirtualFile jar = createJar(name, reference);
      Deployment deployment = VFSDeploymentFactory.getInstance().createVFSDeployment(jar);

      if (clmd == null)
         clmd = createDefaultClassLoadingMetaData(name, parentDomain);

      MutableAttachments attachments = (MutableAttachments)deployment.getPredeterminedManagedObjects();
      attachments.addAttachment(ClassLoadingMetaData.class, clmd);

      return deployment;
   }

   protected ClassLoadingMetaData createDefaultClassLoadingMetaData(String name, String parentDomain)
   {
      ClassLoadingMetaData clmd = new ClassLoadingMetaData();
      if (name != null)
         clmd.setDomain(name + "_Domain");
      clmd.setParentDomain(parentDomain);
      clmd.setImportAll(true);
      clmd.setExportAll(ExportAll.NON_EMPTY);
      clmd.setVersion(Version.DEFAULT_VERSION);
      return clmd;
   }
}