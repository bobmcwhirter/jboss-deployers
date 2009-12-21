package org.jboss.deployers.plugins.sort;

import org.jboss.deployers.spi.deployer.Deployer;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @version $Revision: 1 $
 */
public class OldStagedSortedDeployers implements StagedSortedDeployers
{
   private Map<String, List<Deployer>> deployersByStage = new HashMap<String, List<Deployer>>();
   private DeployerSorter sorter;

   public void addDeployer(String stageName, Deployer deployer)
   {
      List<Deployer> deployers = deployersByStage.get(stageName);
      if (deployers == null)
         deployers = Collections.emptyList();
      deployers = insert(deployers, deployer);
      deployersByStage.put(stageName, deployers);

   }

   public List<Deployer> getDeployerList(String stageName)
   {
      List<Deployer> deployers = deployersByStage.get(stageName);
      if (deployers == null || deployers.isEmpty())
         return Collections.emptyList();

      return deployers;
   }

   public void removeDeployer(String stageName, Deployer deployer)
   {
      List<Deployer> deployers = deployersByStage.get(stageName);
      if (deployers == null)
         return;

      deployers.remove(deployer);
      if (deployers.isEmpty())
         deployersByStage.remove(stageName);
   }

   /**
    * Insert the new Deployer.
    *
    * @param original    the original deployers
    * @param newDeployer the new deployer
    * @return the sorted deployers
    */
   protected List<Deployer> insert(List<Deployer> original, Deployer newDeployer)
   {
      DeployerSorter sorter = this.sorter;
      if (sorter == null)
         sorter = DeployerSorterFactory.newSorter();

      return sorter.sortDeployers(original, newDeployer);
   }

   /**
    * The deployer sorter.
    *
    * @param sorter the sorter
    */
   public void setSorter(DeployerSorter sorter)
   {
      this.sorter = sorter;
   }
}
