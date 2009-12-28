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
package org.jboss.test.deployers;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jboss.deployers.client.spi.DeployerClient;
import org.jboss.deployers.plugins.main.MainDeployerImpl;
import org.jboss.deployers.spi.structure.MetaDataEntry;
import org.jboss.deployers.spi.structure.StructureMetaDataFactory;
import org.jboss.deployers.structure.spi.StructuralDeployers;
import org.jboss.deployers.structure.spi.StructureBuilder;
import org.jboss.deployers.vfs.plugins.structure.AbstractVFSDeploymentContext;
import org.jboss.deployers.vfs.plugins.structure.VFSStructuralDeployersImpl;
import org.jboss.deployers.vfs.plugins.structure.VFSStructureBuilder;
import org.jboss.deployers.vfs.spi.client.VFSDeployment;
import org.jboss.deployers.vfs.spi.client.VFSDeploymentFactory;
import org.jboss.deployers.vfs.spi.structure.StructureDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentContext;
import org.jboss.virtual.VFS;
import org.jboss.virtual.VirtualFile;
import org.jboss.virtual.plugins.context.jar.JarUtils;

/**
 * BaseDeployersVFSTest.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public abstract class BaseDeployersVFSTest extends AbstractDeployerTest
{
   public BaseDeployersVFSTest(String name)
   {
      super(name);
   }
   
   @Override
   public URL getResource(String path)
   {
      URL url = super.getResource(path);
      assertNotNull("Resource not found: " + path, url);
      return url;
   }
   
   /**
    * Get a virtual file
    * 
    * @param root the root
    * @param path the path
    * @return the file
    * @throws Exception for any error
    */
   protected VirtualFile getVirtualFile(String root, String path) throws Exception
   {
      URL url = getResource(root);
      return VFS.getVirtualFile(url, path);
   }
   
   /**
    * Get a url string from a path
    * 
    * @param path the path
    * @return the string
    * @throws Exception for any error
    */
   protected String getURL(String path) throws Exception
   {
      URL url = getResource(path);
      return url.toString();
   }
   /**
    * Get a vfs url string from a path
    * 
    * @param path
    * @return the url
    * @throws Exception
    */
   protected String getVfsURL(String path) throws Exception
   {
      URL url = getResource(path);
      return "vfs"+url.toString();
   }

   /**
    * Get a jar url string from a path
    * 
    * @param path the path
    * @return the string
    * @throws Exception for any error
    */
   protected String getJarURL(String path) throws Exception
   {
      URL url = getResource(path);
      url = JarUtils.createJarURL(url);
      return url.toString();
   }

   protected StructureBuilder createStructureBuilder()
   {
      return new VFSStructureBuilder();
   }

   protected StructuralDeployers createStructuralDeployers()
   {
      StructureBuilder builder = createStructureBuilder();
      VFSStructuralDeployersImpl structure = new VFSStructuralDeployersImpl();
      structure.setStructureBuilder(builder);
      return structure;
   }
   
   protected void addStructureDeployer(DeployerClient main, StructureDeployer deployer)
   {
      MainDeployerImpl mainDeployerImpl = (MainDeployerImpl) main;
      VFSStructuralDeployersImpl structure = (VFSStructuralDeployersImpl) mainDeployerImpl.getStructuralDeployers();
      structure.addDeployer(deployer);
   }
   
   /**
    * Create a deployment
    * 
    * @param root the root
    * @param path the path
    * @return the deployment
    * @throws Exception for any error
    */
   protected VFSDeployment createDeployment(String root, String path) throws Exception
   {
      VirtualFile file = getVirtualFile(root, path);
      return VFSDeploymentFactory.getInstance().createVFSDeployment(file);
   }
   
   /**
    * Create a deployment context
    * 
    * @param root the root
    * @param path the path
    * @return the context
    * @throws Exception for any error
    */
   protected VFSDeploymentContext createDeploymentContext(String root, String path) throws Exception
   {
      VirtualFile file = getVirtualFile(root, path);
      return new AbstractVFSDeploymentContext(file, "");
   }

   /**
    * Create metadata path entries.
    *
    * @param metaDataPath the metadata paths
    * @return the metadata path entries
    */
   protected List<MetaDataEntry> createMetaDataEntries(String... metaDataPath)
   {
      List<MetaDataEntry> entries = new ArrayList<MetaDataEntry>();
      for (String path : metaDataPath)
         entries.add(StructureMetaDataFactory.createMetaDataEntry(path));
      return entries;
   }
}
