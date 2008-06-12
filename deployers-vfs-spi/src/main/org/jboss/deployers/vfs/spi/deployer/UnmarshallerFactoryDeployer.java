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

import java.util.HashMap;
import java.util.Map;

/**
 * Unmarshaller factory deployer.
 * Able to apply features to unmarshaller factory.
 *
 * @param <U> exact unmarshaller factory's flag type
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 */
public abstract class UnmarshallerFactoryDeployer<T, U> extends AbstractVFSParsingDeployer<T>
{
   /** The fixup base uris */
   static String FIXUP_BASE_URIS = "http://apache.org/xml/features/xinclude/fixup-base-uris";

   /** The fixup base lang */
   static String FIXUP_LANGUAGE = "http://apache.org/xml/features/xinclude/fixup-language";

   /** Use default features */
   private boolean useDefaultFeatures = true;

   /** Features map */
   private Map<String, U> features;

   protected UnmarshallerFactoryDeployer(Class<T> output)
   {
      super(output);
   }

   /**
    * Create unmarshaller factory wrapper.
    * @return the unmarshaller factory
    */
   protected abstract UnmarshallerFactory<U> createUnmarshallerFactory();

   public void start() throws Exception
   {
      UnmarshallerFactory<U> unmarshallerFactory = createUnmarshallerFactory();
      if (unmarshallerFactory == null)
         throw new IllegalArgumentException("Unmarshaller factory cannot be null.");

      Map<String,U> features = getFeatures();
      if (isUseDefaultFeatures())
      {
         if (features == null)
            features = new HashMap<String,U>();

         if (features.containsKey(FIXUP_BASE_URIS) == false)
            features.put(FIXUP_BASE_URIS, fromString("false"));

         if (features.containsKey(FIXUP_LANGUAGE) == false)
            features.put(FIXUP_LANGUAGE, fromString("false"));
      }

      if (features != null && features.isEmpty() == false)
      {
         for(Map.Entry<String,U> entry : features.entrySet())
            unmarshallerFactory.setFeature(entry.getKey(), entry.getValue());
      }
   }

   /**
    * Transform string to flag value.
    *
    * @param value the value
    * @return value as T type
    */
   protected abstract U fromString(String value);

   public boolean isUseDefaultFeatures()
   {
      return useDefaultFeatures;
   }

   public void setUseDefaultFeatures(boolean useDefaultFeatures)
   {
      this.useDefaultFeatures = useDefaultFeatures;
   }

   public Map<String, U> getFeatures()
   {
      return features;
   }

   public void setFeatures(Map<String, U> features)
   {
      this.features = features;
   }
}