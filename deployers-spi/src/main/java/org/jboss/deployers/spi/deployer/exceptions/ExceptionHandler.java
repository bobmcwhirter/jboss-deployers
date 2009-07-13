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
package org.jboss.deployers.spi.deployer.exceptions;

import org.jboss.dependency.spi.ControllerContext;

/**
 * Deployment exception handler.
 *
 * @param <T> exact exception type
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public interface ExceptionHandler<T extends Throwable>
{
   /**
    * Get the exception type.
    *
    * @return the exception type
    */
   Class<T> getExceptionType();

   /**
    * Do we match exact exception type.
    *
    * @return true if we only match T, or false if any super type as well.
    */
   boolean matchExactExceptionType();

   /**
    * Handle exception.
    *
    * @param exception the exception to handle
    * @param context the context the caused the exception
    */
   void handleException(T exception, ControllerContext context);
}
