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
package org.jboss.deployers.vfs.spi.deployer;

import java.util.Map;

import org.jboss.vfs.VirtualFile;
import org.jboss.vfs.VFSInputSource;
import org.jboss.xb.binding.ObjectModelFactory;
import org.xml.sax.InputSource;

/**
 * MultipleObjectModelFactoryDeployer.
 *
 * @param <T> the expected type
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 */
public abstract class MultipleObjectModelFactoryDeployer<T> extends MultipleJBossXBDeployer<T>
{
   public MultipleObjectModelFactoryDeployer(Class<T> output, Map<String, Class<?>> mappings)
   {
      super(output, mappings);
   }

   public MultipleObjectModelFactoryDeployer(Class<T> output, Map<String, Class<?>> mappings, String suffix, Class<?> suffixClass)
   {
      super(output, mappings, suffix, suffixClass);
   }

   protected <U> U parse(Class<U> expectedType, VirtualFile file, Object root) throws Exception
   {
      U tRoot;
      if (root != null && expectedType.isInstance(root))
         tRoot = expectedType.cast(root);
      else
         tRoot = null;

      ObjectModelFactory objectModelFactory = getObjectModelFactory(expectedType, file, tRoot);
      if (objectModelFactory == null)
         log.warn("ObjectModelFactory factory is null, expectedType=" + expectedType + ", file=" + file);

      InputSource source = new VFSInputSource(file);
      return getHelper().parse(expectedType, source, tRoot, objectModelFactory);
   }

   /**
    * Get the object model factory
    *
    * @param <U> the expect type
    * @param expectedType the expected class
    * @param file - the file we're about to parse
    * @param root - possibly null pre-existing root
    * @return the object model factory
    */
   protected <U> ObjectModelFactory getObjectModelFactory(Class<U> expectedType, VirtualFile file, U root)
   {
      return getObjectModelFactory(expectedType, file.getName(), root);
   }

   /**
    * Get the object model factory
    *
    * @param <U> the expect type
    * @param expectedType the expected class
    * @param fileName - the fileName
    * @param root - possibly null pre-existing root
    * @return the object model factory
    */
   protected abstract <U> ObjectModelFactory getObjectModelFactory(Class<U> expectedType, String fileName, U root);
}