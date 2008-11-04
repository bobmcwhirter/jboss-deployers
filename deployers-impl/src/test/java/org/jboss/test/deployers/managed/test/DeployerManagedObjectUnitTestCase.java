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
package org.jboss.test.deployers.managed.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.jboss.deployers.client.spi.DeployerClient;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.spi.attachments.MutableAttachments;
import org.jboss.managed.api.ManagedObject;
import org.jboss.managed.api.ManagedProperty;
import org.jboss.managed.api.factory.ManagedObjectFactory;
import org.jboss.managed.plugins.factory.AbstractManagedObjectFactory;
import org.jboss.metatype.api.types.CollectionMetaType;
import org.jboss.metatype.api.types.MetaType;
import org.jboss.metatype.api.types.SimpleMetaType;
import org.jboss.metatype.api.values.CollectionValue;
import org.jboss.metatype.api.values.GenericValue;
import org.jboss.metatype.api.values.MetaValue;
import org.jboss.test.deployers.AbstractDeployerTest;
import org.jboss.test.deployers.deployer.support.AllowedDsTypes;
import org.jboss.test.deployers.deployer.support.ConnMetaData;
import org.jboss.test.deployers.deployer.support.DSMetaData;
import org.jboss.test.deployers.deployer.support.LocalDataSourceMetaData;
import org.jboss.test.deployers.deployer.support.XADataSourceMetaData;
import org.jboss.test.deployers.managed.support.TestAttachment;
import org.jboss.test.deployers.managed.support.TestManagedObjectDeployer;
import org.jboss.util.graph.Graph;

/**
 * DeployerManagedObjectUnitTestCase.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class DeployerManagedObjectUnitTestCase extends AbstractDeployerTest
{
   private TestManagedObjectDeployer deployer = new TestManagedObjectDeployer();
   
   public static Test suite()
   {
      return new TestSuite(DeployerManagedObjectUnitTestCase.class);
   }
   
   public DeployerManagedObjectUnitTestCase(String name)
   {
      super(name);
   }

   public void testManagedObject() throws Exception
   {
      DeployerClient main = getMainDeployer();
      
      // Deploy a context
      Deployment context = createSimpleDeployment("deploy");
      main.addDeployment(context);
      main.process();

      // Check the default settings
      assertNotNull(deployer.lastAttachment);
      assertEquals("initialString1", deployer.lastAttachment.getProperty("string1"));
      assertEquals("initialString2", deployer.lastAttachment.getProperty("string2"));
      
      // Get the managed object
      Map<String, ManagedObject> mos = main.getManagedObjects(context.getName());
      assertNotNull(mos);
      ManagedObject mo = mos.get(TestAttachment.class.getName());
      assertNotNull(mo);
      //
      Graph<Map<String, ManagedObject>> mosg = main.getDeepManagedObjects(context.getName());
      assertEquals("MO Graph", mosg.size(), 1);
      mos = mosg.getRootVertex().getData();
      assertNotNull(mos);
      mo = mos.get(TestAttachment.class.getName());
      assertNotNull(mo);

      // TODO JBMICROCONT-181 the attachment should NOT be the top level managed object
      //      that should be describing the structure and deployment state
      //      with the attachments as sub managed objects
      
      // Check the managed object has the default settings
      assertEquals("initialString1", mo.getProperty("string1").getValue());
      assertEquals("initialString2", mo.getProperty("string2").getValue());
      
      // Change a value
      mo.getProperty("string1").setValue("changedString1");
      
      // Get the changed attachment
      TestAttachment attachment = (TestAttachment) mo.getAttachment();

      // Redeploy with our changed attachment
      MutableAttachments attachments = (MutableAttachments) context.getPredeterminedManagedObjects();
      attachments.addAttachment(TestAttachment.class, attachment);
      main.addDeployment(context);
      main.process();

      // Check the changed settings as seen by the deployer
      assertNotNull(deployer.lastAttachment);
      assertEquals("changedString1", deployer.lastAttachment.getProperty("string1"));
      assertEquals("initialString2", deployer.lastAttachment.getProperty("string2"));
      
      // TODO JBMICROCONT-181 shouldn't have to reget the managed object handles across redeploys?
      mos = main.getManagedObjects(context.getName());
      assertNotNull(mos);
      mo = mos.get(TestAttachment.class.getName());
      assertNotNull(mo);

      // Check the changed settings as described by the managed object
      assertEquals("changedString1", mo.getProperty("string1").getValue());
      assertEquals("initialString2", mo.getProperty("string2").getValue());
   }

   /**
    * Validate the ManagedObjectFactory for DSMetaData class
    *
    */
   @SuppressWarnings("unchecked")
   public void testDSMetaDataManagedObjectFactory()
   {
      ManagedObjectFactory mof = ManagedObjectFactory.getInstance();
      ManagedObject mo = mof.createManagedObject(DSMetaData.class, null);

      // Validate the expected properties
      Map<String, ManagedProperty> propsMap = mo.getProperties();
      assertEquals(2, propsMap.size());
      log.info("DSMetaData properties: "+propsMap);

      // display-name
      ManagedProperty displayName = propsMap.get("display-name");
      assertNotNull(displayName);
      assertEquals("display name of DS deployment", displayName.getDescription());
      assertEquals(SimpleMetaType.STRING, displayName.getMetaType());

      // deployments
      ManagedProperty deployments = propsMap.get("deployments");
      assertNotNull(deployments);
      assertEquals("The DS connection factories", deployments.getDescription());
      MetaType deploymentsType = new CollectionMetaType(List.class.getName(), AbstractManagedObjectFactory.MANAGED_OBJECT_META_TYPE);
      assertEquals(deploymentsType, deployments.getMetaType());
      CollectionValue value = CollectionValue.class.cast(deployments.getValue());
      CollectionMetaType valueType = value.getMetaType();
      assertEquals(AbstractManagedObjectFactory.MANAGED_OBJECT_META_TYPE, valueType.getElementType());

      assertEquals(1, value.getSize());
      // Validate the ConnMetaData ManagedObject
      GenericValue localConnMOGV = GenericValue.class.cast(value.getElements()[0]);
      ManagedObject localConnMO = ManagedObject.class.cast(localConnMOGV.getValue());
      assertEquals(ConnMetaData.class.getName(), localConnMO.getName());
      propsMap = localConnMO.getProperties();
      assertEquals(10, propsMap.size());
      log.info("ConnMetaData properties: "+propsMap);
      ManagedProperty dsType = propsMap.get("datasource-type");
      assertNotNull(dsType);
      Set<MetaValue> dsTypeValues = dsType.getLegalValues();
      assertTrue(dsTypeValues.containsAll(AllowedDsTypes.values));
   }

   /**
    * Validate the ManagedObjectFactory for DSMetaData instance
    *
    */
   @SuppressWarnings("unchecked")
   public void testDSMetaDataManagedObjectFactoryInit()
   {
      ManagedObjectFactory mof = ManagedObjectFactory.getInstance();
      DSMetaData dsmd = new DSMetaData();
      LocalDataSourceMetaData ds = new LocalDataSourceMetaData();
      XADataSourceMetaData xads = new XADataSourceMetaData();
      ArrayList<ConnMetaData> deployments = new ArrayList<ConnMetaData>();
      deployments.add(ds);
      deployments.add(xads);
      dsmd.setDeployments(deployments);
      ManagedObject mo = mof.initManagedObject(dsmd, null, null);

      // Validate the expected properties
      Map<String, ManagedProperty> propsMap = mo.getProperties();
      assertEquals(2, propsMap.size());
      log.info("DSMetaData properties: "+propsMap);

      // display-name
      ManagedProperty displayName = propsMap.get("display-name");
      assertNotNull(displayName);
      assertEquals("display name of DS deployment", displayName.getDescription());
      assertEquals(SimpleMetaType.STRING, displayName.getMetaType());

      // deployments
      ManagedProperty dsDeployments = propsMap.get("deployments");
      assertNotNull(deployments);
      assertEquals("The DS connection factories", dsDeployments.getDescription());
      MetaType deploymentsType = new CollectionMetaType(List.class.getName(), AbstractManagedObjectFactory.MANAGED_OBJECT_META_TYPE);
      assertEquals(deploymentsType, dsDeployments.getMetaType());
      CollectionValue value = CollectionValue.class.cast(dsDeployments.getValue());
      CollectionMetaType valueType = value.getMetaType();
      assertEquals(AbstractManagedObjectFactory.MANAGED_OBJECT_META_TYPE, valueType.getElementType());

      assertEquals(2, value.getSize());
      ManagedObject localConnMO = null;
      ManagedObject xaConnMO = null;
      for(Object md : value)
      {
         GenericValue tmpGV = GenericValue.class.cast(md);
         ManagedObject tmpMO = ManagedObject.class.cast(tmpGV.getValue());
         if (tmpMO.getName().equals(LocalDataSourceMetaData.class.getName()))
            localConnMO = tmpMO;
         if (tmpMO.getName().equals(XADataSourceMetaData.class.getName()))
            xaConnMO = tmpMO;
      }
      assertNotNull(localConnMO);
      assertNotNull(xaConnMO);

      // Validate the LocalDataSourceMetaData ManagedObject
      propsMap = localConnMO.getProperties();
      assertEquals(10, propsMap.size());
      log.info("LocalDataSourceMetaData properties: "+propsMap);
      ManagedProperty dsType = propsMap.get("datasource-type");
      assertNotNull(dsType);
      Set<MetaValue> dsTypeValues = dsType.getLegalValues();
      assertTrue(dsTypeValues.containsAll(AllowedDsTypes.values));

      // Validate the XADataSourceMetaData ManagedObject
      propsMap = xaConnMO.getProperties();
      assertEquals(12, propsMap.size());
      log.info("XADataSourceMetaData properties: "+propsMap);
      ManagedProperty xaDataSourceClass = propsMap.get("xaDataSourceClass");
      assertNotNull(xaDataSourceClass);
      ManagedProperty xaResourceTimeout = propsMap.get("xaResourceTimeout");
      assertNotNull(xaResourceTimeout);
      ManagedProperty secDomain = propsMap.get("security-domain");
      assertNotNull(secDomain);
      MetaType secDomainType = secDomain.getMetaType();
      assertEquals(AbstractManagedObjectFactory.MANAGED_OBJECT_META_TYPE, secDomainType);

      ManagedProperty jndiName = propsMap.get("jndi-name");
      assertNotNull(jndiName);
      ManagedProperty password = propsMap.get("password");
      assertNotNull(password);
      ManagedProperty connProperties = propsMap.get("connection-properties");
      assertNotNull(connProperties);
      ManagedProperty username = propsMap.get("username");
      assertNotNull(username);
      ManagedProperty maxSize = propsMap.get("max-size");
      assertNotNull(maxSize);
      ManagedProperty minSize = propsMap.get("min-size");
      assertNotNull(minSize);

      // Validate setting the properties
      displayName.setValue("testDSMetaDataManagedObjectFactoryInit");
      jndiName.setValue("java:TestDS");
      password.setValue("password".toCharArray());
      username.setValue("username");
      minSize.setValue(new Integer(10));
      maxSize.setValue(new Integer(100));

      Object attachment = xaConnMO.getAttachment();
      assertTrue("attachment is a XADataSourceMetaData("+attachment+")", attachment instanceof XADataSourceMetaData);
      XADataSourceMetaData xaDS = XADataSourceMetaData.class.cast(attachment);
      assertEquals("jndiName", "java:TestDS", xaDS.getJndiName());
      assertEquals("password", "password", new String(xaDS.getPassword()));
      assertEquals("username", "username", xaDS.getUsername());
      assertEquals("minSize", 10, xaDS.getMinSize());
      assertEquals("maxSize", 100, xaDS.getMaxSize());
   }

   
   protected DeployerClient getMainDeployer()
   {
      return createMainDeployer(deployer);
   }
}
