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
package org.jboss.deployers.vfs.spi.deployer;

import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.virtual.VirtualFile;
import org.jboss.virtual.VFSInputSource;
import org.jboss.xb.binding.ObjectModelFactory;
import org.xml.sax.InputSource;

/**
 * ObjectModelFactoryDeployer extends the AbstractParsingDeployer to add an
 * abstract JBossXB ObjectModelFactory accessor that is used from within an
 * overriden parse(DeploymentUnit unit, VirtualFile file) to unmarshall the xml
 * document represented by file into an instance of deploymentType T.
 * 
 * @param <T> the expected type 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @author Scott.Stark@jboss.org
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 * @version $Revision: 1.1 $
 */
public abstract class ObjectModelFactoryDeployer<T> extends JBossXBDeployer<T>
{
   /**
    * Create a new SchemaResolverDeployer.
    * 
    * @param output the output
    * @throws IllegalArgumentException for a null output
    */
   public ObjectModelFactoryDeployer(Class<T> output)
   {
      super(output);
   }

   @Override
   protected T parse(VFSDeploymentUnit unit, VirtualFile file, T root) throws Exception
   {
      InputSource source = new VFSInputSource(file);
      return getHelper().parse(source, root, getObjectModelFactory(root));
   }

   /**
    * Get the object model factory 
    * 
    * @param root - possibly null pre-existing root
    * @return the object model factory
    */
   protected abstract ObjectModelFactory getObjectModelFactory(T root);
}
