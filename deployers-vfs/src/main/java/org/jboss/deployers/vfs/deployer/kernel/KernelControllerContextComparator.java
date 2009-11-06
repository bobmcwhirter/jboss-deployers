package org.jboss.deployers.vfs.deployer.kernel;

import java.util.Comparator;

import org.jboss.deployers.vfs.spi.deployer.helpers.BeanMetaDataDeployerPlugin;

/**
 * Singleton comparator to compare KernelControllerContextCreators by their relative order
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
public class KernelControllerContextComparator implements Comparator<BeanMetaDataDeployerPlugin>
{
   /** Singleton instance*/
   private static final KernelControllerContextComparator INSTANCE = new KernelControllerContextComparator();
   
   private KernelControllerContextComparator()
   {
   }
   
   /**
    * Get the singleton
    * @return the singleton KernelControllerContextComparator
    */
   public static KernelControllerContextComparator getInstance()
   {
      return INSTANCE;
   }
   
   /**
    * Compares two KernelControllerContextCreators' relative orders
    * @param o1 The first KernelContextCreator
    * @param o1 The second KernelContextCreator
    * @return An integer as per the {@link Comparator#compare(Object, Object)} contract
    */
   public int compare(BeanMetaDataDeployerPlugin o1, BeanMetaDataDeployerPlugin o2)
   {
      if (o1.getRelativeOrder() < o2.getRelativeOrder())
         return -1;
      if (o1.getRelativeOrder() > o2.getRelativeOrder())
         return 1;
      
      return 0;
   }
}