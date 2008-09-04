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
package org.jboss.test.deployers.vfs.deployer.nonmetadata.support;

import java.io.InputStream;
import java.util.Set;
import java.util.HashSet;

import org.jboss.deployers.vfs.spi.deployer.FileMatcher;
import org.jboss.deployers.vfs.spi.deployer.AbstractVFSParsingDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.virtual.VirtualFile;

/**
 * Mock .bsh deployer.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class MockBshDeployer extends AbstractVFSParsingDeployer<BshScript> implements FileMatcher
{
   private Set<BshScript> scipts = new HashSet<BshScript>();

   public MockBshDeployer()
   {
      super(BshScript.class);
      setSuffix(".bsh");
   }

   protected BshScript parse(VFSDeploymentUnit unit, VirtualFile file, BshScript root) throws Exception
   {
      InputStream inputStream = openStreamAndValidate(file);
      try
      {
         return new BshScript(inputStream);         
      }
      finally
      {
         inputStream.close();
      }
   }

   protected void init(VFSDeploymentUnit unit, BshScript metaData, VirtualFile file) throws Exception
   {
      scipts.add(metaData);
   }

   public Set<BshScript> getScipts()
   {
      return scipts;
   }
}
