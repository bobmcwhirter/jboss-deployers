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

import java.util.List;

import org.jboss.deployers.vfs.spi.structure.modified.StructureCache;
import org.jboss.test.deployers.BootstrapDeployersTest;
import org.jboss.vfs.VirtualFile;
import org.jboss.vfs.VirtualFileFilter;

/**
 * AbstractStructureCache tests.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class AbstractStructureCacheTest extends BootstrapDeployersTest
{
   protected AbstractStructureCacheTest(String name)
   {
      super(name);
   }

   protected abstract StructureCache<Long> createStructureCache();

   public void testCacheBehavior() throws Throwable
   {
      VirtualFile root = createDeploymentRoot("/annotations", "basic-scan");

      StructureCache<Long> cache = createStructureCache();

      cache.initializeCache(root);

      VirtualFile meta_inf = root.getChild("META-INF");
      List<VirtualFile> leaves = cache.getLeaves(meta_inf);
      assertNull(leaves);

      VirtualFile a = meta_inf.getChild("application.properties");
      VirtualFile b = meta_inf.getChild("jboss-scanning.xml");
      cache.putCacheValue(a, 1l);
      cache.putCacheValue(b, 2l);

      cache.putCacheValue(meta_inf, 1l);

      leaves = cache.getLeaves(meta_inf);
      assertEquals(2, leaves.size());
      leaves = cache.getLeaves(meta_inf, new VirtualFileFilter()
      {
         public boolean accepts(VirtualFile file)
         {
            return file.getName().endsWith(".xml");
         }
      });
      assertEquals(1, leaves.size());

      assertNotNull(cache.getCacheValue(a));
      assertNotNull(cache.getCacheValue(b));
      assertNotNull(cache.getCacheValue(meta_inf));

      leaves = cache.getLeaves(meta_inf);
      assertEquals(2, leaves.size());

      cache.invalidateCache(root);
   }
}