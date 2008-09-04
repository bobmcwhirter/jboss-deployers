package org.jboss.test.deployers.deployer.support;

import java.io.Serializable;
import java.util.Map;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.helpers.AbstractSimpleRealDeployer;
import org.jboss.deployers.spi.deployer.managed.ManagedObjectCreator;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.managed.api.ManagedObject;
import org.jboss.managed.api.factory.ManagedObjectFactory;
import org.jboss.managed.plugins.factory.ManagedObjectFactoryBuilder;

public class MCFDeployer
   extends AbstractSimpleRealDeployer<DSMetaData>
   implements ManagedObjectCreator

{
   public MCFDeployer()
   {
      super(DSMetaData.class);
   }

   @Override
   public void deploy(DeploymentUnit unit, DSMetaData deployment)
      throws DeploymentException
   {
   }

   public void build(DeploymentUnit unit, Map<String, ManagedObject> managedObjects)
      throws DeploymentException
   {
      ManagedObjectFactory factory = ManagedObjectFactoryBuilder.create();
      Map<String, Object> attachments = unit.getAttachments();
      for(Object metaData : attachments.values() )
      {
         if( metaData instanceof Serializable )
         {
            Serializable smetaData = Serializable.class.cast(metaData);
            ManagedObject mo = factory.initManagedObject(smetaData, null, null);
            if (mo != null)
               managedObjects.put(mo.getName(), mo);
         }
      }
   }

}
