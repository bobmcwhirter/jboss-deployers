/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.deployers.spi.deployer;

/**
 * Standard (known) deployment types found in the DeploymentUnit types set.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision:$
 */
public interface StandardDeployerTypes
{
   /** JavaEE enterprise application archive */
   String EAR = "ear";
   
   /** JavaEE client application archive */
   String CAR = "car";
   
   /** JavaEE ejb generic archive */
   String EJB = "ejb";
   
   /** JavaEE ejb 2.1 and earlier archive */
   String EJB2x = "ejb2x";
   
   /** JavaEE ejb 3x archive */
   String EJB3x = "ejb3x";
   
   /** JavaEE JPA archive */
   String JPA = "jpa";
   
   /** JavaEE resource adaptor archive */
   String RAR = "rar";
   
   /** JBoss MC beans archive */
   String MCBEANS = "beans";
   
   /** JBoss service archive */
   String SAR = "sar";
   
   /** JBoss hibernate archive */
   String HAR = "har";
   
   /** JBoss aop archive */
   String AOP = "aop";
   
   /** Spring archive */
   String SPRING = "spring";
   
   /** An OSGi bundle */
   String OSGI_BUNDLE = "osgi";

   /** The deployment type used if a deployer does not specify anything */
   String UNSPECIFIED_TYPE = "unspecified";
}
