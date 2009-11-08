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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.net.URL;

import org.jboss.deployers.client.spi.DeployerClient;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.reflect.spi.TypeInfo;
import org.jboss.reflect.spi.TypeInfoFactory;
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

/**
 * Test case for TypeInfo.
 *
 * @author <a href="mailto:flavia.rainone@jboss.com">Flavia Rainone</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 *
 * @version $Revision$
 */
public abstract class TypeInfoTest extends ReflectTest
{
   public TypeInfoTest(String name)
   {
      super(name);
   }

   public void testJar() throws Exception
   {
      AssembledDirectory directory = createJar();
      assertTypeInfo(directory, PlainJavaBean.class);
   }

   public void testEjbJar() throws Exception
   {
      AssembledDirectory directory = createEjbJar();
      assertTypeInfo(directory, MySLSBean.class);
   }

   public void testWar() throws Exception
   {
      AssembledDirectory directory = createWar();
      assertTypeInfo(directory, AnyServlet.class);
   }

   public void testSar() throws Exception
   {
      AssembledDirectory directory = createSar();
      assertTypeInfo(directory, SomeMBean.class);
   }

   public void testBasicEar() throws Exception
   {
      AssembledDirectory directory = createBasicEar();
      Map<Class<?>, String> classes = new HashMap<Class<?>, String>();
      classes.put(SomeUtil.class, null);
      classes.put(PlainJavaBean.class, null);
      classes.put(MySLSBean.class, null);
      classes.put(AnyServlet.class, "simple.war");
      classes.put(UIBean.class, "simple.war");
      classes.put(JsfBean.class, "jsfapp.war");
      classes.put(CrmFacade.class, "jsfapp.war");
      classes.put(SomeMBean.class, null);
      assertTypeInfo(directory, classes);
   }

   public void testTopLevelWithUtil() throws Exception
   {
      AssembledDirectory directory = createTopLevelWithUtil("/reflect/earutil");
      assertTypeInfo(directory, SomeUtil.class, External.class);
   }

   public void testWarInEar() throws Exception
   {
      AssembledDirectory directory = createWarInEar();
      assertTypeInfo(directory, Collections.<Class<?>, String>singletonMap(AnyServlet.class, "simple.war"));
   }

   public void testJarInEar() throws Exception
   {
      AssembledDirectory directory = createJarInEar();
      assertTypeInfo(directory, PlainJavaBean.class);
   }

   public void testHierarchyCLUsage() throws Exception
   {
      AssembledDirectory directory = createBasicEar();
      DeploymentUnit unit = assertDeploy(directory);
      try
      {
         TypeInfoFactory typeInfoFactory = createTypeInfoFactory();
         DeploymentUnit child = getDeploymentUnit(unit, "simple.war");
         ClassLoader cl = getClassLoader(child);
         TypeInfo ti = typeInfoFactory.getTypeInfo(AnyServlet.class.getName(), cl);
         TypeInfo rt = assertReturnType(ti, "getBean");
         TypeInfo cti = typeInfoFactory.getTypeInfo(PlainJavaBean.class.getName(), getClassLoader(unit));
         assertSame(rt, cti);
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

         TypeInfoFactory factory = createTypeInfoFactory();
         TypeInfo ti1 = factory.getTypeInfo(PlainJavaBean.class.getName(), cl1);
         TypeInfo ti2 = factory.getTypeInfo(PlainJavaBean.class.getName(), cl2);
         assertNotEquals(ti1, ti2);
         TypeInfo ti3 = factory.getTypeInfo(clazz1);
         assertEquals(ti1, ti3);
         TypeInfo ti4 = factory.getTypeInfo(clazz2);
         assertEquals(ti2, ti4);
         assertNotEquals(ti3, ti4);
      }
      finally
      {
         main.undeploy(d1, d2);
      }
   }

   protected void testDomainHierarchy(String top, String left, String right, Deployment... deployments) throws DeploymentException, ClassNotFoundException
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

         TypeInfoFactory factory = createTypeInfoFactory();
         TypeInfo pjbTI = factory.getTypeInfo(PlainJavaBean.class.getName(), topCL);
         TypeInfo asTIL = factory.getTypeInfo(AnyServlet.class.getName(), leftCL);
         TypeInfo asTIR = factory.getTypeInfo(AnyServlet.class.getName(), rightCL);

         TypeInfo rtL = assertReturnType(asTIL, "getBean");
         assertEquals(pjbTI, rtL);

         TypeInfo rtR = assertReturnType(asTIR, "getBean");
         assertEquals(pjbTI, rtR);
      }
      finally
      {
         main.undeploy(deployments);
      }
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
      AssembledDirectory ear = createAssembledDirectory("ptd-ear-1.0-SNAPSHOT.ear", "ptd-ear-1.0-SNAPSHOT.ear");
      addPath(ear, "/reflect/ear2war", "META-INF");
      AssembledDirectory lib = ear.mkdir("lib");
      AssembledDirectory common = lib.mkdir("common.jar");
      addPackage(common, PlainJavaBean.class);

      AssembledDirectory war1 = ear.mkdir("ptd-jsf-1.0-SNAPSHOT.war");
      AssembledDirectory webinf1 = war1.mkdir("WEB-INF");
      addPath(war1, "/reflect/ear2war/war1/", "WEB-INF");
      AssembledDirectory classes1 = webinf1.mkdir("classes");
      addPackage(classes1, AnyServlet.class);

      AssembledDirectory war2 = ear.mkdir("ptd-ws-1.0-SNAPSHOT.war");
      AssembledDirectory webinf2 = war2.mkdir("WEB-INF");
      addPath(war2, "/reflect/ear2war/war2/", "WEB-INF");
      AssembledDirectory classes2 = webinf2.mkdir("classes");
      addPackage(classes2, AnyServlet.class);

      Deployment deployment = createVFSDeployment(ear);
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
         AssembledDirectory jar = createJar();
         addPath(jar, "/reflect/module", "META-INF");

         Deployment deployment = createVFSDeployment(jar);
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

            TypeInfoFactory factory = createTypeInfoFactory();
            TypeInfo asTIL = factory.getTypeInfo(anysClass);
            TypeInfo pjbTI = factory.getTypeInfo(PlainJavaBean.class.getName(), cl);
            TypeInfo rtL = assertReturnType(asTIL, "getBean");
            assertEquals(pjbTI, rtL);
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

   public void testHierarchy(String name) throws Exception
   {
      URL location = AnyServlet.class.getProtectionDomain().getCodeSource().getLocation();
      System.setProperty("jboss.tests.url", location.toExternalForm());
      try
      {
         AssembledDirectory jar = createJar();
         addPath(jar, "/reflect/" + name, "META-INF");

         Deployment deployment = createVFSDeployment(jar);
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

            TypeInfoFactory factory = createTypeInfoFactory();
            TypeInfo tifTIL = factory.getTypeInfo(tifClass);
            TypeInfo tifRT = assertReturnType(tifTIL, "getAnys");
            TypeInfo asTIL = factory.getTypeInfo(anysClass);
            assertEquals(tifRT, asTIL);
            TypeInfo pjbTI = factory.getTypeInfo(PlainJavaBean.class.getName(), cl);
            TypeInfo rtL = assertReturnType(asTIL, "getBean");
            assertEquals(pjbTI, rtL);
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
}