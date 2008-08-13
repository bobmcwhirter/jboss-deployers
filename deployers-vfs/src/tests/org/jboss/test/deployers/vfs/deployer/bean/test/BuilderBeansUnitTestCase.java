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

import java.lang.annotation.Annotation;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.jboss.beans.metadata.api.annotations.Aliases;
import org.jboss.beans.metadata.spi.BeanMetaDataFactory;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.dependency.spi.Controller;
import org.jboss.kernel.plugins.bootstrap.basic.KernelConstants;
import org.jboss.test.deployers.vfs.deployer.bean.support.SimpleAliasTester;
import org.jboss.test.deployers.vfs.deployer.bean.support.SimpleConstructorTester;
import org.jboss.test.deployers.vfs.deployer.bean.support.SimpleInjectionTester;
import org.jboss.test.deployers.vfs.deployer.bean.support.SimpleStartTester;

/**
 * BuilderBeansUnitTestCase.
 *
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 */
public class BuilderBeansUnitTestCase extends AbstractAnnotationBeansTest
{
   public static Test suite()
   {
      return new TestSuite(BuilderBeansUnitTestCase.class);
   }

   public BuilderBeansUnitTestCase(String name) throws Throwable
   {
      super(name);
   }

   protected BeanMetaDataFactory getConstructorTester()
   {
      BeanMetaDataBuilder builder = BeanMetaDataBuilder.createBuilder("Constructor", SimpleConstructorTester.class.getName());
      builder.addConstructorParameter(Controller.class.getName(), builder.createInject(KernelConstants.KERNEL_CONTROLLER_NAME));
      return builder.getBeanMetaDataFactory();
   }

   protected BeanMetaDataFactory getInjectionTester()
   {
      BeanMetaDataBuilder builder = BeanMetaDataBuilder.createBuilder("Injection", SimpleInjectionTester.class.getName());
      builder.addPropertyMetaData("controller", builder.createInject(KernelConstants.KERNEL_CONTROLLER_NAME));
      return builder.getBeanMetaDataFactory();
   }

   protected BeanMetaDataFactory getStartTester()
   {
      BeanMetaDataBuilder builder = BeanMetaDataBuilder.createBuilder("Start", SimpleStartTester.class.getName());
      builder.setStart("onStart").addStartParameter(Controller.class.getName(), builder.createInject(KernelConstants.KERNEL_CONTROLLER_NAME));      
      return builder.getBeanMetaDataFactory();
   }

   protected BeanMetaDataFactory getAliasTester()
   {
      BeanMetaDataBuilder builder = BeanMetaDataBuilder.createBuilder("SomeRandomName", SimpleAliasTester.class.getName());
      builder.setFactoryMethod("factory");
      builder.addPropertyMetaData("controller", builder.createInject(KernelConstants.KERNEL_CONTROLLER_NAME));
      builder.addAnnotation(new AliasesImpl("Alias"));
      return builder.getBeanMetaDataFactory();
   }

   @SuppressWarnings({"ClassExplicitlyAnnotation"})
   private class AliasesImpl implements Aliases
   {
      private String[] value;

      private AliasesImpl(String... aliases)
      {
         this.value = aliases;
      }

      public String[] value()
      {
         return value; 
      }

      public boolean replace()
      {
         return false;
      }

      public Class<? extends Annotation> annotationType()
      {
         return Aliases.class;
      }
   }
}