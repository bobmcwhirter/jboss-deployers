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

import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;
import java.net.URL;

import junit.framework.Test;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.test.deployers.vfs.webbeans.support.WebBeanDiscovery;
import org.jboss.test.deployers.vfs.webbeans.support.crm.CrmWebBean;
import org.jboss.test.deployers.vfs.webbeans.support.ejb.BusinessInterface;
import org.jboss.test.deployers.vfs.webbeans.support.ejb.MySLSBean;
import org.jboss.test.deployers.vfs.webbeans.support.ext.ExternalWebBean;
import org.jboss.test.deployers.vfs.webbeans.support.jar.PlainJavaBean;
import org.jboss.test.deployers.vfs.webbeans.support.ui.UIWebBean;
import org.jboss.test.deployers.vfs.webbeans.support.web.ServletWebBean;
import org.jboss.virtual.VirtualFile;

/**
 * WebBeanDiscoveryTestCase.
 * 
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class WebBeanDiscoveryTestCase extends AbstractWebBeansTest
{
   public WebBeanDiscoveryTestCase(String name)
   {
      super(name);
   }

   public static Test suite()
   {
      return suite(WebBeanDiscoveryTestCase.class);
   }

   public void testSimpleUsage() throws Exception
   {
      VirtualFile ear = createBasicEar();
      VFSDeploymentUnit topUnit = assertDeploy(ear);
      try
      {
         WebBeanDiscovery wbDiscovery = topUnit.getAttachment(WebBeanDiscovery.class);
         assertNotNull(wbDiscovery);

         // TODO - remove this once WBDDeployer is done
         if (wbDiscovery.discoverWebBeanClasses().iterator().hasNext() == false)
            return;

         Set<String> expected = new HashSet<String>();
         expected.add("ejbs.jar/META-INF");
         expected.add("ext.jar/META-INF");
         expected.add("simple.jar/META-INF");
         expected.add("ui.jar/META-INF");
         expected.add("simple.war/WEB-INF");
         expected.add("crm.jar/META-INF");

         for (URL url : wbDiscovery.discoverWebBeansXml())
         {
            boolean found = false;
            Iterator<String> iter = expected.iterator();
            while (iter.hasNext())
            {
               String expectedURL = iter.next();
               if (url.toExternalForm().contains(expectedURL))
               {
                  iter.remove();
                  found = true;
                  break;
               }
            }
            assertTrue("Unexpected wb url: " + url, found);
         }

         assertEmpty("Should be emtpy, missing " + expected, expected);

         addExpectedClass(expected, BusinessInterface.class);
         addExpectedClass(expected, MySLSBean.class);
         addExpectedClass(expected, ExternalWebBean.class);
         addExpectedClass(expected, PlainJavaBean.class);
         addExpectedClass(expected, UIWebBean.class);
         addExpectedClass(expected, ServletWebBean.class);
         addExpectedClass(expected, CrmWebBean.class);

         for (Class<?> clazz : wbDiscovery.discoverWebBeanClasses())
            assertTrue(expected.remove(clazz.getName()));

         assertEmpty("Should be emtpy, missing " + expected, expected);
      }
      finally
      {
         undeploy(topUnit);
      }
   }

   private static void addExpectedClass(Set<String> expected, Class<?> clazz)
   {
      expected.add(clazz.getName());
   }
}
