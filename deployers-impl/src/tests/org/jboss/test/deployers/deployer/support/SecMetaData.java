package org.jboss.test.deployers.deployer.support;

import java.io.Serializable;

import org.jboss.managed.api.annotation.ManagementObject;
import org.jboss.managed.api.annotation.ManagementObjectRef;
import org.jboss.managed.api.annotation.ManagementProperty;

/**
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
@ManagementObject
public class SecMetaData implements Serializable
{
   private static final long serialVersionUID = 1;

   public enum SecurityDeploymentType {NONE, APPLICATION, DOMAIN, DOMAIN_AND_APPLICATION};

   private String domain;
   private SecurityDeploymentType type;

   /**
    * The domain value refers to a security domain component if type is either
    * DOMAIN, DOMAIN_AND_APPLICATION
    * @return the domain
    */
   @ManagementProperty(name="domain-name")
   @ManagementObjectRef(type="SecurityDomain")
   public String getDomain()
   {
      return domain;
   }
   public void setDomain(String domain)
   {
      this.domain = domain;
   }
   @ManagementProperty(name="security-criteria")
   public SecurityDeploymentType getType()
   {
      return type;
   }
   public void setType(SecurityDeploymentType type)
   {
      this.type = type;
   }
   
}
