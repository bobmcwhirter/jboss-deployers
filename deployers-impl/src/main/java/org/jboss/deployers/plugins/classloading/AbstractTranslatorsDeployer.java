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
package org.jboss.deployers.plugins.classloading;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.classloader.spi.ClassLoaderDomain;
import org.jboss.classloader.spi.ClassLoaderPolicy;
import org.jboss.classloader.spi.ClassLoaderSystem;
import org.jboss.classloading.spi.dependency.Module;
import org.jboss.classloading.spi.dependency.policy.ClassLoaderPolicyModule;
import org.jboss.classloading.spi.metadata.ClassLoadingTranslatorMetaData;
import org.jboss.classloading.spi.metadata.ClassLoadingTranslatorsMetaData;
import org.jboss.classloading.spi.metadata.TranslatorScope;
import org.jboss.classloading.spi.metadata.helpers.ReflectionTranslator;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractSimpleRealDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.util.loading.Translator;

/**
 * AbstractTranslatorsDeployer.
 * 
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 * @version $Revision: 1.1 $
 */
public class AbstractTranslatorsDeployer extends AbstractSimpleRealDeployer<ClassLoadingTranslatorsMetaData>
{
   /** Attachment key */
   public static final String TRANSLATORS_KEY = "TRANSLATORS_KEY";

   /** The classloader system */
   private ClassLoaderSystem system;

   public AbstractTranslatorsDeployer()
   {
      super(ClassLoadingTranslatorsMetaData.class);
      // add this deployer right after ClassLoader is created
      addInput(ClassLoader.class);
      addOutput(Translator.class);
      setStage(DeploymentStages.CLASSLOADER);
   }

   /**
    * Validate the config
    */
   public void create()
   {
      if (system == null)
         throw new IllegalStateException("The system has not been set");
   }

   public void deploy(DeploymentUnit unit, ClassLoadingTranslatorsMetaData deployment) throws DeploymentException
   {
      Module module = unit.getAttachment(Module.class);
      if (module == null || (module instanceof ClassLoaderPolicyModule == false))
         return;

      Map<Translator, TranslatorScope> added = new HashMap<Translator, TranslatorScope>();
      try
      {
         List<ClassLoadingTranslatorMetaData> translators = deployment.getTranslators();
         if (translators != null)
         {
            ClassLoaderPolicyModule clpm = (ClassLoaderPolicyModule) module;
            ClassLoaderDomain domain = system.getDomain(module.getDeterminedDomainName());
            ClassLoaderPolicy policy = clpm.getPolicy();
            ClassLoader cl = unit.getClassLoader();

            for (ClassLoadingTranslatorMetaData cltmd : translators)
            {
               TranslatorScope scope = cltmd.getScope();
               if (scope == null)
                  throw new IllegalArgumentException("Null scope for: " + cltmd);

               String className = cltmd.getClassName();
               Object instance = cl.loadClass(className).newInstance();
               String methodName = cltmd.getMethod();
               Translator translator;
               if (methodName != null)
                  translator = new ReflectionTranslator(instance, methodName);
               else
                  translator = Translator.class.cast(instance);

               scope.addTranslator(system, domain, policy, translator);
               added.put(translator, scope);
            }

            unit.addAttachment(TRANSLATORS_KEY, added);
         }
      }
      catch (Exception e)
      {
         ClassLoaderPolicyModule clpm = (ClassLoaderPolicyModule) module;
         ClassLoaderDomain domain = system.getDomain(module.getDeterminedDomainName());
         ClassLoaderPolicy policy = clpm.getPolicy();

         for (Map.Entry<Translator, TranslatorScope> entry : added.entrySet())
         {
            entry.getValue().removeTranslator(system, domain, policy, entry.getKey());
         }

         throw DeploymentException.rethrowAsDeploymentException("Error adding translators.", e);
      }
   }

   @SuppressWarnings({"unchecked"})
   @Override
   public void undeploy(DeploymentUnit unit, ClassLoadingTranslatorsMetaData deployment)
   {
      Module module = unit.getAttachment(Module.class);
      if (module == null || (module instanceof ClassLoaderPolicyModule == false))
         return;

      Map<Translator, TranslatorScope> added = unit.getAttachment(TRANSLATORS_KEY, Map.class);
      if (added != null)
      {
         ClassLoaderPolicyModule clpm = (ClassLoaderPolicyModule) module;
         ClassLoaderDomain domain = system.getDomain(module.getDeterminedDomainName());
         ClassLoaderPolicy policy = clpm.getPolicy();

         for (Map.Entry<Translator, TranslatorScope> entry : added.entrySet())
         {
            entry.getValue().removeTranslator(system, domain, policy, entry.getKey());
         }
      }
   }

   /**
    * Get the system.
    * 
    * @return the system.
    */
   public ClassLoaderSystem getSystem()
   {
      return system;
   }

   /**
    * Set the system.
    * 
    * @param system the system.
    */
   public void setSystem(ClassLoaderSystem system)
   {
      this.system = system;
   }
}
