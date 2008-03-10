/*
* JBoss, Home of Professional Open Source
* Copyright 2007, JBoss Inc., and individual contributors as indicated
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
package org.jboss.test.deployers.scope.support;

import org.jboss.aop.AspectManager;
import org.jboss.aop.advice.AdviceBinding;
import org.jboss.aop.advice.GenericInterceptorFactory;
import org.jboss.aop.proxy.container.AOPProxyFactory;
import org.jboss.aop.proxy.container.AOPProxyFactoryParameters;
import org.jboss.aop.proxy.container.GeneratedAOPProxyFactory;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.helpers.AbstractRealDeployerWithInput;
import org.jboss.deployers.spi.deployer.helpers.DeploymentVisitor;
import org.jboss.deployers.structure.spi.DeploymentUnit;

/**
 * TestComponentMetaDataRepositoryPopulator.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class TestComponentCreator extends AbstractRealDeployerWithInput<TestComponentMetaData>
{
   AOPProxyFactory proxyFactory;
   
   public TestComponentCreator()
   {
      setDeploymentVisitor(new TestComponentMetaDataVisitor());
      setWantComponents(true);
      proxyFactory = new GeneratedAOPProxyFactory();
      
      AspectManager domain = AspectManager.instance();
      
      try
      {
         AdviceBinding binding = new AdviceBinding("execution(* @" + TestClassAnnotation.class.getName() + "->*(..))", null);
         binding.addInterceptorFactory(new GenericInterceptorFactory(TestClassAspect.class));
         domain.addBinding(binding);
      }
      catch (Exception e)
      {
         throw new RuntimeException("Error setting up advices ", e);
      }
   }
   
   public class TestComponentMetaDataVisitor implements DeploymentVisitor<TestComponentMetaData>
   {
      public Class<TestComponentMetaData> getVisitorType()
      {
         return TestComponentMetaData.class;
      }

      public void deploy(DeploymentUnit unit, TestComponentMetaData deployment) throws DeploymentException
      {
         Class<?> deploymentClass = deployment.clazz;
         
         Object target;
         
         try
         {
            target = deploymentClass.newInstance();
         }
         catch (Exception e)
         {
            throw DeploymentException.rethrowAsDeploymentException("Error instantiating object", e);
         }
         
         AOPProxyFactoryParameters params = new AOPProxyFactoryParameters();
         params.setTarget(target);
         params.setMetaData(unit.getMetaData());
         params.setMetaDataHasInstanceLevelData(true);
         Object proxy = proxyFactory.createAdvisedProxy(params);
         unit.addAttachment("proxy", proxy);
      }

      public void undeploy(DeploymentUnit unit, TestComponentMetaData deployment)
      {
         unit.removeAttachment("proxy");
      }
   }
}
