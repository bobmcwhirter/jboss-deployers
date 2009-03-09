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

import java.util.Map;

import org.jboss.xb.util.JBossXBHelper;

/**
 * MultipleJBossXBDeployer.
 *
 * @param <T> the expected type
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 */
public abstract class MultipleJBossXBDeployer<T> extends MultipleVFSParsingDeployer<T>
{
   /** The helper */
   private JBossXBHelper<T> helper;

   /** The features */
   private Map<String, Boolean> features;

   public MultipleJBossXBDeployer(Class<T> output, Map<String, Class<?>> mappings)
   {
      this(output, mappings, null, null);
   }

   public MultipleJBossXBDeployer(Class<T> output, Map<String, Class<?>> mappings, String suffix, Class<?> suffixClass)
   {
      super(output, mappings, suffix, suffixClass);
      this.helper = new JBossXBHelper<T>(output);
   }

   /**
    * Get the helper.
    *
    * @return the helper
    */
   protected JBossXBHelper<T> getHelper()
   {
      return helper;
   }

   public void start() throws Exception
   {
      if (features != null && features.isEmpty() == false)
      {
         for(Map.Entry<String,Boolean> entry : features.entrySet())
            helper.setFeature(entry.getKey(), entry.getValue());
      }
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

   /**
    * Get unmarshaller features.
    *
    * @return the features
    */
   public Map<String, Boolean> getFeatures()
   {
      return features;
   }

   /**
    * Set the unmarshaller features.
    *
    * @param features the features
    */
   public void setFeatures(Map<String, Boolean> features)
   {
      this.features = features;
   }
}