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
package org.jboss.test.deployers.managed.support;

import java.util.Map;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.spi.deployer.managed.ManagedObjectCreator;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.managed.api.ManagedObject;
import org.jboss.managed.api.ManagedProperty;
import org.jboss.managed.plugins.ManagedObjectImpl;
import org.jboss.managed.plugins.ManagedPropertyImpl;

/**
 * TestManagedObjectDeployer.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class TestManagedObjectDeployer extends AbstractDeployer implements ManagedObjectCreator
{
   public static TestAttachment lastAttachment;

   public TestManagedObjectDeployer()
   {
      setType("TestManagedObjectDeployer");
   }

   public void deploy(DeploymentUnit unit) throws DeploymentException
   {
      lastAttachment = unit.getAttachment(TestAttachment.class);
      if (lastAttachment == null)
      {
         lastAttachment = new TestAttachment();
         lastAttachment.setProperty("string1", "initialString1");
         lastAttachment.setProperty("string2", "initialString2");
         unit.addAttachment(TestAttachment.class, lastAttachment);
      }
   }

   public void build(DeploymentUnit unit, Map<String, ManagedObject> managedObjects) throws DeploymentException
   {
      TestAttachment attachment = unit.getAttachment(TestAttachment.class);
      if (attachment != null)
      {
         attachment = attachment.clone();
         ManagedObjectImpl managedObject = new ManagedObjectImpl(TestAttachment.class.getName());
         managedObject.setAttachment(attachment);
         Map<String, ManagedProperty> properties = managedObject.getProperties();
         properties.put("string1", new ManagedPropertyImpl(managedObject, new TestFields(attachment, "string1")));
         properties.put("string2", new ManagedPropertyImpl(managedObject, new TestFields(attachment, "string2")));
         managedObjects.put(TestAttachment.class.getName(), managedObject);
      }
   }
}
