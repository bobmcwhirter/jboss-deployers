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

import org.jboss.test.deployers.BootstrapDeployersTest;
import org.jboss.test.deployers.vfs.webbeans.support.ejb.MySLSBean;
import org.jboss.test.deployers.vfs.webbeans.support.ext.ExternalWebBean;
import org.jboss.test.deployers.vfs.webbeans.support.jar.PlainJavaBean;
import org.jboss.test.deployers.vfs.webbeans.support.ui.UIWebBean;
import org.jboss.test.deployers.vfs.webbeans.support.util.SomeUtil;
import org.jboss.test.deployers.vfs.webbeans.support.web.ServletWebBean;
import org.jboss.virtual.AssembledDirectory;
import org.jboss.deployers.vfs.deployer.kernel.BeanDeployer;

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

   protected AssembledDirectory createBasicEar() throws Exception
   {
      AssembledDirectory ear = createTopLevelWithUtil();

      AssembledDirectory jar = ear.mkdir("simple.jar");
      addPackage(jar, PlainJavaBean.class);
      addPath(jar, "/webbeans/simple/jar", "META-INF");

      AssembledDirectory ejbs = ear.mkdir("ejbs.jar");
      addPackage(ejbs, MySLSBean.class);
      addPath(ejbs, "/webbeans/simple/ejb", "META-INF");

      AssembledDirectory war = ear.mkdir("simple.war");
      AssembledDirectory webinf = war.mkdir("WEB-INF");
      AssembledDirectory classes = webinf.mkdir("classes");
      addPackage(classes, ServletWebBean.class);
      addPath(war, "/webbeans/simple/web", "WEB-INF");

      AssembledDirectory lib = webinf.mkdir("lib");

      AssembledDirectory uijar = lib.mkdir("ui.jar");
      addPackage(uijar, UIWebBean.class);
      addPath(uijar, "/webbeans/simple/ui", "META-INF");

      enableTrace("org.jboss.deployers");

      return ear;
   }

   protected AssembledDirectory createTopLevelWithUtil() throws Exception
   {
      AssembledDirectory topLevel = createAssembledDirectory("top-level.ear", "top-level.ear");
      addPath(topLevel, "/webbeans/simple", "META-INF");

      AssembledDirectory earLib = topLevel.mkdir("lib");

      AssembledDirectory util = earLib.mkdir("util.jar");
      addPackage(util, SomeUtil.class);

      AssembledDirectory ext = earLib.mkdir("ext.jar");
      addPackage(ext, ExternalWebBean.class);
      addPath(ext, "/webbeans/simple/ext", "META-INF");

      return topLevel;
   }
}