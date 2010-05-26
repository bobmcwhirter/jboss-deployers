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
package org.jboss.deployers.vfs.deployer.kernel;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.jboss.beans.metadata.api.annotations.Bean;
import org.jboss.beans.metadata.api.annotations.BeanFactory;
import org.jboss.beans.metadata.plugins.AbstractAliasMetaData;
import org.jboss.beans.metadata.spi.AliasMetaData;
import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.beans.metadata.spi.factory.GenericBeanFactoryMetaData;
import org.jboss.deployers.plugins.annotations.AbstractAnnotationDeployer;
import org.jboss.deployers.spi.deployer.helpers.AbstractAnnotationProcessor;

/**
 * BeanScanningDeployer.<p>
 * <p/>
 * This deployer is responsible for looking for @Bean(Factory)
 * and creating the metadata object.
 *
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 */
public class BeanScanningDeployer extends AbstractAnnotationDeployer
{
   public BeanScanningDeployer()
   {
      super(new BeanAnnotationProcessor(), new BeanFactoryAnnotationProcessor());
   }

   private static class BeanAnnotationProcessor extends AbstractAnnotationProcessor<Bean, BeanMetaData>
   {
      public Class<Bean> getAnnotation()
      {
         return Bean.class;
      }

      public Class<BeanMetaData> getOutput()
      {
         return BeanMetaData.class;
      }

      protected BeanMetaData createMetaDataFromClass(Class<?> clazz, Bean bean)
      {
         String name = bean.name();
         if (name == null)
            throw new IllegalArgumentException("Null bean name: " + clazz);

         BeanMetaDataBuilder builder = BeanMetaDataBuilder.createBuilder(name, clazz.getName());
         String[] aliases = bean.aliases();
         if (aliases != null && aliases.length > 0)
            builder.setAliases(new HashSet<Object>(Arrays.asList(aliases)));
         builder.setMode(bean.mode())
               .setAccessMode(bean.accessMode())
               .setAutowireType(bean.autowireType())
               .setErrorHandlingMode(bean.errorHandlingMode())
               .setAutowireCandidate(bean.autowireCandidate());
         return builder.getBeanMetaData();
      }
   }

   private static class BeanFactoryAnnotationProcessor extends AbstractAnnotationProcessor<BeanFactory, BeanMetaData>
   {
      public Class<BeanFactory> getAnnotation()
      {
         return BeanFactory.class;
      }

      public Class<BeanMetaData> getOutput()
      {
         return BeanMetaData.class;
      }

      protected BeanMetaData createMetaDataFromClass(Class<?> clazz, BeanFactory factory)
      {
         String name = factory.name();
         if (name == null)
            throw new IllegalArgumentException("Null bean name: " + factory);

         GenericBeanFactoryMetaData gbfmd = new GenericBeanFactoryMetaData(name, clazz.getName());
         Class<?> factoryClass = factory.getFactoryClass();
         if (void.class.equals(factoryClass) == false)
            gbfmd.setFactoryClass(factoryClass.getName());
         String[] aliases = factory.aliases();
         if (aliases != null && aliases.length > 0)
         {
            Set<AliasMetaData> aliasesMD = new HashSet<AliasMetaData>();
            for (String alias : aliases)
            {
               AbstractAliasMetaData aamd = new AbstractAliasMetaData();
               aamd.setAlias(alias);
               aliasesMD.add(aamd);
            }
            gbfmd.setAliases(aliasesMD);
         }
         gbfmd.setMode(factory.mode());
         gbfmd.setAccessMode(factory.accessMode());

         return gbfmd.getBeanMetaData();
      }
   }
}