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
package org.jboss.test.deployers.deployer.test;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.deployers.plugins.sort.DeployerSorter;
import org.jboss.deployers.plugins.sort.DominoDeployerSorter;

/**
 * Old domino sorting.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class DominoOrderingUnitTestCase extends AbstractSorterOrderingUnitTest
{
   public DominoOrderingUnitTestCase(String name)
   {
      super(name);
   }

   public static Test suite()
   {
      return new TestSuite(DominoOrderingUnitTestCase.class);
   }

   @Override
   protected DeployerSorter createSorter()
   {
      return new DominoDeployerSorter();
   }
   
   public void testAlgorithmPerformance()
   {
      System.out.println("------------------------------------------------------------------------");
      System.out.println("Exhaustive deployer sorting (" + getClass().getSimpleName() +  ") took: NOT MEASURED (too slow)");
      System.out.println("------------------------------------------------------------------------");
   }
}