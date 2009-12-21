package org.jboss.deployers.plugins.sort;

import org.jboss.deployers.spi.deployer.Deployer;

import java.util.List;

/**
 * Sorted deployers spi.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @version $Revision: 1 $
 */
public interface StagedSortedDeployers
{
   /**
    * Add new deployer.
    *
    * @param stageName the stage name
    * @param deployer the deployer
    */
   void addDeployer(String stageName, Deployer deployer);

   /**
    * The deployer list for stage.
    *
    * @param stageName the stage name
    * @return matching deployer per stage
    */
   List<Deployer> getDeployerList(String stageName);

   /**
    * Remove deployer.
    *
    * @param stageName the stage name
    * @param deployer the deployer
    */
   void removeDeployer(String stageName, Deployer deployer);
}
