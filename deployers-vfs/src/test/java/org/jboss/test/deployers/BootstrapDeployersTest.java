/*
* JBoss, Home of Professional Open Source
* Copyright 2005, JBoss Inc., and individual contributors as indicated
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

import java.io.Closeable;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.classloader.plugins.ClassLoaderUtils;
import org.jboss.classloader.plugins.jdk.AbstractJDKChecker;
import org.jboss.dependency.spi.ControllerContext;
import org.jboss.dependency.spi.dispatch.InvokeDispatchContext;
import org.jboss.deployers.client.spi.DeployerClient;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.structure.spi.main.MainDeployerInternals;
import org.jboss.deployers.structure.spi.main.MainDeployerStructure;
import org.jboss.deployers.vfs.spi.client.VFSDeployment;
import org.jboss.deployers.vfs.spi.client.VFSDeploymentFactory;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.test.deployers.support.AssembledDirectory;
import org.jboss.test.kernel.junit.MicrocontainerTest;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;
import org.jboss.vfs.VirtualFileAssembly;

import junit.framework.AssertionFailedError;

/**
 * BootstrapDeployersTest.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @author <a href="ales.justin@jboss.org">Ales Justin</a>
 * @version $Revision: 1.1 $
 */
public abstract class BootstrapDeployersTest extends MicrocontainerTest
{
   private static Map<VirtualFile, Closeable> assemblyHandles = new HashMap<VirtualFile, Closeable>();
   
   public static BootstrapDeployersTestDelegate getDelegate(Class<?> clazz) throws Exception
   {
      return new BootstrapDeployersTestDelegate(clazz);
   }
   
   public BootstrapDeployersTest(String name)
   {
      super(name);
   }
   
   protected DeployerClient getDeployerClient()
   {
      return getDelegate().getMainDeployer();
   }
   
   protected MainDeployerStructure getMainDeployerStructure()
   {
      return getDelegate().getMainDeployer();
   }
   
   protected MainDeployerInternals getMainDeployerInternals()
   {
      return getDelegate().getMainDeployer();
   }

   protected String getRoot(Class<?> clazz)
   {
      ProtectionDomain pd = clazz.getProtectionDomain();
      CodeSource cs = pd.getCodeSource();
      URL location = cs.getLocation();
      return location.toString();
   }

   protected BootstrapDeployersTestDelegate getDelegate()
   {
      return (BootstrapDeployersTestDelegate) super.getDelegate();
   }
   
   protected VirtualFile createDeploymentRoot(String root, String child) throws IOException, URISyntaxException
   {
      URL resourceRoot = getClass().getResource(root);
      if (resourceRoot == null)
         fail("Resource not found: " + root);
      VirtualFile deployment = VFS.getChild(resourceRoot).getChild(child);
      if (! deployment.exists())
         fail("Child not found " + child + " from " + resourceRoot);
      return deployment;
   }

   protected VFSDeployment createVFSDeployment(String root, String child) throws Exception
   {
      VirtualFile deployment = createDeploymentRoot(root, child);
      return createVFSDeployment(deployment);
   }

   protected VFSDeployment createVFSDeployment(VirtualFile root) throws Exception
   {
      VFSDeploymentFactory factory = VFSDeploymentFactory.getInstance();
      return factory.createVFSDeployment(root);
   }
   
   protected VFSDeploymentUnit assertDeploy(String root, String child) throws Exception
   {
      VFSDeployment deployment = createVFSDeployment(root, child);
      getDeployerClient().deploy(deployment);
      return (VFSDeploymentUnit) getMainDeployerStructure().getDeploymentUnit(deployment.getName(), true);
   }
   
   protected VFSDeploymentUnit assertDeploy(VirtualFile file) throws Exception
   {
      VFSDeployment deployment = createVFSDeployment(file);
      getDeployerClient().deploy(deployment);
      return (VFSDeploymentUnit) getMainDeployerStructure().getDeploymentUnit(deployment.getName(), true);
   }
   
   protected VFSDeploymentUnit addDeployment(String root, String child) throws Exception
   {
      VFSDeployment deployment = createVFSDeployment(root, child);
      getDeployerClient().addDeployment(deployment);
      getDeployerClient().process();
      return (VFSDeploymentUnit) getMainDeployerStructure().getDeploymentUnit(deployment.getName(), true);
   }
   
   protected void undeploy(DeploymentUnit unit)
   {
      try
      {
         undeploy(unit, false);
      }
      catch (Throwable t)
      {
         throw new RuntimeException("Unexpected", t);
      }
   }
   
   protected void undeploy(DeploymentUnit unit, boolean wantError) throws Exception
   {
      try
      {
         getDeployerClient().undeploy(unit.getName());
      }
      catch (Exception e)
      {
         if (wantError)
            throw e;
      }
   }
   
   protected void assertClassLoader(Class<?> clazz, ClassLoader expected)
   {
      if (expected == null)
         return;
      ClassLoader cl = clazz.getClassLoader();
      boolean result = expected.equals(cl);
      assertTrue(ClassLoaderUtils.classToString(clazz) + " should have expected classloader=" + expected, result);
   }
   
   protected void assertClassEquality(Class<?> expected, Class<?> actual)
   {
      assertTrue("Should be the same " + ClassLoaderUtils.classToString(expected) +" and " + ClassLoaderUtils.classToString(actual), expected == actual);
   }
   
   protected void assertNoClassEquality(Class<?> expected, Class<?> actual)
   {
      assertTrue("Should NOT be the same " + ClassLoaderUtils.classToString(expected) +" and " + ClassLoaderUtils.classToString(actual), expected != actual);
   }
   
   protected URL assertGetResource(String name, ClassLoader start)
   {
      URL result = start.getResource(name);
      assertNotNull("Resource not found '" + name  + "' from " + start, result);
      return result;
   }
   
   protected void assertNoResource(String name, ClassLoader start)
   {
      URL result = start.getResource(name);
      assertNull("Did not expect resource '" + name  + "' from " + start, result);
   }
   
   protected Class<?> assertLoadClass(Class<?> reference, ClassLoader start)
   {
      return assertLoadClass(reference, start, start, false);
   }
   
   protected Class<?> assertLoadClass(Class<?> reference, ClassLoader start, boolean isReference)
   {
      return assertLoadClass(reference, start, start, isReference);
   }
   
   protected Class<?> assertLoadClass(Class<?> reference, ClassLoader start, ClassLoader expected)
   {
      return assertLoadClass(reference, start, expected, false);
   }
   
   protected Class<?> assertLoadClass(Class<?> reference, ClassLoader start, ClassLoader expected, boolean isReference)
   {
      Class<?> result = assertLoadClass(reference.getName(), start, expected);
      if (isReference)
         assertClassEquality(reference, result);
      else
         assertNoClassEquality(reference, result);
      return result;
   }
   
   protected Class<?> assertLoadClass(String name, ClassLoader start)
   {
      return assertLoadClass(name, start, start);
   }
   
   protected Class<?> assertLoadClass(String name, ClassLoader start, ClassLoader expected)
   {
      Class<?> result = null;
      try
      {
         result = start.loadClass(name);
         getLog().debug("Got class: " + ClassLoaderUtils.classToString(result) + " for " + name + " from " + start);
      }
      catch (ClassNotFoundException e)
      {
         failure("Did not expect CNFE for " + name + " from " + start, e);
      }
      assertClassLoader(result, expected);
      return result;
   }
   
   protected void assertLoadClassFail(Class<?> reference, ClassLoader start)
   {
      assertLoadClassFail(reference.getName(), start);
   }
      
   protected void assertLoadClassFail(String name, ClassLoader start)
   {
      try
      {
         start.loadClass(name);
         fail("Should not be here!");
      }
      catch (Exception expected)
      {
         checkThrowable(ClassNotFoundException.class, expected);
      }
   }
   
   protected ClassLoader getClassLoader(DeploymentUnit unit)
   {
      return unit.getClassLoader();
   }
   
   protected void assertNoClassLoader(DeploymentUnit unit)
   {
      try
      {
         assertNull("Should not be a classloader: " + unit.getClassLoader());
      }
      catch (Throwable e)
      {
         checkThrowable(IllegalStateException.class, e);
      }
   }
   
   protected ClassLoader getClassLoader(Object name)
   {
      ControllerContext ctx = getControllerContext(name);
      assertNotNull(ctx);
      InvokeDispatchContext dispatch = assertInstanceOf(ctx, InvokeDispatchContext.class, true);
      try
      {
         ClassLoader result = dispatch.getClassLoader();
         assertNotNull(result);
         return result;
      }
      catch (AssertionFailedError e)
      {
         throw e;
      }
      catch (Throwable e)
      {
         throw new RuntimeException("Unexpected error getting classloader", e);
      }
   }

   protected DeploymentUnit assertChild(DeploymentUnit parent, String name)
   {
      String parentName = parent.getName();
      if (parentName.endsWith("/") == false)
         parentName += "/";
      name = parentName + name;
      if (name.endsWith("/") == false)
         name += "/";

      List<DeploymentUnit> children = parent.getChildren();
      for (DeploymentUnit child : children)
      {
         if (name.equals(child.getName()))
            return child;
      }
      throw new AssertionFailedError("Child " + name + " not found in " + children);
   }
   
   protected AssembledDirectory createAssembledDirectory(VirtualFile mountPoint) throws Exception
   {
      VirtualFileAssembly assembly = new VirtualFileAssembly();
      assemblyHandles.put(mountPoint, VFS.mountAssembly(assembly, mountPoint));
      return new AssembledDirectory(getClass(), assembly);
   }
   
   protected VirtualFile getVirtualFile(String path) throws URISyntaxException 
   {
      URL resource = getResource(path);
      if(resource != null)
         return VFS.getChild(resource);
      return null;
   }

   protected void closeAssembly(VirtualFile mountPoint) throws IOException
   {
      if (assemblyHandles.containsKey(mountPoint))
         assemblyHandles.get(mountPoint).close();
   }

   protected void setUp() throws Exception
   {
      super.setUp();
      // This is a hack for a hack. ;-)
      AbstractJDKChecker.getExcluded().add(BootstrapDeployersTest.class);
   }
   
   protected void tearDown() throws Exception 
   {
      try
      {
         for (Closeable handle : assemblyHandles.values())
         {
            handle.close();
         }
      }
      finally
      {
         super.tearDown();
      }
   }
}
