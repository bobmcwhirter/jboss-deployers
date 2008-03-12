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

import junit.framework.Test;
import junit.framework.TestSuite;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentContext;

/**
 * Mock ear structure deployer tests
 * 
 * @author Ales.Justin@jboss.org
 * @author Scott.Stark@jboss.org
 * @author adrian@jboss.org
 * @version $Revision: 61684 $
 */
public class EARStructureUnitTestCase extends AbstractEARStructureTest
{
   public static Test suite()
   {
      return new TestSuite(EARStructureUnitTestCase.class);
   }
   
   public EARStructureUnitTestCase(String name)
   {
      super(name);
   }

   /**
    * Validate packaged ear.
    * @throws Throwable for any error
    */
   public void testPackedEAR() throws Throwable
   {
      VFSDeploymentContext ear = assertDeploy("/structure/ear", "archive.ear");
      assertClassPath(ear, "lib/log5j.jar");
      assertChildContexts(ear, "module-bean1ejb.jar");
   }

   /**
    * Validate scanning of ear.
    * @throws Throwable for any error
    */
   public void testScanEAR() throws Throwable
   {
      VFSDeploymentContext ear = assertDeploy("/structure/ear", "noappxml.ear");
      assertChildContexts(ear, "client.jar", "foobar.sar", "known.jar", "mf.jar", "ts.rar", "webapp.war");
   }

   /**
    * Validate mixed ear.
    * @throws Throwable for any error
    */
   public void testMixedEAR() throws Throwable
   {
      VFSDeploymentContext ear = assertDeploy("/structure/ear", "someappxml.ear");
      assertChildContexts(ear, "client.jar", "foobar.sar", "known.jar", "mf.jar", "ts.rar", "webapp.war");
   }

   /**
    * Validate strict ear.
    * @throws Throwable for any error
    */
   public void testStrictEAR() throws Throwable
   {
      VFSDeploymentContext ear = assertDeploy("/structure/ear", "strict.ear");
      assertChildContexts(ear, "known.jar", "ts.rar");
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
