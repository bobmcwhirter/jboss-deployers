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
package org.jboss.test.deployers.structure.attachments;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.jboss.test.deployers.structure.attachments.test.AbstractDeploymentContextPredeterminedManagedObjectsUnitTestCase;
import org.jboss.test.deployers.structure.attachments.test.AbstractDeploymentContextTransientAttachmentsUnitTestCase;
import org.jboss.test.deployers.structure.attachments.test.AbstractDeploymentContextTransientManagedObjectsUnitTestCase;
import org.jboss.test.deployers.structure.attachments.test.AbstractDeploymentUnitAttachmentHierarchyUnitTestCase;
import org.jboss.test.deployers.structure.attachments.test.AbstractDeploymentUnitAttachmentsUnitTestCase;
import org.jboss.test.deployers.structure.attachments.test.AbstractDeploymentUnitPredeterminedManagedObjectsUnitTestCase;
import org.jboss.test.deployers.structure.attachments.test.AbstractDeploymentUnitTransientAttachmentsUnitTestCase;
import org.jboss.test.deployers.structure.attachments.test.AbstractDeploymentUnitTransientManagedObjectsUnitTestCase;

/**
 * StructureAttachmentsTestSuite.
 * 
 * @author <a href="adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class StructureAttachmentsTestSuite extends TestSuite
{
   public static void main(String[] args)
   {
      TestRunner.run(suite());
   }

   public static Test suite()
   {
      TestSuite suite = new TestSuite("Structure Attachment Tests");

      suite.addTest(AbstractDeploymentContextPredeterminedManagedObjectsUnitTestCase.suite());
      suite.addTest(AbstractDeploymentContextTransientManagedObjectsUnitTestCase.suite());
      suite.addTest(AbstractDeploymentContextTransientAttachmentsUnitTestCase.suite());
      suite.addTest(AbstractDeploymentUnitPredeterminedManagedObjectsUnitTestCase.suite());
      suite.addTest(AbstractDeploymentUnitTransientManagedObjectsUnitTestCase.suite());
      suite.addTest(AbstractDeploymentUnitTransientAttachmentsUnitTestCase.suite());
      suite.addTest(AbstractDeploymentUnitAttachmentsUnitTestCase.suite());
      suite.addTest(AbstractDeploymentUnitAttachmentHierarchyUnitTestCase.suite());

      return suite;
   }
}
