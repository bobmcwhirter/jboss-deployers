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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.beans.metadata.api.annotations.Bean;
import org.jboss.beans.metadata.api.annotations.BeanFactory;
import org.jboss.beans.metadata.plugins.AbstractAliasMetaData;
import org.jboss.beans.metadata.spi.AliasMetaData;
import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.beans.metadata.spi.factory.GenericBeanFactoryMetaData;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.annotations.AnnotationEnvironment;
import org.jboss.deployers.spi.deployer.helpers.AbstractSimpleRealDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;

/**
 * BeanScanningDeployer.<p>
 * <p/>
 * This deployer is responsible for looking for @Bean(Factory)
 * and creating the metadata object.
 *
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 */
public class BeanScanningDeployer extends AbstractSimpleRealDeployer<AnnotationEnvironment>
{
   public BeanScanningDeployer()
   {
      this(null);
   }

   /**
    * We depend on KernelDeploymentDeployer for the order if it's present,
    * but can be null, then order doesn't matter, but we still go +10.
    *
    * @param kdd the kernel deployment deployer
    */
   public BeanScanningDeployer(KernelDeploymentDeployer kdd)
   {
      super(AnnotationEnvironment.class);
      setInputs(BeanMetaData.class);
      setOutput(BeanMetaData.class);
      if (kdd != null)
         setRelativeOrder(kdd.getRelativeOrder() + 10);
      else
         setRelativeOrder(getRelativeOrder() + 10);
   }

   /**
    * Add beam metadata as component.
    *
    * @param unit the deployment unit
    * @param bean the bean metadata
    */
   protected static void addBeanComponent(DeploymentUnit unit, BeanMetaData bean)
   {
      DeploymentUnit component = unit.addComponent(bean.getName());
      component.addAttachment(BeanMetaData.class.getName(), bean);
   }

   public void deploy(DeploymentUnit unit, AnnotationEnvironment env) throws DeploymentException
   {
      Map<String, DeploymentUnit> components = null;

      Set<Class<?>> beans = env.classIsAnnotatedWith(Bean.class);
      if (beans != null && beans.isEmpty() == false)
      {
         components = new HashMap<String, DeploymentUnit>();
         mapComponents(unit, components);

         for (Class<?> beanClass : beans)
         {
            Bean bean = beanClass.getAnnotation(Bean.class);
            String name = bean.name();
            if (name == null)
               throw new IllegalArgumentException("Null bean name: " + beanClass);

            DeploymentUnit component = components.get(name);
            BeanMetaData bmd = null;
            if (component != null)
               bmd = component.getAttachment(BeanMetaData.class);

            if (bmd == null)
            {
               BeanMetaDataBuilder builder = BeanMetaDataBuilder.createBuilder(name, beanClass.getName());
               String[] aliases = bean.aliases();
               if (aliases != null && aliases.length > 0)
                  builder.setAliases(new HashSet<Object>(Arrays.asList(aliases)));
               builder.setMode(bean.mode())
                      .setAccessMode(bean.accessMode())
                      .setAutowireType(bean.autowireType())
                      .setErrorHandlingMode(bean.errorHandlingMode())
                      .setAutowireCandidate(bean.autowireCandidate());

               addBeanComponent(unit, builder.getBeanMetaData());
            }
            else
            {
               // TODO should we do something .. or leave it to previous metadata?
               log.info("BeanMetaData with such name already exists: " + bmd + ", scanned: " + beanClass);
            }
         }
      }

      Set<Class<?>> beanFactories = env.classIsAnnotatedWith(BeanFactory.class);
      if (beanFactories != null && beanFactories.isEmpty() == false)
      {
         if (components == null)
         {
            components = new HashMap<String, DeploymentUnit>();
            mapComponents(unit, components);
         }

         for (Class<?> beanFactoryClass : beanFactories)
         {
            BeanFactory beanFactory = beanFactoryClass.getAnnotation(BeanFactory.class);
            String name = beanFactory.name();
            if (name == null)
               throw new IllegalArgumentException("Null bean name: " + beanFactoryClass);

            DeploymentUnit component = components.get(name);
            BeanMetaData bmd = null;
            if (component != null)
               bmd = component.getAttachment(BeanMetaData.class);

            if (bmd == null)
            {
               GenericBeanFactoryMetaData gbfmd = new GenericBeanFactoryMetaData(name, beanFactoryClass.getName());
               String[] aliases = beanFactory.aliases();
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
               gbfmd.setMode(beanFactory.mode());
               gbfmd.setAccessMode(beanFactory.accessMode());

               List<BeanMetaData> bfBeans = gbfmd.getBeans();
               for (BeanMetaData bfb : bfBeans)
                  addBeanComponent(unit, bfb);
            }
            else
            {
               // TODO should we do something .. or leave it to previous metadata?               
               log.info("BeanMetaData with such name already exists: " + bmd + ", scanned: " + beanFactoryClass);
            }
         }
      }
   }

   /**
    * Map components.
    *
    * @param unit the deployment unit
    * @param map  map to fill
    */
   protected static void mapComponents(DeploymentUnit unit, Map<String, DeploymentUnit> map)
   {
      List<DeploymentUnit> components = unit.getComponents();
      if (components != null && components.isEmpty() == false)
      {
         for (DeploymentUnit component : components)
         {
            map.put(component.getName(), component);
            mapComponents(component, map);
         }
      }
   }
}