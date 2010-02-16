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
package org.jboss.deployers.vfs.plugins.structure.explicit;

import java.io.IOException;
import java.net.URL;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.vfs.plugins.structure.AbstractVFSStructureDeployer;
import org.jboss.deployers.vfs.spi.structure.StructureContext;
import org.jboss.vfs.VirtualFile;
import org.jboss.xb.binding.Unmarshaller;
import org.jboss.xb.binding.UnmarshallerFactory;

/**
 * A structural deployer that looks for a jboss-structure.xml descriptor as
 * the defining structure.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 1.1 $
 */
public class DeclaredStructure extends AbstractVFSStructureDeployer
{
   /**
    * Set the relative order to 0 by default.
    */
   public DeclaredStructure()
   {
      setRelativeOrder(0);
   }

   public boolean determineStructure(StructureContext structureContext) throws DeploymentException
   {
      VirtualFile file = structureContext.getFile();
      try
      {
         boolean trace = log.isTraceEnabled();
         if (isLeaf(file) == false)
         {
            boolean isJBossStructure = false;
            if (trace)
               log.trace(file + " is not a leaf");
            try
            {
               VirtualFile jbossStructure = file.getChild("META-INF/jboss-structure.xml");
               if (jbossStructure.exists())
               {
                  if (trace)
                     log.trace("... context has a META-INF/jboss-structure.xml");

                  URL url = jbossStructure.toURL();
                  UnmarshallerFactory factory = UnmarshallerFactory.newInstance();
                  Unmarshaller unmarshaller = factory.newUnmarshaller();
                  StructureMetaDataObjectFactory ofactory = new StructureMetaDataObjectFactory();
                  unmarshaller.unmarshal(url.toString(), ofactory, structureContext.getMetaData());
                  isJBossStructure = true;
               }
            }
            catch (IOException e)
            {
               log.warn("Exception while looking for META-INF/jboss-structure.xml: " + e);
            }
            if (trace)
               log.trace(file + " isJBossStructure: " + isJBossStructure);
            return isJBossStructure;
         }
      }
      catch (Exception e)
      {
         throw DeploymentException.rethrowAsDeploymentException("Error determining structure: " + file.getName(), e);
      }
      return false;
   }
}
