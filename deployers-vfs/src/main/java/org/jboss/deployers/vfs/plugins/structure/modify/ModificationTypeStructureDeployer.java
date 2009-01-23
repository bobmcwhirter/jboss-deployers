/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.deployers.vfs.plugins.structure.modify;

import java.util.List;
import java.util.ArrayList;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.vfs.spi.structure.StructureContext;
import org.jboss.deployers.vfs.spi.structure.StructureDeployer;

/**
 * Determine if we need some modification.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class ModificationTypeStructureDeployer implements StructureDeployer
{
   private List<ModificationTypeMatcher> matchers;

   public boolean determineStructure(StructureContext context) throws DeploymentException
   {
      if (matchers != null && matchers.isEmpty() == false)
      {
         for (ModificationTypeMatcher matcher : matchers)
         {
            if (matcher.determineModification(context))
            {
               break;
            }
         }
      }
      return false;
   }

   /**
    * Set modification type matchers.
    *
    * @param matchers the modification type matchers.
    */
   public void setMatchers(List<ModificationTypeMatcher> matchers)
   {
      this.matchers = matchers;
   }

   /**
    * Add modification type matcher.
    *
    * @param matcher the modification type matcher
    */
   public void addMatcher(ModificationTypeMatcher matcher)
   {
      if (matchers == null)
         matchers = new ArrayList<ModificationTypeMatcher>();

      matchers.add(matcher);
   }

   /**
    * Remove modification type matcher.
    *
    * @param matcher the modification type matcher
    */
   public void removeMatcher(ModificationTypeMatcher matcher)
   {
      if (matchers != null)
      {
         matchers.remove(matcher);
      }
   }

   public boolean isSupportsCandidateAnnotations()
   {
      return false;
   }

   public int getRelativeOrder()
   {
      return 1;
   }

   public void setRelativeOrder(int order)
   {
   }
}
