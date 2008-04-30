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

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;

import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtMember;
import javassist.NotFoundException;
import org.jboss.classloading.spi.visitor.ClassFilter;
import org.jboss.classloading.spi.visitor.ResourceContext;
import org.jboss.classloading.spi.visitor.ResourceFilter;
import org.jboss.classloading.spi.visitor.ResourceVisitor;
import org.jboss.deployers.spi.annotations.AnnotationEnvironment;
import org.jboss.logging.Logger;
import org.jboss.metadata.spi.signature.Signature;

/**
 * Generic annotation scanner deployer.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class GenericAnnotationResourceVisitor implements ResourceVisitor
{
   private static final Logger log = Logger.getLogger(GenericAnnotationResourceVisitor.class);

   private ClassPool pool;
   private boolean forceAnnotations;
   private DefaultAnnotationEnvironment env;

   public GenericAnnotationResourceVisitor(ClassPool pool, ClassLoader classLoader)
   {
      if (pool == null)
         throw new IllegalArgumentException("Null pool");
      if (classLoader == null)
         throw new IllegalArgumentException("Null classloader");

      this.pool = pool;
      this.env = new DefaultAnnotationEnvironment(classLoader);
   }

   public ResourceFilter getFilter()
   {
      return ClassFilter.INSTANCE;
   }

   public void visit(ResourceContext resource)
   {
      try
      {
         CtClass ctClass = pool.makeClass(resource.getInputStream());
         try
         {
            handleCtClass(ctClass, resource);
         }
         finally
         {
            ctClass.detach();               
         }
      }
      catch (ClassNotFoundException e)
      {
         if (forceAnnotations)
            throw new RuntimeException(e);

         logThrowable(resource, e);
      }
      catch (Throwable t)
      {
         logThrowable(resource, t);
      }
   }

   /**
    * Log throwable.
    *
    * @param resource the resource we're visiting
    * @param t the throwable
    */
   protected void logThrowable(ResourceContext resource, Throwable t)
   {
      if (log.isTraceEnabled())
         log.trace("Exception reading resource: " + resource.getResourceName(), t);
   }

   /**
    * Handle CtClass for annotations.
    *
    * @param ctClass the ct class instance
    * @param resource the resource we're visiting
    * @throws ClassNotFoundException for any annotations lookup problems
    * @throws NotFoundException for any annotations lookup problems
    */
   protected void handleCtClass(CtClass ctClass, ResourceContext resource) throws ClassNotFoundException, NotFoundException
   {
      Object[] annotations = forceAnnotations ? ctClass.getAnnotations() : ctClass.getAvailableAnnotations();
      handleAnnotations(ElementType.TYPE, null, annotations, resource);
      handleCtMembers(ElementType.CONSTRUCTOR, ctClass.getDeclaredConstructors(), resource);
      handleCtMembers(ElementType.METHOD, ctClass.getDeclaredMethods(), resource);
      handleCtMembers(ElementType.FIELD, ctClass.getDeclaredFields(), resource);

      // interfaces
      CtClass[] interfaces = ctClass.getInterfaces();
      if (interfaces != null && interfaces.length > 0)
      {
         for (CtClass intf : interfaces)
            handleCtClass(intf, resource);
      }

      // super class
      ctClass = ctClass.getSuperclass();
      if (ctClass != null)
         handleCtClass(ctClass, resource);
   }

   /**
    * Handle CtMembers for annotations.
    *
    * @param type where we found the annotations
    * @param members the ct member instances
    * @param resource the resource we're visiting
    * @throws ClassNotFoundException for any annotations lookup problems
    */
   protected void handleCtMembers(ElementType type, CtMember[] members, ResourceContext resource) throws ClassNotFoundException
   {
      if (members != null && members.length > 0)
      {
         for (CtMember member : members)
         {
            Object[] annotations = forceAnnotations ? member.getAnnotations() : member.getAvailableAnnotations();
            handleAnnotations(type, member, annotations, resource);
            if (member instanceof CtBehavior)
            {
               CtBehavior behavior = (CtBehavior)member;
               Object[][] paramAnnotations = forceAnnotations ? behavior.getParameterAnnotations() : behavior.getAvailableParameterAnnotations();
               for (Object[] paramAnn : paramAnnotations)
                  handleAnnotations(ElementType.PARAMETER, member, paramAnn, resource);
            }
         }
      }
   }

   /**
    * Handle annotations.
    *
    * @param type where we found the annotations
    * @param member the ct member
    * @param annotations the actual annotations
    * @param resource the resource we're visiting
    */
   protected void handleAnnotations(ElementType type, CtMember member, Object[] annotations, ResourceContext resource)
   {
      if (annotations != null && annotations.length > 0)
      {
         for (Object annObject : annotations)
         {
            Annotation annotation = Annotation.class.cast(annObject);
            Signature signature = null;
/*
            if (member != null)
               signature = JavassistSignatureFactory.getSignature(member);
*/
            env.putAnnotation(annotation.annotationType(), type, resource.getClassName(), signature);
         }
      }
   }

   /**
    * Should we force all annotations to be available.
    *
    * @param forceAnnotations the force annotations flag
    */
   public void setForceAnnotations(boolean forceAnnotations)
   {
      this.forceAnnotations = forceAnnotations;
   }

   AnnotationEnvironment getEnv()
   {
      return env;
   }
}