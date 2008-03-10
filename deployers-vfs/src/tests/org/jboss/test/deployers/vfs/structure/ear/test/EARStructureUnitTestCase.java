/*
* JBoss, Home of Professional Open Source
* Copyright 2006, JBoss Inc., and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* This is free softeare; you can redistribute it and/or modify it
* under the terms of the GNU Lesser General Public License as
* published by the Free Softeare Foundation; either version 2.1 of
* the License, or (at your option) any later version.
*
* This softeare is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied earranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this softeare; if not, write to the Free
* Softeare Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
* 02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/
package org.jboss.test.deployers.vfs.structure.ear.test;

import java.util.HashSet;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.jboss.deployers.vfs.plugins.structure.file.FileStructure;
import org.jboss.deployers.vfs.plugins.structure.jar.JARStructure;
import org.jboss.deployers.vfs.plugins.structure.war.WARStructure;
import org.jboss.deployers.vfs.spi.client.VFSDeployment;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentContext;
import org.jboss.test.deployers.vfs.structure.AbstractStructureTest;
import org.jboss.test.deployers.vfs.structure.ear.support.MockEarStructureDeployer;
import org.jboss.virtual.plugins.context.jar.JarUtils;

/**
 * Mock ear structure deployer tests
 * 
 * @author Scott.Stark@jboss.org
 * @author adrian@jboss.org
 * @version $Revision: 61684 $
 */
public class EARStructureUnitTestCase extends AbstractStructureTest
{
   public static Test suite()
   {
      return new TestSuite(EARStructureUnitTestCase.class);
   }
   
   public EARStructureUnitTestCase(String name)
   {
      super(name);
   }

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      enableTrace("org.jboss.deployers");
   }

   protected VFSDeploymentContext determineStructure(VFSDeployment deployment) throws Exception
   {
      Set<String> defaultSuffixes = JarUtils.getSuffixes();
      JARStructure jarStructure = new JARStructure();
      try
      {
         Set<String> suffixes = new HashSet<String>(jarStructure.getSuffixes());
         suffixes.add(".ejb3");
         jarStructure.setSuffixes(suffixes);
         return determineStructureWithStructureDeployers(deployment, new FileStructure(), new WARStructure(), jarStructure, new MockEarStructureDeployer());
      }
      finally
      {
         jarStructure.setSuffixes(defaultSuffixes);
      }
   }
   
   public void testNotAnEAR() throws Throwable
   {
      // TODO JBMICROCONT-185 This gets recognised by the jar deployer assertNotValid("/structure/ear", "notanear");
   }

   /**
    * Validate a basic ear with modules having no subdeployments 
    * @throws Throwable for any problem
    */
   public void testSimpleWithAppXml() throws Throwable
   {      
      VFSDeploymentContext ear = assertDeploy("/structure/ear", "simplewithappxml.ear");
      assertClassPath(ear, "lib/lib0.jar");
      assertChildContexts(ear, "module-service.xml", "module-bean1ejb.jar", "module-bean2.ejb3", "module-client1.jar", "module-mbean1.sar", "module-mcf1-ds.xml", "module-mcf1.rar", "module-web1.war");
   }

   /**
    * Validate a ear type of structure specified via the ear
    * META-INF/application.properties parsed by the MockEarStructureDeployer.
    * The ear modules having subdeployments 
    * @throws Throwable for any problem
    */
   public void testComplexWithAppXml() throws Throwable
   {      
      VFSDeploymentContext ear = assertDeploy("/structure/ear", "complexwithappxml.ear");
      assertClassPath(ear, "lib/lib0.jar");
      assertChildContexts(ear, "module-service.xml", "module-bean1ejb.jar", "module-bean2.ejb3", "module-client1.jar", "module-mbean1.sar", "module-mcf1-ds.xml", "module-mcf1.rar", "module-web1.war", "subdir/relative.jar");

      // Validate that the expected module subdeployments are there
      VFSDeploymentContext child = assertChildContext(ear, "module-mbean1.sar");
      assertChildContexts(child, "extensions.aop", "submbean.sar", "submbean2-service.xml");
   }

   /**
    * Basic getMetaDataFile/getFile tests.
    * 
    * @throws Throwable for any problem
    */
   public void testComplexWithAppFinds() throws Throwable
   {
      VFSDeploymentContext ear = assertDeploy("/structure/ear", "complexwithappxml.ear");

      // META-INF/application.properties
      assertMetaDataFile(ear, "application.properties");
      assertNoFile(ear, "application.properties");

      // lib/lib0.jar
      assertNoMetaDataFile(ear, "lib/lib0.jar");
      assertFile(ear, "lib/lib0.jar");
   }
}
