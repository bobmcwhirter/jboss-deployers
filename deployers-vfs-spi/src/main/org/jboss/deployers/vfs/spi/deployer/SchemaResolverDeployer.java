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

import java.io.InputStream;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.virtual.VirtualFile;
import org.jboss.xb.annotations.JBossXmlSchema;
import org.jboss.xb.annotations.JBossXmlConstants;
import org.jboss.xb.binding.Unmarshaller;
import org.jboss.xb.binding.UnmarshallerFactory;
import org.jboss.xb.binding.sunday.unmarshalling.DefaultSchemaResolver;
import org.jboss.xb.binding.sunday.unmarshalling.SingletonSchemaResolverFactory;

/**
 * SchemaResolverDeployer.
 * 
 * @param <T> the expected type 
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @author Scott.Stark@jboss.org
 * @version $Revision 1.1 $
 */
public class SchemaResolverDeployer<T> extends AbstractVFSParsingDeployer<T>
{
   /** Unmarshaller factory */
   private static final UnmarshallerFactory factory = UnmarshallerFactory.newInstance();

   /** The singleton schema resolver */
   private static DefaultSchemaResolver resolver = (DefaultSchemaResolver)SingletonSchemaResolverFactory.getInstance().getSchemaBindingResolver();

   /** Whether the Unmarshaller will use schema validation */
   private boolean useSchemaValidation = true;

   /** Whether to validate */
   private boolean useValidation = true;

   /** Whether we register with  jbossxb */
   private boolean registerWithJBossXB;

   /** The namespace */
   private String namespace;

   /**
    * Create a new SchemaResolverDeployer.
    * 
    * @param output the output
    * @throws IllegalArgumentException for a null output
    */
   public SchemaResolverDeployer(Class<T> output)
   {
      super(output);
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
    * Get the registerWithJBossXB.
    *
    * @return the registerWithJBossXB
    */
   public boolean isRegisterWithJBossXB()
   {
      return registerWithJBossXB;
   }

   /**
    * Set the registerWithJBossXB.
    *
    * @param registerWithJBossXB the registerWithJBossXB
    */
   public void setRegisterWithJBossXB(boolean registerWithJBossXB)
   {
      this.registerWithJBossXB = registerWithJBossXB;
   }

   /**
    * Get the namespace, if it was set.
    *
    * @return the namespace
    */
   public String getNamespace()
   {
      return namespace;
   }

   /**
    * Check if we need to register schema to jbossxb.
    */
   public void create()
   {
      if (isRegisterWithJBossXB())
      {
         namespace = findNamespace();
         if (namespace == null || JBossXmlConstants.DEFAULT.equals(namespace))
            throw new IllegalArgumentException(
                  "RegisterWithJBossXB is enabled, but cannot find namespace on class or package: " + getOutput() +
                  ", perhaps missing @JBossXmlSchema or using default namespace attribute."
            );

         resolver.addClassBinding(namespace, getOutput());
      }
   }

   /**
    * Remove registered schema
    */
   public void destroy()
   {
      if (isRegisterWithJBossXB())
      {
         // namespace should exist, since we got past create
         resolver.removeClassBinding(namespace);
      }
   }

   /**
    * Find the namespace on class/package
    *
    * @return jboss xml schema namespace
    */
   protected String findNamespace()
   {
      Class<T> metadata = getOutput();
      JBossXmlSchema jBossXmlSchema = metadata.getAnnotation(JBossXmlSchema.class);
      if (jBossXmlSchema == null)
      {
         Package pckg = metadata.getPackage();
         if (pckg != null)
            jBossXmlSchema = pckg.getAnnotation(JBossXmlSchema.class);
      }
      return jBossXmlSchema != null ? jBossXmlSchema.namespace() : null;
   }

   protected T parse(VFSDeploymentUnit unit, VirtualFile file, T root) throws Exception
   {
      if (file == null)
         throw new IllegalArgumentException("Null file");

      VirtualFile altDD = (VirtualFile) unit.getAttachment(getOutput().getName() + ".altDD");
      if(altDD != null)
         file = altDD;

      return parse(file);
   }

   protected T parse(VirtualFile file) throws Exception
   {
      if (file == null)
         throw new IllegalArgumentException("Null file");
      
      log.debug("Parsing file: "+file+" for deploymentType: " + getOutput());
      Unmarshaller unmarshaller = factory.newUnmarshaller();
      unmarshaller.setSchemaValidation(isUseSchemaValidation());
      unmarshaller.setValidation(isUseValidation());
      InputStream is = openStreamAndValidate(file);
      Object parsed = null;
      try
      {
         parsed = unmarshaller.unmarshal(is, resolver);
         log.debug("Parsed file: "+file+" to: "+parsed);
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

      return getOutput().cast(parsed);
   }
}
