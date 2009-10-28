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

import junit.framework.Test;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.reflect.plugins.javassist.JavassistTypeInfoFactory;
import org.jboss.reflect.spi.ClassInfo;
import org.jboss.reflect.spi.MethodInfo;
import org.jboss.reflect.spi.TypeInfo;
import org.jboss.reflect.spi.TypeInfoFactory;
import org.jboss.test.deployers.vfs.classpool.support.jar.PlainJavaBean;
import org.jboss.test.deployers.vfs.classpool.support.web.AnyServlet;
import org.jboss.virtual.AssembledDirectory;

/**
 * Javassist test case for TypeInfo.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 *
 * @version $Revision: 95613 $
 */
public class JavassistTypeInfoTestCase extends TypeInfoTest
{
   public JavassistTypeInfoTestCase(String name)
   {
      super(name);
   }

   public static Test suite()
   {
      return suite(JavassistTypeInfoTestCase.class);
   }

   protected TypeInfoFactory createTypeInfoFactory()
   {
      return new JavassistTypeInfoFactory();
   }

   @Override
   public void testHierarchyCLUsage() throws Exception
   {
      AssembledDirectory directory = createBasicEar();
      DeploymentUnit unit = assertDeploy(directory);
      try
      {
         TypeInfoFactory typeInfoFactory = new JavassistTypeInfoFactory();
         DeploymentUnit child = getDeploymentUnit(unit, "simple.war");
         ClassLoader cl = getClassLoader(child);
         
         try
         {
            cl.loadClass(PlainJavaBean.class.getName());
         } catch (ClassNotFoundException e) {}
         
         
         ClassLoader cl1 = getClassLoader(unit);
         try
         {
            cl1.loadClass(PlainJavaBean.class.getName());
         } catch (ClassNotFoundException e) {}
         
         TypeInfo ti = typeInfoFactory.getTypeInfo(AnyServlet.class.getName(), cl);
         ClassInfo ci = assertInstanceOf(ti, ClassInfo.class);
         MethodInfo mi = ci.getDeclaredMethod("getBean");
         assertNotNull("No such 'getBean' method on " + ci, mi);
         TypeInfo rt = mi.getReturnType();
         TypeInfo cti = typeInfoFactory.getTypeInfo(PlainJavaBean.class.getName(), getClassLoader(unit));
         assertSame(rt, cti);
      }
      finally
      {
         undeploy(unit);
      }
   }
}