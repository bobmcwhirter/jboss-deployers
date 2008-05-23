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

import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.virtual.VirtualFile;
import org.jboss.xb.annotations.JBossXmlConstants;

/**
 * SchemaResolverDeployer.
 * 
 * @param <T> the expected type 
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @author Scott.Stark@jboss.org
 * @version $Revision 1.1 $
 */
public class SchemaResolverDeployer<T> extends JBossXBDeployer<T>
{
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
         namespace = findNamespace(getOutput());
         if (namespace == null || JBossXmlConstants.DEFAULT.equals(namespace))
            throw new IllegalArgumentException(
                  "RegisterWithJBossXB is enabled, but cannot find namespace on class or package: " + getOutput() +
                  ", perhaps missing @JBossXmlSchema or using default namespace attribute."
            );

         addClassBinding(namespace, getOutput());
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
         removeClassBinding(namespace);
      }
   }

   protected T parse(VFSDeploymentUnit unit, VirtualFile file, T root) throws Exception
   {
      if (file == null)
         throw new IllegalArgumentException("Null file");
      return parse(getOutput(), file);
   }
}
