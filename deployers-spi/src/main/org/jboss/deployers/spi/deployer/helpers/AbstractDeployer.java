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
package org.jboss.deployers.spi.deployer.helpers;

import java.util.HashSet;
import java.util.Set;

import org.jboss.deployers.spi.deployer.Deployer;
import org.jboss.deployers.spi.deployer.DeploymentStage;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.logging.Logger;

/**
 * AbstractDeployer.
 * 
 * @author <a href="adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public abstract class AbstractDeployer implements Deployer
{
   /** The log */
   protected Logger log = Logger.getLogger(getClass());
   
   /** The relative order */
   private int relativeOrder = 0;
   
   /** The deployment stage */
   private DeploymentStage stage = DeploymentStages.REAL;
   
   /** Top level only */
   private boolean topLevelOnly;
   
   /** Components only */
   private boolean componentsOnly;
   
   /** Want components */
   private boolean wantComponents;
   
   /** All inputs */
   private boolean allInputs;
   
   /** The input type */
   private Class<?> input;
   
   /** The output type */
   private Class<?> output;
   
   /** The input name */
   private Set<String> inputs;
   
   /** The output name */
   private Set<String> outputs;
   
   /** Whether to process parents first */
   private boolean parentFirst = true;
   
   public int getRelativeOrder()
   {
      return relativeOrder;
   }

   public void setRelativeOrder(int relativeOrder)
   {
      this.relativeOrder = relativeOrder;
   }

   public DeploymentStage getStage()
   {
      return stage;
   }

   /**
    * Set the stage.
    * 
    * @param stage the stage.
    * @throws IllegalArgumentException for a null stage
    */
   public void setStage(DeploymentStage stage)
   {
      if (stage == null)
         throw new IllegalArgumentException("Null stage");
      this.stage = stage;
   }

   public boolean isAllInputs()
   {
      return allInputs;
   }

   /**
    * Set the allInputs.
    * 
    * @param allInputs the allInputs.
    */
   public void setAllInputs(boolean allInputs)
   {
      this.allInputs = allInputs;
   }

   public boolean isComponentsOnly()
   {
      return componentsOnly;
   }

   /**
    * Set the components only.
    * 
    * @param componentsOnly the componentsOnly.
    */
   public void setComponentsOnly(boolean componentsOnly)
   {
      this.componentsOnly = componentsOnly;
      setWantComponents(true);
   }

   public boolean isWantComponents()
   {
      return wantComponents;
   }

   /**
    * Set the want components.
    * 
    * @param wantComponents the want components.
    */
   public void setWantComponents(boolean wantComponents)
   {
      this.wantComponents = wantComponents;
   }

   public boolean isTopLevelOnly()
   {
      return topLevelOnly;
   }

   /**
    * Set the top level only.
    * 
    * @param topLevelOnly the top level only.
    */
   public void setTopLevelOnly(boolean topLevelOnly)
   {
      this.topLevelOnly = topLevelOnly;
   }

   public Class<?> getInput()
   {
      return input;
   }

   /**
    * Set the input
    * 
    * @param input the input
    */
   @SuppressWarnings("unchecked")
   public void setInput(Class<?> input)
   {
      addInput(input);
      this.input = input;
   }

   public Class<?> getOutput()
   {
      return output;
   }

   /**
    * Set the output
    * 
    * @param output the outputs
    */
   @SuppressWarnings("unchecked")
   public void setOutput(Class<?> output)
   {
      addOutput(output);
      this.output = output;
   }

   public Set<String> getInputs()
   {
      return inputs;
   }

   /**
    * Set the inputs.
    * 
    * @param inputs the inputs.
    */
   public void setInputs(Set<String> inputs)
   {
      this.inputs = inputs;
   }

   /**
    * Set the inputs.
    * 
    * @param inputs the inputs.
    */
   @SuppressWarnings("unchecked")
   public void setInputs(String... inputs)
   {
      if (inputs == null)
      {
         setInputs((Set) null);
         return;
      }
      Set<String> temp = new HashSet<String>(inputs.length);
      for (String input : inputs)
      {
         if (input == null)
            throw new IllegalArgumentException("Null input");
         temp.add(input);
      }
      setInputs(temp);
   }

   /**
    * Set the inputs.
    * 
    * @param inputs the inputs.
    */
   @SuppressWarnings("unchecked")
   public void setInputs(Class<?>... inputs)
   {
      if (inputs == null)
      {
         setInputs((Set) null);
         return;
      }
      Set<String> temp = new HashSet<String>(inputs.length);
      for (Class<?> input : inputs)
      {
         if (input == null)
            throw new IllegalArgumentException("Null input");
         temp.add(input.getName());
      }
      setInputs(temp);
   }

   /**
    * Add an input
    * 
    * @param input the input
    */
   public void addInput(String input)
   {
      if (input == null)
         throw new IllegalArgumentException("Null input");
      
      if (inputs == null)
         inputs = new HashSet<String>();
      
      inputs.add(input);
   }

   /**
    * Add an input
    * 
    * @param input the input
    */
   public void addInput(Class<?> input)
   {
      if (input == null)
         throw new IllegalArgumentException("Null input");
      
      addInput(input.getName());
   }
   
   public Set<String> getOutputs()
   {
      return outputs;
   }

   /**
    * Set the outputs.
    * 
    * @param outputs the outputs.
    */
   public void setOutputs(Set<String> outputs)
   {
      this.outputs = outputs;
   }

   /**
    * Set the outputs.
    * 
    * @param outputs the outputs.
    */
   @SuppressWarnings("unchecked")
   public void setOutputs(String... outputs)
   {
      if (outputs == null)
      {
         setOutputs((Set) null);
         return;
      }
      Set<String> temp = new HashSet<String>(outputs.length);
      for (String output : outputs)
      {
         if (output == null)
            throw new IllegalArgumentException("Null output");
         temp.add(output);
      }
      setOutputs(temp);
   }

   /**
    * Set the outputs.
    * 
    * @param outputs the outputs.
    */
   @SuppressWarnings("unchecked")
   public void setOutputs(Class<?>... outputs)
   {
      if (outputs == null)
      {
         setOutputs((Set) null);
         return;
      }
      Set<String> temp = new HashSet<String>(outputs.length);
      for (Class<?> output : outputs)
      {
         if (output == null)
            throw new IllegalArgumentException("Null output");
         temp.add(output.getName());
      }
      setOutputs(temp);
   }

   /**
    * Add an output
    * 
    * @param output the output
    */
   public void addOutput(String output)
   {
      if (output == null)
         throw new IllegalArgumentException("Null output");
      
      if (outputs == null)
         outputs = new HashSet<String>();
      
      outputs.add(output);
   }

   /**
    * Add an output
    * 
    * @param output the output
    */
   public void addOutput(Class<?> output)
   {
      if (output == null)
         throw new IllegalArgumentException("Null output");
      
      addOutput(output.getName());
   }

   public boolean isParentFirst()
   {
      return parentFirst;
   }

   /**
    * Set the parentFirst.
    * 
    * @param parentFirst the parentFirst.
    */
   public void setParentFirst(boolean parentFirst)
   {
      this.parentFirst = parentFirst;
   }

   @Deprecated
   public void setType(String type)
   {
      log.warn("Types should be set using Managed* api for " + this);
   }

   public void undeploy(DeploymentUnit unit)
   {
      // Nothing
   }
}
