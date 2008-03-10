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
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.virtual.VirtualFile;
import org.xml.sax.InputSource;

/**
 * JAXBDeployer.
 * 
 * @param <T> the expected type 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public abstract class JAXBDeployer<T> extends AbstractVFSParsingDeployer<T>
{
   /** The JAXBContext */ 
   private JAXBContext context;

   /** The properties */
   private Map<String, Object> properties;
   
   /**
    * Create a new JAXBDeployer.
    * 
    * @param output the output
    * @throws IllegalArgumentException for a null output
    */
   public JAXBDeployer(Class<T> output)
   {
      super(output);
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
    * Create lifecycle
    * 
    * @throws Exception for any problem
    */
   public void create() throws Exception
   {
      if (properties != null)
         context = JAXBContext.newInstance(new Class[] { getOutput() }, properties);
      else
         context = JAXBContext.newInstance(getOutput());
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
      Unmarshaller unmarshaller = context.createUnmarshaller();
      InputStream is = file.openStream();
      try
      {
         InputSource source = new InputSource(is);
         source.setSystemId(file.toURI().toString());
         Object o = unmarshaller.unmarshal(source);
         return getOutput().cast(o);
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
}
