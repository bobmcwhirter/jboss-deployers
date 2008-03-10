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

import java.lang.annotation.Annotation;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.helpers.AbstractRealDeployerWithInput;
import org.jboss.deployers.spi.deployer.helpers.DeploymentVisitor;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.metadata.spi.MutableMetaData;
import org.jboss.metadata.spi.scope.CommonLevels;
import org.jboss.metadata.spi.scope.ScopeKey;

/**
 * TestComponentMetaDataRepositoryPopulator.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class TestComponentMetaDataRepositoryPopulator extends AbstractRealDeployerWithInput<TestComponentMetaData>
{
   public TestComponentMetaDataRepositoryPopulator()
   {
      setDeploymentVisitor(new TestComponentMetaDataVisitor());
      setWantComponents(true);
      setOutput(TestComponentMetaData.class);
   }
   
   public class TestComponentMetaDataVisitor implements DeploymentVisitor<TestComponentMetaData>
   {
      public Class<TestComponentMetaData> getVisitorType()
      {
         return TestComponentMetaData.class;
      }

      public void deploy(DeploymentUnit unit, TestComponentMetaData deployment) throws DeploymentException
      {
         // Add in the class scope
         ScopeKey key = unit.getScope();
         key.addScope(CommonLevels.CLASS, deployment.clazz);
         
         // Populate the instance annotations
         MutableMetaData mutable = unit.getMutableMetaData();
         for (Annotation annotation : deployment.classAnnotations)
            mutable.addAnnotation(annotation);
      }

      public void undeploy(DeploymentUnit unit, TestComponentMetaData deployment)
      {
      }
   }
}
