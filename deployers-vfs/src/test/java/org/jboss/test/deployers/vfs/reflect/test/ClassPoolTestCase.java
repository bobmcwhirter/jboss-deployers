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

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import junit.framework.Test;

import org.jboss.classpool.spi.ClassPoolRepository;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.reflect.plugins.javassist.JavassistTypeInfoFactory;
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
      AssembledDirectory directory = createTopLevelWithUtil("/reflect/earutil");
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

   public void testHierarchyCLUsage() throws Exception
   {
      AssembledDirectory directory = createBasicEar();
      DeploymentUnit unit = assertDeploy(directory);
      try
      {
         DeploymentUnit child = getDeploymentUnit(unit, "simple.war");
         ClassLoader cl = getClassLoader(child);
         ClassPoolRepository repository = ClassPoolRepository.getInstance();
         ClassPool classPool = repository.registerClassLoader(cl);
         CtClass ctClass = classPool.getCtClass(AnyServlet.class.getName());
         CtMethod ctMethod = ctClass.getDeclaredMethod("getBean");
         assertNotNull("No such 'getBean' method on " + ctClass, ctMethod);
         CtClass returnCtClass = ctMethod.getReturnType();
         classPool = repository.registerClassLoader(getClassLoader(unit));
         CtClass returnCtClass2 = classPool.getCtClass(PlainJavaBean.class.getName());
         assertSame(returnCtClass, returnCtClass2);
      }
      finally
      {
         undeploy(unit);
      }
   }
}