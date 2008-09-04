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
package org.jboss.test.deployers.structure.attachments.test;

import java.util.Collections;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.deployers.spi.attachments.MutableAttachments;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.structure.spi.helpers.AbstractDeploymentContext;
import org.jboss.deployers.structure.spi.helpers.AbstractDeploymentUnit;
import org.jboss.test.BaseTestCase;

/**
 * DeploymentUnitAttachmentHierarchyUnitTestCase.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class AbstractDeploymentUnitAttachmentHierarchyUnitTestCase extends BaseTestCase
{
   public static Test suite()
   {
      return new TestSuite(AbstractDeploymentUnitAttachmentHierarchyUnitTestCase.class);
   }
   
   private DeploymentUnit unit;

   private MutableAttachments predeterminedManagedObjects;
   
   private MutableAttachments transientManagedObjects;

   private MutableAttachments transientAttachments;
   
   public AbstractDeploymentUnitAttachmentHierarchyUnitTestCase(String name)
   {
      super(name);
      AbstractDeploymentContext context = new AbstractDeploymentContext("attachments", "");
      unit = new AbstractDeploymentUnit(context);
      context.setDeploymentUnit(unit);
      predeterminedManagedObjects = (MutableAttachments) context.getPredeterminedManagedObjects();
      transientManagedObjects = context.getTransientManagedObjects();
      transientAttachments = context.getTransientAttachments();
   }
   
   public void testPredeterminedIsFirstByName()
   {
      predeterminedManagedObjects.addAttachment("Name", "pMO");
      transientManagedObjects.addAttachment("Name", "tMO");
      transientAttachments.addAttachment("Name", "tA");
      assertEquals("pMO", unit.getAttachment("Name"));
      Map<String, String> expected = Collections.singletonMap("Name", "pMO");
      assertEquals(expected, unit.getAttachments());
   }
   
   public void testPredeterminedIsFirstByNameAndType()
   {
      predeterminedManagedObjects.addAttachment("Name", "pMO", String.class);
      transientManagedObjects.addAttachment("Name", "tMO", String.class);
      transientAttachments.addAttachment("Name", "tA", String.class);
      assertEquals("pMO", unit.getAttachment("Name"));
      assertEquals("pMO", unit.getAttachment("Name", String.class));
      Map<String, String> expected = Collections.singletonMap("Name", "pMO");
      assertEquals(expected, unit.getAttachments());
   }
   
   public void testPredeterminedIsFirstByType()
   {
      predeterminedManagedObjects.addAttachment(String.class, "pMO");
      transientManagedObjects.addAttachment(String.class, "tMO");
      transientAttachments.addAttachment(String.class, "tA");
      assertEquals("pMO", unit.getAttachment(String.class.getName()));
      assertEquals("pMO", unit.getAttachment(String.class.getName(), String.class));
      assertEquals("pMO", unit.getAttachment(String.class));
      Map<String, String> expected = Collections.singletonMap(String.class.getName(), "pMO");
      assertEquals(expected, unit.getAttachments());
   }
   
   public void testTransientManagedObjectsIsSecondByName()
   {
      transientManagedObjects.addAttachment("Name", "tMO");
      transientAttachments.addAttachment("Name", "tA");
      assertEquals("tMO", unit.getAttachment("Name"));
      Map<String, String> expected = Collections.singletonMap("Name", "tMO");
      assertEquals(expected, unit.getAttachments());
   }
   
   public void testTransientManagedObjectsIsSecondAndType()
   {
      transientManagedObjects.addAttachment("Name", "tMO", String.class);
      transientAttachments.addAttachment("Name", "tA", String.class);
      assertEquals("tMO", unit.getAttachment("Name"));
      assertEquals("tMO", unit.getAttachment("Name", String.class));
      Map<String, String> expected = Collections.singletonMap("Name", "tMO");
      assertEquals(expected, unit.getAttachments());
   }
   
   public void testTransientManagedObjectsIsSecondByType()
   {
      transientManagedObjects.addAttachment(String.class, "tMO");
      transientAttachments.addAttachment(String.class, "tA");
      assertEquals("tMO", unit.getAttachment(String.class.getName()));
      assertEquals("tMO", unit.getAttachment(String.class.getName(), String.class));
      assertEquals("tMO", unit.getAttachment(String.class));
      Map<String, String> expected = Collections.singletonMap(String.class.getName(), "tMO");
      assertEquals(expected, unit.getAttachments());
   }
   
   public void testTransientAttrachmentsIsThirdByName()
   {
      transientAttachments.addAttachment("Name", "tA");
      assertEquals("tA", unit.getAttachment("Name"));
      Map<String, String> expected = Collections.singletonMap("Name", "tA");
      assertEquals(expected, unit.getAttachments());
   }
   
   public void testTransientAttrachmentsIsThirdAndType()
   {
      transientAttachments.addAttachment("Name", "tA", String.class);
      assertEquals("tA", unit.getAttachment("Name"));
      assertEquals("tA", unit.getAttachment("Name", String.class));
      Map<String, String> expected = Collections.singletonMap("Name", "tA");
      assertEquals(expected, unit.getAttachments());
   }
   
   public void testTransientAttrachmentsIsThirdByType()
   {
      transientAttachments.addAttachment(String.class, "tA");
      assertEquals("tA", unit.getAttachment(String.class.getName()));
      assertEquals("tA", unit.getAttachment(String.class.getName(), String.class));
      assertEquals("tA", unit.getAttachment(String.class));
      Map<String, String> expected = Collections.singletonMap(String.class.getName(), "tA");
      assertEquals(expected, unit.getAttachments());
   }
}
