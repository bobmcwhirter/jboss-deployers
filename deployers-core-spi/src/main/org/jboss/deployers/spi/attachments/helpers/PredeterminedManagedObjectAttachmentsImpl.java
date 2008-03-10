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
package org.jboss.deployers.spi.attachments.helpers;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.jboss.deployers.spi.attachments.Attachments;
import org.jboss.deployers.spi.attachments.AttachmentsFactory;
import org.jboss.deployers.spi.attachments.PredeterminedManagedObjectAttachments;

/**
 * PredeterminedManagedObjectAttachmentsImpl.
 * 
 * @author <a href="adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class PredeterminedManagedObjectAttachmentsImpl implements PredeterminedManagedObjectAttachments, Externalizable
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -195530143173768763L;
   
   /** The predetermined attachments */
   private Attachments predetermined = AttachmentsFactory.createMutableAttachments();

   public Attachments getPredeterminedManagedObjects()
   {
      return predetermined;
   }

   public void setPredeterminedManagedObjects(Attachments predetermined)
   {
      if (predetermined == null)
         throw new IllegalArgumentException("Null predetermined");
      this.predetermined = predetermined;
   }

   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
   {
      predetermined = (Attachments) in.readObject();
   }

   /**
    * @serialData attachments
    * @param out the output
    * @throws IOException for any error
    */
   public void writeExternal(ObjectOutput out) throws IOException
   {
      out.writeObject(predetermined);
   }
}
