/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.deployers.vfs.spi.structure.helpers;

import java.security.AccessController;
import java.security.PrivilegedAction;

import org.jboss.vfs.VirtualFile;

/**
 * @author Scott.Stark@jboss.org
 * @version $Revision: 60921 $
 */
public class SecurityActions
{
   private SecurityActions()
   {
   }

   /**
    * Actions for File access 
    */
   interface FileActions 
   { 
      FileActions PRIVILEGED = new FileActions() 
      { 
         public boolean isLeaf(final VirtualFile f)
         {
            return AccessController.doPrivileged(new PrivilegedAction<Boolean>()
            {
               public Boolean run()
               {
                  return Boolean.valueOf(f.isFile());
               }
            }).booleanValue();
         }
      }; 

      FileActions NON_PRIVILEGED = new FileActions() 
      {
         public boolean isLeaf(VirtualFile f)
         {
            return f.isFile();
         }
         
      };

      boolean isLeaf(VirtualFile f);
   } 

   static boolean isLeaf(VirtualFile f)
   {
      SecurityManager sm = System.getSecurityManager();
      if( sm != null )
         return FileActions.PRIVILEGED.isLeaf(f);
      else
         return FileActions.NON_PRIVILEGED.isLeaf(f);
   }
}
