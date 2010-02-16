/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.deployers.vfs.plugins.dependency;

import java.io.InputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import org.jboss.deployers.vfs.spi.deployer.AbstractVFSParsingDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.vfs.VirtualFile;

/**
 * AliasesParserDeployer.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class AliasesParserDeployer extends AbstractVFSParsingDeployer<DeploymentAliases>
{
   public AliasesParserDeployer()
   {
      super(DeploymentAliases.class);
      setName("aliases.txt");
      setTopLevelOnly(true);
   }

   protected DeploymentAliases parse(VFSDeploymentUnit unit, VirtualFile file, DeploymentAliases root) throws Exception
   {
      Set<Object> alises = new HashSet<Object>();
      InputStream is = file.openStream();
      try
      {
         BufferedReader br = new BufferedReader(new InputStreamReader(is));
         String line;
         while ((line = br.readLine()) != null)
         {
            alises.add(line);
         }
      }
      finally
      {
         try
         {
            is.close();
         }
         catch (IOException ignored)
         {
         }
      }
      return new DepoymentAliasesImpl(alises);
   }
   
   private class DepoymentAliasesImpl implements DeploymentAliases
   {
      private Set<Object> aliases;

      private DepoymentAliasesImpl(Set<Object> aliases)
      {
         this.aliases = aliases;
      }

      public Set<Object> getAliases()
      {
         return aliases;
      }
   }
}