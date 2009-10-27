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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.reflect.plugins.javassist.JavassistTypeInfoFactory;
import org.jboss.reflect.spi.ClassInfo;
import org.jboss.reflect.spi.MethodInfo;
import org.jboss.reflect.spi.TypeInfo;
import org.jboss.reflect.spi.TypeInfoFactory;
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
public class ClassPoolTestCase extends ClassPoolTest
{
   public ClassPoolTestCase(String name)
   {
      super(name);
   }

   public static Test suite()
   {
      return suite(ClassPoolTestCase.class);
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
      Map<Class<?>, String> classes = new HashMap<Class<?>, String>();
      classes.put(SomeUtil.class, null);
      classes.put(PlainJavaBean.class, null);
      classes.put(MySLSBean.class, null);
      classes.put(AnyServlet.class, "simple.war");
      classes.put(UIBean.class, "simple.war");
      classes.put(JsfBean.class, "jsfapp.war");
      classes.put(CrmFacade.class, "jsfapp.war");
      classes.put(SomeMBean.class, null);
      assertClassPool(directory, classes);
   }
   
   public void testTopLevelWithUtil() throws Exception 
   {
      AssembledDirectory directory = createTopLevelWithUtil("/classpool/earutil");
      assertClassPool(directory, SomeUtil.class, External.class);
   }
   
   public void testWarInEar() throws Exception 
   {
      AssembledDirectory directory = createWarInEar();
      assertClassPool(directory, Collections.<Class<?>, String>singletonMap(AnyServlet.class, "simple.war"));
   }
   
   public void testJarInEar() throws Exception 
   {
      AssembledDirectory directory = createJarInEar();
      assertClassPool(directory, PlainJavaBean.class);
   }

   /**
    * FIXME -- classpools issue?
    */
   public void testHierarchyCLUsage() throws Exception
   {
      AssembledDirectory directory = createBasicEar();
      DeploymentUnit unit = assertDeploy(directory);
      try
      {
         TypeInfoFactory typeInfoFactory = new JavassistTypeInfoFactory();
         DeploymentUnit child = getDeploymentUnit(unit, "simple.war");
         ClassLoader cl = getClassLoader(child);
         TypeInfo ti = typeInfoFactory.getTypeInfo(AnyServlet.class.getName(), cl);
         ClassInfo ci = assertInstanceOf(ti, ClassInfo.class);
         MethodInfo mi = ci.getDeclaredMethod("getBean");
         TypeInfo rt = mi.getReturnType();
         TypeInfo cti = typeInfoFactory.getTypeInfo(PlainJavaBean.class.getName(), getClassLoader(unit));
         assertSame(rt, cti);
      }
      finally
      {
         undeploy(unit);
      }
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
}