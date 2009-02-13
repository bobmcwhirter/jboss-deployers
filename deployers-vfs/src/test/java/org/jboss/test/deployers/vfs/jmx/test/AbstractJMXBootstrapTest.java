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
package org.jboss.test.deployers.vfs.jmx.test;

import java.util.HashSet;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.test.deployers.BootstrapDeployersTest;
import org.jboss.test.deployers.BootstrapDeployersTestDelegate;

/**
 * AbstractJMXBootstrapTest.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public abstract class AbstractJMXBootstrapTest extends BootstrapDeployersTest
{
   public AbstractJMXBootstrapTest(String name)
   {
      super(name);
   }
   
   public static BootstrapDeployersTestDelegate getDelegate(Class<?> clazz) throws Exception
   {
      BootstrapDeployersTestDelegate delegate = BootstrapDeployersTest.getDelegate(clazz);
      MBeanServer server = MBeanServerFactory.newMBeanServer();
      delegate.setMBeanServer(server);
      return delegate;
   }
   
   protected MBeanServer getMBeanServer()
   {
      return getDelegate().getMBeanServer();
   }
   
   protected Set<ObjectName> assertMBeans(DeploymentUnit unit) throws Exception
   {
      Set<ObjectName> names = new HashSet<ObjectName>();
      assertTopLevelMBean(unit, names);
      for (DeploymentUnit child : unit.getChildren())
         assertSubDeploymentMBean(child, names);
      for (DeploymentUnit component : unit.getComponents())
         assertComponentMBean(component, names);
      return names;
   }
   
   protected void assertNoMBeans(Set<ObjectName> names) throws Exception
   {
      for (ObjectName objectName : names)
      {
         boolean result = getMBeanServer().isRegistered(objectName);
         getLog().debug(objectName + " isRegistered=" + result);
         assertFalse(objectName + " should NOT be registered with the MBeanServer", result);
      }
   }
   
   protected void assertNoMBeans(DeploymentUnit unit) throws Exception
   {
      assertNoTopLevelMBean(unit);
      for (DeploymentUnit child : unit.getChildren())
         assertNoSubDeploymentMBean(child);
      for (DeploymentUnit component : unit.getComponents())
         assertNoComponentMBean(component);
   }
   
   protected void assertTopLevelMBean(DeploymentUnit unit, Set<ObjectName> names) throws Exception
   {
      ObjectName objectName = getTopLevelObjectName(unit);
      names.add(objectName);
      boolean result = getMBeanServer().isRegistered(objectName);
      getLog().debug(objectName + " isRegistered=" + result);
      assertTrue(objectName + " should be registered with the MBeanServer", result);
   }
   
   protected void assertNoTopLevelMBean(DeploymentUnit unit) throws Exception
   {
      ObjectName objectName = getTopLevelObjectName(unit); 
      boolean result = getMBeanServer().isRegistered(objectName);
      getLog().debug(objectName + " isRegistered=" + result);
      assertFalse(objectName + " should NOT be registered with the MBeanServer", result);
   }
   
   protected ObjectName getTopLevelObjectName(DeploymentUnit unit) throws Exception
   {
      String name = unit.getName();
      return new ObjectName("jboss.deployment:id=\"" + name + "\",type=Deployment"); 
   }
   
   protected void assertSubDeploymentMBean(DeploymentUnit unit, Set<ObjectName> names) throws Exception
   {
      ObjectName objectName = getSubDeploymentObjectName(unit); 
      names.add(objectName);
      boolean result = getMBeanServer().isRegistered(objectName);
      getLog().debug(objectName + " isRegistered=" + result);
      assertTrue(objectName + " should be registered with the MBeanServer", result);
      for (DeploymentUnit child : unit.getChildren())
         assertSubDeploymentMBean(child, names);
      for (DeploymentUnit component : unit.getComponents())
         assertComponentMBean(component, names);
   }
   
   protected void assertNoSubDeploymentMBean(DeploymentUnit unit) throws Exception
   {
      ObjectName objectName = getSubDeploymentObjectName(unit); 
      boolean result = getMBeanServer().isRegistered(objectName);
      getLog().debug(objectName + " isRegistered=" + result);
      assertFalse(objectName + " should NOT be registered with the MBeanServer", result);
      for (DeploymentUnit child : unit.getChildren())
         assertNoSubDeploymentMBean(child);
      for (DeploymentUnit component : unit.getComponents())
         assertNoComponentMBean(component);
   }
   
   protected ObjectName getSubDeploymentObjectName(DeploymentUnit unit) throws Exception
   {
      String name = unit.getName();
      return new ObjectName("jboss.deployment:id=\"" + name + "\",type=SubDeployment"); 
   }
   
   protected void assertComponentMBean(DeploymentUnit unit, Set<ObjectName> names) throws Exception
   {
      ObjectName objectName = getComponentObjectName(unit);
      names.add(objectName);
      boolean result = getMBeanServer().isRegistered(objectName);
      getLog().debug(objectName + " isRegistered=" + result);
      assertTrue(objectName + " should be registered with the MBeanServer", result);
   }
   
   protected void assertNoComponentMBean(DeploymentUnit unit) throws Exception
   {
      ObjectName objectName = getComponentObjectName(unit); 
      boolean result = getMBeanServer().isRegistered(objectName);
      getLog().debug(objectName + " isRegistered=" + result);
      assertFalse(objectName + " should NOT be registered with the MBeanServer", result);
   }
   
   protected ObjectName getComponentObjectName(DeploymentUnit unit) throws Exception
   {
      String name = unit.getName();
      return new ObjectName("jboss.deployment:id=\"" + name + "\",type=Component"); 
   }
}
