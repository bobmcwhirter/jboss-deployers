package org.jboss.deployers.plugins.sort;

import org.jboss.deployers.spi.deployer.Deployer;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class NewStagedSortedDeployers implements StagedSortedDeployers
{
   private Map<String, SortedDeployers> deployersByStage = new HashMap<String, SortedDeployers>();

   public void addDeployer(String stageName, Deployer deployer)
   {
      SortedDeployers deployers = deployersByStage.get(stageName);
      if (deployers == null)
      {
         deployers = new SortedDeployers();
         deployersByStage.put(stageName, deployers);
      }
      deployers.sort(deployer);

   }

   public List<Deployer> getDeployerList(String stageName)
   {
      SortedDeployers deployers = deployersByStage.get(stageName);
      if (deployers == null)
         return Collections.emptyList();

      return deployers.getDeployers();
   }

   public void removeDeployer(String stageName, Deployer deployer)
   {
      SortedDeployers deployers = deployersByStage.get(stageName);
      if (deployers != null)
         deployers.removeDeployer(deployer);
   }
}
