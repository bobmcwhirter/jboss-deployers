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
package org.jboss.test.deployers.vfs.deployer.bean.test;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.jboss.beans.metadata.api.annotations.Bean;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.beans.metadata.spi.factory.BeanFactory;
import org.jboss.classloader.plugins.filter.CombiningClassFilter;
import org.jboss.classloader.plugins.system.DefaultClassLoaderSystem;
import org.jboss.classloader.spi.ClassLoaderSystem;
import org.jboss.classloader.spi.ParentPolicy;
import org.jboss.classloader.spi.filter.ClassFilter;
import org.jboss.classloader.spi.filter.PackageClassFilter;
import org.jboss.classloading.spi.dependency.ClassLoading;
import org.jboss.classloading.spi.metadata.ClassLoadingMetaData;
import org.jboss.dependency.spi.ControllerContext;
import org.jboss.deployers.plugins.annotations.GenericAnnotationDeployer;
import org.jboss.deployers.plugins.classloading.AbstractLevelClassLoaderSystemDeployer;
import org.jboss.deployers.plugins.classloading.ClassLoadingDefaultDeployer;
import org.jboss.deployers.vfs.deployer.kernel.BeanDeployer;
import org.jboss.deployers.vfs.deployer.kernel.BeanMetaDataDeployer;
import org.jboss.deployers.vfs.deployer.kernel.BeanScanningDeployer;
import org.jboss.deployers.vfs.deployer.kernel.KernelDeploymentDeployer;
import org.jboss.deployers.vfs.plugins.classloader.VFSClassLoaderClassPathDeployer;
import org.jboss.deployers.vfs.plugins.classloader.VFSClassLoaderDescribeDeployer;
import org.jboss.deployers.vfs.spi.client.VFSDeployment;
import org.jboss.kernel.Kernel;
import org.jboss.kernel.spi.dependency.KernelController;
import org.jboss.test.deployers.vfs.deployer.AbstractDeployerUnitTest;

/**
 * BeanScanningUnitTestCase.
 *
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 */
public class BeanScanningUnitTestCase extends AbstractDeployerUnitTest
{
   public static Test suite()
   {
      return new TestSuite(BeanScanningUnitTestCase.class);
   }

   public BeanScanningUnitTestCase(String name) throws Throwable
   {
      super(name);
   }

   protected void addDeployers(Kernel kernel)
   {
      BeanDeployer beanDeployer = new BeanDeployer();

      ClassLoadingDefaultDeployer cldd = new ClassLoadingDefaultDeployer();
      ClassLoadingMetaData clmd = new ClassLoadingMetaData();
      cldd.setDefaultMetaData(clmd);

      VFSClassLoaderClassPathDeployer vfscp = new VFSClassLoaderClassPathDeployer();
      VFSClassLoaderDescribeDeployer vfsdd = new VFSClassLoaderDescribeDeployer();
      ClassLoading classLoading = new ClassLoading();
      KernelController controller = kernel.getController();
      BeanMetaDataBuilder builder = BeanMetaDataBuilder.createBuilder("ClassLoading", ClassLoading.class.getName());
      builder.addMethodInstallCallback("addModule");
      builder.addMethodUninstallCallback("removeModule");
      try
      {
         controller.install(builder.getBeanMetaData(), classLoading);
      }
      catch (Throwable t)
      {
         throw new RuntimeException(t);
      }
      vfsdd.setClassLoading(classLoading);

      ClassLoaderSystem system = new DefaultClassLoaderSystem();
      // allow MC annotations, so that both, deployer and tester see the same
      ClassFilter mcAnnFilter = new PackageClassFilter(new String[]{"org.jboss.beans.metadata.api.annotations"});
      ClassFilter filter = new CombiningClassFilter(false, new ClassFilter[]{ClassFilter.JAVA_ONLY, mcAnnFilter});
      ParentPolicy policy = new ParentPolicy(filter, ClassFilter.NOTHING);
      system.getDefaultDomain().setParentPolicy(policy);

      AbstractLevelClassLoaderSystemDeployer clsd = new AbstractLevelClassLoaderSystemDeployer();
      clsd.setClassLoading(classLoading);
      clsd.setSystem(system);

      GenericAnnotationDeployer gad = new GenericAnnotationDeployer();
      KernelDeploymentDeployer kernelDeploymentDeployer = new KernelDeploymentDeployer();
      BeanScanningDeployer bsd = new BeanScanningDeployer();
      BeanMetaDataDeployer beanMetaDataDeployer = new BeanMetaDataDeployer(kernel);

      addDeployer(main, beanDeployer);
      addDeployer(main, cldd);
      addDeployer(main, vfsdd);
      addDeployer(main, vfscp);
      addDeployer(main, clsd);
      addDeployer(main, gad);
      addDeployer(main, kernelDeploymentDeployer);
      addDeployer(main, bsd);
      addDeployer(main, beanMetaDataDeployer);
   }

   public void testNoOverride() throws Throwable
   {
      VFSDeployment context = createDeployment("/bean", "scan_no_override");
      assertDeploy(context);

      ControllerContext testCC = controller.getInstalledContext("Test");
      assertNotNull(testCC);
      Object testTarget = testCC.getTarget();
      assertNotNull(testTarget);
      Class<?> testClass = testTarget.getClass();
      assertTrue(testClass.isAnnotationPresent(Bean.class));

      ControllerContext testCCBF = controller.getInstalledContext("TestBF");
      assertNotNull(testCCBF);
      Object target = testCCBF.getTarget();
      assertInstanceOf(target, BeanFactory.class, false);
      BeanFactory bf = (BeanFactory)target;
      Object bfTarget = bf.createBean();
      assertNotNull(bfTarget);
      Class<?> bfTClass = bfTarget.getClass();
      assertTrue(bfTClass.isAnnotationPresent(org.jboss.beans.metadata.api.annotations.BeanFactory.class));

      assertUndeploy(context);
      assertNull(controller.getContext("TestBF", null));
      assertNull(controller.getContext("Test", null));
   }

/*
   // TODO - uncomment when we know what to do with merge
   
   public void testWithOverride() throws Throwable
   {
      VFSDeployment context = createDeployment("/bean", "scan_w_override");
      assertDeploy(context);

      ControllerContext testCC = controller.getInstalledContext("Test");
      assertNotNull(testCC);
      assertInstanceOf(testCC.getTarget(), SimpleInjectee.class, false);

      ControllerContext testCCBF = controller.getInstalledContext("TestBF");
      assertNotNull(testCCBF);
      Object target = testCCBF.getTarget();
      assertInstanceOf(target, BeanFactory.class, false);
      BeanFactory bf = (BeanFactory)target;
      assertInstanceOf(bf.createBean(), SimpleInjectee.class, false);

      assertUndeploy(context);
      assertNull(controller.getContext("TestBF", null));
      assertNull(controller.getContext("Test", null));
   }
*/
}