/*
* JBoss, Home of Professional Open Source
* Copyright 2008, JBoss Inc., and individual contributors as indicated
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
package org.jboss.deployers.vfs.plugins.classloader;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.Executors;

import org.jboss.classloading.spi.metadata.ClassLoadingMetaData;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.vfs.spi.deployer.AbstractVFSRealDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.util.id.GUID;
import org.jboss.vfs.TempFileProvider;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;

/**
 * TempURLDeployer.
 *
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 * @version $Revision: 1.1 $
 */
public class InMemoryClassesDeployer extends AbstractVFSRealDeployer
{
   /** The name of the dynamic class root */
   public static final String DYNAMIC_CLASS_URL_KEY = "DYNAMIC_CLASS_URL_KEY";

   /** The name of the dynamic class root */
   public static final String DYNAMIC_CLASS_KEY = "DYNAMIC_CLASS_KEY";

   /** The name of the mount closeable handle */
   private static final String MOUNT_HANDLE_KEY = "MOUNT_HANDLE_KEY";

   /** The temp file provider */
   private TempFileProvider tempFileProvider = TempFileProvider.create("vfsinmemory", Executors.newScheduledThreadPool(2));

   /** The host name creator */
   private HostNameCreator hostNameCreator;

   public InMemoryClassesDeployer() throws IOException
   {
      // Make it run before the classloader describe deployer
      setStage(DeploymentStages.DESCRIBE);
      setOutput(ClassLoadingMetaData.class);
      setTopLevelOnly(true);
   }

   /**
    * Create host name.
    *
    * @param unit the deployment unit
    * @return the host name
    */
   protected String createHost(VFSDeploymentUnit unit)
   {
      return (hostNameCreator != null) ? hostNameCreator.createHostName(unit) : GUID.asString();
   }

   public void deploy(VFSDeploymentUnit unit) throws DeploymentException
   {
      try
      {
         VirtualFile classes = VFS.getChild(createHost(unit));
         URL dynamicClassRoot = classes.toURL();
         Closeable closeable = VFS.mountTemp(classes, tempFileProvider);
         unit.addAttachment(MOUNT_HANDLE_KEY, closeable);
         unit.addAttachment(DYNAMIC_CLASS_KEY, classes);
         unit.addAttachment(DYNAMIC_CLASS_URL_KEY, dynamicClassRoot);
         unit.prependClassPath(classes);
         log.debug("Dynamic class root for " + unit.getName() + " is " + dynamicClassRoot);
      }
      catch (Exception e)
      {
         throw new DeploymentException("Error creating dynamic class root", e);
      }
   }

   @Override
   public void undeploy(VFSDeploymentUnit unit)
   {
      log.debug("Removing dynamic class root for " + unit.getName());
      try
      {
         unit.removeAttachment(DYNAMIC_CLASS_URL_KEY, URL.class);

         VirtualFile classes = unit.removeAttachment(DYNAMIC_CLASS_KEY, VirtualFile.class);
         if (classes != null)
         {
            unit.removeClassPath(classes);
         }
      }
      finally
      {
         try
         {
            Closeable closeable = unit.removeAttachment(MOUNT_HANDLE_KEY, Closeable.class);
            if (closeable != null)
            {
               closeable.close();
            }
         }
         catch (Exception e)
         {
            log.warn("Error deleting dynamic class root for " + unit.getName(), e);
         }
      }
   }

   /**
    * Set host name creator.
    *
    * @param hostNameCreator the host name creator
    */
   public void setHostNameCreator(HostNameCreator hostNameCreator)
   {
      this.hostNameCreator = hostNameCreator;
   }

   /**
    * The temp file provider.
    *
    * @param tempFileProvider temp file provider
    */
   public void setTempFileProvider(TempFileProvider tempFileProvider)
   {
      if (tempFileProvider == null)
         throw new IllegalArgumentException("Null temp file provider.");
      this.tempFileProvider = tempFileProvider;
   }
}
