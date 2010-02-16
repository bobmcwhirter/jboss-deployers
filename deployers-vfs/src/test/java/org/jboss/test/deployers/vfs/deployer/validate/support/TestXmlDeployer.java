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
package org.jboss.test.deployers.vfs.deployer.validate.support;

import org.jboss.test.deployers.vfs.deployer.jaxp.support.SomeBean;
import org.jboss.deployers.vfs.spi.deployer.JAXPDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.vfs.VirtualFile;
import org.w3c.dom.Document;

/**
 * @author ales.justin@jboss.org
 */
public class TestXmlDeployer extends JAXPDeployer<SomeBean>
{
   private SomeBean lastBean;

   public TestXmlDeployer()
   {
      super(SomeBean.class);
      setSuffix(".jbean");
   }

   public SomeBean getLastBean()
   {
      return lastBean;
   }

   @Override
   protected SomeBean parse(VFSDeploymentUnit unit, VirtualFile file, Document doc) throws Exception
   {
      String name = doc.getDocumentElement().getAttribute("name");
      String version = doc.getDocumentElement().getAttribute("version");

      SomeBean bean = new SomeBean();
      bean.setName(name);
      bean.setVersion(version);
      lastBean = bean;
      return bean;
   }
}
