/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.deployers.client.plugins.deployment;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Set;

import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.spi.attachments.helpers.PredeterminedManagedObjectAttachmentsImpl;

/**
 * AbstractDeployment.
 * 
 * @author <a href="adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class AbstractDeployment extends PredeterminedManagedObjectAttachmentsImpl implements Deployment
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 2;
   
   /** The name of the deployment */
   private String name;

   /**
    * Create a new AbstractDeployment.
    */
   public AbstractDeployment()
   {
   }
   
   /**
    * Create a new AbstractDeployment.
    * 
    * @param name the name of the deployment
    * @throws IllegalArgumentException for a null name
    */
   public AbstractDeployment(String name)
   {
      setName(name);
   }

   /**
    * Get the name.
    * 
    * @return the name.
    */
   public String getName()
   {
      return name;
   }

   /**
    * Get the simple name.
    * 
    * @return the name.
    */
   public String getSimpleName()
   {
      return name;
   }

   /**
    * Set the name.
    * 
    * @param name the name.
    * @throws IllegalArgumentException for a null name
    */
   public void setName(String name)
   {
      if (name == null)
          throw new IllegalArgumentException("Null name");
      this.name = name;
   }

   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
   {
      super.readExternal(in);
      boolean hasName = in.readBoolean();
      if (hasName)
         name = in.readUTF();
   }

   /**
    * @serialData name from {@link #getName()}
    * @param out the output
    * @throws IOException for any error
    */
   public void writeExternal(ObjectOutput out) throws IOException
   {
      super.writeExternal(out);
      String name = getName();
      out.writeBoolean(name != null);
      if (name != null)
         out.writeUTF(name);
   }
}
