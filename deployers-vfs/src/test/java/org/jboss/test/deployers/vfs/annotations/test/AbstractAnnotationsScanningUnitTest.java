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

import java.lang.annotation.Annotation;
import java.util.Set;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.mcann.AnnotationRepository;
import org.jboss.mcann.Element;
import org.jboss.test.deployers.BootstrapDeployersTest;
import org.jboss.test.deployers.vfs.annotations.support.NoExtRecurseFilter;
import org.jboss.test.deployers.vfs.annotations.support.ext.External;
import org.jboss.test.deployers.vfs.annotations.support.jar.JarMarkOnClass;
import org.jboss.test.deployers.vfs.annotations.support.jar.impl.JarMarkOnClassImpl;
import org.jboss.test.deployers.vfs.annotations.support.util.Util;
import org.jboss.test.deployers.vfs.annotations.support.war.WebMarkOnClass;
import org.jboss.test.deployers.vfs.annotations.support.war.impl.WebMarkOnClassImpl;
import org.jboss.test.deployers.vfs.annotations.support.warlib.SomeUIClass;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;

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
      VirtualFile ear = createTopLevelWithUtil();
      createAssembledDirectory(ear.getChild("simple.jar"))
        .addPackage(JarMarkOnClassImpl.class)
        .addPackage(JarMarkOnClass.class)
        .addPath("/annotations/basic-scan/jar");
      
      createAssembledDirectory(ear.getChild("simple.war"))
         .addPackage("WEB-INF/classes", WebMarkOnClassImpl.class)
         .addPackage("WEB-INF/classes", WebMarkOnClass.class)
         .addPackage("WEB-INF/lib/ui.jar", SomeUIClass.class)
         .addPath("/annotations/basic-scan/web");

      enableTrace("org.jboss.deployers");

      DeploymentUnit unit = assertDeploy(ear);
      assertEar(unit);
      try
      {
         AnnotationRepository env = unit.getAttachment(AnnotationRepository.class);
         assertNotNull(env);
         Set<Element<Annotation, Class<?>>> annotations = env.classIsAnnotatedWith("org.jboss.test.deployers.vfs.annotations.support.MarkedAnnotation");
         assertNotNull(annotations);
         assertEquals(1, annotations.size());

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
      AnnotationRepository env = unit.getAttachment(AnnotationRepository.class);
      assertNotNull(env);

      Set classes = env.classIsAnnotatedWith("org.jboss.test.deployers.vfs.annotations.support.Marked");
      assertNotNull(classes);
      assertEquals(classes.toString(), onClass, classes.size());

      Set methods = env.classHasMethodAnnotatedWith("org.jboss.test.deployers.vfs.annotations.support.Marked");
      assertNotNull(methods);
      assertEquals(methods.toString(), onMethod, methods.size());

      Set fields = env.classHasFieldAnnotatedWith("org.jboss.test.deployers.vfs.annotations.support.Marked");
      assertNotNull(fields);
      assertEquals(fields.toString(), onFiled, fields.size());
   }

   protected VirtualFile createTopLevelWithUtil() throws Exception
   {
      VirtualFile topLevel = VFS.getChild(getName()).getChild("top-level.ear");
      
      createAssembledDirectory(topLevel)
         .addPath("/annotations/basic-scan")
         .addPackage("lib/util.jar", Util.class)
         .addPackage("lib/ext.jar", External.class)
         .addPackage("lib/ann.jar", NoExtRecurseFilter.class);
      return topLevel;
   }
}