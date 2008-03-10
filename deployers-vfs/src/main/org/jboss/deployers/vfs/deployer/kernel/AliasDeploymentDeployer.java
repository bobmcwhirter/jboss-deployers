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

import java.util.Set;

import org.jboss.beans.metadata.spi.NamedAliasMetaData;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.helpers.AbstractComponentDeployer;
import org.jboss.deployers.spi.deployer.helpers.DeploymentVisitor;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.kernel.spi.deployment.KernelDeployment;

/**
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

   protected static void addAliasComponent(DeploymentUnit unit, NamedAliasMetaData alias)
   {
      DeploymentUnit component = unit.addComponent(alias.getAliasValue().toString());
      component.addAttachment(NamedAliasMetaData.class.getName(), alias);
   }

   protected static void removeAliasComponent(DeploymentUnit unit, NamedAliasMetaData alias)
   {
      unit.removeComponent(alias.getAliasValue().toString());
   }

   /**
    * KernelDeploymentVisitor.
    */
   public static class KernelDeploymentVisitor implements DeploymentVisitor<KernelDeployment>
   {
      public Class<KernelDeployment> getVisitorType()
      {
         return KernelDeployment.class;
      }

      public void deploy(DeploymentUnit unit, KernelDeployment deployment) throws DeploymentException
      {
         Set<NamedAliasMetaData> aliases = deployment.getAliases();
         if (aliases != null && aliases.isEmpty() == false)
         {
            for (NamedAliasMetaData alias : aliases)
               addAliasComponent(unit, alias);
         }
      }

      public void undeploy(DeploymentUnit unit, KernelDeployment deployment)
      {
         Set<NamedAliasMetaData> aliases = deployment.getAliases();
         if (aliases != null && aliases.isEmpty() == false)
         {
            for (NamedAliasMetaData alias : aliases)
               removeAliasComponent(unit, alias);
         }
      }
   }

   /**
    * AliasMetaDataVisitor.
    */
   public static class AliasMetaDataVisitor implements DeploymentVisitor<NamedAliasMetaData>
   {
      public Class<NamedAliasMetaData> getVisitorType()
      {
         return NamedAliasMetaData.class;
      }

      public void deploy(DeploymentUnit unit, NamedAliasMetaData deployment) throws DeploymentException
      {
         addAliasComponent(unit, deployment);
      }

      public void undeploy(DeploymentUnit unit, NamedAliasMetaData deployment)
      {
         removeAliasComponent(unit, deployment);
      }
   }
}
