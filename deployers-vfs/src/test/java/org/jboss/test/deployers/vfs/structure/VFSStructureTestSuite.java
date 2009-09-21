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
package org.jboss.test.deployers.vfs.structure;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.jboss.test.deployers.vfs.structure.dir.test.DirStructureUnitTestCase;
import org.jboss.test.deployers.vfs.structure.dir.test.EsbStructureUnitTestCase;
import org.jboss.test.deployers.vfs.structure.dir.test.GroupingStructureUnitTestCase;
import org.jboss.test.deployers.vfs.structure.dir.test.RealDirStructureUnitTestCase;
import org.jboss.test.deployers.vfs.structure.ear.test.EARStructureRecognizeTestCase;
import org.jboss.test.deployers.vfs.structure.ear.test.EARStructureUnitTestCase;
import org.jboss.test.deployers.vfs.structure.ear.test.InnerModificationUnitTestCase;
import org.jboss.test.deployers.vfs.structure.explicit.test.DeclaredStructureUnitTestCase;
import org.jboss.test.deployers.vfs.structure.explicit.test.ModificationTypeUnitTestCase;
import org.jboss.test.deployers.vfs.structure.file.test.CombinedFileStructureUnitTestCase;
import org.jboss.test.deployers.vfs.structure.file.test.ConfiguredSuffixFileStructureUnitTestCase;
import org.jboss.test.deployers.vfs.structure.file.test.FileMatcherTestCase;
import org.jboss.test.deployers.vfs.structure.file.test.FileStructureUnitTestCase;
import org.jboss.test.deployers.vfs.structure.jar.test.CombinedJARStructureUnitTestCase;
import org.jboss.test.deployers.vfs.structure.jar.test.ConfiguredSuffixJARStructureUnitTestCase;
import org.jboss.test.deployers.vfs.structure.jar.test.JARStructureUnitTestCase;
import org.jboss.test.deployers.vfs.structure.modified.test.DirRemovalModificationTestCase;
import org.jboss.test.deployers.vfs.structure.modified.test.MetaDataStructureModificationTestCase;
import org.jboss.test.deployers.vfs.structure.modified.test.MetaDataStructureModificationTreeCacheTestCase;
import org.jboss.test.deployers.vfs.structure.modified.test.SynchModificationTestCase;
import org.jboss.test.deployers.vfs.structure.test.StructureDeployerContextClassLoaderTestCase;
import org.jboss.test.deployers.vfs.structure.test.TerminateStructureTestCase;
import org.jboss.test.deployers.vfs.structure.war.test.CombinedWARStructureUnitTestCase;
import org.jboss.test.deployers.vfs.structure.war.test.WARStructureUnitTestCase;
import org.jboss.test.deployers.vfs.structure.war.test.WARUnpackUnitTestCase;

/**
 * VFSStructureTestSuite.
 * 
 * @author <a href="adrian@jboss.org">Adrian Brock</a>
 * @author <a href="ales.justin@jboss.org">Ales Justin</a>
 * @version $Revision: 1.1 $
 */
public class VFSStructureTestSuite extends TestSuite
{
   public static void main(String[] args)
   {
      TestRunner.run(suite());
   }

   public static Test suite()
   {
      TestSuite suite = new TestSuite("VFS Structure Tests");

      suite.addTest(ConfiguredSuffixJARStructureUnitTestCase.suite());
      suite.addTest(JARStructureUnitTestCase.suite());
      suite.addTest(WARStructureUnitTestCase.suite());
      suite.addTest(ConfiguredSuffixFileStructureUnitTestCase.suite());
      suite.addTest(FileStructureUnitTestCase.suite());
      suite.addTest(FileMatcherTestCase.suite());
      suite.addTest(DeclaredStructureUnitTestCase.suite());
      suite.addTest(EARStructureUnitTestCase.suite());
      suite.addTest(EARStructureRecognizeTestCase.suite());
      suite.addTest(CombinedJARStructureUnitTestCase.suite());
      suite.addTest(CombinedWARStructureUnitTestCase.suite());
      suite.addTest(CombinedFileStructureUnitTestCase.suite());
      suite.addTest(TerminateStructureTestCase.suite());
      suite.addTest(StructureDeployerContextClassLoaderTestCase.suite());
      suite.addTest(WARUnpackUnitTestCase.suite());
      suite.addTest(ModificationTypeUnitTestCase.suite());
      suite.addTest(InnerModificationUnitTestCase.suite());
      suite.addTest(DirStructureUnitTestCase.suite());
      suite.addTest(RealDirStructureUnitTestCase.suite());
      suite.addTest(GroupingStructureUnitTestCase.suite());
      suite.addTest(EsbStructureUnitTestCase.suite());
      suite.addTest(MetaDataStructureModificationTestCase.suite());
      suite.addTest(MetaDataStructureModificationTreeCacheTestCase.suite());
      suite.addTest(SynchModificationTestCase.suite());
      suite.addTest(DirRemovalModificationTestCase.suite());

      return suite;
   }
}
