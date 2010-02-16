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
package org.jboss.test.deployers.vfs.structure.ear.test;

import java.util.HashSet;
import java.util.Set;

import org.jboss.deployers.vfs.plugins.structure.file.FileStructure;
import org.jboss.deployers.vfs.plugins.structure.jar.JARStructure;
import org.jboss.deployers.vfs.plugins.structure.war.WARStructure;
import org.jboss.deployers.vfs.spi.client.VFSDeployment;
import org.jboss.deployers.vfs.spi.structure.StructureDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentContext;
import org.jboss.test.deployers.vfs.structure.AbstractStructureTest;
import org.jboss.test.deployers.vfs.structure.ear.support.AppClient;
import org.jboss.test.deployers.vfs.structure.ear.support.MockEarStructureDeployer;
import org.jboss.test.deployers.vfs.structure.ear.support.Service;
import org.jboss.test.deployers.vfs.structure.ear.support.Servlet;
import org.jboss.test.deployers.vfs.structure.ear.support.Stateless;

/**
 * Abstract ear structure deployer tests
 *
 * @author ales.justin@jboss.org
 */
public abstract class AbstractEARStructureTest extends AbstractStructureTest
{
   protected AbstractEARStructureTest(String name)
   {
      super(name);
   }

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      enableTrace("org.jboss.deployers");
   }

   protected StructureDeployer createEarStructureDeployer()
   {
      return new MockEarStructureDeployer();
   }

   protected VFSDeploymentContext determineStructure(VFSDeployment deployment) throws Exception
   {
      Set<String> defaultSuffixes = JARStructure.DEFAULT_JAR_SUFFIXES;
      JARStructure jarStructure = new JARStructure();
      jarStructure.setSupportsCandidateAnnotations(true);
      jarStructure.addCandidateAnnotation(Stateless.class);
      jarStructure.addCandidateAnnotation(Service.class);
      jarStructure.addCandidateAnnotation(AppClient.class);
      jarStructure.addCandidateAnnotation(Servlet.class);
      try
      {
         Set<String> suffixes = new HashSet<String>(jarStructure.getSuffixes());
         suffixes.add(".ejb3");
         jarStructure.setSuffixes(suffixes);
         return determineStructureWithStructureDeployers(deployment, new FileStructure(), new WARStructure(), jarStructure, createEarStructureDeployer());
      }
      finally
      {
         jarStructure.setSuffixes(defaultSuffixes);
      }
   }
}
