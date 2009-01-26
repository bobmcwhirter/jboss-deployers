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
package org.jboss.test.deployers.vfs.structureprocessor.test;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.jboss.deployers.structure.spi.StructureBuilder;
import org.jboss.deployers.structure.spi.StructureProcessor;
import org.jboss.deployers.structure.spi.helpers.AbstractStructureBuilder;
import org.jboss.test.deployers.BaseDeployersVFSTest;

/**
 * StructureProcessorUnitTest.
 *
 * @author <a href="ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class StructureProcessorUnitTest extends BaseDeployersVFSTest
{
   public static Test suite()
   {
      return new TestSuite(StructureProcessorUnitTest.class);
   }

   public StructureProcessorUnitTest(String name)
   {
      super(name);
   }

   @Override
   protected StructureBuilder createStructureBuilder()
   {
      StructureBuilder structureBuilder = super.createStructureBuilder();
      if (structureBuilder instanceof AbstractStructureBuilder)
      {
         AbstractStructureBuilder asb = AbstractStructureBuilder.class.cast(structureBuilder);
         asb.setStructureProcessor(createStructureProcessor());
      }
      return structureBuilder;
   }

   protected abstract StructureProcessor createStructureProcessor();
}