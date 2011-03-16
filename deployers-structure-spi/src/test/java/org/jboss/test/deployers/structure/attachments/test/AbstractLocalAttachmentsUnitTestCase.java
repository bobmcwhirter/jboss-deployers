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

import junit.framework.Test;
import junit.framework.TestSuite;
import org.jboss.deployers.spi.attachments.Attachments;
import org.jboss.deployers.spi.attachments.LocalAttachments;
import org.jboss.deployers.spi.attachments.MutableAttachments;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.structure.spi.helpers.AbstractDeploymentContext;
import org.jboss.deployers.structure.spi.helpers.AbstractDeploymentUnit;
import org.jboss.test.deployers.attachments.test.AttachmentsTest;
import org.jboss.test.deployers.attachments.test.ExpectedAttachments;

import java.util.Date;

/**
 * AbstractLocalAttachmentsUnitTestCase.
 * 
 * @author <a href="ales.justin@jboss.org">Ales Justin</a>
 */
public class AbstractLocalAttachmentsUnitTestCase extends AttachmentsTest
{
   public static Test suite()
   {
      return new TestSuite(AbstractLocalAttachmentsUnitTestCase.class);
   }

   private DeploymentUnit unit;

   public AbstractLocalAttachmentsUnitTestCase(String name)
   {
      super(name);
      AbstractDeploymentContext context = new AbstractDeploymentContext("attachments", "");
      unit = new AbstractDeploymentUnit(context);
      context.setDeploymentUnit(unit);
   }

   public void testLocal() throws Exception
   {
      DeploymentUnit component = unit.addComponent("component");

      Date date1 = new Date();
      Date date2 = new Date();
      Integer i1 = 1;

      unit.addAttachment(Date.class, date1);
      unit.addAttachment("date1", date1, Date.class);
      component.addAttachment(Date.class, date2);
      component.addAttachment("date2", date2, Date.class);
      unit.addAttachment(Integer.class, i1);

      ExpectedAttachments ea = new ExpectedAttachments();
      ea.add("date2", date2);
      ea.add(Date.class.getName(), date2);

      assertEquals(i1, unit.getAttachment(Integer.class));
      assertEquals(i1, component.getAttachment(Integer.class));

      LocalAttachments la = assertInstanceOf(component, LocalAttachments.class);
      assertEquals(date2, la.getLocalAttachment(Date.class));
      assertEquals(date2, la.getLocalAttachment("date2"));
      assertEquals(date2, la.getLocalAttachment("date2", Date.class));
      assertTrue(la.isLocalAttachmentPresent(Date.class));
      assertTrue(la.isLocalAttachmentPresent("date2"));
      assertTrue(la.isLocalAttachmentPresent("date2", Date.class));
      assertTrue(la.hasLocalAttachments());
      assertEquals(ea.expected, la.getLocalAttachments());
   }

   @Override
   public void testSerialization() throws Exception
   {
      // ignore
   }

   protected Attachments getAttachments()
   {
      return unit;
   }

   protected MutableAttachments getMutable()
   {
      return unit;
   }
}
