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
package org.jboss.test.deployers.structure.test;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.deployers.spi.structure.ContextInfo;
import org.jboss.deployers.spi.structure.StructureMetaData;
import org.jboss.deployers.spi.structure.StructureMetaDataFactory;
import org.jboss.test.deployers.structure.AbstractStructureMetaDataTest;

/**
 * StructureMetaDataImplUnitTestCase.
 * 
 * @author <a href="adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class StructureMetaDataFactoryStructureUnitTestCase extends AbstractStructureMetaDataTest
{
   public static Test suite()
   {
      return new TestSuite(StructureMetaDataFactoryStructureUnitTestCase.class);
   }
   
   public StructureMetaDataFactoryStructureUnitTestCase(String name)
   {
      super(name);
   }

   @Override
   protected StructureMetaData createDefault()
   {
      return StructureMetaDataFactory.createStructureMetaData();
   }

   @Override
   protected ContextInfo createContext(String path)
   {
      return StructureMetaDataFactory.createContextInfo(path);
   }
}
