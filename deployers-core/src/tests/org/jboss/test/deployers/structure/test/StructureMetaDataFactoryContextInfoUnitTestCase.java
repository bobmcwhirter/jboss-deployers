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

import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.jboss.deployers.spi.structure.ClassPathEntry;
import org.jboss.deployers.spi.structure.ContextInfo;
import org.jboss.deployers.spi.structure.StructureMetaDataFactory;
import org.jboss.test.deployers.structure.AbstractContextInfoTest;

/**
 * ContextInfoImplUnitTestCase.
 * 
 * @author <a href="adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class StructureMetaDataFactoryContextInfoUnitTestCase extends AbstractContextInfoTest
{
   public static Test suite()
   {
      return new TestSuite(StructureMetaDataFactoryContextInfoUnitTestCase.class);
   }
   
   public StructureMetaDataFactoryContextInfoUnitTestCase(String name)
   {
      super(name);
   }

   @Override
   protected ContextInfo createDefault()
   {
      return StructureMetaDataFactory.createContextInfo();
   }

   @Override
   protected ContextInfo createPath(String path)
   {
      return StructureMetaDataFactory.createContextInfo(path);
   }

   @Override
   protected ContextInfo createPathAndClassPath(String path, List<ClassPathEntry> classPath)
   {
      return StructureMetaDataFactory.createContextInfo(path, classPath);
   }
   
   @Override
   protected ContextInfo createPathAndMetaDataAndClassPath(String path, String metaDataPath, List<ClassPathEntry> classPath)
   {
      return StructureMetaDataFactory.createContextInfo(path, metaDataPath, classPath);
   }

   @Override
   protected ContextInfo createPathAndMetaDataAndClassPath(String path, List<String> metaDataPath, List<ClassPathEntry> classPath)
   {
      return StructureMetaDataFactory.createContextInfo(path, metaDataPath, classPath);
   }

   @Override
   protected ClassPathEntry createClassPathEntry(String path)
   {
      return StructureMetaDataFactory.createClassPathEntry(path);
   }
}
