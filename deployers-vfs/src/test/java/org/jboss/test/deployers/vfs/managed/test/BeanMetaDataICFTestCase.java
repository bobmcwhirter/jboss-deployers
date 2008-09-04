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
package org.jboss.test.deployers.vfs.managed.test;

import junit.framework.Test;
import org.jboss.beans.metadata.plugins.AbstractBeanMetaData;
import org.jboss.beans.metadata.plugins.AbstractPropertyMetaData;
import org.jboss.deployers.vfs.deployer.kernel.managed.BeanInstanceClassFactory;
import org.jboss.managed.api.ManagedObject;
import org.jboss.managed.api.factory.ManagedObjectFactory;
import org.jboss.test.BaseTestCase;
import org.jboss.test.deployers.vfs.managed.support.PlainMCBean;
import org.jboss.test.deployers.vfs.managed.support.MCBeanWithRuntimeName;

/**
 * Test bean meta data ICF.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class BeanMetaDataICFTestCase extends BaseTestCase
{
   public BeanMetaDataICFTestCase(String name)
   {
      super(name);
   }

   public static Test suite()
   {
      return suite(BeanMetaDataICFTestCase.class);
   }

   public void testICF() throws Exception
   {
      ManagedObjectFactory factory = ManagedObjectFactory.getInstance();
      BeanInstanceClassFactory instanceClassFactory = new BeanInstanceClassFactory();
      factory.setInstanceClassFactory(AbstractBeanMetaData.class, instanceClassFactory);
      try
      {
         AbstractBeanMetaData plainMCBean = new AbstractBeanMetaData("plain", PlainMCBean.class.getName());
         plainMCBean.addProperty(new AbstractPropertyMetaData("id", "PlainMCBean"));
         ManagedObject mo = factory.initManagedObject(plainMCBean, "PlainMCBean", "MC");
         assertNotNull(mo);
         assertEquals("plain", mo.getComponentName());

         AbstractBeanMetaData runtimeMCBean = new AbstractBeanMetaData("runtime", MCBeanWithRuntimeName.class.getName());
         runtimeMCBean.addProperty(new AbstractPropertyMetaData("id", "RuntimeMCBean"));
         runtimeMCBean.addProperty(new AbstractPropertyMetaData("componentName", "FromObjectRuntime"));
         ManagedObject mo2 = factory.initManagedObject(runtimeMCBean, "RuntimeMCBean", "MC");
         assertNotNull(mo2);
         assertEquals("FromObjectRuntime", mo2.getComponentName());
      }
      finally
      {
         factory.setInstanceClassFactory(AbstractBeanMetaData.class, null);
      }
   }
}
