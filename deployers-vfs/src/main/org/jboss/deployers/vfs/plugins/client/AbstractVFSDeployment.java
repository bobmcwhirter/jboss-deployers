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
package org.jboss.deployers.vfs.plugins.client;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.jboss.deployers.client.plugins.deployment.AbstractDeployment;
import org.jboss.deployers.vfs.spi.client.VFSDeployment;
import org.jboss.virtual.VirtualFile;

/**
 * AbstractVFSDeployment.
 * 
 * @author <a href="adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class AbstractVFSDeployment extends AbstractDeployment implements VFSDeployment
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 3992263833911364088L;

   /** The root */
   private VirtualFile root;

   /**
    * Get the vfs file name safely
    * 
    * @param root the virutal file
    * @return the name
    */
   static final String safeVirtualFileName(VirtualFile root)
   {
      if (root == null)
         throw new IllegalArgumentException("Null root");
      try
      {
         return root.toURI().toString();
      }
      catch (Exception e)
      {
         return root.getName();
      }
   }

   /**
    * For serialization
    */
   public AbstractVFSDeployment()
   {
   }
   
   /**
    * Create a new VFSDeployment.
    * 
    * @param root the root
    * @throws IllegalArgumentException for a null root
    */
   public AbstractVFSDeployment(VirtualFile root)
   {
      super(safeVirtualFileName(root));
      this.root = root;
   }
   
   public VirtualFile getRoot()
   {
      return root;
   }

   @Override
   public String getSimpleName()
   {
      return root.getName();
   }

   
   @Override
   public String toString()
   {
      return "AbstractVFSDeployment("+getSimpleName()+")";
   }

   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
   {
      super.readExternal(in);
      root = (VirtualFile) in.readObject();
   }

   /**
    * @serialData root from {@link #getRoot()}
    * @param out the output
    * @throws IOException for any error
    */
   public void writeExternal(ObjectOutput out) throws IOException
   {
      super.writeExternal(out);
      out.writeObject(root);
   }
}
