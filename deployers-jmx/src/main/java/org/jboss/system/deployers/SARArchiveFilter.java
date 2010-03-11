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
package org.jboss.system.deployers;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.jboss.vfs.VirtualFile;
import org.jboss.vfs.VirtualFileFilter;

/**
 * SARArchiveFilter.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @author <a href="ales.justin@jboss.org">Ales Justin</a>
 * @version $Revision$
 */
public class SARArchiveFilter implements VirtualFileFilter
{
   /** The regexp= marker const */
   private static final String REGEXP = "regexp=";

   /** The regexp pattern */
   private Pattern regexp;

   /** The patterns */
   private Set<String> patterns;
   
   /** Whether there is the accept all wildcard */
   private boolean allowAll = false;
   
   /**
    * Create a new SARArchiveFilter.
    * 
    * @param patternsString the pattern string
    * @throws IllegalArgumentException for a null string
    */
   public SARArchiveFilter(String patternsString)
   {
      if (patternsString == null)
         throw new IllegalArgumentException("Null patternsString");

      if (patternsString.startsWith(REGEXP))
      {
         regexp = Pattern.compile(patternsString.substring(REGEXP.length()));
      }
      else
      {
         StringTokenizer tokens = new StringTokenizer (patternsString, ",");
         patterns = new HashSet<String>(tokens.countTokens());
         while (tokens.hasMoreTokens ())
         {
            String token = tokens.nextToken();
            patterns.add(token.trim());
         }
         allowAll = patterns.contains("*");
      }
   }
   
   public boolean accepts(VirtualFile file)
   {
      if (allowAll)
         return true;

      String fileName = file.getName();
      if (regexp != null)
         return regexp.matcher(fileName).matches();
      else
         return patterns.contains(file.getName());
   }
}
