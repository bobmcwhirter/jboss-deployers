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
package org.jboss.deployers.vfs.spi.structure.helpers;

import org.jboss.deployers.vfs.spi.structure.StructureContext;
import org.jboss.virtual.VirtualFileFilter;
import org.jboss.virtual.VirtualFileVisitor;
import org.jboss.virtual.VisitorAttributes;

/**
 * DefaultCandidateStructureVisitorFactory.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class DefaultCandidateStructureVisitorFactory implements CandidateStructureVisitorFactory
{
   /** An instance of the factory */
   public static final DefaultCandidateStructureVisitorFactory INSTANCE = new DefaultCandidateStructureVisitorFactory();

   /** The filter */
   private VirtualFileFilter filter;
   
   /**
    * Get the filter.
    * 
    * @return the filter.
    */
   public VirtualFileFilter getFilter()
   {
      return filter;
   }

   /**
    * Set the filter.
    * 
    * @param filter the filter.
    */
   public void setFilter(VirtualFileFilter filter)
   {
      this.filter = filter;
   }

   public VirtualFileVisitor createVisitor(StructureContext context, VisitorAttributes attributes) throws Exception
   {
      AbstractCandidateStructureVisitor visitor = new AbstractCandidateStructureVisitor(context, attributes);
      if (filter != null)
         visitor.setFilter(filter);
      return visitor;
   }
}
