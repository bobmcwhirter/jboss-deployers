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
import org.jboss.deployers.spi.attachments.MutableAttachments;
import org.jboss.deployers.structure.spi.helpers.AbstractDeploymentContext;
import org.jboss.test.deployers.attachments.test.AttachmentsTest;

/**
 * AbstractDeploymentContextTransientAttachmentsUnitTestCase.
 * 
 * @author <a href="adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class AbstractDeploymentContextTransientAttachmentsUnitTestCase extends AttachmentsTest
{
   public static Test suite()
   {
      return new TestSuite(AbstractDeploymentContextTransientAttachmentsUnitTestCase.class);
   }
   
   public AbstractDeploymentContextTransientAttachmentsUnitTestCase(String name)
   {
      super(name);
   }

   protected Attachments getAttachments()
   {
      AbstractDeploymentContext context = new AbstractDeploymentContext("test", "");
      return context.getTransientAttachments();
   }

   protected MutableAttachments getMutable()
   {
      AbstractDeploymentContext context = new AbstractDeploymentContext("test", "");
      return context.getTransientAttachments();
   }
}
