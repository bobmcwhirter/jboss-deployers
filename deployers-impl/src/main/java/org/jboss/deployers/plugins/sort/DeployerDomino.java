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
package org.jboss.deployers.plugins.sort;

import java.util.Set;

import org.jboss.deployers.spi.deployer.Deployer;

/**
 * Representing deployer as a domino.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class DeployerDomino implements Domino<Set<String>>
{
   private Deployer deployer;
   private Dots<Set<String>> head;
   private Dots<Set<String>> tail;

   public DeployerDomino(Deployer deployer)
   {
      if (deployer == null)
         throw new IllegalArgumentException("Null deployer!");
      this.deployer = deployer;
      this.head = new SetDots<String>(deployer.getInputs());
      this.tail = new SetDots<String>(deployer.getOutputs());
   }

   public Deployer getDeployer()
   {
      return deployer;
   }

   public Dots<Set<String>> getHead()
   {
      return head;
   }

   public Dots<Set<String>> getTail()
   {
      return tail;
   }

   public int getRelativeOrder()
   {
      return deployer.getRelativeOrder();
   }

   public void setRelativeOrder(int order)
   {
      deployer.setRelativeOrder(order);
   }

   public String getInfo()
   {
      StringBuilder builder = new StringBuilder();
      builder.append(deployer);
      builder.append("{inputs=").append(deployer.getInputs());
      builder.append(" outputs=").append(deployer.getOutputs());
      builder.append("}\n");
      return builder.toString();
   }

   @Override
   public String toString()
   {
      return deployer + ", " + head + "|" + tail;
   }
}
