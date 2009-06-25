/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.deployers.spi.deployer;

/**
 * The standard deployment stages.
 * 
 * @author <a href="adrian@jboss.org">Adrian Brock</a>
 * @author <a href="ropalka@redhat.com">Richard Opalka</a>
 * @version $Revision: 1.1 $
 */
public final class DeploymentStages
{
   
   /**
    * Forbidden constructor.
    */
   private DeploymentStages()
   {
      super();
   }

   /** The not installed stage - nothing is done here */
   public static final DeploymentStage NOT_INSTALLED = new DeploymentStage("Not Installed");

   /** The parse stage - where metadata is read */
   public static final DeploymentStage PARSE = new DeploymentStage("Parse");

   /** The post parse stage - where metadata can be fixed up */
   public static final DeploymentStage POST_PARSE = new DeploymentStage("PostParse");

   /** The pre describe stage - where default dependencies metadata can be created */
   public static final DeploymentStage PRE_DESCRIBE = new DeploymentStage("PreDescribe");

   /** The describe stage - where dependencies are established */
   public static final DeploymentStage DESCRIBE = new DeploymentStage("Describe");

   /** The classloader stage - where classloaders are created */
   public static final DeploymentStage CLASSLOADER = new DeploymentStage("ClassLoader");

   /** The post classloader stage - e.g. aop */
   public static final DeploymentStage POST_CLASSLOADER = new DeploymentStage("PostClassLoader");

   /** The pre real stage - where before real deployments are done */
   public static final DeploymentStage PRE_REAL = new DeploymentStage("PreReal");

   /** The real stage - where real deployment processing is done */
   public static final DeploymentStage REAL = new DeploymentStage("Real");

   /** The installed stage - could be used to provide valve in future? */
   public static final DeploymentStage INSTALLED = new DeploymentStage("Installed");

   /**
    * Turn deployment stages into immutable objects.
    */
   static
   {
      int index = 1;
      DeploymentStages.NOT_INSTALLED.initialize( null, PARSE, index++ );
      DeploymentStages.PARSE.initialize( NOT_INSTALLED, POST_PARSE, index++ );
      DeploymentStages.POST_PARSE.initialize( PARSE, PRE_DESCRIBE, index++ );
      DeploymentStages.PRE_DESCRIBE.initialize( POST_PARSE, DESCRIBE, index++ );
      DeploymentStages.DESCRIBE.initialize( PRE_DESCRIBE, CLASSLOADER, index++ );
      DeploymentStages.CLASSLOADER.initialize( DESCRIBE, POST_CLASSLOADER, index++ );
      DeploymentStages.POST_CLASSLOADER.initialize( CLASSLOADER, PRE_REAL, index++ );
      DeploymentStages.PRE_REAL.initialize( POST_CLASSLOADER, REAL, index++ );
      DeploymentStages.REAL.initialize( PRE_REAL, INSTALLED, index++ );
      DeploymentStages.INSTALLED.initialize( REAL, null, index++ );
   }

   /**
    * Returns DeploymentStage instance associated with <b>stageAsString</b> stage name.
    * 
    * @param stageAsString name of the stage
    * @return associated DeploymentStage
    */
   public static final DeploymentStage valueOf( final String stageAsString )
   {
      if ( null == stageAsString )
      {
         throw new NullPointerException( "stage" );
      }
      
      DeploymentStage currentStage = DeploymentStages.NOT_INSTALLED;
      while ( currentStage != null )
      {
         if ( currentStage.getName().equals( stageAsString ) )
         {
            return currentStage;
         }
         else
         {
            currentStage = currentStage.getBefore();
         }
      }

      throw new IllegalArgumentException( "Uknown stage: " + stageAsString );
   }
   
}
