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
package org.jboss.test.deployers.exceptions.test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Test;
import org.jboss.deployers.client.spi.DeployerClient;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.plugins.attachments.AttachmentsImpl;
import org.jboss.deployers.plugins.deployers.DeployersImpl;
import org.jboss.deployers.spi.deployer.Deployers;
import org.jboss.test.deployers.AbstractDeployerTest;
import org.jboss.test.deployers.exceptions.support.AnySimpleExceptionNotificationListener;
import org.jboss.test.deployers.exceptions.support.ComplexException;
import org.jboss.test.deployers.exceptions.support.SimpleException;
import org.jboss.test.deployers.exceptions.support.SimpleExceptionDeployer;
import org.jboss.test.deployers.exceptions.support.SimpleExceptionNotificationListener;

/**
 * Simple exception handler test.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@SuppressWarnings({"ThrowableInstanceNeverThrown"})
public class SimpleExceptionNotificationListenerTestCase extends AbstractDeployerTest
{
   public SimpleExceptionNotificationListenerTestCase(String name)
   {
      super(name);
   }

   public static Test suite()
   {
      return suite(SimpleExceptionNotificationListenerTestCase.class);
   }

   @Override
   protected Deployers createDeployers()
   {
      Deployers deployers = super.createDeployers();
      ((DeployersImpl)deployers).addExceptionNotificationListener(new SimpleExceptionNotificationListener());
      ((DeployersImpl)deployers).addExceptionNotificationListener(new AnySimpleExceptionNotificationListener());
      return deployers;
   }

   protected void testExceptionHandling(Exception exception, Set<Object> expected) throws Throwable
   {
      Deployment deployment = createSimpleDeployment("Test");
      AttachmentsImpl attachments = new AttachmentsImpl();
      attachments.addAttachment(Exception.class, exception);
      deployment.setPredeterminedManagedObjects(attachments);

      DeployerClient main = createMainDeployer(new SimpleExceptionDeployer()); 

      SimpleException.failures.clear();
      try
      {
         assertDeploy(main, deployment);
         fail("Should not be here.");
      }
      catch (Throwable t)
      {
         assertEquals(expected, SimpleException.failures);         
      }
      finally
      {
         assertUndeploy(main, deployment);
      }
   }

   public void testExactMatch() throws Throwable
   {
      Exception exception = new ComplexException("Failure", null);
      testExceptionHandling(exception, Collections.<Object>singleton("Test_AnySimpleExceptionNotificationListener"));
   }

   public void testAnyMatch() throws Throwable
   {
      Exception exception = new SimpleException("Failure", null);
      Set<Object> expected = new HashSet<Object>(Arrays.asList("Test_SimpleExceptionNotificationListener", "Test_AnySimpleExceptionNotificationListener"));
      testExceptionHandling(exception, expected);
   }

   public void testNoMatch() throws Throwable
   {
      Exception exception = new IllegalArgumentException("Failed");
      testExceptionHandling(exception, Collections.<Object>emptySet());
   }

   public void testNested() throws Throwable
   {
      Exception exception = new IllegalArgumentException("Failed", new ComplexException("Nested", null));
      testExceptionHandling(exception, Collections.<Object>singleton("Test_AnySimpleExceptionNotificationListener"));
   }
}
