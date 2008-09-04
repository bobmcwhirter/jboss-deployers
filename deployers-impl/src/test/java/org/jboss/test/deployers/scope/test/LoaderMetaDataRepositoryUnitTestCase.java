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
package org.jboss.test.deployers.scope.test;

import java.lang.annotation.Annotation;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.annotation.factory.AnnotationCreator;
import org.jboss.deployers.client.spi.DeployerClient;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.plugins.deployers.DeployersImpl;
import org.jboss.deployers.spi.attachments.MutableAttachments;
import org.jboss.deployers.spi.attachments.PredeterminedManagedObjectAttachments;
import org.jboss.deployers.spi.deployer.Deployers;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.metadata.plugins.repository.basic.BasicMetaDataRepository;
import org.jboss.test.deployers.AbstractDeployerTest;
import org.jboss.test.deployers.scope.support.TestClassAnnotation;
import org.jboss.test.deployers.scope.support.TestClassAspect;
import org.jboss.test.deployers.scope.support.TestComponent1;
import org.jboss.test.deployers.scope.support.TestComponent2;
import org.jboss.test.deployers.scope.support.TestComponentCreator;
import org.jboss.test.deployers.scope.support.TestComponentDeployer;
import org.jboss.test.deployers.scope.support.TestComponentMetaData;
import org.jboss.test.deployers.scope.support.TestComponentMetaDataContainer;
import org.jboss.test.deployers.scope.support.TestComponentMetaDataRepositoryLoader;

/**
 * LoaderMetaDataRepositoryUnitTestCase.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class LoaderMetaDataRepositoryUnitTestCase extends AbstractDeployerTest
{
   private TestComponentDeployer deployer1 = new TestComponentDeployer();
   private TestComponentMetaDataRepositoryLoader deployer2 = new TestComponentMetaDataRepositoryLoader();
   private TestComponentCreator deployer3 = new TestComponentCreator();

   private BasicMetaDataRepository repository;
   
   public static Test suite()
   {
      return new TestSuite(LoaderMetaDataRepositoryUnitTestCase.class);
   }
   
   public LoaderMetaDataRepositoryUnitTestCase(String name)
   {
      super(name);
   }

   public void testClassAnnotation() throws Exception
   {
      DeployerClient main = getMainDeployer();
      
      Deployment a = createSimpleDeployment("A");
      TestComponentMetaData c = new TestComponentMetaData("C");
      c.clazz = TestComponent1.class;
      TestComponentMetaDataContainer md = new TestComponentMetaDataContainer(c);
      addMetaData(a, md);
      main.addDeployment(a);
      try
      {
         main.process();
         main.checkComplete();
         DeploymentUnit unit = getDeploymentUnit(main, "A");
         List<DeploymentUnit> components = unit.getComponents();
         assertEquals(1, components.size());
         DeploymentUnit component = components.get(0);
         TestComponent1 proxy = component.getAttachment("proxy", TestComponent1.class);
         
         TestClassAspect.classAnnotation = null;
         proxy.doSomething();
         
         TestClassAnnotation annotation = TestClassAspect.classAnnotation;
         assertNotNull(annotation);
         assertEquals("Class", annotation.where());
      }
      finally
      {
         main.removeDeployment(a);
         main.process();
      }
   }

   public void testInstanceAnnotation() throws Exception
   {
      DeployerClient main = getMainDeployer();
      
      Deployment a = createSimpleDeployment("A");
      TestComponentMetaData c = new TestComponentMetaData("C");
      c.clazz = TestComponent2.class;
      c.classAnnotations.add((Annotation) AnnotationCreator.createAnnotation("@" + TestClassAnnotation.class.getName() + "(where=\"Instance\")", getClass().getClassLoader()));
      TestComponentMetaDataContainer md = new TestComponentMetaDataContainer(c);
      addMetaData(a, md);
      main.addDeployment(a);
      try
      {
         main.process();
         main.checkComplete();
         DeploymentUnit unit = getDeploymentUnit(main, "A");
         List<DeploymentUnit> components = unit.getComponents();
         assertEquals(1, components.size());
         DeploymentUnit component = components.get(0);
         TestComponent2 proxy = component.getAttachment("proxy", TestComponent2.class);
         
         TestClassAspect.classAnnotation = null;
         proxy.doSomething();
         
         TestClassAnnotation annotation = TestClassAspect.classAnnotation;
         assertNotNull(annotation);
         assertEquals("Instance", annotation.where());
      }
      finally
      {
         main.removeDeployment(a);
         main.process();
      }
   }
   
   protected static void addMetaData(PredeterminedManagedObjectAttachments attachments, TestComponentMetaDataContainer md)
   {
      MutableAttachments mutable = (MutableAttachments) attachments.getPredeterminedManagedObjects();
      mutable.addAttachment(TestComponentMetaDataContainer.class, md);
   }
   
   protected DeployerClient getMainDeployer()
   {
      return createMainDeployer(deployer1, deployer2, deployer3);
   }

   @Override
   protected Deployers createDeployers()
   {
      DeployersImpl deployers = (DeployersImpl) super.createDeployers();
      repository = new BasicMetaDataRepository();
      deployers.setRepository(repository);
      deployer2.setRepository(repository);
      return deployers;
   }
}
