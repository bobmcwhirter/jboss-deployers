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

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.helpers.AbstractRealDeployerWithInput;
import org.jboss.deployers.spi.deployer.helpers.DeploymentVisitor;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.metadata.plugins.loader.reflection.AnnotatedElementMetaDataLoader;
import org.jboss.metadata.plugins.repository.basic.BasicMetaDataRepository;
import org.jboss.metadata.spi.scope.Scope;
import org.jboss.metadata.spi.scope.ScopeKey;

/**
 * TestComponentMetaDataRepositoryPopulator.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class TestComponentMetaDataRepositoryLoader extends AbstractRealDeployerWithInput<TestComponentMetaData>
{
   private BasicMetaDataRepository repository;

   public TestComponentMetaDataRepositoryLoader()
   {
      setDeploymentVisitor(new TestComponentMetaDataVisitor());
      setWantComponents(true);
      setOutput(TestComponentMetaData.class);
   }
   
   public void setRepository(BasicMetaDataRepository repository)
   {
      this.repository = repository;
   }
   
   public class TestComponentMetaDataVisitor implements DeploymentVisitor<TestComponentMetaData>
   {
      public Class<TestComponentMetaData> getVisitorType()
      {
         return TestComponentMetaData.class;
      }

      public void deploy(DeploymentUnit unit, TestComponentMetaData deployment) throws DeploymentException
      {
         // Create a scope for the class
         AnnotatedElementMetaDataLoader loader = new AnnotatedElementMetaDataLoader(deployment.clazz);
         repository.addMetaDataRetrieval(loader);

         // Add it to our scope
         ScopeKey key = unit.getScope();
         for (Scope scope : loader.getScope().getScopes())
            key.addScope(scope);
         
         // Add the loader
         ScopeKey mutableScope = unit.getMutableScope();
         TestComponentMetaDataLoader componentMetaDataLoader = new TestComponentMetaDataLoader(mutableScope, deployment);
         repository.addMetaDataRetrieval(componentMetaDataLoader);
      }

      public void undeploy(DeploymentUnit unit, TestComponentMetaData deployment)
      {
         // Remove the scopes
         AnnotatedElementMetaDataLoader loader = new AnnotatedElementMetaDataLoader(deployment.getClass());
         repository.removeMetaDataRetrieval(loader.getScope());

         ScopeKey mutableScope = unit.getMutableScope();
         repository.removeMetaDataRetrieval(mutableScope);
      }
   }
}
