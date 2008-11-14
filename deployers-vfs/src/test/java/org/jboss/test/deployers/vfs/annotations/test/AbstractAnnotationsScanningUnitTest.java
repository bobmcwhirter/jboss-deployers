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
package org.jboss.test.deployers.vfs.annotations.test;

import java.util.Set;

import org.jboss.deployers.spi.annotations.AnnotationEnvironment;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.test.deployers.BootstrapDeployersTest;
import org.jboss.test.deployers.vfs.annotations.support.ext.External;
import org.jboss.test.deployers.vfs.annotations.support.jar.JarMarkOnClass;
import org.jboss.test.deployers.vfs.annotations.support.jar.impl.JarMarkOnClassImpl;
import org.jboss.test.deployers.vfs.annotations.support.util.Util;
import org.jboss.test.deployers.vfs.annotations.support.war.WebMarkOnClass;
import org.jboss.test.deployers.vfs.annotations.support.war.impl.WebMarkOnClassImpl;
import org.jboss.test.deployers.vfs.annotations.support.warlib.SomeUIClass;
import org.jboss.virtual.AssembledDirectory;

/**
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public abstract class AbstractAnnotationsScanningUnitTest extends BootstrapDeployersTest
{
   protected AbstractAnnotationsScanningUnitTest(String name)
   {
      super(name);
   }

   public void testBasicScanning() throws Throwable
   {
      AssembledDirectory ear = createTopLevelWithUtil();

      AssembledDirectory jar = ear.mkdir("simple.jar");
      addPackage(jar, JarMarkOnClassImpl.class);
      addPackage(jar, JarMarkOnClass.class);
      addPath(jar, "/annotations/basic-scan/jar", "META-INF");

      AssembledDirectory war = ear.mkdir("simple.war");
      AssembledDirectory webinf = war.mkdir("WEB-INF");
      AssembledDirectory classes = webinf.mkdir("classes");
      addPackage(classes, WebMarkOnClassImpl.class);
      addPackage(classes, WebMarkOnClass.class);
      AssembledDirectory lib = webinf.mkdir("lib");
      AssembledDirectory uijar = lib.mkdir("ui.jar");
      addPackage(uijar, SomeUIClass.class);
      addPath(war, "/annotations/basic-scan/web", "WEB-INF");

      enableTrace("org.jboss.deployers");

      DeploymentUnit unit = assertDeploy(ear);
      assertEar(unit);
      try
      {
         DeploymentUnit jarUnit = assertChild(unit, "simple.jar");
         assertJar(jarUnit);
         DeploymentUnit webUnit = assertChild(unit, "simple.war");
         assertWar(webUnit);
      }
      finally
      {
         undeploy(unit);
      }
   }

   protected abstract void assertEar(DeploymentUnit ear);
   protected abstract void assertJar(DeploymentUnit jar);
   protected abstract void assertWar(DeploymentUnit war);

   @SuppressWarnings("unchecked")
   protected void assertAnnotations(DeploymentUnit unit, int onClass, int onMethod, int onFiled)
   {
      AnnotationEnvironment env = unit.getAttachment(AnnotationEnvironment.class);
      assertNotNull(env);

      Set classes = env.classIsAnnotatedWith("org.jboss.test.deployers.vfs.annotations.support.Marked");
      assertNotNull(classes);
      assertEquals(onClass, classes.size());

      Set methods = env.classHasMethodAnnotatedWith("org.jboss.test.deployers.vfs.annotations.support.Marked");
      assertNotNull(methods);
      assertEquals(onMethod, methods.size());

      Set fields = env.classHasFieldAnnotatedWith("org.jboss.test.deployers.vfs.annotations.support.Marked");
      assertNotNull(fields);
      assertEquals(onFiled, fields.size());
   }

   protected AssembledDirectory createTopLevelWithUtil() throws Exception
   {
      AssembledDirectory topLevel = createAssembledDirectory("top-level.ear", "top-level.ear");
      addPath(topLevel, "/annotations/basic-scan", "META-INF");
      AssembledDirectory earLib = topLevel.mkdir("lib");
      AssembledDirectory util = earLib.mkdir("util.jar");
      addPackage(util, Util.class);
      AssembledDirectory ext = earLib.mkdir("ext.jar");
      addPackage(ext, External.class);
      return topLevel;
   }
}