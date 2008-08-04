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

import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.List;

import junit.framework.AssertionFailedError;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jboss.classloader.plugins.ClassLoaderUtils;
import org.jboss.classloader.plugins.jdk.AbstractJDKChecker;
import org.jboss.dependency.spi.ControllerContext;
import org.jboss.dependency.spi.dispatch.InvokeDispatchContext;
import org.jboss.deployers.client.spi.DeployerClient;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.structure.spi.main.MainDeployerStructure;
import org.jboss.deployers.vfs.spi.client.VFSDeployment;
import org.jboss.deployers.vfs.spi.client.VFSDeploymentFactory;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.test.AbstractTestDelegate;
import org.jboss.test.kernel.junit.MicrocontainerTest;
import org.jboss.virtual.AssembledDirectory;
import org.jboss.virtual.VFS;
import org.jboss.virtual.VirtualFile;
import org.jboss.virtual.plugins.context.file.FileSystemContext;
import org.jboss.virtual.plugins.context.jar.JarUtils;
import org.jboss.virtual.plugins.vfs.helpers.SuffixesExcludeFilter;

/**
 * BootstrapDeployersTest.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public abstract class BootstrapDeployersTest extends MicrocontainerTest
{
   public static AbstractTestDelegate getDelegate(Class<?> clazz) throws Exception
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
   
   protected VFSDeployment createVFSDeployment(String root, String child) throws Exception
   {
      URL resourceRoot = getClass().getResource(root);
      if (resourceRoot == null)
         fail("Resource not found: " + root);
      VirtualFile deployment = VFS.getVirtualFile(resourceRoot, child);
      if (deployment == null)
         fail("Child not found " + child + " from " + resourceRoot);
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

   protected AssembledDirectory createAssembledDirectory(String name) throws Exception
   {
      return createAssembledDirectory(name, "");     
   }

   protected AssembledDirectory createAssembledDirectory(String name, String rootName) throws Exception
   {
      return AssembledDirectory.createAssembledDirectory(name, rootName);
   }

   protected void addPackage(AssembledDirectory dir, Class<?> reference) throws Exception
   {
      String packagePath = ClassLoaderUtils.packageNameToPath(reference.getName());
      dir.addResources(reference, new String[] { packagePath + "/*.class" } , new String[0]);
   }

   protected void addPath(final AssembledDirectory dir, String path, String name) throws Exception
   {
      URL url = getResource(path);
      if (url == null)
         fail(path + " not found");

      VirtualFile file = VFS.getVirtualFile(url, name);
      // TODO - remove this filter after new VFS relase
      SuffixesExcludeFilter noJars = new SuffixesExcludeFilter(JarUtils.getSuffixes());
      dir.addPath(file, noJars);
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

   protected void setUp() throws Exception
   {
      super.setUp();
      // This is a hack for a hack. ;-)
      AbstractJDKChecker.getExcluded().add(BootstrapDeployersTest.class);

      // Reduce the noise from the VFS
      // FIXME add method change logging levels to AbstractTestCase
      Logger.getLogger(FileSystemContext.class).setLevel(Level.INFO);
   }
}
