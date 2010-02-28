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
import java.util.Set;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.structure.ContextInfo;
import org.jboss.deployers.spi.structure.StructureMetaData;
import org.jboss.deployers.vfs.plugins.structure.AbstractVFSArchiveStructureDeployer;
import org.jboss.deployers.vfs.plugins.structure.jar.JARStructure;
import org.jboss.deployers.vfs.spi.structure.StructureContext;
import org.jboss.vfs.VirtualFile;
import org.jboss.xb.binding.Unmarshaller;
import org.jboss.xb.binding.UnmarshallerFactory;

/**
 * A structural deployer that looks for a jboss-structure.xml descriptor as
 * the defining structure.
 * 
 * @author Scott.Stark@jboss.org
 * @author <a href="jbailey@redhat.com">John Bailey</a>
 * @version $Revision: 1.1 $
 */
public class DeclaredStructure extends AbstractVFSArchiveStructureDeployer
{
   /**
    * Set of suffixes used to determine if an archive mount is needed
    */
   private final Set<String> suffixes;
   
   /**
    * Construct with a default jar suffixes
    */
   public DeclaredStructure()
   {
      this(JARStructure.DEFAULT_JAR_SUFFIXES);
   }
   
   /**
    * Set the relative order to 0 by default.
    * 
    * @param suffixes the suffixes
    */
   public DeclaredStructure(Set<String> suffixes)
   {
      setRelativeOrder(0);
      if (suffixes == null)
         throw new IllegalArgumentException("Null suffixes");
      this.suffixes = suffixes;
   }
   
   @Override
   protected boolean hasValidSuffix(String name)
   {
      int idx = name.lastIndexOf('.');
      return idx != -1 && suffixes.contains(name.substring(idx).toLowerCase());
   }

   public boolean doDetermineStructure(StructureContext structureContext) throws DeploymentException
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
                  mountChildren(structureContext);
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

   /**
    * Iterate through the contexts and mount anything that looks like an archive 
    * 
    * @param structureContext the context
    * @throws IOException if errors occur during mounting
    */
   protected void mountChildren(StructureContext structureContext) throws IOException
   {
      final StructureMetaData structureMetaData = structureContext.getMetaData();
      final VirtualFile structureRoot = structureContext.getRoot();
      if(structureContext != null)
      {
         for(ContextInfo contextInfo : structureMetaData.getContexts()) 
         {
            final String contextPath = contextInfo.getPath(); 
            if(hasValidSuffix(contextPath))
            {
               final VirtualFile child = structureRoot.getChild(contextPath);
               if(child.exists() && child.isFile()) 
               {
                  performMount(child);
               }
            }
         }
      }
      
   }
}
