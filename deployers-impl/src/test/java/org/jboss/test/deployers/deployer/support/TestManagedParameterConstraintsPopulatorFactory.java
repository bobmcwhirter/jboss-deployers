/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.deployers.deployer.support;

import org.jboss.managed.api.Fields;
import org.jboss.managed.spi.factory.ManagedParameterConstraintsPopulator;
import org.jboss.managed.spi.factory.ManagedParameterConstraintsPopulatorFactory;
import org.jboss.metatype.api.values.SimpleValueSupport;
import org.jboss.reflect.spi.ParameterInfo;

/**
 * Example ManagedParameterConstraintsPopulatorFactory
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public class TestManagedParameterConstraintsPopulatorFactory implements
      ManagedParameterConstraintsPopulatorFactory
{
   public ManagedParameterConstraintsPopulator newInstance()
   {
      return new TestManagedParameterConstraintsPopulator();
   }

   static class TestManagedParameterConstraintsPopulator
      implements ManagedParameterConstraintsPopulator
   {
      public void populateManagedParameter(String methodName, ParameterInfo info, Fields fields)
      {
         if (methodName.equals("constrainedIntx10"))
            populateconstrainedIntx10Arg(fields);
      }

      void populateconstrainedIntx10Arg(Fields fields)
      {
         fields.setField(Fields.MINIMUM_VALUE, SimpleValueSupport.wrap(0));
         fields.setField(Fields.MAXIMUM_VALUE, SimpleValueSupport.wrap(100));
      }
   }
}
