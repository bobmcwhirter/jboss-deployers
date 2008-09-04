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
package org.jboss.test.deployers.vfs.deployer.bean.test;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.jboss.beans.metadata.plugins.AbstractBeanMetaData;
import org.jboss.beans.metadata.spi.BeanMetaDataFactory;
import org.jboss.test.deployers.vfs.deployer.bean.support.SimpleAnnotatedAlias;
import org.jboss.test.deployers.vfs.deployer.bean.support.SimpleAnnotatedConstructor;
import org.jboss.test.deployers.vfs.deployer.bean.support.SimpleAnnotatedInjection;
import org.jboss.test.deployers.vfs.deployer.bean.support.SimpleAnnotatedStart;

/**
 * AnnotatedBeansUnitTestCase.
 *
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 */
public class AnnotatedBeansUnitTestCase extends AbstractAnnotationBeansTest
{
   public static Test suite()
   {
      return new TestSuite(AnnotatedBeansUnitTestCase.class);
   }

   public AnnotatedBeansUnitTestCase(String name) throws Throwable
   {
      super(name);
   }

   protected BeanMetaDataFactory getConstructorTester()
   {
      return new AbstractBeanMetaData("Constructor", SimpleAnnotatedConstructor.class.getName());
   }

   protected BeanMetaDataFactory getInjectionTester()
   {
      return new AbstractBeanMetaData("Injection", SimpleAnnotatedInjection.class.getName());
   }

   protected BeanMetaDataFactory getStartTester()
   {
      return new AbstractBeanMetaData("Start", SimpleAnnotatedStart.class.getName());
   }

   protected BeanMetaDataFactory getAliasTester()
   {
      return new AbstractBeanMetaData("SomeRandomName", SimpleAnnotatedAlias.class.getName());
   }
}