/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.test.deployers.vfs.webbeans.test;

import org.jboss.deployers.vfs.deployer.kernel.BeanDeployer;
import org.jboss.test.deployers.BootstrapDeployersTest;
import org.jboss.test.deployers.vfs.webbeans.support.ejb.MySLSBean;
import org.jboss.test.deployers.vfs.webbeans.support.ext.ExternalWebBean;
import org.jboss.test.deployers.vfs.webbeans.support.jar.PlainJavaBean;
import org.jboss.test.deployers.vfs.webbeans.support.jsf.NotWBJsfBean;
import org.jboss.test.deployers.vfs.webbeans.support.ui.UIWebBean;
import org.jboss.test.deployers.vfs.webbeans.support.util.SomeUtil;
import org.jboss.test.deployers.vfs.webbeans.support.web.ServletWebBean;
import org.jboss.test.deployers.vfs.webbeans.support.crm.CrmWebBean;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;

/**
 * AbstractWebBeansTest.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class AbstractWebBeansTest extends BootstrapDeployersTest
{
   protected AbstractWebBeansTest(String name)
   {
      super(name);
   }

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();

      // fix bean deployer to take more restictive suffix
      // else it's gonna pick up our web-beans.xml
      BeanDeployer deployer = assertBean("BeanDeployer", BeanDeployer.class);
      deployer.setSuffix("-jboss-beans.xml");
   }

   @Override
   protected void tearDown() throws Exception
   {
      // put the old suffix back
      BeanDeployer deployer = assertBean("BeanDeployer", BeanDeployer.class);
      deployer.setSuffix("-beans.xml");

      super.tearDown();
   }

   protected VirtualFile createBasicEar() throws Exception
   {
      VirtualFile ear = createTopLevelWithUtil();

      VirtualFile jar = ear.getChild("simple.jar");
      createAssembledDirectory(jar)
         .addPackage(PlainJavaBean.class)
         .addPath("/webbeans/simple/jar");

      VirtualFile ejbs = ear.getChild("ejbs.jar");
      createAssembledDirectory(ejbs)
         .addPackage(MySLSBean.class)
         .addPath("/webbeans/simple/ejb");

      
      VirtualFile war = ear.getChild("simple.war");
      createAssembledDirectory(war)
         .addPackage("WEB-INF/classes", ServletWebBean.class)
         .addPath("/webbeans/simple/web")
         .addPackage("WEB-INF/lib/ui.jar", UIWebBean.class)
         .addPath("WEB-INF/lib/ui.jar", "/webbeans/simple/ui");

      // war w/o web-beans.xml
      war = ear.getChild("crm.war");
      createAssembledDirectory(war)
         .addPackage("WEB-INF/classes", NotWBJsfBean.class)
         .addPackage("WEB-INF/lib/crm.jar", CrmWebBean.class)
         .addPath("WEB-INF/lib/crm.jar", "/webbeans/simple/crm");

      enableTrace("org.jboss.deployers");

      return ear;
   }

   protected VirtualFile createTopLevelWithUtil() throws Exception
   {
      VirtualFile earFile = VFS.getChild(getName()).getChild("top-level.ear");
      createAssembledDirectory(earFile)
         .addPath("/webbeans/simple")
         .addPackage("lib/util.jar", SomeUtil.class)
         .addPackage("lib/ext.jar", ExternalWebBean.class)
         .addPath("lib/ext.jar", "/webbeans/simple/ext");
      return earFile;
   }
}