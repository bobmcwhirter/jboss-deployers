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
package org.jboss.test.deployers.vfs.deployer.jaxp.test;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.jboss.deployers.spi.DeploymentState;
import org.jboss.deployers.spi.attachments.MutableAttachments;
import org.jboss.deployers.vfs.plugins.structure.file.FileStructure;
import org.jboss.deployers.vfs.plugins.structure.jar.JARStructure;
import org.jboss.deployers.vfs.spi.client.VFSDeployment;
import org.jboss.test.deployers.vfs.deployer.DeployerClientTest;
import org.jboss.test.deployers.vfs.deployer.jaxp.support.SomeBean;
import org.jboss.test.deployers.vfs.deployer.jaxp.support.TestXmlDeployer;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Tests of the JAXPDeployer.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 60707 $
 */
public class JAXPDeployerUnitTestCase extends DeployerClientTest
{
   public static Test suite()
   {
      return new TestSuite(JAXPDeployerUnitTestCase.class);
   }

   private TestXmlDeployer deployer;

   public JAXPDeployerUnitTestCase(String name) throws Throwable
   {
      super(name);
   }

   protected void setUp() throws Exception
   {
      super.setUp();
      try
      {
         main = createMainDeployer();
         addStructureDeployer(main, new JARStructure());
         addStructureDeployer(main, new FileStructure());
         
         deployer = new TestXmlDeployer();
         deployer.create();
         addDeployer(main, deployer);
      }
      catch (Throwable t)
      {
         throw new RuntimeException(t);
      }
   }

   public void testDocOverride() throws Exception
   {
      enableTrace("org.jboss.deployers");
      enableTrace("org.jboss.dependency");
      
      VFSDeployment context = createDeployment("/jaxp", "somebean.jbean");
      assertDeploy(context, null, null);
      SomeBean bean = deployer.getLastBean();
      assertNotNull(bean);
      assertEquals("bean.name", bean.getName(), "bean1");
      assertEquals("bean.version", bean.getVersion(), "1.0");

      // Now deploy with an overriden Document
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      DOMImplementation dom = builder.getDOMImplementation();
      Document doc = dom.createDocument(null, "somebean", null);
      Element root = doc.getDocumentElement();
      root.setAttribute("name", "bean1.1");
      root.setAttribute("version", "1.1");
      assertDeploy(context, Document.class, doc);

      bean = deployer.getLastBean();
      assertNotNull(bean);
      assertEquals("bean.name", bean.getName(), "bean1.1");
      assertEquals("bean.version", bean.getVersion(), "1.1");

      assertUndeploy(context);
   }

   protected <T> void assertDeploy(VFSDeployment context, Class<T> clazz, T mo) throws Exception
   {
      main.addDeployment(context);
      if (mo != null)
      {
         MutableAttachments attachments = (MutableAttachments) context.getPredeterminedManagedObjects();
         attachments.addAttachment(clazz, mo);
      }
      main.process();
      assertEquals("Should be Deployed " + context, DeploymentState.DEPLOYED, main.getDeploymentState(context.getName()));
   }

}
