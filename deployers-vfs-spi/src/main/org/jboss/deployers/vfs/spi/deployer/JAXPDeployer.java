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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.util.xml.JBossEntityResolver;
import org.jboss.virtual.VirtualFile;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * JAXPDeployer is an abstract deployer that overrides parse to run a jaxp
 * parse of the VirtualFile passed to parse.
 * 
 * @see #parse(VFSDeploymentUnit, VirtualFile, Object)
 * @see #doParse(VFSDeploymentUnit, VirtualFile)
 * 
 * @param <T> the expected type 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @author Scott.Stark@jboss.org
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 * @version $Revision: 60707 $
 */
public abstract class JAXPDeployer<T> extends UnmarshallerFactoryDeployer<T, Boolean>
{
   /** Use a namespace aware parser */
   private boolean useNamespaceAwareParser = true;

   /** A flag indicating if deployment descriptors should be validated */
   private boolean validateDTDs;
   
   /** The document builder factory */
   private DocumentBuilderFactory documentBuilderFactory;
   
   /**
    * Create a new JAXPDeployer.
    * 
    * @param output the output
    * @throws IllegalArgumentException for a null output
    */
   public JAXPDeployer(Class<T> output)
   {
      super(output);
   }

   /**
    * Get the useNamespaceAwareParser.
    * 
    * @return the useNamespaceAwareParser.
    */
   public boolean isUseNamespaceAwareParser()
   {
      return useNamespaceAwareParser;
   }

   /**
    * Set the useNamespaceAwareParser.
    * 
    * @param useNamespaceAwareParser the useNamespaceAwareParser.
    */
   public void setUseNamespaceAwareParser(boolean useNamespaceAwareParser)
   {
      this.useNamespaceAwareParser = useNamespaceAwareParser;
   }

   /**
    * Get the validateDTDs.
    * 
    * @return the validateDTDs.
    */
   public boolean isValidateDTDs()
   {
      return validateDTDs;
   }

   /**
    * Set the validateDTDs.
    * 
    * @param validateDTDs the validateDTDs.
    */
   public void setValidateDTDs(boolean validateDTDs)
   {
      this.validateDTDs = validateDTDs;
   }

   /**
    * Get the documentBuilderFactory.
    * 
    * @return the documentBuilderFactory.
    * @throws IllegalStateException if the create method has not been invoked
    */
   protected DocumentBuilderFactory getDocumentBuilderFactory()
   {
      if (documentBuilderFactory == null)
         throw new IllegalStateException("Document builder factory has not been constructed");
      return documentBuilderFactory;
   }

   /**
    * Create lifecycle
    * 
    * @throws Exception for any problem
    */
   public void create() throws Exception
   {
      documentBuilderFactory = DocumentBuilderFactory.newInstance();
      documentBuilderFactory.setNamespaceAware(useNamespaceAwareParser);
      documentBuilderFactory.setValidating(validateDTDs);
   }

   /**
    * Destroy lifecycle
    */
   public void destroy()
   {
      documentBuilderFactory = null;
   }

   protected UnmarshallerFactory<Boolean> createUnmarshallerFactory()
   {
      return new UnmarshallerFactory<Boolean>()
      {
         public void setFeature(String featureName, Boolean flag) throws Exception
         {
            getDocumentBuilderFactory().setFeature(featureName, flag);
         }
      };
   }

   protected Boolean fromString(String value)
   {
      return Boolean.valueOf(value);
   }

   @Override
   protected T parse(VFSDeploymentUnit unit, VirtualFile file, T root) throws Exception
   {
     // First look for an existing Document attachment 
      Document document = unit.getAttachment(Document.class);
      if( document == null )
      {
         // Next parse the metadata file
         document = doParse(unit, file);
      }

      // Transform the document into a T instance
      return parse(unit, file, document);
   }
   
   /**
    * Do the parsing
    * 
    * @param unit the deployment unit
    * @param file the metadata file
    * @return the document
    * @throws Exception for any error
    */
   protected Document doParse(VFSDeploymentUnit unit, VirtualFile file) throws Exception
   {
      if (file == null)
         throw new IllegalArgumentException("Null file");

      log.debug("Parsing: " + file.getName());
      
      DocumentBuilder parser = getDocumentBuilderFactory().newDocumentBuilder();
      InputStream is = openStreamAndValidate(file);
      try
      {
         InputSource source = new InputSource(is);
         source.setSystemId(file.toURI().toString());
         parser.setEntityResolver(new JBossEntityResolver());
         return parser.parse(source);
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
   }

   /**
    * Parse a deployment
    * 
    * @param unit the deployment unit
    * @param file the metadata file
    * @param document the document
    * @return the metadata
    * @throws Exception for any error
    */
   protected abstract T parse(VFSDeploymentUnit unit, VirtualFile file, Document document) throws Exception;
}
