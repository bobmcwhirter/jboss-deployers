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
package org.jboss.deployers.vfs.deployer.kernel;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jboss.beans.metadata.spi.NamedAliasMetaData;
import org.jboss.deployers.spi.deployer.helpers.AbstractComponentDeployer;
import org.jboss.deployers.spi.deployer.helpers.AbstractComponentVisitor;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeploymentVisitor;
import org.jboss.kernel.spi.deployment.KernelDeployment;

/**
 * Handle alias components.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class AliasDeploymentDeployer extends AbstractComponentDeployer<KernelDeployment, NamedAliasMetaData>
{
   /**
    * Create a new AliasDeploymentDeployer.
    */
   public AliasDeploymentDeployer()
   {
      setDeploymentVisitor(new KernelDeploymentVisitor());
      setComponentVisitor(new AliasMetaDataVisitor());
   }

   /**
    * KernelDeploymentVisitor.
    */
   public static class KernelDeploymentVisitor extends AbstractDeploymentVisitor<NamedAliasMetaData, KernelDeployment>
   {
      public Class<KernelDeployment> getVisitorType()
      {
         return KernelDeployment.class;
      }

      protected List<? extends NamedAliasMetaData> getComponents(KernelDeployment deployment)
      {
         Set<NamedAliasMetaData> aliases = deployment.getAliases();
         if (aliases != null && aliases.isEmpty() == false)
            return new ArrayList<NamedAliasMetaData>(aliases);
         else
            return null;
      }

      protected Class<NamedAliasMetaData> getComponentType()
      {
         return NamedAliasMetaData.class;
      }

      protected String getComponentName(NamedAliasMetaData attachment)
      {
         return attachment.getAliasValue().toString();
      }
   }

   /**
    * AliasMetaDataVisitor.
    */
   public static class AliasMetaDataVisitor extends AbstractComponentVisitor<NamedAliasMetaData>
   {
      protected String getComponentName(NamedAliasMetaData attachment)
      {
         return attachment.getAliasValue().toString();
      }

      public Class<NamedAliasMetaData> getVisitorType()
      {
         return NamedAliasMetaData.class;
      }
   }
}
