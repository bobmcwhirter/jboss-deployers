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
package org.jboss.test.deployers.vfs.classloading.test;

import org.jboss.classloading.spi.metadata.ClassLoadingDomainMetaData;
import org.jboss.classloading.spi.metadata.FilterMetaData;
import org.jboss.classloading.spi.metadata.ParentPolicyMetaData;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.test.deployers.BootstrapDeployersTest;

import junit.framework.Test;

/**
 * ClassLoadingDomain metadata test case.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class ClassLoadingDomainMetaDataUnitTestCase extends BootstrapDeployersTest
{
   public ClassLoadingDomainMetaDataUnitTestCase(String name)
   {
      super(name);
   }

   public static Test suite()
   {
      return suite(ClassLoadingDomainMetaDataUnitTestCase.class);
   }

   public void testXmlParsing() throws Exception
   {
      DeploymentUnit du = addDeployment("/classloading", "smoke");
      try
      {
         ClassLoadingDomainMetaData cldmd = du.getAttachment(ClassLoadingDomainMetaData.class);
         assertNotNull(cldmd);
         ParentPolicyMetaData ppmd = cldmd.getParentPolicy();
         assertNotNull(ppmd);
         FilterMetaData bf = ppmd.getBeforeFilter();
         assertNotNull(bf);
         Object value = bf.getValue();
         assertInstanceOf(value, String[].class);
         assertEquals(new String[]{"org.jboss.acme", "com.redhat.acme"}, (String[]) value);
         FilterMetaData af = ppmd.getAfterFilter();
         assertNotNull(af);
         value = af.getValue();
         assertInstanceOf(value, String[].class);
         assertEquals(new String[]{"org.jboss.foobar", "com.redhat.foobar"}, (String[]) value);
      }
      finally
      {
         undeploy(du);
      }
   }
}