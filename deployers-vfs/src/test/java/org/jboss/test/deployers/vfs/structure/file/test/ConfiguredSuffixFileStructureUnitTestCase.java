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
package org.jboss.test.deployers.vfs.structure.file.test;

import java.util.HashSet;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.deployers.vfs.plugins.structure.file.FileStructure;
import org.jboss.test.BaseTestCase;

/**
 * FileStructureUnitTestCase.
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
public class ConfiguredSuffixFileStructureUnitTestCase extends BaseTestCase
{
   /** The file structure deployer */
   private static final Set<String> defaultSuffixes = new HashSet<String>(new FileStructure().getSuffixes());
   
   public static Test suite()
   {
      return new TestSuite(ConfiguredSuffixFileStructureUnitTestCase.class);
   }
   
   public ConfiguredSuffixFileStructureUnitTestCase(String name)
   {
      super(name);
   }

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      enableTrace("org.jboss.deployers");
   }
  
   public void testDefaults() throws Exception
   {
      assertNotNull("default suffixes should not be null", defaultSuffixes);
      assertTrue("default suffixes size should be > 0", defaultSuffixes.size() > 0);
   }
   
   public void testNewUsingDefaults() throws Exception
   {
      FileStructure structure = new FileStructure();

      Set<String> suffixes = structure.getSuffixes();
      assertNotNull(suffixes);
      assertEquals(defaultSuffixes.size(), suffixes.size());
      for (String suff : defaultSuffixes)
      {
         suffixes.contains(suff);
      }
   }
   
   public void testOverwriteDefaults() throws Exception
   {
      try
      {
         Set<String> newSuffixes = new HashSet<String>();
         
         newSuffixes = new HashSet<String>();
         newSuffixes.add("-ds.xml");
         newSuffixes.add("-dd.xml");
         newSuffixes.add("-service.xml");
         
         FileStructure structure = new FileStructure(newSuffixes);
         
         Set<String> suffixes = structure.getSuffixes();
         assertNotNull(suffixes);
         assertEquals(3, suffixes.size());
         assertTrue(suffixes.contains("-ds.xml"));
         assertTrue(suffixes.contains("-dd.xml"));
         assertTrue(suffixes.contains("-service.xml"));
      }
      finally
      {
         // Reset
         new FileStructure(defaultSuffixes);
      }
   }
   
}
