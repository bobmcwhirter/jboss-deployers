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
package org.jboss.test.deployers.vfs.deployer.facelets.support;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Mock of Facelets's Classpath class.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
@SuppressWarnings("unchecked")
public final class Classpath
{
   public static URL[] search(ClassLoader cl, String prefix, String suffix) throws IOException
   {
      Enumeration[] e = new Enumeration[]{
            cl.getResources(prefix),
            cl.getResources(prefix + "MANIFEST.MF")
      };
      Set all = new LinkedHashSet();
      URL url;
      URLConnection conn;
      JarFile jarFile;
      for (int i = 0, s = e.length; i < s; ++i)
      {
         while (e[i].hasMoreElements())
         {
            url = (URL)e[i].nextElement();
            conn = url.openConnection();
            conn.setUseCaches(false);
            conn.setDefaultUseCaches(false);
            if (conn instanceof JarURLConnection)
            {
               jarFile = ((JarURLConnection)conn).getJarFile();
            }
            else
            {
               jarFile = getAlternativeJarFile(url);
            }
            if (jarFile != null)
            {
               searchJar(cl, all, jarFile, prefix, suffix);
            }
            else
            {
               boolean searchDone = searchDir(all, new File(URLDecoder.decode(url.getFile(), "UTF-8")), suffix);
               if (searchDone == false)
               {
                  searchFromURL(all, prefix, suffix, url);
               }
            }
         }
      }
      return (URL[])all.toArray(new URL[all.size()]);
   }

   private static boolean searchDir(Set result, File file, String suffix) throws IOException
   {
      if (file.exists() && file.isDirectory())
      {
         File[] fc = file.listFiles();
         String path;
         for (int i = 0; i < fc.length; i++)
         {
            path = fc[i].getAbsolutePath();
            if (fc[i].isDirectory())
            {
               searchDir(result, fc[i], suffix);
            }
            else if (path.endsWith(suffix))
            {
               result.add(fc[i].toURL());
            }
         }
         return true;
      }
      return false;
   }

   /**
    * Search from URL.
    * Fall back on prefix tokens if
    * not able to read from original url param.
    *
    * @param result the result urls
    * @param prefix the current prefix
    * @param suffix the suffix to match
    * @param url the current url to start search
    * @throws IOException for any error
    */
   private static void searchFromURL(Set result, String prefix, String suffix, URL url) throws IOException
   {
      boolean done = false;
      InputStream is = getInputStream(url);
      if (is != null)
      {
         try
         {
            ZipInputStream zis;
            if (is instanceof ZipInputStream)
               zis = (ZipInputStream)is;
            else
               zis = new ZipInputStream(is);
            ZipEntry entry = zis.getNextEntry();
            String urlString = url.toExternalForm();
            while (entry != null)
            {
               String entryName = entry.getName();
               if (entryName.endsWith(suffix))
               {
                  result.add(new URL(urlString + entryName));
               }
               entry = zis.getNextEntry();
            }
            done = true;
         }
         catch (Exception ignore)
         {
         }
      }
      if (done == false && prefix.length() > 0)
      {
         String urlString = url.toExternalForm();
         String[] split = prefix.split("/");
         prefix = join(split, true);
         String end = join(split, false);
         int p = urlString.lastIndexOf(end);
         url = new URL(urlString.substring(0, p));
         searchFromURL(result, prefix, suffix, url);
      }
   }

   /**
    * Join tokens, exlude last if param equals true.
    *
    * @param tokens the tokens
    * @param excludeLast do we exclude last token
    * @return joined tokens
    */
   private static String join(String[] tokens, boolean excludeLast)
   {
      StringBuffer join = new StringBuffer();
      for (int i = 0; i < tokens.length - (excludeLast ? 1 : 0); i++)
         join.append(tokens[i]).append("/");
      return join.toString();
   }

   /**
    * Open input stream from url.
    * Ignore any errors.
    *
    * @param url the url to open
    * @return input stream or null if not possible
    */
   private static InputStream getInputStream(URL url)
   {
      try
      {
         return url.openStream();
      }
      catch (Throwable t)
      {
         return null;
      }
   }

   /**
    * For URLs to JARs that do not use JarURLConnection - allowed by
    * the servlet spec - attempt to produce a JarFile object all the same.
    * Known servlet engines that function like this include Weblogic
    * and OC4J.
    * This is not a full solution, since an unpacked WAR or EAR will not
    * have JAR "files" as such.
    */
   private static JarFile getAlternativeJarFile(URL url) throws IOException
   {
      String urlFile = url.getFile();
      // Trim off any suffix - which is prefixed by "!/" on Weblogic
      int separatorIndex = urlFile.indexOf("!/");

      // OK, didn't find that. Try the less safe "!", used on OC4J
      if (separatorIndex == -1)
      {
         separatorIndex = urlFile.indexOf('!');
      }

      if (separatorIndex != -1)
      {
         String jarFileUrl = urlFile.substring(0, separatorIndex);
         // And trim off any "file:" prefix.
         if (jarFileUrl.startsWith("file:"))
         {
            jarFileUrl = jarFileUrl.substring("file:".length());
         }
         return new JarFile(jarFileUrl);
      }
      return null;
   }

   private static void searchJar(ClassLoader cl, Set result, JarFile file,
                                 String prefix, String suffix) throws IOException
   {
      Enumeration e = file.entries();
      JarEntry entry;
      String name;
      while (e.hasMoreElements())
      {
         try
         {
            entry = (JarEntry)e.nextElement();
         }
         catch (Throwable t)
         {
            continue;
         }
         name = entry.getName();
         if (name.startsWith(prefix) && name.endsWith(suffix))
         {
            Enumeration e2 = cl.getResources(name);
            while (e2.hasMoreElements())
            {
               result.add(e2.nextElement());
				}
			}
		}
	}
}
