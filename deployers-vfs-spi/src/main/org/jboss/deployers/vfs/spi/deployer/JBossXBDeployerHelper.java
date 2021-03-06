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

import java.io.IOException;
import java.io.InputStream;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.logging.Logger;
import org.jboss.virtual.VirtualFile;
import org.jboss.xb.annotations.JBossXmlSchema;
import org.jboss.xb.binding.ObjectModelFactory;
import org.jboss.xb.binding.Unmarshaller;
import org.jboss.xb.binding.UnmarshallerFactory;
import org.jboss.xb.binding.sunday.unmarshalling.DefaultSchemaResolver;
import org.jboss.xb.binding.sunday.unmarshalling.SingletonSchemaResolverFactory;
import org.xml.sax.InputSource;

/**
 * JBossXB deployer helper.
 *
 * @param <T> the expected type
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 */
public class JBossXBDeployerHelper<T> implements org.jboss.deployers.vfs.spi.deployer.UnmarshallerFactory<Boolean>
{
   /** The log */
   private Logger log = Logger.getLogger(JBossXBDeployerHelper.class);

   /** Unmarshaller factory */
   private static final UnmarshallerFactory factory = UnmarshallerFactory.newInstance();

   /** The singleton schema resolver */
   private static DefaultSchemaResolver resolver = (DefaultSchemaResolver)SingletonSchemaResolverFactory.getInstance().getSchemaBindingResolver();

   /** The output */
   private Class<T> output;

   /** Whether the Unmarshaller will use schema validation */
   private boolean useSchemaValidation = true;

   /** Whether to validate */
   private boolean useValidation = true;

   /**
    * Create a new SchemaResolverDeployer.
    *
    * @param output the output
    * @throws IllegalArgumentException for a null output
    */
   protected JBossXBDeployerHelper(Class<T> output)
   {
      if (output == null)
         throw new IllegalArgumentException("Null output.");
      this.output = output;
   }

   public void setFeature(String featureName, Boolean flag) throws Exception
   {
      factory.setFeature(featureName, flag);
   }

   /**
    * Get the useSchemaValidation.
    *
    * @return the useSchemaValidation.
    */
   public boolean isUseSchemaValidation()
   {
      return useSchemaValidation;
   }

   /**
    * Set the useSchemaValidation.
    *
    * @param useSchemaValidation the useSchemaValidation.
    */
   public void setUseSchemaValidation(boolean useSchemaValidation)
   {
      this.useSchemaValidation = useSchemaValidation;
   }

   /**
    * Get the useValidation.
    *
    * @return the useValidation.
    */
   public boolean isUseValidation()
   {
      return useValidation;
   }

   /**
    * Set the useValidation.
    *
    * @param useValidation the useValidation.
    */
   public void setUseValidation(boolean useValidation)
   {
      this.useValidation = useValidation;
   }

   /**
    * Add class binding.
    *
    * @param namespace the namespace
    * @param metadata the metadata
    */
   public static void addClassBinding(String namespace, Class<?> metadata)
   {
      resolver.addClassBinding(namespace, metadata);
   }

   /**
    * Remove class binding.
    *
    * @param namespace the namespace
    */
   public static void removeClassBinding(String namespace)
   {
      resolver.removeClassBinding(namespace);
   }

   /**
    * Find the namespace on class/package
    *
    * @param metadata the metadata class
    * @return jboss xml schema namespace
    */
   public static String findNamespace(Class<?> metadata)
   {
      JBossXmlSchema jBossXmlSchema = metadata.getAnnotation(JBossXmlSchema.class);
      if (jBossXmlSchema == null)
      {
         Package pckg = metadata.getPackage();
         if (pckg != null)
            jBossXmlSchema = pckg.getAnnotation(JBossXmlSchema.class);
      }
      return jBossXmlSchema != null ? jBossXmlSchema.namespace() : null;
   }

   /**
    * Parse file to output metadata.
    *
    * @param file the file to parse
    * @return new metadata instance
    * @throws Exception for any error
    */
   public T parse(VirtualFile file) throws Exception
   {
      return parse(output, file);
   }

   /**
    * Parse the file to create metadata instance.
    *
    * @param <U> the expect type
    * @param expectedType the expected type
    * @param file the file
    * @return new metadata instance
    * @throws Exception for any error
    */
   public <U> U parse(Class<U> expectedType, VirtualFile file) throws Exception
   {
      if (expectedType == null)
         throw new IllegalArgumentException("Null expected type");
      if (file == null)
         throw new IllegalArgumentException("Null file");

      log.debug("Parsing file: "+file+" for type: " + expectedType);
      Unmarshaller unmarshaller = factory.newUnmarshaller();
      unmarshaller.setSchemaValidation(isUseSchemaValidation());
      unmarshaller.setValidation(isUseValidation());
      InputStream is = openStreamAndValidate(file);
      Object parsed;
      try
      {
         InputSource source = new InputSource(is);
         source.setSystemId(file.toURI().toString());
         parsed = unmarshaller.unmarshal(source, resolver);
      }
      finally
      {
         try
         {
            is.close();
         }
         catch (Exception ignored)
         {
         }
      }
      if (parsed == null)
         throw new DeploymentException("The xml " + file.getPathName() + " is not well formed!");

      log.debug("Parsed file: "+file+" to: "+parsed);
      return expectedType.cast(parsed);
   }

   /**
    * Parse the file using object model factory.
    *
    * @param file the file to parse
    * @param root the previous root
    * @param omf the object model factory
    * @return new metadata instance
    * @throws Exception for any error
    */
   public T parse(VirtualFile file, T root, ObjectModelFactory omf) throws Exception
   {
      return parse(output, file, root, omf);
   }

   /**
    * Parse the file using object model factory.
    *
    * @param <U> the expect type
    * @param expectedType the expected type
    * @param file the file to parse
    * @param root the previous root
    * @param omf the object model factory
    * @return new metadata instance
    * @throws Exception for any error
    */
   public <U> U parse(Class<U> expectedType, VirtualFile file, U root, ObjectModelFactory omf) throws Exception
   {
      if (file == null)
         throw new IllegalArgumentException("Null file");

      log.debug("Parsing file: "+file+" for deploymentType: " + expectedType);

      Unmarshaller unmarshaller = factory.newUnmarshaller();
      unmarshaller.setSchemaValidation(isUseSchemaValidation());
      unmarshaller.setValidation(isUseValidation());
      InputStream is = openStreamAndValidate(file);
      Object parsed;
      try
      {
         InputSource source = new InputSource(is);
         source.setSystemId(file.toURI().toString());
         parsed = unmarshaller.unmarshal(source, omf, root);
      }
      finally
      {
         try
         {
            is.close();
         }
         catch (Exception ignored)
         {
         }
      }
      if (parsed == null)
         throw new DeploymentException("The xml " + file.getPathName() + " is not well formed!");

      log.debug("Parsed file: "+file+" to: "+parsed);
      return expectedType.cast(parsed);
   }

   /**
    * Open stream and validate if not null.
    *
    * @param file the virtual file
    * @return non-null input stream
    * @throws Exception for any error or if file's stream is null
    */
   protected static InputStream openStreamAndValidate(VirtualFile file) throws Exception
   {
      if (file == null)
         throw new IllegalArgumentException("Null file");

      InputStream inputStream = SecurityActions.openStream(file);
      if (inputStream == null)
         throw new IOException("Null file stream: " + file);

      return inputStream;
   }
}