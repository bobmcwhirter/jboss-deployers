/*
* JBoss, Home of Professional Open Source
* Copyright 2007, JBoss Inc., and individual contributors as indicated
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.jboss.deployers.client.spi.DeployerClient;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.spi.attachments.MutableAttachments;
import org.jboss.managed.api.ComponentType;
import org.jboss.managed.api.ManagedComponent;
import org.jboss.managed.api.ManagedDeployment;
import org.jboss.managed.api.ManagedObject;
import org.jboss.managed.api.ManagedOperation;
import org.jboss.managed.api.ManagedParameter;
import org.jboss.managed.api.ManagedProperty;
import org.jboss.managed.api.factory.ManagedObjectFactory;
import org.jboss.metatype.api.types.SimpleMetaType;
import org.jboss.metatype.api.values.GenericValue;
import org.jboss.metatype.api.values.MetaValue;
import org.jboss.test.deployers.deployer.support.ConnMetaData;
import org.jboss.test.deployers.deployer.support.CustomName;
import org.jboss.test.deployers.deployer.support.DSMetaData;
import org.jboss.test.deployers.deployer.support.DSService;
import org.jboss.test.deployers.deployer.support.LocalDataSourceMetaData;
import org.jboss.test.deployers.deployer.support.MCFDeployer;
import org.jboss.test.deployers.deployer.support.RuntimeComponentMetaData;
import org.jboss.test.deployers.deployer.support.SecMetaData;
import org.jboss.test.deployers.deployer.support.SecurityDeployment;
import org.jboss.test.deployers.deployer.support.SimpleMetaData;
import org.jboss.test.deployers.deployer.support.TestServiceAttributeMetaData;
import org.jboss.test.deployers.deployer.support.TestServiceMetaData;
import org.jboss.test.deployers.deployer.support.TestServiceMetaDataICF;
import org.jboss.test.deployers.deployer.support.XADataSourceMetaData;
import org.jboss.test.deployers.managed.support.MockProfileService;

/**
 * ManagedDeployment unit tests.
 * 
 * @author Scott.Stark@jboss.org
 * @author Ales.Justin@jboss.org
 * @version $Revision$
 */
public class DeployerManagedDeploymentUnitTestCase extends AbstractManagedObjectUnitTest
{
   private MCFDeployer deployer = new MCFDeployer();
   
   public static Test suite()
   {
      return new TestSuite(DeployerManagedDeploymentUnitTestCase.class);
   }

   public DeployerManagedDeploymentUnitTestCase(String name)
   {
      super(name);
   }

   public void testAnnotationScan() throws Exception
   {
      ManagedObjectFactory mof = ManagedObjectFactory.getInstance();
      ManagedObject mo = mof.createManagedObject(DSMetaData.class, null);

      Map<String, ManagedProperty> props = mo.getProperties();
      log.info("DSMetaData props: "+props);
      assertEquals(2, props.size());
   }

   public void testComponentNameAndSetValue() throws Exception
   {
      DeployerClient main = getMainDeployer();
      MockProfileService ps = new MockProfileService(main);
      ManagedObjectFactory mof = ManagedObjectFactory.getInstance();
      TestServiceMetaDataICF tsicf = new TestServiceMetaDataICF();
      mof.setInstanceClassFactory(TestServiceMetaData.class, tsicf);

      // Deploy a datasource with local and xa factories
      Deployment ctx1 = createSimpleDeployment("deployment1");
      MutableAttachments a1 = (MutableAttachments) ctx1.getPredeterminedManagedObjects();

      DSMetaData dsmd = new DSMetaData();
      dsmd.setDiplayName("deployment1 DS");
      // The base LocalDataSourceMeta
      LocalDataSourceMetaData ds = new LocalDataSourceMetaData();
      ds.setJndiName("java:DefaultDS1");
      ds.setMaxSize(100);
      ds.setMinSize(10);
      ds.setPassword("password1".toCharArray());
      ds.setUsername("username1");
      SecMetaData smd1 = new SecMetaData();
      smd1.setDomain("java:/jaas/domain1");
      ds.setSecurityMetaData(smd1);

      ArrayList<ConnMetaData> deployments = new ArrayList<ConnMetaData>();
      deployments.add(ds);
      dsmd.setDeployments(deployments);

      a1.addAttachment(DSMetaData.class, dsmd);

      // The mbeans associated with the local DS
      TestServiceMetaData localMBeans = new TestServiceMetaData();
      localMBeans.setObjectName("jboss.jca:service.SecurityDomain");
      localMBeans.setCode(SimpleMetaData.class.getName());
      ArrayList<TestServiceAttributeMetaData> localMBeanAttrs = new ArrayList<TestServiceAttributeMetaData>();
      localMBeanAttrs.add(new TestServiceAttributeMetaData("java:/jaas/domain1", "domain"));
      localMBeanAttrs.add(new TestServiceAttributeMetaData("java:DefaultDS1", "jndiName"));
      TestServiceAttributeMetaData typeAttribute = new TestServiceAttributeMetaData(SimpleMetaData.SecurityDeploymentType.NONE, "type");
      localMBeanAttrs.add(typeAttribute);
      localMBeans.setAttributes(localMBeanAttrs);
      a1.addAttachment(TestServiceMetaData.class, localMBeans);
      ps.addDeployment(ctx1);

      Deployment ctx2 = createSimpleDeployment("deployment2");
      MutableAttachments a2 = (MutableAttachments)ctx2.getPredeterminedManagedObjects();

      TestServiceMetaData localMBeans2 = new TestServiceMetaData();
      localMBeans2.setCode(RuntimeComponentMetaData.class.getName());
      ArrayList<TestServiceAttributeMetaData> localMBeanAttrs2 = new ArrayList<TestServiceAttributeMetaData>();
      localMBeanAttrs2.add(new TestServiceAttributeMetaData("java:/jaas/domain2", "domain"));
      CustomName customName = new CustomName("runtime-name-1");
      localMBeanAttrs2.add(new TestServiceAttributeMetaData(customName, "customName"));
      localMBeans2.setAttributes(localMBeanAttrs2);
      a2.addAttachment(TestServiceMetaData.class, localMBeans2);
      ps.addDeployment(ctx2);

      ps.process();

      ManagedObject mo = ps.getManagedObject("java:/jaas/domain1/SecurityDomain");
      assertNotNull(mo);
      assertEquals(localMBeans.getObjectName(), mo.getComponentName());

      ManagedObject mo2 = ps.getManagedObject("java:/jaas/domain2/SecurityDomain");
      assertNotNull(mo2);
      assertEquals(customName.getName(), mo2.getComponentName());

      ManagedDeployment md = ps.getManagedDeployment("deployment1");
      assertNotNull(md);
      ManagedComponent mc = md.getComponent("java:/jaas/domain1");
      assertNotNull(mc);
      ManagedProperty prop = mc.getProperty("security-criteria");
      assertNotNull(prop);
      assertEquals(typeAttribute.getValue(), SimpleMetaData.SecurityDeploymentType.NONE);
      SimpleMetaData.SecurityDeploymentType value = SimpleMetaData.SecurityDeploymentType.APPLICATION;
      prop.setValue(getMetaValueFactory().create(value));
      assertEquals(typeAttribute.getValue(), SimpleMetaData.SecurityDeploymentType.APPLICATION);

      ManagedProperty targetProp = mc.getProperty("jndi-name");
      assertNotNull(targetProp);
      targetProp.setValue(getMetaValueFactory().create("java:DefaultDS2"));
      // test target runtime component invocation (if intendet here)      
   }

   public void testManagedDeployment() throws Exception
   {
      DeployerClient main = getMainDeployer();
      MockProfileService ps = new MockProfileService(main);
      ManagedObjectFactory mof = ManagedObjectFactory.getInstance();
      TestServiceMetaDataICF tsicf = new TestServiceMetaDataICF();
      mof.setInstanceClassFactory(TestServiceMetaData.class, tsicf);
      
      // Deploy a datasource with local and xa factories
      Deployment ctx1 = createSimpleDeployment("deployment1");
      DSMetaData dsmd = new DSMetaData();
      dsmd.setDiplayName("deployment1 DS");
      // TODO: dsmd.setUrl(new URL("vfsfile:/tmp/some-ds.xml"));
      // The base LocalDataSourceMeta
      LocalDataSourceMetaData ds = new LocalDataSourceMetaData();
      ds.setJndiName("java:DefaultDS1");
      ds.setMaxSize(100);
      ds.setMinSize(10);
      ds.setPassword("password1".toCharArray());
      ds.setUsername("username1");
      SecMetaData smd1 = new SecMetaData();
      smd1.setDomain("java:/jaas/domain1");
      ds.setSecurityMetaData(smd1);

      XADataSourceMetaData xads = new XADataSourceMetaData();
      xads.setJndiName("java:DefaultXADS2");
      ds.setMaxSize(100);
      ds.setMinSize(10);
      ds.setPassword("password2".toCharArray());
      ds.setUsername("username2");
      xads.setXaDataSourceClass("org.jboss.xa.SomeXADS");
      xads.setXaResourceTimeout(300);
      SecMetaData smd2 = new SecMetaData();
      smd2.setDomain("java:/jaas/domain2");
      xads.setSecurityMetaData(smd2);

      ArrayList<ConnMetaData> deployments = new ArrayList<ConnMetaData>();
      deployments.add(ds);
      deployments.add(xads);
      dsmd.setDeployments(deployments);

      MutableAttachments a1 = (MutableAttachments) ctx1.getPredeterminedManagedObjects();
      a1.addAttachment(DSMetaData.class, dsmd);
      // The mbeans associated with the local DS
      TestServiceMetaData localMBeans = new TestServiceMetaData();
      localMBeans.setCode(DSService.class.getName());
      ArrayList<TestServiceAttributeMetaData> localMBeanAttrs = new ArrayList<TestServiceAttributeMetaData>();
      localMBeanAttrs.add(new TestServiceAttributeMetaData("java:DefaultDS1", "managementName"));
      localMBeans.setAttributes(localMBeanAttrs);
      a1.addAttachment(TestServiceMetaData.class, localMBeans);
      ps.addDeployment(ctx1);

      // Deploy security domain1
      Deployment secCtx1 = createSimpleDeployment("sec-domain1");
      MutableAttachments sda1 = (MutableAttachments) secCtx1.getPredeterminedManagedObjects();
      SecurityDeployment sd1 = new SecurityDeployment();
      sd1.setDomainName("java:/jaas/domain1");
      sda1.addAttachment(SecurityDeployment.class, sd1);
      ps.addDeployment(secCtx1);
      // Deploy security domain2
      Deployment secCtx2 = createSimpleDeployment("sec-domain2");
      MutableAttachments sda2 = (MutableAttachments) secCtx2.getPredeterminedManagedObjects();
      SecurityDeployment sd2 = new SecurityDeployment();
      sd2.setDomainName("java:/jaas/domain2");
      sda2.addAttachment(SecurityDeployment.class, sd2);
      ps.addDeployment(secCtx2);

      // Process the deployments to build the ManagedObjects
      ps.process();

      // Validate the deployment1 ManagedObjects
      ManagedDeployment mo1 = ps.getManagedDeployment("deployment1");
      validateDeployment1(mo1, ps);

      // Validate that the ManagedDeployment is serializable
      assertInstanceOf(mo1, Serializable.class);
      byte[] bytes = serialize((Serializable)mo1);
      ManagedDeployment dmo1 = (ManagedDeployment)deserialize(bytes);
      validateDeployment1(dmo1, ps);
   }

   protected void validateDeployment1(ManagedDeployment mo1, MockProfileService ps)
   {
      // Validate the deployment1 ManagedObjects
      assertNotNull("deployment1 ManagedDeployment", mo1);
      ManagedProperty deploymentsProp = mo1.getProperty("deployments");
      assertNotNull("deployments prop", deploymentsProp);
      // Get the deployments MCs
      Map<String, ManagedComponent> mcs1 = mo1.getComponents();
      assertEquals("deployment1 has 2 ManagedComponent", 2, mcs1.size());

      // Get the deployments property MOs
      ManagedComponent localDataMO = null;
      ManagedComponent xaDataMO = null;
      for(ManagedComponent mc : mcs1.values())
      {
         if (mc.getAttachmentName().equals(LocalDataSourceMetaData.class.getName()))
            localDataMO = mc;
         else if (mc.getAttachmentName().equals(XADataSourceMetaData.class.getName()))
            xaDataMO = mc;
      }
      // Get the LocalDataSourceMetaData/SecMetaData/domain ManagedProperty
      assertNotNull("LocalDataSourceMetaData MO", localDataMO);
      assertEquals("LocalDataSourceMetaData comp type", new ComponentType("DataSource", "LocalTx"), localDataMO.getType());
      Map<String, ManagedProperty> localDataProps = localDataMO.getProperties();
      log.debug("LocalDataSourceMetaData MO.props: "+localDataProps);
      assertNotNull("LocalDataSourceMetaData MO.props", localDataProps);
      ManagedProperty localSecDomainProp = localDataMO.getProperty("security-domain");
      assertNotNull("localSecDomainProp", localSecDomainProp);
      GenericValue localSecDomainPropGV = (GenericValue) localSecDomainProp.getValue();
      ManagedObject localSecDomainPropMO = (ManagedObject) localSecDomainPropGV.getValue();
      ManagedProperty localSecDomainRefProp = localSecDomainPropMO.getProperty("domain-name");
      assertNotNull("localSecDomainRefProp", localSecDomainRefProp);

      // Get the XADataSourceMetaData/SecMetaData/domain ManagedProperty
      log.debug("XADataSourceMetaData MO: "+xaDataMO);
      assertNotNull("XADataSourceMetaData", xaDataMO);
      assertEquals("XADataSourceMetaData comp type", new ComponentType("DataSource", "XA"), xaDataMO.getType());
      assertNotNull("XADataSourceMetaData MO.props", xaDataMO.getProperties());
      ManagedProperty xaSecDomainProp = xaDataMO.getProperty("security-domain");
      assertNotNull("xaSecDomainProp", xaSecDomainProp);
      GenericValue xaSecDomainPropGV = (GenericValue) xaSecDomainProp.getValue();
      ManagedObject xaSecDomainPropMO = (ManagedObject) xaSecDomainPropGV.getValue();
      ManagedProperty xaSecDomainRefProp = xaSecDomainPropMO.getProperty("domain-name");
      assertNotNull("xaSecDomainRefProp", xaSecDomainRefProp);

      // Should be 0 unresolved ManagementObjectRef for the sec-domain1/sec-domain2
      Map<String, Set<ManagedProperty>> unresolvedRefs = ps.getUnresolvedRefs();
      log.info("unresolvedRefs: "+unresolvedRefs);
      assertEquals("Should be 0 ManagementObjectRef", 0, unresolvedRefs.size());

      ManagedDeployment secMD1 = ps.getManagedDeployment("sec-domain1");
      ManagedDeployment secMD2 = ps.getManagedDeployment("sec-domain2");

      // Validate that the sec-domain1 ManagedObject is the target of the localSecDomainRefProp
      log.info("sec-domain1 ManagedObjectNames: "+ secMD1.getManagedObjectNames());
      ManagedObject sd1MO = secMD1.getManagedObject("java:/jaas/domain1");
      
      assertNotNull("java:/jaas/domain1 MO", sd1MO);
      ManagedObject localSecDomainPropTarget = localSecDomainRefProp.getTargetManagedObject();
      assertEquals(sd1MO, localSecDomainPropTarget);

      // Validate that the sec-domain2 ManagedObject is the target of the xaSecDomainRefProp
      ManagedObject sd2MO = secMD2.getManagedObject("java:/jaas/domain2");
      assertNotNull("java:/jaas/domain2 MO", sd2MO);
      ManagedObject xaSecDomainPropTarget = xaSecDomainRefProp.getTargetManagedObject();
      assertEquals(sd2MO, xaSecDomainPropTarget);

      // Validate the operations on the localDataMO
      Set<ManagedOperation> localDataOps = localDataMO.getOperations();
      assertEquals("localDataOps ops count is 4", 4, localDataOps.size());
      ManagedOperation flushPool = null;
      ManagedOperation closePool = null;
      ManagedOperation takesString = null;
      ManagedOperation constrainedIntx10 = null;
      
      for(ManagedOperation op : localDataOps)
      {
         if (op.getName().equals("flushPool"))
            flushPool = op;
         if (op.getName().equals("closePool"))
            closePool = op;
         if (op.getName().equals("takesString"))
            takesString = op;
         if (op.getName().equals("constrainedIntx10"))
            constrainedIntx10 = op;
         
      }
      // flushPool
      assertNotNull("flushPool found", flushPool);
      assertEquals("flushPool", flushPool.getName());
      assertEquals("Flush the connections in the pool", flushPool.getDescription());
      assertEquals(ManagedOperation.Impact.WriteOnly, flushPool.getImpact());
      assertEquals(SimpleMetaType.VOID, flushPool.getReturnType());
      assertEquals("zero params", 0, flushPool.getParameters().length);
      // closePool
      assertNotNull("closePool found", closePool);
      assertEquals("closePool", closePool.getName());
      assertEquals("Close the connections in the pool", closePool.getDescription());
      assertEquals(ManagedOperation.Impact.WriteOnly, closePool.getImpact());
      assertEquals(SimpleMetaType.VOID, closePool.getReturnType());
      assertEquals("zero params", 0, closePool.getParameters().length);
      // takesString
      assertNotNull("takesString found", takesString);
      assertEquals("takesString", takesString.getName());
      assertEquals("Takes a string and returns it", takesString.getDescription());
      assertEquals(ManagedOperation.Impact.ReadOnly, takesString.getImpact());
      assertEquals(SimpleMetaType.STRING, takesString.getReturnType());
      ManagedParameter[] takesStringParams = takesString.getParameters();
      assertEquals("one params", 1, takesStringParams.length);
      assertEquals("param name", "input", takesStringParams[0].getName());
      assertEquals("param description", "The string to return", takesStringParams[0].getDescription());
      assertEquals("param type", SimpleMetaType.STRING, takesStringParams[0].getMetaType());
      // constrainedIntx10
      assertNotNull("constrainedIntx10 found", constrainedIntx10);
      assertEquals("constrainedIntx10", constrainedIntx10.getName());
      assertEquals("Takes an int and multiples by 10", constrainedIntx10.getDescription());
      assertEquals(ManagedOperation.Impact.ReadOnly, constrainedIntx10.getImpact());
      assertEquals(SimpleMetaType.INTEGER_PRIMITIVE, constrainedIntx10.getReturnType());
      ManagedParameter[] constrainedIntx10Params = constrainedIntx10.getParameters();
      assertEquals("one params", 1, constrainedIntx10Params.length);
      assertEquals("param name", "input", constrainedIntx10Params[0].getName());
      assertEquals("param description", "The int to multiple", constrainedIntx10Params[0].getDescription());
      assertEquals("param type", SimpleMetaType.INTEGER_PRIMITIVE, constrainedIntx10Params[0].getMetaType());
      Object min = constrainedIntx10Params[0].getMinimumValue();
      assertEquals("param min is 0", 0, (MetaValue)min);
      assertEquals("param min is 100", 100, (MetaValue)constrainedIntx10Params[0].getMaximumValue());
      
      // Validate that the localDataMO includes the runtime properties
      ManagedProperty rtp1 = localDataProps.get("runtimeProp1");
      assertNotNull("runtimeProp1", rtp1);
      ManagedProperty rtp2 = localDataProps.get("runtimeProp2");
      assertNotNull("runtimeProp2", rtp2);      
   }

   protected DeployerClient getMainDeployer()
   {
      return createMainDeployer(deployer);
   }
}
