/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.deployers.vfs.classpool.support;

import org.jboss.classloader.spi.filter.ClassFilter;
import org.jboss.classloader.spi.filter.RecursivePackageClassFilter;
import org.jboss.classloading.spi.metadata.ClassLoadingMetaData;
import org.jboss.classloading.spi.metadata.ExportAll;
import org.jboss.classloading.spi.version.Version;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.vfs.spi.deployer.AbstractVFSRealDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;

/**
 * MockWarClassLoaderDeployer based on WarClassLoaderDeployer.
 *  
 * @author Scott.Stark@jboss.org
 * @author adrian@jboss.org
 * @author ales.justin@jboss.org
 * @author <a href="flavia.rainone@jboss.com">Flavia Rainone</a>
 */
public class MockWarClassLoaderDeployer extends AbstractVFSRealDeployer
{
   /** The parent class loader first model flag */
   private boolean java2ClassLoadingCompliance = false;

   /** Package names that should be ignored for class loading */
   private String filteredPackages;

   /**
    * Create a new WarClassLoaderDeployer.
    */
   public MockWarClassLoaderDeployer()
   {
      setStage(DeploymentStages.POST_PARSE);
      addInput(ClassLoadingMetaData.class);
      setOutput(ClassLoadingMetaData.class);
   }

   public boolean isJava2ClassLoadingCompliance()
   {
      return java2ClassLoadingCompliance;
   }
   
   public void setJava2ClassLoadingCompliance(boolean flag)
   {
      this.java2ClassLoadingCompliance = flag;
   }

   public String getFilteredPackages()
   {
      return filteredPackages;
   }
   public void setFilteredPackages(String pkgs)
   {
      this.filteredPackages = pkgs;
   }

   public void deploy(VFSDeploymentUnit unit) throws DeploymentException
   {
      // ignore if it already has classloading or not war deployment
      if (unit.getSimpleName().endsWith(".war") == false || unit.isAttachmentPresent(ClassLoadingMetaData.class))
         return;

      // The default domain name is the unit name
      String domainName = unit.getName();

      // The default classloading compliance is on the deployer
      boolean j2seClassLoadingCompliance = java2ClassLoadingCompliance;

      // Create a classloading metadata
      // NOTE: Don't explicitly set the parentDomain otherwise it will create a top level classloader
      //       for subdeployments rather than a classloader hanging off the main deployment's classloader
      ClassLoadingMetaData classLoadingMetaData = new ClassLoadingMetaData();
      classLoadingMetaData.setName(unit.getName());
      classLoadingMetaData.setDomain(domainName);
      classLoadingMetaData.setExportAll(ExportAll.NON_EMPTY);
      classLoadingMetaData.setImportAll(true);
      classLoadingMetaData.setVersion(Version.DEFAULT_VERSION);
      classLoadingMetaData.setJ2seClassLoadingCompliance(j2seClassLoadingCompliance);
      ClassFilter filter;
      if (filteredPackages != null)
      {
         filter = RecursivePackageClassFilter.createRecursivePackageClassFilterFromString(filteredPackages);
         classLoadingMetaData.setExcluded(filter);
      }
      unit.addAttachment(ClassLoadingMetaData.class, classLoadingMetaData);
   }
}
