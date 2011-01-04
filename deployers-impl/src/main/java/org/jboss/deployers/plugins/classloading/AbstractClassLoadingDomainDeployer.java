/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.deployers.plugins.classloading;

import org.jboss.classloader.spi.ClassLoaderDomain;
import org.jboss.classloader.spi.ClassLoaderSystem;
import org.jboss.classloader.spi.Loader;
import org.jboss.classloader.spi.ParentPolicy;
import org.jboss.classloader.spi.ShutdownPolicy;
import org.jboss.classloading.spi.metadata.ClassLoadingDomainMetaData;
import org.jboss.classloading.spi.metadata.LoaderMetaData;
import org.jboss.classloading.spi.metadata.ParentPolicyMetaData;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractSimpleRealDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;

/**
 * AbstractClassLoadingDomainDeployer.
 * 
 * @author <a href="ales.justin@jboss.org">Ales Justin</a>
 */
public class AbstractClassLoadingDomainDeployer extends AbstractSimpleRealDeployer<ClassLoadingDomainMetaData>
{
   /** The classloader system */
   private ClassLoaderSystem system;

   /** The parent usage flag */
   private boolean useDefaultDomain = true;

   public AbstractClassLoadingDomainDeployer()
   {
      super(ClassLoadingDomainMetaData.class);
      setStage(DeploymentStages.DESCRIBE);
   }

   /**
    * Validate the config
    */
   public void create()
   {
      if (system == null)
         throw new IllegalStateException("The system has not been set");
   }

   public void deploy(DeploymentUnit unit, ClassLoadingDomainMetaData deployment) throws DeploymentException
   {
      String name = deployment.getName();
      if (name == null || "<unknown>".equals(name))
         name = unit.getName();

      ParentPolicyMetaData ppmd = deployment.getParentPolicy();
      ParentPolicy pp = (ppmd != null) ? ppmd.createParentPolicy() : ParentPolicy.BEFORE;

      Loader parent = null;
      String parentDomain = deployment.getParentDomain();
      LoaderMetaData lmd = deployment.getParent();
      if (parentDomain != null && lmd != null)
         throw new DeploymentException("Cannot define both: parent-domain and parent loader: " + deployment);

      if (lmd != null)
      {
         parent = lmd.getValue();
      }
      else if (parentDomain != null)
      {
         parent = system.getDomain(parentDomain);
      }

      if (parent == null && isUseDefaultDomain())
         parent = system.getDefaultDomain();

      ShutdownPolicy shutdownPolicy = deployment.getShutdownPolicy();
      Boolean useLoadClassForParent = deployment.getUseLoadClassForParent();

      system.createAndRegisterDomain(name, pp, parent, shutdownPolicy, useLoadClassForParent);
   }

   public void undeploy(DeploymentUnit unit, ClassLoadingDomainMetaData deployment)
   {
      String name = deployment.getName();
      if (name == null || "<unknown>".equals(name))
         name = unit.getName();

      // should be already removed, but let's make sure
      ClassLoaderDomain domain = system.getDomain(name);
      if (domain != null)
         system.unregisterDomain(domain);
   }

   /**
    * Get the system.
    *
    * @return the system.
    */
   public ClassLoaderSystem getSystem()
   {
      return system;
   }

   /**
    * Set the system.
    *
    * @param system the system.
    */
   public void setSystem(ClassLoaderSystem system)
   {
      this.system = system;
   }

   /**
    * Do we use default domain by default as parent.
    *
    * @return true if default domain is used as default parent
    */
   public boolean isUseDefaultDomain()
   {
      return useDefaultDomain;
   }

   /**
    * Set use-default-domain as default parent flag.
    *
    * @param useDefaultDomain the use-default-domain
    */
   public void setUseDefaultDomain(boolean useDefaultDomain)
   {
      this.useDefaultDomain = useDefaultDomain;
   }
}
