/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.test.deployers.vfs.structure.modified.test;

import org.jboss.deployers.structure.spi.main.MainDeployerInternals;
import org.jboss.deployers.vfs.spi.structure.modified.MetaDataStructureModificationChecker;
import org.jboss.deployers.vfs.spi.structure.modified.StructureModificationChecker;
import org.jboss.deployers.vfs.spi.structure.modified.SynchAdapter;
import org.jboss.deployers.vfs.spi.structure.modified.SynchWrapperModificationChecker;
import org.jboss.test.deployers.BootstrapDeployersTest;
import org.jboss.virtual.VirtualFileFilter;
import org.jboss.virtual.VisitorAttributes;

/**
 * AbstractSynchTest.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class AbstractSynchTest extends BootstrapDeployersTest
{
   protected AbstractSynchTest(String name)
   {
      super(name);
   }

   protected StructureModificationChecker createStructureModificationChecker()
   {
      MainDeployerInternals mainDeployerInternals = getMainDeployerInternals();
      VirtualFileFilter filter = createFilter();
      VirtualFileFilter recurseFilter = createRecurseFilter();
      SynchAdapter synchAdapter = createSynchAdapter();

      MetaDataStructureModificationChecker mdsmc = new MetaDataStructureModificationChecker(mainDeployerInternals);
      mdsmc.setFilter(filter);
      mdsmc.start();

      SynchWrapperModificationChecker synch = new SynchWrapperModificationChecker(mdsmc, synchAdapter);

      VisitorAttributes attributes = new VisitorAttributes();
      attributes.setLeavesOnly(true);
      attributes.setRecurseFilter(recurseFilter);

      synch.setOriginalAttributes(attributes);
      synch.setTempAttributes(attributes);

      return synch;
   }

   protected abstract VirtualFileFilter createFilter();

   protected abstract VirtualFileFilter createRecurseFilter();

   protected abstract SynchAdapter createSynchAdapter();
}