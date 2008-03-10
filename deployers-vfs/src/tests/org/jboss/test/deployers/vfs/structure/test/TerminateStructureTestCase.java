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
package org.jboss.test.deployers.vfs.structure.test;

import junit.framework.Test;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.structure.StructureMetaData;
import org.jboss.deployers.vfs.spi.client.VFSDeployment;
import org.jboss.deployers.vfs.spi.structure.StructureDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentContext;
import org.jboss.deployers.vfs.spi.structure.VFSStructuralDeployers;
import org.jboss.deployers.vfs.spi.structure.helpers.AbstractStructureDeployer;
import org.jboss.test.deployers.vfs.structure.AbstractStructureTest;
import org.jboss.virtual.VirtualFile;

/**
 * Terminate test case.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class TerminateStructureTestCase extends AbstractStructureTest
{
   public TerminateStructureTestCase(String name)
   {
      super(name);
   }

   public static Test suite()
   {
      return suite(TerminateStructureTestCase.class);
   }

   protected StructureDeployer[] getStructureDeployers(int failNumber, int size, boolean checkCause)
   {
      StructureDeployer[] deployers = new StructureDeployer[size];
      for(int i = 0; i < size; i++)
      {
         if (i == failNumber)
         {
            if (checkCause)
               deployers[i] = new REStructureDeployer(i);
            else
               deployers[i] = new FailStructureDeployer(i);
         }
         else
            deployers[i] = new PassStructureDeployer(i);
      }
      return deployers;
   }

   protected void checkFailedNumber(VFSDeployment deployment, int failNumber, int size, boolean checkCause)
         throws Exception
   {
      try
      {
         determineStructureWithStructureDeployers(deployment, getStructureDeployers(failNumber, size, checkCause));
         fail("Should not be here.");
      }
      catch (Throwable t)
      {
         if (checkCause)
            t = t.getCause();
         String msg = t.getMessage();
         int number = Integer.parseInt(msg);
         assertEquals(failNumber, number);
      }
   }

   public void testTerminate() throws Exception
   {
      // some deployment
      VFSDeployment deployment = createDeployment("/structure/file", "simple");
      checkFailedNumber(deployment, 0, 3, false);
      checkFailedNumber(deployment, 1, 3, false);
      checkFailedNumber(deployment, 2, 3, false);
   }

   public void testRuntimeTerminate() throws Exception
   {
      // some deployment
      VFSDeployment deployment = createDeployment("/structure/file", "simple");
      checkFailedNumber(deployment, 0, 3, true);
      checkFailedNumber(deployment, 1, 3, true);
      checkFailedNumber(deployment, 2, 3, true);
   }

   protected VFSDeploymentContext determineStructure(VFSDeployment deployment) throws Exception
   {
      throw new UnsupportedOperationException("No use case.");
   }

   private class PassStructureDeployer extends AbstractStructureDeployer
   {
      public PassStructureDeployer(int order)
      {
         setRelativeOrder(order);
      }

      public boolean determineStructure(VirtualFile root, VirtualFile parent, VirtualFile file, StructureMetaData metaData, VFSStructuralDeployers deployers) throws DeploymentException
      {
         return false;
      }
   }

   private class FailStructureDeployer extends AbstractStructureDeployer
   {
      public FailStructureDeployer(int order)
      {
         setRelativeOrder(order);
      }

      public boolean determineStructure(VirtualFile root, VirtualFile parent, VirtualFile file, StructureMetaData metaData, VFSStructuralDeployers deployers) throws DeploymentException
      {
         throw new DeploymentException(String.valueOf(getRelativeOrder()));
      }
   }

   private class REStructureDeployer extends AbstractStructureDeployer
   {
      public REStructureDeployer(int order)
      {
         setRelativeOrder(order);
      }

      public boolean determineStructure(VirtualFile root, VirtualFile parent, VirtualFile file, StructureMetaData metaData, VFSStructuralDeployers deployers) throws DeploymentException
      {
         throw new RuntimeException(String.valueOf(getRelativeOrder()));
      }
   }
}
