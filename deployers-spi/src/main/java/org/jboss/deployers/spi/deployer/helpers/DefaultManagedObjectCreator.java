/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.deployers.spi.deployer.helpers;

import java.util.Map;
import java.util.Set;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.managed.ManagedObjectCreator;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.logging.Logger;
import org.jboss.managed.api.ManagedObject;
import org.jboss.managed.api.factory.ManagedObjectFactory;
import org.jboss.managed.plugins.factory.ManagedObjectFactoryBuilder;
import org.jboss.metadata.spi.MetaData;

/**
 * A default ManagedObjectCreator that loops through the deployment attachment
 * names and for those which have a non-null value, calls the ManagedObjectFactory
 * to build the attachment ManagedObject.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision:$
 */
public class DefaultManagedObjectCreator
   implements ManagedObjectCreator
{
   private static Logger log = Logger.getLogger(DefaultManagedObjectCreator.class);
   private ManagedObjectFactory mof;

   
   public ManagedObjectFactory getMof()
   {
      return mof;
   }

   public void setMof(ManagedObjectFactory mof)
   {
      this.mof = mof;
   }


   /**
    * Build managed object.
    *
    * @param unit the deployment unit
    * @param managedObjects map of managed objects
    * @throws DeploymentException for any deployment exception
    */
   public void build(DeploymentUnit unit, Set<String> attachments,
      Map<String, ManagedObject> managedObjects)
      throws DeploymentException
   {
      MetaData metaData = unit.getMetaData();
      ManagedObjectFactory factory = mof;
      if(factory == null )
         factory = ManagedObjectFactoryBuilder.create();

      for(String name : attachments)
      {
         Object instance = unit.getAttachment(name);
         if (instance != null)
         {
            ManagedObject mo = factory.initManagedObject(instance, metaData);
            if (mo != null)
               managedObjects.put(mo.getName(), mo);
         }
      }
   }
}
