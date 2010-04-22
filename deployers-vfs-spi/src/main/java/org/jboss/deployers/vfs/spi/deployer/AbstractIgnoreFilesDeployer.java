/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.deployers.vfs.spi.deployer;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.CollectionNameIgnoreMechanism;
import org.jboss.deployers.spi.deployer.matchers.NameIgnoreMechanism;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.vfs.VirtualFile;

/**
 * Create a path ignore mechanism based on txt file.
 * The file should include relative paths wrt its owner (sub)deployment.
 * e.g. META-INF/some-custom.xml or WEB-INF/lib/ui.jar/META-INF/persistence.xml
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class AbstractIgnoreFilesDeployer extends AbstractVFSParsingDeployer<NameIgnoreMechanism>
{
   public AbstractIgnoreFilesDeployer()
   {
      super(NameIgnoreMechanism.class);
      setStage(DeploymentStages.PRE_PARSE);
      setName("jboss-ignore.txt");
   }

   protected NameIgnoreMechanism parse(VFSDeploymentUnit unit, VirtualFile file, NameIgnoreMechanism root) throws Exception
   {
      InputStream is = file.openStream();
      try
      {
         Set<String> ignoredPaths = new HashSet<String>();

         BufferedReader reader = new BufferedReader(new InputStreamReader(is));
         String line;
         while ((line = reader.readLine()) != null)
         {
            line = line.trim();
            if (line.length() > 0)
               ignoredPaths.add(line);
         }

         return ignoredPaths.isEmpty() ? null : new CollectionNameIgnoreMechanism(null, ignoredPaths);
      }
      finally
      {
         is.close();
      }
   }
}
