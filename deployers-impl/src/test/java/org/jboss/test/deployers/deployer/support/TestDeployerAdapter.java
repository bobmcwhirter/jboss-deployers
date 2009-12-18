/**
 * 
 */
package org.jboss.test.deployers.deployer.support;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;

/**
 * Deployer adapter.
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public final class TestDeployerAdapter extends AbstractDeployer
{
   
   final String name;

   public TestDeployerAdapter( final String name )
   {
      super();
      this.name = name;
   }

   public void deploy(DeploymentUnit unit) throws DeploymentException
   {
      // NOOP
   }
   
   @Override
   public String toString()
   {
      return super.toString() + "-" + this.name;
   }

}
