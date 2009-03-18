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

import java.net.URL;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.XMLConstants;

import org.xml.sax.SAXException;

/**
 * Handle Schema.
 *
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 */
public class SchemaHelper
{
   /**
    * Get schema.
    *
    * @param schemaLocation the schema location
    * @return schema or null if schema location is null
    * @throws SAXException for any error
    */
   static Schema getSchema(String schemaLocation) throws SAXException
   {
      if (schemaLocation == null)
         return null;
      
      ClassLoader tcl = SecurityActions.getContextClassLoader();
      URL schemaURL = tcl.getResource(schemaLocation);
      if(schemaURL == null)
         throw new IllegalStateException("Schema URL is null:" + schemaLocation);

      SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
      return factory.newSchema(schemaURL);
   }
}