package org.jboss.test.deployers.vfs.deployer.jaxp.support;

import org.jboss.deployers.vfs.spi.deployer.JAXPDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.virtual.VirtualFile;
import org.w3c.dom.Document;

/**
 * @author adrian@jboss.org
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
