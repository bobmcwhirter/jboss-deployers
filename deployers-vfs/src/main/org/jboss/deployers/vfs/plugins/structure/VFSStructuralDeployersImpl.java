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
package org.jboss.deployers.vfs.plugins.structure;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.List;

import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.Ordered;
import org.jboss.deployers.spi.attachments.MutableAttachments;
import org.jboss.deployers.spi.structure.ContextInfo;
import org.jboss.deployers.spi.structure.StructureMetaData;
import org.jboss.deployers.spi.structure.StructureMetaDataFactory;
import org.jboss.deployers.structure.spi.helpers.AbstractStructuralDeployers;
import org.jboss.deployers.vfs.spi.client.VFSDeployment;
import org.jboss.deployers.vfs.spi.structure.StructureDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSStructuralDeployers;
import org.jboss.deployers.vfs.spi.structure.helpers.AbstractStructureDeployer;
import org.jboss.logging.Logger;
import org.jboss.virtual.VirtualFile;

/**
 * VFSStructuralDeployersImpl.
 * 
 * @author <a href="adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class VFSStructuralDeployersImpl extends AbstractStructuralDeployers implements VFSStructuralDeployers
{
   /** The log */
   private static final Logger log = Logger.getLogger(VFSStructuralDeployersImpl.class);
   
   /** The deployers */
   private Set<StructureDeployer> structureDeployers = new TreeSet<StructureDeployer>(Ordered.COMPARATOR);
   
   /**
    * Create a new VFSStructuralDeployers.
    */
   public VFSStructuralDeployersImpl()
   {
   }

   /**
    * Create a new VFSStructurealDeployers.
    * 
    * @param structureDeployers the given deployers
    * @throws IllegalArgumentException for null deployers
    */
   public VFSStructuralDeployersImpl(Set<StructureDeployer> structureDeployers)
   {
      setDeployers(structureDeployers);
   }

   /**
    * Get the structureDeployers.
    * 
    * @return the structureDeployers.
    */
   public Set<StructureDeployer> getDeployers()
   {
      return structureDeployers;
   }
   
   /**
    * Set the structureDeployers.
    * 
    * @param deployers the structureDeployers.
    * @throws IllegalArgumentException for null deployers
    */
   public void setDeployers(Set<StructureDeployer> deployers)
   {
      if (deployers == null)
         throw new IllegalArgumentException("Null deployers");
      
      // Remove all the old deployers that are not in the new set
      HashSet<StructureDeployer> oldDeployers = new HashSet<StructureDeployer>(structureDeployers);
      oldDeployers.removeAll(deployers);
      for (StructureDeployer deployer : oldDeployers)
         removeDeployer(deployer);
      
      // Add all the new deployers that were not already present
      HashSet<StructureDeployer> newDeployers = new HashSet<StructureDeployer>(deployers);
      newDeployers.removeAll(structureDeployers);
      for (StructureDeployer deployer : newDeployers)
         addDeployer(deployer);
   }

   /**
    * Add a structure deployer
    * 
    * @param deployer the deployer
    */
   public synchronized void addDeployer(StructureDeployer deployer)
   {
      if (deployer == null)
         throw new IllegalArgumentException("Null deployer");
      structureDeployers.add(new StructureDeployerWrapper(deployer));
      log.debug("Added structure deployer " + deployer);
   }

   /**
    * Remove a structure deployer
    * 
    * @param deployer the deployer
    */
   public synchronized void removeDeployer(StructureDeployer deployer)
   {
      if (deployer == null)
         throw new IllegalArgumentException("Null deployer");
      structureDeployers.remove(deployer);
      log.debug("Removed structure deployer " + deployer);
   }
   
   public boolean determineStructure(VirtualFile root, VirtualFile parent, VirtualFile file, StructureMetaData structureMetaData) throws DeploymentException
   {
      StructureMetaData structure = StructureMetaDataFactory.createStructureMetaData();
      boolean result = doDetermineStructure(root, parent, file, structure);
      if (result)
      {
         String relativePath = AbstractStructureDeployer.getRelativePath(parent, file);
         
         // Something said it recognised it
         ContextInfo recognised = structure.getContext("");
         if (recognised == null)
            throw new IllegalStateException("Something recognised the deployment, but there is no context? " + file);
         
         // Create the context in the parent structure
         ContextInfo parentContext;
         List<String> metaDataPath = recognised.getMetaDataPath();
         if (metaDataPath == null || metaDataPath.isEmpty())
            parentContext = StructureMetaDataFactory.createContextInfo(relativePath, recognised.getClassPath());
         else
            parentContext = StructureMetaDataFactory.createContextInfo(relativePath, metaDataPath, recognised.getClassPath());

         // copy the modification type information
         parentContext.setModificationType(recognised.getModificationType());
         structureMetaData.addContext(parentContext);
         MutableAttachments attachments = (MutableAttachments) parentContext.getPredeterminedManagedObjects();
         attachments.addAttachment(StructureMetaData.class, structure);
      }
      return result;
   }
   
   /**
    * Determine the structure
    * 
    * @param root the root file
    * @param parent the parent file
    * @param file the file
    * @param structureMetaData the structure metadata
    * @return true when recognised
    * @throws DeploymentException for any error
    */
   protected boolean doDetermineStructure(VirtualFile root, VirtualFile parent, VirtualFile file, StructureMetaData structureMetaData) throws DeploymentException
   {
      StructureDeployer[] theDeployers; 
      synchronized (this)
      {
         if (structureDeployers.isEmpty())
            throw new IllegalStateException("No structure deployers");
         
         theDeployers = structureDeployers.toArray(new StructureDeployer[structureDeployers.size()]);
      }

      boolean trace = log.isTraceEnabled();
      if (trace)
         log.trace("Determining structure for " + file.getName() + " deployers=" + Arrays.asList(theDeployers));
      
      
      boolean result = false;
      for (StructureDeployer deployer : theDeployers)
      {
         if (deployer.determineStructure(root, parent, file, structureMetaData, this))
         {
            if (trace)
               log.trace(file.getName() + " recognised by " + deployer);
            result = true;
            break;
         }
      }
      if (result == false && trace)
         log.trace(file.getName() + " not recognised");
      return result;
   }

   @Override
   protected void determineStructure(Deployment deployment, StructureMetaData structure) throws Exception
   {
      if (deployment instanceof VFSDeployment == false)
         throw new DeploymentException("Structure can only be determined for VFSDeployments " + deployment);
      
      VFSDeployment vfsDeployment = (VFSDeployment) deployment;
      
      VirtualFile root = vfsDeployment.getRoot();
      if (root == null)
         throw new IllegalStateException("Deployment has no root " + deployment);
      if (doDetermineStructure(root, null, root, structure) == false)
         throw new DeploymentException("No deployer recognised the structure of " + deployment.getName());
   }
}
