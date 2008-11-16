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
package org.jboss.deployers.plugins.annotations;

import java.util.AbstractList;

/**
 * Put elements directly into env
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
class EnvPutList extends AbstractList<CommitElement>
{
   private DefaultAnnotationEnvironment env;

   EnvPutList(DefaultAnnotationEnvironment env)
   {
      if (env == null)
         throw new IllegalArgumentException("Null env.");
      this.env = env;
   }

   public boolean add(CommitElement ce)
   {
      env.putAnnotation(ce.getAnnotation(), ce.getType(), ce.getClassName(), ce.getSignature());
      return true;
   }

   public CommitElement get(int index)
   {
      throw new UnsupportedOperationException("Should not be invoked.");
   }

   public int size()
   {
      return 0;
   }
}