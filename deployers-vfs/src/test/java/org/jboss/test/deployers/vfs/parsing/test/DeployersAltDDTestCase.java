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
package org.jboss.test.deployers.vfs.parsing.test;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.plugins.structure.AbstractVFSDeploymentContext;
import org.jboss.deployers.vfs.spi.deployer.AbstractVFSParsingDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.managed.api.ManagedObject;
import org.jboss.test.BaseTestCase;
import org.jboss.virtual.VirtualFile;
import org.jboss.virtual.spi.VFSContext;
import org.jboss.virtual.spi.VirtualFileHandler;

/**
 * alt-dd tests.
 *
 * @author <a href="mailto:alex@jboss.com">Alexey Loubyansky</a>
 */
public class DeployersAltDDTestCase extends BaseTestCase
{
   public static Test suite()
   {
      return suite(DeployersAltDDTestCase.class);
   }

   public DeployersAltDDTestCase(String test)
   {
      super(test);
   }

   public void testAltDD() throws Exception
   {
      final VirtualFile altDD = getMockVF();
      AbstractVFSDeploymentContext context = new AbstractVFSDeploymentContext();
      DeploymentUnit unit = context.getDeploymentUnit();
      unit.addAttachment(Object.class.getName() + ".altDD", altDD);

      final boolean pickedUpAltDD[] = new boolean[1];
      AbstractVFSParsingDeployer<Object> deployer = new AbstractVFSParsingDeployer<Object>(Object.class)
      {
         @Override
         protected Object parse(VFSDeploymentUnit unit, VirtualFile file, Object root) throws Exception
         {
            if(file == altDD)
               pickedUpAltDD[0] = true;
            return null;
         }

         public void build(DeploymentUnit unit, Map<String, ManagedObject> managedObjects) throws DeploymentException
         {
         }
      };
      
      deployer.setName("dd");
      deployer.deploy(unit);
      
      assertTrue("Picked up alt-dd", pickedUpAltDD[0]);
   }

   private static VirtualFile getMockVF()
   {
      VirtualFile altDD = new VirtualFile(new VirtualFileHandler()
      {
         private static final long serialVersionUID = 1L;

         public void close()
         {
         }

         public boolean exists() throws IOException
         {
            return false;
         }

         public VirtualFileHandler getChild(String arg0) throws IOException
         {
            return null;
         }

         public List<VirtualFileHandler> getChildren(boolean arg0) throws IOException
         {
            return null;
         }

         public long getLastModified() throws IOException
         {
            return 0;
         }

         public String getName()
         {
            return null;
         }

         public VirtualFileHandler getParent() throws IOException
         {
            return null;
         }

         public String getPathName()
         {
            return null;
         }

         public long getSize() throws IOException
         {
            return 0;
         }

         public VFSContext getVFSContext()
         {
            return null;
         }

         public VirtualFile getVirtualFile()
         {
            return null;
         }

         public boolean hasBeenModified() throws IOException
         {
            return false;
         }

         public boolean isHidden() throws IOException
         {
            return false;
         }

         public boolean isLeaf() throws IOException
         {
            return false;
         }

         public String getLocalPathName()
         {
            return null;
         }

         public boolean isNested() throws IOException
         {
            return false;
         }

         public InputStream openStream() throws IOException
         {
            return null;
         }

         public URI toURI() throws URISyntaxException
         {
            return null;
         }

         public URL toURL() throws MalformedURLException, URISyntaxException
         {
            return null;
         }

         public URL toVfsUrl() throws MalformedURLException, URISyntaxException
         {
            return null;
         }

         public void replaceChild(VirtualFileHandler original, VirtualFileHandler replacement)
         {

         }

         public boolean removeChild(String name) throws IOException
         {
            return false;
         }

         public boolean delete(int gracePeriod) throws IOException
         {
            return false;
         }
      });
      return altDD;
   }
}
