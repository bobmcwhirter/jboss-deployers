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
package org.jboss.test.deployers.vfs.classloader.test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import junit.framework.Test;

import org.jboss.classloading.spi.metadata.CapabilitiesMetaData;
import org.jboss.classloading.spi.metadata.ClassLoadingMetaData;
import org.jboss.classloading.spi.metadata.ExportAll;
import org.jboss.classloading.spi.metadata.RequirementsMetaData;
import org.jboss.classloading.spi.version.Version;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.managed.api.ManagedDeployment;
import org.jboss.managed.api.ManagedObject;
import org.jboss.managed.api.ManagedProperty;
import org.jboss.metatype.api.types.MetaType;
import org.jboss.metatype.api.types.MetaTypeFactory;
import org.jboss.metatype.api.values.MetaValue;
import org.jboss.metatype.api.values.MetaValueFactory;
import org.jboss.test.deployers.BootstrapDeployersTest;

/**
 * ManagedObjectClassLoadingParserUnitTestCase.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class ManagedObjectClassLoadingParserUnitTestCase extends BootstrapDeployersTest
{
   private MetaTypeFactory mtFactory = MetaTypeFactory.getInstance();
   private MetaValueFactory mvFactory = MetaValueFactory.getInstance();

   public static Test suite()
   {
      return suite(ManagedObjectClassLoadingParserUnitTestCase.class);
   }

   public ManagedObjectClassLoadingParserUnitTestCase(String name)
   {
      super(name);
   }
   
   protected ManagedProperty assertManagedProperty(ManagedObject mo, String name, MetaType metaType, MetaValue metaValue)
   {
      ManagedProperty property = mo.getProperty(name);
      assertNotNull("No property " + name, property);
      assertEquals(metaType, property.getMetaType());
      assertEquals(metaValue, property.getValue());
      return property;
   }
   
   protected <T> ManagedProperty assertManagedProperty(ManagedObject mo, String name, Class<T> type, T value)
   {
      MetaType metaType = mtFactory.resolve(type);

      MetaValue metaValue = null;
      if (value != null)
         metaValue = mvFactory.create(value);
      return assertManagedProperty(mo, name, metaType, metaValue);
   }

   public void testManagedObject() throws Exception
   {
      VFSDeploymentUnit unit0 = addDeployment("/classloader", "deployment0");
      try
      {
         ManagedDeployment managedDeployment = getDeployerClient().getManagedDeployment(unit0.getName());
         assertNotNull(managedDeployment);
         ManagedObject mo = managedDeployment.getManagedObject(ClassLoadingMetaData.class.getName());
         assertNotNull(mo);
         getLog().debug("ManagedObject: " + mo + " properties=" + mo.getProperties());

         List<String> expectedProperties = Arrays.asList("name", "version", "domain", "parentDomain", "exportAll", "included", "excluded", "excludedExport", "importAll", "parentFirst", "cache", "blackList", "capabilities", "requirements");
         Set<String> actualProperties = mo.getPropertyNames();
         for (String expected : expectedProperties)
         {
            if (actualProperties.contains(expected) == false)
               fail("Expected property: " + expected);
         }
         for (String actual : actualProperties)
         {
            if (expectedProperties.contains(actual) == false)
               fail("Did not expect property: " + actual);
         }

         assertManagedProperty(mo, "name", String.class, "test");
         assertManagedProperty(mo, "version", Version.class, Version.DEFAULT_VERSION);
         assertManagedProperty(mo, "domain", String.class, null);
         assertManagedProperty(mo, "parentDomain", String.class, null);
         assertManagedProperty(mo, "exportAll", ExportAll.class, null);
         assertManagedProperty(mo, "included", String.class, null);
         assertManagedProperty(mo, "excluded", String.class, null);
         assertManagedProperty(mo, "excludedExport", String.class, null);
         assertManagedProperty(mo, "importAll", boolean.class, false);
         assertManagedProperty(mo, "parentFirst", boolean.class, true);
         assertManagedProperty(mo, "capabilities", CapabilitiesMetaData.class, new CapabilitiesMetaData());
         assertManagedProperty(mo, "requirements", RequirementsMetaData.class, new RequirementsMetaData());
         
      }
      finally
      {
         undeploy(unit0);
      }
   }
}
