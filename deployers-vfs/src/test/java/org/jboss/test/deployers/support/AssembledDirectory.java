/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, JBoss Inc., and individual contributors as indicated
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
package org.jboss.test.deployers.support;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;
import java.util.regex.Pattern;

import org.jboss.classloader.plugins.ClassLoaderUtils;
import org.jboss.deployers.vfs.plugins.structure.jar.JARStructure;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;
import org.jboss.vfs.VirtualFileAssembly;
import org.jboss.vfs.VirtualFileFilter;
import org.jboss.vfs.VisitorAttributes;
import org.jboss.vfs.util.FilterVirtualFileVisitor;
import org.jboss.vfs.util.SuffixesExcludeFilter;

/**
 * Bridge class used to help migrate existing tests to use VirtualFileAssembly.
 *  
 * @author <a href="jbailey@redhat.com">John Bailey</a>
 *
 */
public class AssembledDirectory
{
   private final VirtualFileAssembly assembly;
   private final Class<?> testClass;
   
   public AssembledDirectory(Class<?> testClass, VirtualFileAssembly assembly)
   {
      this.testClass = testClass;
      this.assembly = assembly;
   }
   
   public AssembledDirectory add(VirtualFile virtualFile)
   {
      assembly.add(virtualFile);
      return this;
   }

   public AssembledDirectory addPackage(Class<?> reference) throws Exception
   {
      return addPackage("", reference);
   }
   
   public AssembledDirectory addPackage(String path, Class<?> reference) throws Exception
   {
      String packagePath = ClassLoaderUtils.packageNameToPath(reference.getName());
      return addResources(path + "/" + packagePath, reference, new String[] {packagePath + "/*.class"}, new String[0]);
   }

   public AssembledDirectory addResources(String path, Class<?> reference, final String[] includes, final String[] excludes)
   {
      return addResources(path, reference, includes, excludes, reference.getClassLoader());
   }

   public AssembledDirectory addResources(String path, Class<?> reference, final String[] includes, final String[] excludes, ClassLoader loader)
   {
      String resource = reference.getName().replace('.', '/') + ".class";
      URL url = loader.getResource(resource);
      if (url == null)
         throw new RuntimeException("Could not find baseResource: " + resource);

      String urlString = url.toString();
      int idx = urlString.lastIndexOf(resource);
      urlString = urlString.substring(0, idx);

      try
      {
         url = new URL(urlString);
         final VirtualFile parent = VFS.getChild(url);

         VisitorAttributes va = new VisitorAttributes();
         va.setLeavesOnly(true);
         va.setRecurseFilter(new SuffixesExcludeFilter(JARStructure.DEFAULT_JAR_SUFFIXES));

         VirtualFileFilter filter = new VirtualFileFilter()
         {
            public boolean accepts(VirtualFile file)
            {
               boolean matched = false;
               String path = file.getPathNameRelativeTo(parent);
               for (String include : includes)
               {
                  if (antMatch(path, include))
                  {
                     matched = true;
                     break;
                  }
               }
               if (matched == false)
                  return false;
               if (excludes != null)
               {
                  for (String exclude : excludes)
                  {
                     if (antMatch(path, exclude))
                        return false;
                  }
               }
               return true;
            }
         };

         FilterVirtualFileVisitor visitor = new FilterVirtualFileVisitor(filter, va);
         parent.visit(visitor);
         List<VirtualFile> files = visitor.getMatched();
         for (VirtualFile vf : files)
         {
            assembly.add(path + "/" + vf.getName(), vf);
         }
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }
      catch (URISyntaxException e)
      {
         throw new RuntimeException(e);
      }
      return this;
   }

   public AssembledDirectory addPath(String existingPath) throws Exception
   {
      return addPath("", existingPath);
   }
   
   public AssembledDirectory addPath(VirtualFile existingPath) throws Exception 
   {
      return addPath("", existingPath);
   }
   
   public AssembledDirectory addPath(String assemblyPath, String existingPath) throws Exception 
   {
      return addPath(assemblyPath, getVirtualFile(existingPath));
   }
   
   public AssembledDirectory addPath(String assemblyPath, VirtualFile existingPath) throws Exception
   {
      SuffixesExcludeFilter noJars = new SuffixesExcludeFilter(JARStructure.DEFAULT_JAR_SUFFIXES);
      FilterVirtualFileVisitor visitor = new FilterVirtualFileVisitor(noJars);
      existingPath.visit(visitor);
      for (VirtualFile match : visitor.getMatched())
      {
         assembly.add(assemblyPath + "/" + match.getName(), match);
      }
      return this;
   }

   /**
    * Create a regular expression pattern from an Ant file matching pattern
    *
    * @param matcher the matcher pattern
    * @return the pattern instance
    */
   private Pattern getPattern(String matcher)
   {
      if (matcher == null)
         throw new IllegalArgumentException("Null matcher");

      matcher = matcher.replace(".", "\\.");
      matcher = matcher.replace("*", ".*");
      matcher = matcher.replace("?", ".{1}");
      return Pattern.compile(matcher);
   }

   /**
    * Determine whether a given file path matches an Ant pattern.
    *
    * @param path the path
    * @param expression the expression
    * @return true if we match
    */
   private boolean antMatch(String path, String expression)
   {
      if (path == null)
         throw new IllegalArgumentException("Null path");
      if (expression == null)
         throw new IllegalArgumentException("Null expression");
      if (path.startsWith("/"))
         path = path.substring(1);
      if (expression.endsWith("/"))
         expression += "**";
      String[] paths = path.split("/");
      String[] expressions = expression.split("/");

      int x = 0, p;
      Pattern pattern = getPattern(expressions[0]);

      for (p = 0; p < paths.length && x < expressions.length; p++)
      {
         if (expressions[x].equals("**"))
         {
            do
            {
               x++;
            }
            while (x < expressions.length && expressions[x].equals("**"));
            if (x == expressions.length)
               return true; // "**" with nothing after it
            pattern = getPattern(expressions[x]);
         }
         String element = paths[p];
         if (pattern.matcher(element).matches())
         {
            x++;
            if (x < expressions.length)
            {
               pattern = getPattern(expressions[x]);
            }
         }
         else if (!(x > 0 && expressions[x - 1].equals("**"))) // our previous isn't "**"
         {
            return false;
         }
      }
      if (p < paths.length)
         return false;
      if (x < expressions.length)
         return false;
      return true;
   }
   
   protected VirtualFile getVirtualFile(String path) throws URISyntaxException 
   {
      URL resource = getResource(path);
      if(resource != null)
         return VFS.getChild(resource);
      return null;
   }

   
   private URL getResource(final String name)
   {
      PrivilegedAction<URL> action = new PrivilegedAction<URL>()
      {
         public URL run()
         {
            return testClass.getResource(name);
         }
      };
      return AccessController.doPrivileged(action);
   }
   
}
