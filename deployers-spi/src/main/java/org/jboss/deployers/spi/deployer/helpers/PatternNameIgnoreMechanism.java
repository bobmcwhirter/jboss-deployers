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
package org.jboss.deployers.spi.deployer.helpers;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.jboss.deployers.spi.deployer.matchers.NameIgnoreMechanism;
import org.jboss.deployers.structure.spi.DeploymentUnit;

/**
 * Ignore a pattern.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class PatternNameIgnoreMechanism implements NameIgnoreMechanism
{
   private Pattern ignored;
   private boolean match;

   public PatternNameIgnoreMechanism(String regexp)
   {
      if (regexp == null)
         throw new IllegalArgumentException("Null regexp");

      ignored = Pattern.compile(regexp);
   }

   public boolean ignore(DeploymentUnit unit, String name)
   {
      Matcher matcher = ignored.matcher(name);
      return (match) ? matcher.matches() : matcher.find();
   }

   /**
    * Do we use match.
    * by default find is used.
    *
    * @param match the match flag
    */
   public void setMatch(boolean match)
   {
      this.match = match;
   }
}