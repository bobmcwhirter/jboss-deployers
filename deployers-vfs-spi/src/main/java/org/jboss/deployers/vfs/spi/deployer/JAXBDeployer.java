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
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.helpers.DefaultValidationEventHandler;
import javax.xml.validation.Schema;

import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.virtual.VFSInputSource;
import org.jboss.virtual.VirtualFile;
import org.xml.sax.InputSource;

/**
 * JAXBDeployer.
 *
 * @param <T> the expected type
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 * @version $Revision: 1.1 $
 */
public abstract class JAXBDeployer<T> extends AbstractVFSParsingDeployer<T>
{
   /** The JAXBContext */
   private JAXBContext context;

   /** The properties */
   private Map<String, Object> properties;

   /** The classes to be bound */
   private Class<?>[] classesToBeBound;

   /** The schema location */
   private String schemaLocation;

   /** The validation event handler */
   private ValidationEventHandler validationEventHandler = new DefaultValidationEventHandler();

   /**
    * Create a new JAXBDeployer.
    *
    * @param output the output
    * @throws IllegalArgumentException for a null output
    */
   public JAXBDeployer(Class<T> output)
   {
      super(output);
      classesToBeBound = new Class<?>[]{output};
   }

   /**
    * Get the properties.
    *
    * @return the properties.
    */
   public Map<String, Object> getProperties()
   {
      return properties;
   }

   /**
    * Set the properties.
    *
    * @param properties the properties.
    */
   public void setProperties(Map<String, Object> properties)
   {
      this.properties = properties;
   }

   /**
    * Set the classes to be bound.
    *
    * @param classesToBeBound the classes to be bouond
    */
   public void setClassesToBeBound(Class<?>... classesToBeBound)
   {
      this.classesToBeBound = classesToBeBound;
   }

   /**
    * Set schema location.
    *
    * @param schemaLocation the schema location
    */
   public void setSchemaLocation(String schemaLocation)
   {
      this.schemaLocation = schemaLocation;
   }

   /**
    * Set the validation event handler.
    *
    * @param validationEventHandler the validation event handler
    */
   public void setValidationEventHandler(ValidationEventHandler validationEventHandler)
   {
      this.validationEventHandler = validationEventHandler;
   }

   /**
    * Create lifecycle
    *
    * @throws Exception for any problem
    */
   public void create() throws Exception
   {
      context = createContext();
   }

   /**
    * Create context.
    *
    * @return new context instance
    * @throws Exception for any error
    */
   protected JAXBContext createContext() throws Exception
   {
      if (properties != null)
         return JAXBContext.newInstance(classesToBeBound(), properties);
      else
         return JAXBContext.newInstance(classesToBeBound());
   }

   /**
    * Get classes to be bound.
    *
    * @return the classes to be bound
    */
   protected Class<?>[] classesToBeBound()
   {
      return classesToBeBound;
   }

   /**
    * Destroy lifecycle
    */
   public void destroy()
   {
      context = null;
   }

   @Override
   protected T parse(VFSDeploymentUnit unit, VirtualFile file, T root) throws Exception
   {
      if (file == null)
         throw new IllegalArgumentException("Null file");

      log.debug("Parsing: " + file.getName());

      Unmarshaller unmarshaller = context.createUnmarshaller();
      unmarshaller.setEventHandler(validationEventHandler);
      Schema schema = SchemaHelper.getSchema(schemaLocation);
      if (schema != null)
         unmarshaller.setSchema(schema);

      InputSource source = new VFSInputSource(file);
      Object result = unmarshaller.unmarshal(source);
      return getOutput().cast(result);
   }
}
