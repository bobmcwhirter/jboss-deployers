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
package org.jboss.deployers.vfs.spi.deployer;

/**
 * JBoss XB deployer.
 *
 * @param <T> the expected type
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 */
public abstract class JBossXBDeployer<T> extends UnmarshallerFactoryDeployer<T, Boolean>
{
   /** The helper */
   private JBossXBDeployerHelper<T> helper;

   /**
    * Create a new SchemaResolverDeployer.
    *
    * @param output the output
    * @throws IllegalArgumentException for a null output
    */
   public JBossXBDeployer(Class<T> output)
   {
      super(output);
      this.helper = new JBossXBDeployerHelper<T>(output);
   }

   /**
    * Get the helper.
    *
    * @return the helper
    */
   protected JBossXBDeployerHelper<T> getHelper()
   {
      return helper;
   }

   protected UnmarshallerFactory<Boolean> createUnmarshallerFactory()
   {
      return getHelper();
   }

   protected Boolean fromString(String value)
   {
      return Boolean.valueOf(value);
   }

   /**
    * Get the useSchemaValidation.
    *
    * @return the useSchemaValidation.
    */
   public boolean isUseSchemaValidation()
   {
      return helper.isUseSchemaValidation();
   }

   /**
    * Set the useSchemaValidation.
    *
    * @param useSchemaValidation the useSchemaValidation.
    */
   public void setUseSchemaValidation(boolean useSchemaValidation)
   {
      helper.setUseSchemaValidation(useSchemaValidation);
   }

   /**
    * Get the useValidation.
    *
    * @return the useValidation.
    */
   public boolean isUseValidation()
   {
      return helper.isUseValidation();
   }

   /**
    * Set the useValidation.
    *
    * @param useValidation the useValidation.
    */
   public void setUseValidation(boolean useValidation)
   {
      helper.setUseValidation(useValidation);
   }
}