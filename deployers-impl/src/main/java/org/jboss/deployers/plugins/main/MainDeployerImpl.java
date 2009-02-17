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
package org.jboss.deployers.plugins.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.ListIterator;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.client.spi.main.MainDeployer;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.DeploymentState;
import org.jboss.deployers.spi.deployer.Deployers;
import org.jboss.deployers.spi.deployer.DeploymentStage;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.managed.ManagedDeploymentCreator;
import org.jboss.deployers.structure.spi.DeploymentContext;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.structure.spi.StructuralDeployers;
import org.jboss.deployers.structure.spi.helpers.RevertedDeploymentContextComparator;
import org.jboss.deployers.structure.spi.main.MainDeployerStructure;
import org.jboss.logging.Logger;
import org.jboss.managed.api.ManagedDeployment;
import org.jboss.managed.api.ManagedObject;
import org.jboss.util.graph.Graph;
import org.jboss.util.graph.Vertex;

/**
 * MainDeployerImpl.
 *
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @author <a href="scott.stark@jboss.org">Scott Stark</a>
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 * @version $Revision$
 */
public class MainDeployerImpl implements MainDeployer, MainDeployerStructure
{
   /** The log */
   private static final Logger log = Logger.getLogger(MainDeployerImpl.class);

   /** Whether we are shutdown */
   private AtomicBoolean shutdown = new AtomicBoolean(false);

   /** The deployers */
   private Deployers deployers;

   /** The structural deployers */
   private StructuralDeployers structuralDeployers;

   /** The ManagedDeploymentCreator plugin */
   private ManagedDeploymentCreator mgtDeploymentCreator;

   /** The deployments by name */
   private Map<String, DeploymentContext> topLevelDeployments = new ConcurrentHashMap<String, DeploymentContext>();

   /** All deployments by name */
   private Map<String, DeploymentContext> allDeployments = new ConcurrentHashMap<String, DeploymentContext>();

   /** Deployments in error by name */
   private Map<String, DeploymentContext> errorDeployments = new ConcurrentHashMap<String, DeploymentContext>();

   /** Deployments missing deployers */
   private Map<String, Deployment> missingDeployers = new ConcurrentHashMap<String, Deployment>();

   /** The undeploy work */
   private List<DeploymentContext> undeploy = new CopyOnWriteArrayList<DeploymentContext>();

   /** The deploy work */
   private List<DeploymentContext> deploy = new CopyOnWriteArrayList<DeploymentContext>();

   /** The process lock */
   private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

   /** The top deployment context comparator */
   private Comparator<DeploymentContext> comparator;
   private Comparator<DeploymentContext> reverted;

   /** The deployment waiting to be processed */
   private Map<String, Deployment> toDeploy = Collections.synchronizedMap(new LinkedHashMap<String, Deployment>());

   /**
    * Set the top deployment context comparator.
    *
    * @param comparator the deployment context comparator
    */
   public void setComparator(Comparator<DeploymentContext> comparator)
   {
      if (comparator == null)
         throw new IllegalArgumentException("Null comparator");

      this.comparator = comparator;
      this.reverted = new RevertedDeploymentContextComparator(comparator);
   }

   /**
    * Get the deployers
    *
    * @return the deployers
    */
   public synchronized Deployers getDeployers()
   {
      return deployers;
   }

   /**
    * Set the deployers
    *
    * @param deployers the deployers
    * @throws IllegalArgumentException for null deployers
    */
   public synchronized void setDeployers(Deployers deployers)
   {
      if (deployers == null)
         throw new IllegalArgumentException("Null deployers");

      this.deployers = deployers;
   }

   /**
    * Get the structural deployers
    *
    * @return the structural deployers
    */
   public synchronized StructuralDeployers getStructuralDeployers()
   {
      return structuralDeployers;
   }

   /**
    * Set the structural deployers
    *
    * @param deployers the deployers
    * @throws IllegalArgumentException for null deployers
    */
   public synchronized void setStructuralDeployers(StructuralDeployers deployers)
   {
      if (deployers == null)
         throw new IllegalArgumentException("Null deployers");

      structuralDeployers = deployers;
   }

   /**
    * Get managed deployment creator.
    *
    * @return the managed deployment creator
    */
   public ManagedDeploymentCreator getMgtDeploymentCreator()
   {
      return mgtDeploymentCreator;
   }

   /**
    * Set managed deployment creator.
    *
    * @param mgtDeploymentCreator the managed deployment creator
    */
   public void setMgtDeploymentCreator(ManagedDeploymentCreator mgtDeploymentCreator)
   {
      this.mgtDeploymentCreator = mgtDeploymentCreator;
   }

   public Deployment getDeployment(String name)
   {
      DeploymentContext context = getTopLevelDeploymentContext(name);
      if (context != null)
         return context.getDeployment();
      else
         return toDeploy.get(name);
   }

   @Deprecated
   public DeploymentContext getDeploymentContext(String name)
   {
      if (name == null)
         throw new IllegalArgumentException("Null name");

      return allDeployments.get(name);
   }

   @Deprecated
   public DeploymentContext getDeploymentContext(String name, boolean errorNotFound) throws DeploymentException
   {
      DeploymentContext context = getDeploymentContext(name);
      if (errorNotFound && context == null)
         throw new DeploymentException("Context " + name + " not found");

      return context;
   }

   public DeploymentUnit getDeploymentUnit(String name)
   {
      DeploymentContext context = getDeploymentContext(name);
      if (context == null)
         return null;
      return context.getDeploymentUnit();
   }

   public DeploymentUnit getDeploymentUnit(String name, boolean errorNotFound) throws DeploymentException
   {
      DeploymentUnit unit = getDeploymentUnit(name);
      if (errorNotFound && unit == null)
         throw new DeploymentException("Unit " + name + " not found");

      return unit;
   }

   /**
    * Get a top level deployment context by name
    *
    * @param name the name
    * @return the context
    */
   public DeploymentContext getTopLevelDeploymentContext(String name)
   {
      if (name == null)
         throw new IllegalArgumentException("Null name");

      return topLevelDeployments.get(name);
   }

   // TODO - introduce some interface or push to MDStructure

   /**
    * Get all deployments.
    *
    * @return all deployments
    */
   public Collection<DeploymentContext> getAll()
   {
      return Collections.unmodifiableCollection(allDeployments.values());
   }

   /**
    * Get errors.
    *
    * @return the errors
    */
   public Collection<DeploymentContext> getErrors()
   {
      return Collections.unmodifiableCollection(errorDeployments.values());
   }

   /**
    * Get missing deployers deployments.
    *
    * @return the missing deployer deployments
    */
   public Collection<Deployment> getMissingDeployer()
   {
      return Collections.unmodifiableCollection(missingDeployers.values());
   }

   public Collection<Deployment> getTopLevel()
   {
      List<Deployment> result = new ArrayList<Deployment>();
      for (DeploymentContext context : topLevelDeployments.values())
      {
         Deployment deployment = context.getDeployment();
         if (deployment != null)
            result.add(deployment);
         else
            throw new IllegalStateException("Context has no deployment? " + context.getName());
      }
      return result;
   }

   public void addDeployment(Deployment deployment) throws DeploymentException
   {
      if (deployment == null)
         throw new DeploymentException("Null context");

      lockRead();
      try
      {
         if (shutdown.get())
            throw new DeploymentException("The main deployer is shutdown");

         String name = deployment.getName();
         checkExistingTopLevelDeployment(name, true);
         toDeploy.put(name, deployment);
      }
      finally
      {
         unlockRead();
      }
   }

   /**
    * Process added deployments.
    *
    * @throws DeploymentException for any error
    */
   protected void processToDeploy() throws DeploymentException
   {
      List<String> added = new ArrayList<String>();

      try
      {
         for (Map.Entry<String, Deployment> entry : toDeploy.entrySet())
         {
            determineDeploymentContext(entry.getValue(), true);
            added.add(entry.getKey());
         }
      }
      catch (DeploymentException e)
      {
         ListIterator<String> iter = added.listIterator(added.size());
         while (iter.hasPrevious())
         {
            try
            {
               removeDeployment(iter.previous(), true);
            }
            catch (Throwable  ignored)
            {
            }
         }
         throw e;
      }
      finally
      {
         toDeploy.clear();
      }
   }

   /**
    * Add a deployment
    *
    * @param deployment the deployment
    * @param addToDeploy should we add this deployment to deploy collection
    * @throws DeploymentException for any error
    */
   protected void addDeployment(Deployment deployment, boolean addToDeploy) throws DeploymentException
   {
      if (deployment == null)
         throw new DeploymentException("Null context");

      lockRead();
      try
      {
         if (shutdown.get())
            throw new DeploymentException("The main deployer is shutdown");

         String name = deployment.getName();
         log.debug("Add deployment: " + name);

         checkExistingTopLevelDeployment(name, addToDeploy);
         determineDeploymentContext(deployment, addToDeploy);
      }
      finally
      {
         unlockRead();
      }
   }

   /**
    * Check for existing deployment context - redeploy.
    * Method should take read lock.
    *
    * @param name the deployment name
    * @param addToDeploy should we add this deployment to deploy collection
    */
   protected void checkExistingTopLevelDeployment(String name, boolean addToDeploy)
   {
      DeploymentContext previous = topLevelDeployments.get(name);
      if (previous != null)
      {
         log.debug("Removing previous deployment: " + previous.getName());
         removeContext(previous, addToDeploy);
      }
      else
      {
         previous = allDeployments.get(name);
         if (previous != null)
            throw new IllegalStateException("Deployment already exists as a subdeployment: " + name);
      }
   }

   /**
    * Determine deployment context.
    * Method should take read lock.
    *
    * @param deployment the deployment
    * @param addToDeploy should we add this deployment to deploy collection
    * @throws DeploymentException for any error
    */
   protected void determineDeploymentContext(Deployment deployment, boolean addToDeploy) throws DeploymentException
   {
      String name = deployment.getName();
      DeploymentContext context = null;
      try
      {
         context = determineStructure(deployment);
         if (DeploymentState.ERROR.equals(context.getState()))
         {
            errorDeployments.put(name, context);
         }

         context.getTransientAttachments().addAttachment(MainDeployer.class, this);
         topLevelDeployments.put(name, context);
         addContext(context, addToDeploy);
      }
      catch (DeploymentException e)
      {
         missingDeployers.put(name, deployment);
         throw e;
      }
      catch (Throwable t)
      {
         // was structure determined?
         if (context == null)
            missingDeployers.put(name, deployment);

         throw DeploymentException.rethrowAsDeploymentException("Error determining deployment structure for " + name, t);
      }
   }

   public boolean removeDeployment(Deployment deployment) throws DeploymentException
   {
      return removeDeployment(deployment, true);
   }

   /**
    * Remove a deployment by name
    *
    * @param deployment thedeployment
    * @param addToUndeploy should we add to undeploy collection
    * @return false when the context was previously unknown
    * @throws DeploymentException for any error
    */
   protected boolean removeDeployment(Deployment deployment, boolean addToUndeploy) throws DeploymentException
   {
      if (deployment == null)
         throw new DeploymentException("Null deployment");

      return removeDeployment(deployment.getName(), addToUndeploy);
   }

   public boolean removeDeployment(String name) throws DeploymentException
   {
      return removeDeployment(name, true);
   }

   /**
    * Remove a deployment by name
    *
    * @param name the name of the deployment
    * @param addToUndeploy should we add to undeploy collection
    * @return false when the context was previously unknown
    * @throws DeploymentException for any error
    */
   protected boolean removeDeployment(String name, boolean addToUndeploy) throws DeploymentException
   {
      if (name == null)
         throw new DeploymentException("Null name");

      lockRead();
      try
      {
         if (shutdown.get())
            throw new IllegalStateException("The main deployer is shutdown");

         log.debug("Remove deployment context: " + name);

         DeploymentContext context = topLevelDeployments.remove(name);
         if (context == null)
            return false;

         context.getTransientAttachments().removeAttachment(MainDeployer.class);
         removeContext(context, addToUndeploy);

         return true;
      }
      finally
      {
         unlockRead();
      }
   }

   public void deploy(Deployment... deployments) throws DeploymentException
   {
      if (deployments == null)
         throw new IllegalArgumentException("Null deployments.");

      if (deployers == null)
         throw new IllegalStateException("No deployers");

      lockRead();
      try
      {
         if (shutdown.get())
            throw new IllegalStateException("The main deployer is shutdown");

         DeploymentContext[] contexts = new DeploymentContext[deployments.length];
         for(int i = 0; i < deployments.length; i++)
         {
            try
            {
               Deployment deployment = deployments[i];
               addDeployment(deployment, false);
               DeploymentContext context = getTopLevelDeploymentContext(deployment.getName());
               if (contexts == null)
                  throw new DeploymentException("Deployment context not found: " + deployment.getName());

               deployers.process(Collections.singletonList(context), null);
               contexts[i] = context;
            }
            catch(Throwable t)
            {
               DeploymentContext[] deployedContexts = new DeploymentContext[i];
               System.arraycopy(contexts, 0, deployedContexts, 0, i);
               deployers.process(null, Arrays.asList(deployedContexts));
               throw DeploymentException.rethrowAsDeploymentException("Unable to deploy deployments, cause: " + deployments[i], t);
            }
         }
         try
         {
            deployers.checkComplete(contexts);
         }
         catch (DeploymentException e)
         {
            deployers.process(null, Arrays.asList(contexts));
            throw e;
         }
      }
      finally
      {
         unlockRead();
      }
   }

   public void undeploy(Deployment... deployments) throws DeploymentException
   {
      if (deployments == null)
         throw new IllegalArgumentException("Null deployments.");

      if (deployers == null)
         throw new IllegalStateException("No deployers");

      lockRead();
      try
      {
         if (shutdown.get())
            throw new IllegalStateException("The main deployer is shutdown");

         for(Deployment deployment : deployments)
         {
            DeploymentContext context = getTopLevelDeploymentContext(deployment.getName());
            if (context != null)
            {
               try
               {
                  removeDeployment(deployment, false);
                  deployers.process(null, Collections.singletonList(context));
               }
               catch (DeploymentException e)
               {
                  if (log.isTraceEnabled())
                     log.trace("Ignored exception while undeploying deployment " + deployment.getName() + ":" + e);
               }
            }
            else if (log.isTraceEnabled())
            {
               log.trace("No such deployment present: " + deployment.getName());
            }
         }
      }
      finally
      {
         unlockRead();
      }
   }

   public void undeploy(String... names) throws DeploymentException
   {
      if (names == null)
         throw new IllegalArgumentException("Null names.");

      List<Deployment> deployments = new ArrayList<Deployment>();
      for(String name : names)
      {
         DeploymentContext context = getTopLevelDeploymentContext(name);
         if (context != null)
            deployments.add(context.getDeployment());
         else if (log.isTraceEnabled())
            log.trace("No such deployment present: " + name);
      }
      if (deployments.isEmpty() == false)
         undeploy(deployments.toArray(new Deployment[deployments.size()]));
   }

   public void process()
   {
      if (deployers == null)
         throw new IllegalStateException("No deployers");

      lockWrite();
      try
      {
         if (shutdown.get())
            throw new IllegalStateException("The main deployer is shutdown");

         List<DeploymentContext> undeployContexts = null;
         if (undeploy.isEmpty() == false)
         {
            // Undeploy in reverse order (subdeployments first)
            undeployContexts = new ArrayList<DeploymentContext>(undeploy.size());
            for (int i = undeploy.size() - 1; i >= 0; --i)
               undeployContexts.add(undeploy.get(i));
            if (reverted != null)
               Collections.sort(undeployContexts, reverted);
            undeploy.clear();
         }
         if (undeployContexts != null)
         {
            deployers.process(null, undeployContexts);
         }

         try
         {
            processToDeploy();
         }
         catch (DeploymentException e)
         {
            throw new RuntimeException("Error while processing new deployments", e);
         }

         List<DeploymentContext> deployContexts = null;
         if (deploy.isEmpty() == false)
         {
            deployContexts = new ArrayList<DeploymentContext>(deploy);
            if (comparator != null)
               Collections.sort(deployContexts, comparator);
            deploy.clear();
         }
         if (deployContexts != null)
         {
            deployers.process(deployContexts, null);
         }
      }
      finally
      {
         unlockWrite();
      }
   }

   public DeploymentStage getDeploymentStage(String deploymentName) throws DeploymentException
   {
      if (deployers == null)
         throw new IllegalStateException("No deployers");

      lockRead();
      try
      {
         DeploymentContext context = getTopLevelDeploymentContext(deploymentName);
         if (context == null)
            return DeploymentStages.NOT_INSTALLED;
         DeploymentStage result = deployers.getDeploymentStage(context);
         if (result != null)
            return result;
         else
            return DeploymentStages.NOT_INSTALLED;
      }
      catch (Error e)
      {
         throw e;
      }
      catch (Throwable t)
      {
         throw DeploymentException.rethrowAsDeploymentException("Error getting stage for " + deploymentName, t);
      }
      finally
      {
         unlockRead();
      }
   }

   public void change(String deploymentName, DeploymentStage stage) throws DeploymentException
   {
      if (deployers == null)
         throw new IllegalStateException("No deployers");

      lockRead();
      try
      {
         DeploymentContext context = getTopLevelDeploymentContext(deploymentName);
         if (context == null)
            throw new DeploymentException("Top level deployment " + deploymentName + " not found");
         try
         {
            deployers.change(context, stage);
         }
         catch (Error e)
         {
            throw e;
         }
         catch (Throwable t)
         {
            throw DeploymentException.rethrowAsDeploymentException("Error changing context " + deploymentName + " to stage " + stage, t);
         }
      }
      finally
      {
         unlockRead();
      }
   }

   public void prepareShutdown()
   {
      if (deployers != null)
         deployers.shutdown();
   }

   public void shutdown()
   {
      prepareShutdown();
      lockWrite();
      try
      {
         while (topLevelDeployments.isEmpty() == false)
         {
            // Remove all the contexts
            for (DeploymentContext context : topLevelDeployments.values())
            {
               topLevelDeployments.remove(context.getName());
               removeContext(context, true);
            }

            // Do it
            process();
         }

         shutdown.set(true);
      }
      finally
      {
         unlockWrite();
      }
   }

   public void checkComplete() throws DeploymentException
   {
      if (deployers == null)
         throw new IllegalStateException("Null deployers");

      deployers.checkComplete(errorDeployments.values(), missingDeployers.values());
   }

   /**
    * Get the names from deployments.
    *
    * @param deployments the deployments
    * @return depolyment names
    */
   protected static String[] getDeploymentNames(Deployment... deployments)
   {
      if (deployments == null)
         throw new IllegalArgumentException("Null deployments");

      String[] names = new String[deployments.length];
      for(int i = 0; i < deployments.length; i++)
      {
         if (deployments[i] == null)
            throw new IllegalArgumentException("Null deployment: " + i);
         names[i] = deployments[i].getName();
      }
      return names;
   }

   /**
    * Get the deployment contexts.
    *
    * @param names the deployment names
    * @return depolyment contexts
    * @throws DeploymentException if context is not found
    */
   protected DeploymentContext[] getDeploymentContexts(String... names) throws DeploymentException
   {
      if (names == null)
         throw new IllegalArgumentException("Null names");

      DeploymentContext[] contexts = new DeploymentContext[names.length];
      for(int i = 0; i < names.length; i++)
      {
         contexts[i] = getTopLevelDeploymentContext(names[i]);
         if (contexts[i] == null)
            throw new DeploymentException("Deployment context not found: " + names[i]);
      }

      return contexts;
   }

   public void checkComplete(Deployment... deployments) throws DeploymentException
   {
      if (deployments == null)
         throw new IllegalArgumentException("Null deployments");

      checkComplete(getDeploymentNames(deployments));
   }

   public void checkComplete(String... names) throws DeploymentException
   {
      if (names == null)
         throw new IllegalArgumentException("Null names");

      if (deployers == null)
         throw new IllegalStateException("Null deployers");

      deployers.checkComplete(getDeploymentContexts(names));
   }

   public void checkStructureComplete(Deployment... deployments) throws DeploymentException
   {
      if (deployments == null)
         throw new IllegalArgumentException("Null deployments");

      checkStructureComplete(getDeploymentNames(deployments));
   }

   public void checkStructureComplete(String... names) throws DeploymentException
   {
      if (names == null)
         throw new IllegalArgumentException("Null names");

      if (deployers == null)
         throw new IllegalStateException("Null deployers");

      deployers.checkStructureComplete(getDeploymentContexts(names));
   }

   public DeploymentState getDeploymentState(String name)
   {
      DeploymentContext context = getDeploymentContext(name);
      if (context == null)
         return DeploymentState.UNDEPLOYED;
      return context.getState();
   }

   public ManagedDeployment getManagedDeployment(String name) throws DeploymentException
   {
      if (mgtDeploymentCreator == null)
         throw new IllegalArgumentException("Null managed deployment creator.");

      DeploymentContext context = getDeploymentContext(name, true);
      Map<String, ManagedObject> rootMOs = getManagedObjects(context);
      ManagedDeployment root = mgtDeploymentCreator.build(context.getDeploymentUnit(), rootMOs, null);
      for (DeploymentContext childContext : context.getChildren())
      {
         processManagedDeployment(childContext, root);
      }
      return root;
   }

   public Map<String, ManagedObject> getManagedObjects(String name) throws DeploymentException
   {
      DeploymentContext context = getDeploymentContext(name, true);
      return getManagedObjects(context);
   }

   public Map<String, ManagedObject> getManagedObjects(DeploymentContext context) throws DeploymentException
   {
      if (context == null)
         throw new IllegalArgumentException("Null context");

      if (deployers == null)
         throw new IllegalStateException("No deployers");

      return deployers.getManagedObjects(context);
   }

   public Graph<Map<String, ManagedObject>> getDeepManagedObjects(String name) throws DeploymentException
   {
      DeploymentContext context = getDeploymentContext(name);
      Graph<Map<String, ManagedObject>> managedObjectsGraph = new Graph<Map<String, ManagedObject>>();
      Vertex<Map<String, ManagedObject>> parent = new Vertex<Map<String, ManagedObject>>(context.getName());
      managedObjectsGraph.setRootVertex(parent);
      Map<String, ManagedObject> managedObjects = getManagedObjects(context);
      parent.setData(managedObjects);
      processManagedObjects(context, managedObjectsGraph, parent);

      return managedObjectsGraph;
   }

   /**
    * Get the managed objects for a context
    *
    * @param context the context
    * @param graph the graph
    * @param parent the parent node
    * @throws DeploymentException for any problem
    */
   protected void processManagedObjects(DeploymentContext context, Graph<Map<String, ManagedObject>> graph, Vertex<Map<String, ManagedObject>> parent)
      throws DeploymentException
   {
      List<DeploymentContext> children = context.getChildren();
      for(DeploymentContext child : children)
      {
         Vertex<Map<String, ManagedObject>> vertex = new Vertex<Map<String, ManagedObject>>(child.getName());
         Map<String, ManagedObject> managedObjects = getManagedObjects(context);
         vertex.setData(managedObjects);
         graph.addEdge(parent, vertex, 0);
         processManagedObjects(child, graph, vertex);
      }
   }

   /**
    * Recursively process the DeploymentContext into ManagedDeployments.
    *
    * @param context the context
    * @param parent the parent
    * @throws DeploymentException for any error
    */
   protected void processManagedDeployment(DeploymentContext context, ManagedDeployment parent)
      throws DeploymentException
   {
      if (mgtDeploymentCreator == null)
         throw new IllegalArgumentException("Null managed deployment creator.");

      DeploymentUnit unit = context.getDeploymentUnit();
      Map<String, ManagedObject> MOs = getManagedObjects(context);
      ManagedDeployment md = mgtDeploymentCreator.build(unit, MOs, parent);
      for (DeploymentContext childContext : context.getChildren())
      {
         processManagedDeployment(childContext, md);
      }
   }

   /**
    * Determine the structure of a deployment
    *
    * @param deployment the deployment
    * @return the deployment context
    * @throws DeploymentException for an error determining the deployment structure
    */
   private DeploymentContext determineStructure(Deployment deployment) throws DeploymentException
   {
      StructuralDeployers structuralDeployers = getStructuralDeployers();
      if (structuralDeployers != null)
      {
          DeploymentContext result = structuralDeployers.determineStructure(deployment);
          if (result != null)
             return result;
      }
      throw new DeploymentException("No structural deployers.");
   }

   /**
    * Add a context.
    *
    * @param context the context
    * @param addToDeploy should we add to deploy collection
    */
   private void addContext(DeploymentContext context, boolean addToDeploy)
   {
      allDeployments.put(context.getName(), context);
      if (context.getState() == DeploymentState.ERROR)
      {
         log.debug("Not scheduling addition of context already in error: " + context.getName() + " reason=" + context.getProblem());
         return;
      }
      context.setState(DeploymentState.DEPLOYING);
      DeploymentContext parent = context.getParent();
      log.debug("Scheduling deployment: " + context.getName() + " parent=" + parent);

      // Process the top level only
      if (context.isTopLevel() && addToDeploy)
         deploy.add(context);

      // Add all the children
      List<DeploymentContext> children = context.getChildren();
      if (children != null)
      {
         for (DeploymentContext child : children)
            addContext(child, addToDeploy);
      }
   }

   /**
    * Remove a context
    *
    * @param context the context
    * @param addToUndeploy add to undeploy collection
    */
   private void removeContext(DeploymentContext context, boolean addToUndeploy)
   {
      String name = context.getName();
      allDeployments.remove(name);
      errorDeployments.remove(name);
      missingDeployers.remove(name);
      if (DeploymentState.ERROR.equals(context.getState()) == false)
         context.setState(DeploymentState.UNDEPLOYING);
      DeploymentContext parent = context.getParent();
      log.debug("Scheduling undeployment: " + name + " parent=" + parent);

      // Process the top level only
      if (context.isTopLevel() && addToUndeploy)
         undeploy.add(context);

      // Remove all the children
      List<DeploymentContext> children = context.getChildren();
      if (children != null)
      {
         for (DeploymentContext child : children)
            removeContext(child, addToUndeploy);
      }
   }

   /**
    * Lock for read
    */
   protected void lockRead()
   {
      lock.readLock().lock();
   }

   /**
    * Unlock for read
    */
   protected void unlockRead()
   {
      lock.readLock().unlock();
   }

   /**
    * Lock for write
    */
   protected void lockWrite()
   {
      lock.writeLock().lock();
   }

   /**
    * Unlock for write
    */
   protected void unlockWrite()
   {
      lock.writeLock().unlock();
   }
}
