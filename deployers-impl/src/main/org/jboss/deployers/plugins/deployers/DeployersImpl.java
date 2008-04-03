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
package org.jboss.deployers.plugins.deployers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jboss.dependency.spi.Controller;
import org.jboss.dependency.spi.ControllerContext;
import org.jboss.dependency.spi.ControllerContextActions;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.dependency.spi.DependencyInfo;
import org.jboss.dependency.spi.DependencyItem;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.client.spi.IncompleteDeploymentException;
import org.jboss.deployers.client.spi.IncompleteDeployments;
import org.jboss.deployers.client.spi.MissingDependency;
import org.jboss.deployers.plugins.sort.DeployerSorter;
import org.jboss.deployers.plugins.sort.DeployerSorterFactory;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.DeploymentState;
import org.jboss.deployers.spi.deployer.Deployer;
import org.jboss.deployers.spi.deployer.Deployers;
import org.jboss.deployers.spi.deployer.DeploymentStage;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.managed.ManagedObjectCreator;
import org.jboss.deployers.structure.spi.DeploymentContext;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.structure.spi.scope.ScopeBuilder;
import org.jboss.kernel.spi.dependency.KernelController;
import org.jboss.logging.Logger;
import org.jboss.managed.api.ManagedObject;
import org.jboss.metadata.spi.repository.MutableMetaDataRepository;

/**
 * DeployersImpl.
 *
 * @author <a href="adrian@jboss.org">Adrian Brock</a>
 * @author <a href="ales.justin@jboss.org">Ales Justin</a>
 * @version $Revision: 1.1 $
 */
public class DeployersImpl implements Deployers, ControllerContextActions
{
   /** The log */
   private static final Logger log = Logger.getLogger(DeployersImpl.class);
   
   /** Whether we are shutdown */
   private AtomicBoolean shutdown = new AtomicBoolean(false);
   
   /** The dependency state machine */
   private Controller controller;
   
   /** The repository */
   private MutableMetaDataRepository repository;
   
   /** The deployment stages by name */
   private Map<String, DeploymentStage> stages = new ConcurrentHashMap<String, DeploymentStage>();
   
   /** The deployers */
   private Set<DeployerWrapper> deployers = new HashSet<DeployerWrapper>();

   /** The deployers by stage and type */
   private Map<String, List<Deployer>> deployersByStage = new HashMap<String, List<Deployer>>();
   
   /** The scope builder */
   private ScopeBuilder scopeBuilder;

   /**
    * Create a new DeployersImpl.
    *
    * @param controller the controller
    * @throws IllegalArgumentException for a null controller
    */
   public DeployersImpl(Controller controller)
   {
      this(controller, null);
   }
   
   /**
    * Create a new DeployersImpl.
    * 
    * @param controller the controller
    * @param deployers the deployers
    * @throws IllegalArgumentException for a null controller
    */
   public DeployersImpl(Controller controller, Set<Deployer> deployers)
   {
      if (controller == null)
         throw new IllegalArgumentException("Null controller");
      this.controller = controller;
      
      // Add the standard stages
      addDeploymentStage(DeploymentStages.NOT_INSTALLED);
      addDeploymentStage(DeploymentStages.PARSE);
      addDeploymentStage(DeploymentStages.POST_PARSE);
      addDeploymentStage(DeploymentStages.PRE_DESCRIBE);
      addDeploymentStage(DeploymentStages.DESCRIBE);
      addDeploymentStage(DeploymentStages.CLASSLOADER);
      addDeploymentStage(DeploymentStages.POST_CLASSLOADER);
      addDeploymentStage(DeploymentStages.PRE_REAL);
      addDeploymentStage(DeploymentStages.REAL);
      addDeploymentStage(DeploymentStages.INSTALLED);
      
      // Create the deployers
      if (deployers != null)
         setDeployers(deployers);
   }
   
   public void shutdown()
   {
      shutdown.set(true);
   }

   /**
    * Check whether we are shutdown
    */
   protected void checkShutdown()
   {
      if (shutdown.get())
         throw new IllegalStateException("Deployers are shutdown");
   }
   
   /**
    * Get the deployers.
    * 
    * @return the deployers.
    */
   public Set<DeployerWrapper> getDeployerWrappers()
   {
      return deployers;
   }
   
   /**
    * Set the deployers.
    * 
    * @param deployers the deployers.
    * @throws IllegalArgumentException for null deployers
    */
   public void setDeployers(Set<Deployer> deployers)
   {
      if (deployers == null)
         throw new IllegalArgumentException("Null deployers");
      
      // Remove all the old deployers that are not in the new set
      HashSet<Deployer> oldDeployers = new HashSet<Deployer>(this.deployers);
      oldDeployers.removeAll(deployers);
      for (Deployer deployer : oldDeployers)
         removeDeployer(deployer);
      
      // Add all the new deployers that were not already present
      HashSet<Deployer> newDeployers = new HashSet<Deployer>(deployers);
      newDeployers.removeAll(this.deployers);
      for (Deployer deployer : newDeployers)
         addDeployer(deployer);
   }

   /**
    * Add a deployer
    * 
    * @param deployer the deployer
    */
   public synchronized void addDeployer(Deployer deployer)
   {
      if (deployer == null)
         throw new IllegalArgumentException("Null deployer");

      DeploymentStage stage = deployer.getStage();
      if (stage == null)
         throw new IllegalArgumentException("Deployer has no stage: " + deployer);

      addDeploymentStage(stage);
      
      DeployerWrapper wrapper = new DeployerWrapper(deployer);
      
      // Ignore duplicates
      if (deployers.contains(wrapper))
         return;
      
      String stageName = stage.getName();
      List<Deployer> deployers = deployersByStage.get(stageName);
      if (deployers == null)
         deployers = Collections.emptyList();
      deployers = insert(deployers, wrapper);
      deployersByStage.put(stageName, deployers);
      
      this.deployers.add(wrapper);
      
      StringBuilder builder = new StringBuilder();
      builder.append("Added deployer ").append(deployer).append(" for stage ").append(stageName).append('\n');
      for (Deployer temp : getDeployersList(stageName))
      {
         builder.append(temp);
         builder.append("{inputs=").append(temp.getInputs());
         builder.append(" outputs=").append(temp.getOutputs());
         builder.append("}\n");
      }
      log.debug(builder);
   }

   /**
    * Remove a deployer
    * 
    * @param deployer the deployer
    */
   public synchronized void removeDeployer(Deployer deployer)
   {
      if (deployer == null)
         throw new IllegalArgumentException("Null deployer");
      deployers.remove(new DeployerWrapper(deployer));

      DeploymentStage stage = deployer.getStage();
      if (stage == null)
      {
         log.warn("Deployer has no stage: " + deployer);
         return;
      }
      
      String stageName = stage.getName();
      List<Deployer> deployers = deployersByStage.get(stageName);
      if (deployers == null)
         return;
      
      deployers.remove(deployer);
      if (deployers.isEmpty())
         deployersByStage.remove(stageName);
      
      log.debug("Removed deployer " + deployer + " from stage " + stageName);
   }

   /**
    * Add a deployment stage
    * 
    * @param stage the deployment stage
    */
   protected synchronized void addDeploymentStage(DeploymentStage stage)
   {
      if (stage == null)
         throw new IllegalArgumentException("Null stage");
      
      // Already done?
      String stageName = stage.getName();
      if (stages.containsKey(stageName))
         return;

      ControllerState preceeds = null;
      String before = stage.getBefore();
      String after = stage.getAfter();
      if (before != null || after != null)
      {
         // Determine where to put the stage
         List<ControllerState> states = controller.getStates();
         for (int i = 0; i < states.size(); ++i)
         {
            ControllerState state = states.get(i);
            String stateName = state.getStateString();
            if (before != null && before.equals(stateName))
            {
               preceeds = state;
               break;
            }
            if (after != null && after.equals(stateName))
            {
               if (i < states.size()-1)
               {
                  preceeds = states.get(i+1);
                  break;
               }
            }
         }
      }

      controller.addState(new ControllerState(stageName), preceeds);
      stages.put(stageName, stage);
      log.debug("Added stage " + stageName + " before " + preceeds);
   }

   /**
    * Get the scopeBuilder.
    * 
    * @return the scopeBuilder.
    */
   public ScopeBuilder getScopeBuilder()
   {
      return scopeBuilder;
   }

   /**
    * Set the scopeBuilder.
    * 
    * @param scopeBuilder the scopeBuilder.
    */
   public void setScopeBuilder(ScopeBuilder scopeBuilder)
   {
      this.scopeBuilder = scopeBuilder;
   }

   /**
    * Get the repository.
    * 
    * @return the repository.
    */
   public MutableMetaDataRepository getRepository()
   {
      return repository;
   }

   /**
    * Set the repository.
    * 
    * @param repository the repository.
    */
   public void setRepository(MutableMetaDataRepository repository)
   {
      this.repository = repository;
   }

   public void start()
   {
      // Bootstrap the repository
      if (repository == null && controller instanceof KernelController)
      {
         KernelController kernelController = (KernelController) controller;
         repository = kernelController.getKernel().getMetaDataRepository().getMetaDataRepository();
      }
   }
   
   public Map<String, ManagedObject> getManagedObjects(DeploymentContext context) throws DeploymentException
   {
      if (context == null)
         throw new IllegalArgumentException("Null context");
      
      checkShutdown();
      
      Map<String, ManagedObject> managedObjects = new HashMap<String, ManagedObject>();
      for (DeployerWrapper deployer : deployers)
         deployer.build(context.getDeploymentUnit(), managedObjects);
      
      return managedObjects;
   }

   /**
    * Get the ManagedObjectBuilder for a deployer.
    * 
    * @param deployer - the deployer to set the ManagedObjectBuilder for.
    * @return managedObjectBuilder for deployer, may be null
    * @throws IllegalArgumentException for a null deployer
    */
   public ManagedObjectCreator getDeployerManagedObjectBuilder(Deployer deployer)
   {
      if (deployer == null)
         throw new IllegalArgumentException("Null deployer");
      
      ManagedObjectCreator result = null;
      for (DeployerWrapper wrapper : deployers)
      {
         if (wrapper.equals(deployer))
            result = wrapper.getManagedObjectCreator();
      }
      return result;
   }

   /**
    * Set the ManagedObjectBuilder for a deployer. This allows one to override the given deployer
    * ManagedObjectBuilder or assign one when the deployer does not provide a ManagedObjectBuilder.
    * 
    * @param deployer - the deployer to set the ManagedObjectBuilder for.
    * @param managedObjectCreator the managed object builder to set to the deployer
    * @throws IllegalArgumentException for a null deployer
    */
   public void setDeployerManagedObjectBuilder(Deployer deployer, ManagedObjectCreator managedObjectCreator)
   {
      if (deployer == null)
         throw new IllegalArgumentException("Null deployer");

      for (DeployerWrapper wrapper : deployers)
      {
         if (wrapper.equals(deployer))
            wrapper.setManagedObjectCreator(managedObjectCreator);
      }
   }

   public DeploymentStage getDeploymentStage(DeploymentContext context) throws DeploymentException
   {
      DeploymentControllerContext deploymentControllerContext = context.getTransientAttachments().getAttachment(ControllerContext.class.getName(), DeploymentControllerContext.class);
      if (deploymentControllerContext == null)
         return null;
      ControllerState state = deploymentControllerContext.getState();
      if (ControllerState.ERROR.equals(state))
         return DeploymentStages.NOT_INSTALLED;
      return new DeploymentStage(state.getStateString());
   }

   public void change(DeploymentContext context, DeploymentStage stage) throws DeploymentException
   {
      if (context == null)
         throw new DeploymentException("Null context");
      if (stage == null)
         throw new DeploymentException("Null stage");
      
      String stageName = stage.getName();
      if (stages.containsKey(stage.getName()) == false)
         throw new DeploymentException("Unknown deployment stage: " + stage);
      
      DeploymentControllerContext deploymentControllerContext = context.getTransientAttachments().getAttachment(ControllerContext.class.getName(), DeploymentControllerContext.class);
      if (deploymentControllerContext == null)
         throw new DeploymentException("Deployment " + context.getName() + " has no deployment controller context");

      checkShutdown();
      
      ControllerState state = new ControllerState(stageName);
      try
      {
         controller.change(deploymentControllerContext, state);
      }
      catch (Throwable t)
      {
         context.setState(DeploymentState.ERROR);
         context.setProblem(t);
      }
      Throwable problem = context.getProblem();
      if (problem != null)
         throw DeploymentException.rethrowAsDeploymentException("Error changing to stage " + stage + " for " + context.getName(), problem);
   }
   
   public void process(List<DeploymentContext> deploy, List<DeploymentContext> undeploy)
   {
      boolean trace = log.isTraceEnabled();
      
      // There is something to undeploy
      if (undeploy != null && undeploy.isEmpty() == false)
      {
         // Build a list in reverse order
         List<DeploymentControllerContext> toUndeploy = new ArrayList<DeploymentControllerContext>();
         for (int i = undeploy.size()-1; i >= 0; --i)
         {
            DeploymentContext context = undeploy.get(i);
            if (DeploymentState.ERROR.equals(context.getState()) == false)
               context.setState(DeploymentState.UNDEPLOYING);
            log.debug("Undeploying " + context.getName());
            DeploymentControllerContext deploymentControllerContext = context.getTransientAttachments().getAttachment(ControllerContext.class.getName(), DeploymentControllerContext.class);
            if (deploymentControllerContext == null)
            {
               log.debug("DeploymentContext has no DeploymentControllerContext during undeploy request, ignoring: " + context);
            }
            else
            {
               toUndeploy.add(deploymentControllerContext);
            }
         }

         // Go through the states in reverse order
         List<ControllerState> states = controller.getStates();
         for (int i = states.size()-1; i >= 0; --i)
         {
            ControllerState state = states.get(i);
            for (DeploymentControllerContext deploymentControllerContext : toUndeploy)
            {
               ControllerState current = deploymentControllerContext.getState();
               int currentIdx = states.indexOf(current);
               DeploymentContext context = deploymentControllerContext.getDeploymentContext();
               if (currentIdx != -1 && currentIdx > i)
               {
                  try
                  {
                     controller.change(deploymentControllerContext, state);
                  }
                  catch (Throwable t)
                  {
                     log.warn("Error during undeploy", t);
                     context.setState(DeploymentState.ERROR);
                     context.setProblem(t);
                  }
               }
               else
               {
                  if (trace)
                     log.trace("Not moving " + deploymentControllerContext + " to state " + state + " it is at " + current);
               }
            }
         }

         // Uninstall the contexts
         for (DeploymentControllerContext deploymentControllerContext : toUndeploy)
         {
            DeploymentContext context = deploymentControllerContext.getDeploymentContext();
            context.getTransientAttachments().removeAttachment(ControllerContext.class);
            try
            {
               controller.uninstall(deploymentControllerContext.getName());
               setState(context, DeploymentState.UNDEPLOYED, null);
               // This is now in the abstract classloader deployer.undeploy,
               // but left here in case somebody isn't using that.
               removeClassLoader(context);
               cleanup(context);
               log.debug("Fully Undeployed " + context.getName());
            }
            catch (Throwable t)
            {
               log.warn("Error during uninstall", t);
               context.setState(DeploymentState.ERROR);
               context.setProblem(t);
            }
         }
      }
      
      // There is something to deploy
      if (deploy != null && deploy.isEmpty() == false)
      {
         // Create the controller contexts
         for (DeploymentContext context : deploy)
         {
            checkShutdown();

            DeploymentControllerContext deploymentControllerContext = new DeploymentControllerContext(context, this);
            try
            {
               controller.install(deploymentControllerContext);
               context.setState(DeploymentState.DEPLOYING);
               log.debug("Deploying " + context.getName());
               context.getTransientAttachments().addAttachment(ControllerContext.class, deploymentControllerContext);
               if (scopeBuilder != null)
                  context.getTransientAttachments().addAttachment(ScopeBuilder.class, scopeBuilder);
               if (repository != null)
                  context.getTransientAttachments().addAttachment(MutableMetaDataRepository.class, repository);
            }
            catch (Throwable t)
            {
               // Set the error on the parent
               context.setState(DeploymentState.ERROR);
               context.setProblem(t);
               // Set the children to not deployed
               setState(context, DeploymentState.UNDEPLOYED, DeploymentState.DEPLOYING);
            }
         }

         // Go through the states in order
         List<ControllerState> states = controller.getStates();
         for (int i = 0; i < states.size(); ++i)
         {
            ControllerState state = states.get(i);
            for (DeploymentContext context : deploy)
            {
               DeploymentControllerContext deploymentControllerContext = context.getTransientAttachments().getAttachment(ControllerContext.class.getName(), DeploymentControllerContext.class);
               ControllerState current = deploymentControllerContext.getState();
               int currentIdx = states.indexOf(current);
               if (currentIdx != -1 && currentIdx < i)
               {
                  checkShutdown();
                  try
                  {
                     controller.change(deploymentControllerContext, state);
                  }
                  catch (Throwable t)
                  {
                     context.setState(DeploymentState.ERROR);
                     context.setProblem(t);
                  }
               }
               else
               {
                  if (trace)
                     log.trace("Not moving " + deploymentControllerContext + " to state " + state + " it is at " + current);
               }
            }
         }
      }
   }

   /**
    * Get the root cause of a throwable
    * 
    * @param original the original
    * @return the root
    */
   private static Throwable getRootCause(Throwable original)
   {
      if (original == null)
         return null;
      Throwable result = original;
      Throwable cause = result.getCause();
      while (cause != null)
      {
         result = cause;
         cause = cause.getCause();
      }
      return result;
   }

   public void checkComplete(Collection<DeploymentContext> errors, Collection<Deployment> missingDeployer) throws DeploymentException
   {
      Map<String, Throwable> deploymentsInError = null;
      Collection<String> deploymentsMissingDeployer = null;
      Map<String, Throwable> contextsInError = null;
      Map<String, Set<MissingDependency>> contextsMissingDependencies = null;

      if (errors != null && errors.isEmpty() == false)
      {
         deploymentsInError = new HashMap<String, Throwable>();
         for (DeploymentContext context : errors)
            deploymentsInError.put(context.getName(), getRootCause(context.getProblem()));
      }

      if (missingDeployer != null && missingDeployer.isEmpty() == false)
      {
         deploymentsMissingDeployer = new HashSet<String>();
         for (Deployment context : missingDeployer)
            deploymentsMissingDeployer.add(context.getName());
      }

      List<ControllerState> states = controller.getStates();

      Set<ControllerContext> notInstalled = controller.getNotInstalled();
      if (notInstalled.isEmpty() == false)
      {
         for (Iterator<ControllerContext> i = notInstalled.iterator(); i.hasNext();)
         {
            ControllerContext context = i.next();
            if (context.getState().equals(context.getRequiredState()))
               i.remove();
         }
         if (notInstalled.isEmpty() == false)
         {
            contextsInError = new HashMap<String, Throwable>();
            contextsMissingDependencies = new HashMap<String, Set<MissingDependency>>();
            for (ControllerContext context : notInstalled)
            {
               checkControllerContext(context, contextsInError, contextsMissingDependencies, states);
            }
         }
      }

      IncompleteDeployments incomplete = new IncompleteDeployments(deploymentsInError, deploymentsMissingDeployer, contextsInError, contextsMissingDependencies);
      if (incomplete.isIncomplete())
         throw new IncompleteDeploymentException(incomplete);
   }

   /**
    * Check controller context.
    *
    * @param context the controller context
    * @param contextsInError contexts in error map
    * @param contextsMissingDependencies contexts missing dependecies map
    * @param states controller states
    */
   protected final void checkControllerContext(
         ControllerContext context,
         Map<String, Throwable> contextsInError,
         Map<String, Set<MissingDependency>> contextsMissingDependencies,
         List<ControllerState> states)
   {
      if (context.getState().equals(ControllerState.ERROR))
         contextsInError.put(context.getName().toString(), getRootCause(context.getError()));
      else
      {
         Object contextName = context.getName();
         String name = contextName.toString();
         Set<MissingDependency> dependencies = new HashSet<MissingDependency>();
         DependencyInfo dependsInfo = context.getDependencyInfo();
         for (DependencyItem item : dependsInfo.getIDependOn(null))
         {
            if (item.isResolved() == false)
            {
               String dependency;
               ControllerState actualState = null;
               String actualStateString;

               Object iDependOn = item.getIDependOn();
               if (contextName.equals(iDependOn) == false)
               {
                  boolean includeDependency = true;

                  if (iDependOn == null)
                  {
                     dependency = "<UNKNOWN " + item.getName() + ">";
                     actualStateString = "** UNRESOLVED " + item.toHumanReadableString() + " **";
                  }
                  else
                  {
                     dependency = iDependOn.toString();
                     ControllerContext other = controller.getContext(iDependOn, null);
                     if (other == null)
                        actualStateString = "** NOT FOUND " + item.toHumanReadableString() + " **";
                     else
                     {
                        actualState = other.getState();
                        actualStateString = actualState.getStateString();
                        if (actualState != null && actualState.equals(ControllerState.ERROR) == false)
                        {
                           ControllerState dependentState = item.getDependentState();
                           if (dependentState == null)
                              dependentState = ControllerState.INSTALLED;

                           int dependentIndex = states.indexOf(dependentState);
                           int actualIndex = states.indexOf(actualState);
                           if (actualIndex >= dependentIndex)
                              includeDependency = false;
                        }
                     }
                  }

                  if (includeDependency)
                  {
                     ControllerState requiredState = item.getWhenRequired();
                     String requiredStateString = requiredState.getStateString();
                     int required = states.indexOf(requiredState);
                     int actual = actualState == null ? -1 : states.indexOf(actualState);
                     if (required > actual)
                     {
                        MissingDependency missing = new MissingDependency(name, dependency, requiredStateString, actualStateString);
                        dependencies.add(missing);
                     }
                  }
               }
            }
         }
         contextsMissingDependencies.put(name, dependencies);
      }
   }

   public void checkComplete(DeploymentContext... contexts) throws DeploymentException
   {
      checkComplete(true, contexts);
   }

   public void checkStructureComplete(DeploymentContext... contexts) throws DeploymentException
   {
      checkComplete(false, contexts);
   }

   /**
    * Check if deployments are complete.
    *
    * @param contexts the deployment contexts
    * @param checkContexts do we check contexts
    * @throws DeploymentException throw error if deployment is incomplete
    */
   protected void checkComplete(boolean checkContexts, DeploymentContext... contexts) throws DeploymentException
   {
      if (contexts == null)
         throw new IllegalArgumentException("Null contexts");

      Map<String, Throwable> deploymentsInError = new HashMap<String, Throwable>();
      Collection<String> deploymentsMissingDeployer = new HashSet<String>();
      Map<String, Throwable> contextsInError = new HashMap<String, Throwable>();
      Map<String, Set<MissingDependency>> contextsMissingDependencies = new HashMap<String, Set<MissingDependency>>();

      for(DeploymentContext context : contexts)
      {
         Throwable problem = context.getProblem();
         if (problem != null)
            deploymentsInError.put(context.getName(), problem);

         if (isDeployed(context) == false)
            deploymentsMissingDeployer.add(context.getName());

         if (checkContexts)
         {
            Set<ControllerContext> notInstalled = controller.getNotInstalled();
            List<ControllerState> states = controller.getStates();
            checkComplete(context, contextsInError, contextsMissingDependencies, notInstalled, states);
         }
      }

      // reset if not used
      if (deploymentsInError.isEmpty())
         deploymentsInError = null;
      if (deploymentsMissingDeployer.isEmpty())
         deploymentsMissingDeployer = null;
      if (contextsInError.isEmpty())
         contextsInError = null;
      if (contextsMissingDependencies.isEmpty())
         contextsMissingDependencies = null;

      IncompleteDeployments incomplete = new IncompleteDeployments(deploymentsInError, deploymentsMissingDeployer, contextsInError, contextsMissingDependencies);
      if (incomplete.isIncomplete())
         throw new IncompleteDeploymentException(incomplete);
   }

   /**
    * Is context deployed.
    *
    * @param context the deployment context
    * @return true if context deployed, false otherwise
    */
   protected boolean isDeployed(DeploymentContext context)
   {
      return context.isDeployed() || DeploymentState.DEPLOYED.equals(context.getState());
   }

   /**
    * Check complete on deployment context.
    *
    * @param context the deployment context
    * @param contextsInError contexts in error map
    * @param contextsMissingDependencies contexts missing dependecies map
    * @param notInstalled the not installed contexts
    * @param states controller states
    */
   protected final void checkComplete(
         DeploymentContext context,
         Map<String, Throwable> contextsInError,
         Map<String, Set<MissingDependency>> contextsMissingDependencies,
         Set<ControllerContext> notInstalled,
         List<ControllerState> states)
   {
      DeploymentControllerContext dcc = context.getTransientAttachments().getAttachment(ControllerContext.class.getName(), DeploymentControllerContext.class);
      checkControllerContext(dcc, contextsInError, contextsMissingDependencies, notInstalled, states);

      Set<Object> names = context.getControllerContextNames();
      if (names != null && names.isEmpty() == false)
      {
         for(Object name : names)
         {
            ControllerContext cc = controller.getContext(name, null);
            checkControllerContext(cc, contextsInError, contextsMissingDependencies, notInstalled, states);
         }
      }

      List<DeploymentContext> children = context.getChildren();
      if (children != null && children.isEmpty() == false)
      {
         for(DeploymentContext child : children)
            checkComplete(child, contextsInError, contextsMissingDependencies, notInstalled, states);
      }

      List<DeploymentContext> components = context.getComponents();
      if (components != null && components.isEmpty() == false)
      {
         for(DeploymentContext component : components)
            checkComplete(component, contextsInError, contextsMissingDependencies, notInstalled, states);
      }
   }

   /**
    * Check complete on deployment context.
    *
    * @param context the deployment context
    * @param contextsInError contexts in error map
    * @param contextsMissingDependencies contexts missing dependecies map
    * @param notInstalled the not installed contexts
    * @param states controller states
    */
   protected void checkControllerContext(
         ControllerContext context,
         Map<String, Throwable> contextsInError,
         Map<String, Set<MissingDependency>> contextsMissingDependencies,
         Set<ControllerContext> notInstalled,
         List<ControllerState> states)
   {
      if (context != null)
      {
         if (context.getState().equals(context.getRequiredState()) == false && notInstalled.contains(context))
         {
            checkControllerContext(context, contextsInError, contextsMissingDependencies, states);
         }
      }
   }

   public void install(ControllerContext context, ControllerState fromState, ControllerState toState) throws Throwable
   {
      DeploymentControllerContext deploymentControllerContext = (DeploymentControllerContext) context;
      String stageName = toState.getStateString();
      
      DeploymentContext deploymentContext = deploymentControllerContext.getDeploymentContext();
      try
      {
         List<Deployer> theDeployers = getDeployersList(stageName);
         
         if (log.isTraceEnabled())
            log.trace("Deployers for " + stageName + " " + theDeployers);
         
         if (theDeployers.isEmpty() == false)
         {
            int i = 0;
            try
            {
               while (i < theDeployers.size())
               {
                  Deployer deployer = theDeployers.get(i);
                  if (deployer.isParentFirst())
                     doInstallParentFirst(deployer, deploymentContext);
                  else
                     doInstallParentLast(deployer, deploymentContext);
                  ++i;
               }
            }
            catch (Throwable t)
            {
               deploymentContext.setState(DeploymentState.ERROR);
               deploymentContext.setProblem(t);
               
               // Unwind the previous deployments
               for (int j = i-1; j >= 0; --j)
               {
                  Deployer deployer = theDeployers.get(j);
                  if (deployer.isParentFirst())
                     doUninstallParentLast(deployer, deploymentContext, true, true);
                  else
                     doUninstallParentFirst(deployer, deploymentContext, true, true);
               }
               
               // It can happen that subdeployments are not processed if the parent fails immediately
               // so there is no callback to undeploy when nothing was done
               setState(deploymentContext, DeploymentState.UNDEPLOYED, DeploymentState.DEPLOYING);
               throw t;
            }
         }
      }
      finally
      {
         if (ControllerState.INSTALLED.equals(toState) && DeploymentState.DEPLOYING.equals(deploymentContext.getState()))
         {
            log.debug("Fully Deployed " + context.getName());
            setState(deploymentContext, DeploymentState.DEPLOYED, null);
         }
      }
   }
   
   /**
    * Do the install parent first
    * 
    * @param deployer the deployer
    * @param context the context
    * @throws Throwable for any problem
    */
   protected void doInstallParentFirst(Deployer deployer, DeploymentContext context) throws Throwable
   {
      List<DeploymentContext> currentComponents = context.getComponents();
      // Take a copy of the components so we don't start looping on newly added components
      // in the component deployers
      List<DeploymentContext> components = null;
      if (currentComponents != null && currentComponents.isEmpty() == false)
         components = new ArrayList<DeploymentContext>(currentComponents);

      DeploymentUnit unit = context.getDeploymentUnit();
      if (isRelevant(deployer, unit, context.isTopLevel(), context.isComponent()))
      {
         try
         {
            deployer.deploy(unit);
         }
         catch (DeploymentException e)
         {
            context.setState(DeploymentState.ERROR);
            context.setProblem(e);
            throw e;
         }
      }
      else if (log.isTraceEnabled())
         log.trace("Deployer " + deployer + " not relevant for " + context.getName());
      
      if (components != null)
      {
         try
         {
            for (int i = 0; i < components.size(); ++i)
            {
               DeploymentContext component = components.get(i);
               try
               {
                  doInstallParentFirst(deployer, component);
               }
               catch (DeploymentException e)
               {
                  // Unwind the previous components
                  for (int j = i - 1; j >= 0; --j)
                  {
                     component = components.get(j);
                     doUninstallParentLast(deployer, component, false, true);
                  }
                  throw e;
               }
            }
         }
         catch (DeploymentException e)
         {
            // Just undeploy this context
            doUninstallParentLast(deployer, context, false, false);
            throw e;
         }
      }

      List<DeploymentContext> children = context.getChildren();
      if (children != null)
      {
         try
         {
            for (int i = 0; i < children.size(); ++i)
            {
               DeploymentContext child = children.get(i);
               try
               {
                  doInstallParentFirst(deployer, child);
               }
               catch (DeploymentException e)
               {
                  // Unwind the previous children
                  for (int j = i - 1; j >= 0; --j)
                  {
                     child = children.get(j);
                     doUninstallParentLast(deployer, child, true, true);
                  }
                  throw e;
               }
            }
         }
         catch (DeploymentException e)
         {
            // Undeploy the context but the children are already unwound
            doUninstallParentLast(deployer, context, false, true);
            throw e;
         }
      }         
   }
   
   /**
    * Do the install parent last
    * 
    * @param deployer the deployer
    * @param context the context
    * @throws Throwable for any problem
    */
   protected void doInstallParentLast(Deployer deployer, DeploymentContext context) throws Throwable
   {
      List<DeploymentContext> children = context.getChildren();
      for (int i = 0; i < children.size(); ++i)
      {
         DeploymentContext child = children.get(i);
         try
         {
            doInstallParentLast(deployer, child);
         }
         catch (DeploymentException e)
         {
            // Unwind the previous children
            for (int j = i - 1; j >= 0; --j)
            {
               child = children.get(j);
               doUninstallParentFirst(deployer, child, true, true);
            }
            throw e;
         }
      }
      
      List<DeploymentContext> components = context.getComponents();
      if (components != null)
      {
         try
         {
            for (int i = 0; i < components.size(); ++i)
            {
               DeploymentContext component = components.get(i);
               try
               {
                  doInstallParentLast(deployer, component);
               }
               catch (DeploymentException e)
               {
                  // Unwind the previous components
                  for (int j = i - 1; j >= 0; --j)
                  {
                     component = components.get(j);
                     doUninstallParentFirst(deployer, component, true, true);
                  }
                  throw e;
               }
            }
         }
         catch (DeploymentException e)
         {
            // Just undeploy the children, the components are already unwound
            doUninstallParentFirst(deployer, context, false, false);
            throw e;
         }
      }

      DeploymentUnit unit = context.getDeploymentUnit();
      if (isRelevant(deployer, unit, context.isTopLevel(), context.isComponent()))
      {
         try
         {
            deployer.deploy(unit);
         }
         catch (DeploymentException e)
         {
            // Undeploy the children and components
            doUninstallParentFirst(deployer, context, false, true);
            context.setState(DeploymentState.ERROR);
            context.setProblem(e);
            throw e;
         }
      }
      else if (log.isTraceEnabled())
         log.trace("Deployer " + deployer + " not relevant for " + context.getName());
   }
   
   public void uninstall(ControllerContext context, ControllerState fromState, ControllerState toState)
   {
      DeploymentControllerContext deploymentControllerContext = (DeploymentControllerContext) context;
      String stageName = fromState.getStateString();
      
      DeploymentContext deploymentContext = deploymentControllerContext.getDeploymentContext();
      List<Deployer> theDeployers = getDeployersList(stageName);
      
      if (log.isTraceEnabled())
         log.trace("Deployers for " + stageName + " " + theDeployers);

      if  (theDeployers.isEmpty() == false)
      {
         for (int i = theDeployers.size()-1; i >= 0; --i)
         {
            Deployer deployer = theDeployers.get(i);
            if (deployer.isParentFirst())
               doUninstallParentLast(deployer, deploymentContext, true, true);
            else
               doUninstallParentFirst(deployer, deploymentContext, true, true);
         }
      }
   }

   /**
    * Do the uninstall parent last
    *
    * @param deployer the deployer
    * @param context the context
    * @param doChildren whether to do children
    * @param doComponents whether to do components
    */
   protected void doUninstallParentLast(Deployer deployer, DeploymentContext context, boolean doChildren, boolean doComponents)
   {
      if (doChildren)
      {
         List<DeploymentContext> children = context.getChildren();
         if (children != null && children.isEmpty() == false)
         {
            for (int i = children.size()-1; i >=  0; --i)
            {
               DeploymentContext child = children.get(i);
               doUninstallParentLast(deployer, child, true, true);
            }
         }
      }

      if (doComponents)
      {
         List<DeploymentContext> components = context.getComponents();
         if (components != null && components.isEmpty() == false)
         {
            for (int i = components.size()-1; i >=  0; --i)
            {
               DeploymentContext component = components.get(i);
               doUninstallParentLast(deployer, component, false, true);
            }
         }
      }

      DeploymentUnit unit = context.getDeploymentUnit();
      if (isRelevant(deployer, unit, context.isTopLevel(), context.isComponent()))
         deployer.undeploy(unit);
      else if (log.isTraceEnabled())
         log.trace("Deployer " + deployer + " not relevant for " + context.getName());
   }

   /**
    * Do the uninstall parent first
    *
    * @param deployer the deployer
    * @param context the context
    * @param doContext whether to do context
    * @param doComponents whether to do components
    */
   protected void doUninstallParentFirst(Deployer deployer, DeploymentContext context, boolean doContext, boolean doComponents)
   {
      if (doContext)
      {
         DeploymentUnit unit = context.getDeploymentUnit();
         if (isRelevant(deployer, unit, context.isTopLevel(), context.isComponent()))
            deployer.undeploy(unit);
         else if (log.isTraceEnabled())
            log.trace("Deployer " + deployer + " not relevant for " + context.getName());
      }

      if (doComponents)
      {
         List<DeploymentContext> components = context.getComponents();
         if (components != null && components.isEmpty() == false)
         {
            for (int i = components.size()-1; i >=  0; --i)
            {
               DeploymentContext component = components.get(i);
               doUninstallParentFirst(deployer, component, true, true);
            }
         }
      }

      List<DeploymentContext> children = context.getChildren();
      if (children != null && children.isEmpty() == false)
      {
         for (int i = children.size()-1; i >=  0; --i)
         {
            DeploymentContext child = children.get(i);
            doUninstallParentFirst(deployer, child, true, true);
         }
      }
   }
   
   /**
    * Build a list of  deployers for this stage
    * 
    * @param stageName the stage name
    * @return the deployers
    */
   protected synchronized List<Deployer> getDeployersList(String stageName)
   {
      List<Deployer> deployers = deployersByStage.get(stageName);
      if (deployers == null || deployers.isEmpty())
         return Collections.emptyList();
      
      return deployers;
   }
   
   /**
    * Test whether a deployer is relevant
    * 
    * @param deployer deployer
    * @param unit the deployment unit
    * @param isTopLevel whether this is a top level deployment
    * @param isComponent whether this is a component
    * @return the deployers
    */
   protected boolean isRelevant(Deployer deployer, DeploymentUnit unit, boolean isTopLevel, boolean isComponent)
   {
      // Deployer only wants top level
      if (deployer.isTopLevelOnly() && isTopLevel == false)
         return false;

      // Deployer only wants components
      if (deployer.isComponentsOnly() && isComponent == false)
         return false;

      // Deployer doesn't wants components
      if (deployer.isWantComponents() == false && isComponent)
         return false;
      
      if (deployer.isAllInputs() == false)
      {
         // No attachment for the input type
         Class<?> input = deployer.getInput();
         if (input != null && unit.getAttachment(input) == null)
            return false;
      }
      return true;
   }
   
   /**
    * Insert the new Deployer.
    *
    * @param original the original deployers
    * @param newDeployer the new deployer
    * @return the sorted deployers
    */
   protected List<Deployer> insert(List<Deployer> original, Deployer newDeployer)
   {
      DeployerSorter sorter = DeployerSorterFactory.newSorter();
      return sorter.sortDeployers(original, newDeployer);
   }

   /**
    * Set the deployment state for a context and its children
    * 
    * @param context the context
    * @param state the state
    * @param ifState the ifState
    */
   private static void setState(DeploymentContext context, DeploymentState state, DeploymentState ifState)
   {
      if (ifState == null || ifState.equals(context.getState()))
         context.setState(state);
      List<DeploymentContext> children = context.getChildren();
      if (children != null && children.isEmpty() == false)
      {
         for (DeploymentContext child : children)
            setState(child, state, ifState);
      }
   }

   /**
    * Remove a classloader for a context and its children
    * 
    * @param context the context
    */
   private static void removeClassLoader(DeploymentContext context)
   {
      context.removeClassLoader();
      List<DeploymentContext> children = context.getChildren();
      if (children != null && children.isEmpty() == false)
      {
         for (DeploymentContext child : children)
            removeClassLoader(child);
      }
   }

   /**
    * Cleanup the deployment context
    * 
    * @param context the context
    */
   private static void cleanup(DeploymentContext context)
   {
      context.cleanup();
      List<DeploymentContext> children = context.getChildren();
      if (children != null && children.isEmpty() == false)
      {
         for (DeploymentContext child : children)
            cleanup(child);
      }
      List<DeploymentContext> components = context.getComponents();
      if (components != null && components.isEmpty() == false)
      {
         for (DeploymentContext component : components)
            cleanup(component);
      }
   }
}
