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
package org.jboss.deployers.vfs.plugins.xb;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlTransient;

import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.BeanMetaDataFactory;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.vfs.spi.deployer.SchemaResolverDeployer;
import org.jboss.xb.annotations.JBossXmlSchema;

/**
 * Shortcut for defining new schema resolver deployer.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
@JBossXmlSchema(namespace="urn:jboss:deployers:2.0", elementFormDefault=XmlNsForm.QUALIFIED)
@XmlRootElement(name="jbossxb-parser")
@XmlType(name="parserType")
public class SchemaResolverDeployerMetaData implements Serializable, BeanMetaDataFactory
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;

   private String name;
   private String metadata;
   private String suffix;
   private String fileName;
   private boolean useSchemaValidation = true;
   private boolean useValidation = true;
   private boolean registerWithJBossXB;
   private String jarExtension;
   private boolean includeDeploymentFile;
   private boolean buildManagedObject;

   @XmlTransient
   public List<BeanMetaData> getBeans()
   {
      if (getSuffix() == null && getFileName() == null)
         throw new IllegalArgumentException("Both suffix and file-name are null!");

      BeanMetaDataBuilder builder = BeanMetaDataBuilder.createBuilder(getName(), SchemaResolverDeployer.class.getName());
      builder.addConstructorParameter(Class.class.getName(), getMetadata());
      builder.addPropertyMetaData("suffix", getSuffix());
      builder.addPropertyMetaData("name", getFileName());
      builder.addPropertyMetaData("useSchemaValidation", isUseSchemaValidation());
      builder.addPropertyMetaData("useValidation", isUseValidation());
      builder.addPropertyMetaData("registerWithJBossXB", isRegisterWithJBossXB());
      builder.addPropertyMetaData("includeDeploymentFile", isIncludeDeploymentFile());
      builder.addPropertyMetaData("buildManagedObject", isBuildManagedObject());
      return Collections.singletonList(builder.getBeanMetaData());
   }

   public String getName()
   {
      return name;
   }

   @XmlAttribute(required = true)
   public void setName(String name)
   {
      this.name = name;
   }

   public String getMetadata()
   {
      return metadata;
   }

   @XmlAttribute(required = true)
   public void setMetadata(String metadata)
   {
      this.metadata = metadata;
   }

   public String getSuffix()
   {
      return suffix;
   }

   @XmlAttribute
   public void setSuffix(String suffix)
   {
      this.suffix = suffix;
   }

   public String getFileName()
   {
      return fileName;
   }

   @XmlAttribute(name = "file-name")
   public void setFileName(String fileName)
   {
      this.fileName = fileName;
   }

   public boolean isUseSchemaValidation()
   {
      return useSchemaValidation;
   }

   @XmlAttribute(name = "use-schema-validation")
   public void setUseSchemaValidation(boolean useSchemaValidation)
   {
      this.useSchemaValidation = useSchemaValidation;
   }

   public boolean isUseValidation()
   {
      return useValidation;
   }

   @XmlAttribute(name = "use-validation")
   public void setUseValidation(boolean useValidation)
   {
      this.useValidation = useValidation;
   }

   public boolean isRegisterWithJBossXB()
   {
      return registerWithJBossXB;
   }

   @XmlAttribute(name = "register-with-jbossxb")
   public void setRegisterWithJBossXB(boolean registerWithJBossXB)
   {
      this.registerWithJBossXB = registerWithJBossXB;
   }

   public String getJarExtension()
   {
      return jarExtension;
   }

   @XmlAttribute(name = "jar-extension")
   public void setJarExtension(String jarExtension)
   {
      this.jarExtension = jarExtension;
   }

   public boolean isIncludeDeploymentFile()
   {
      return includeDeploymentFile;
   }

   @XmlAttribute(name = "include-deployment-file")
   public void setIncludeDeploymentFile(boolean includeDeploymentFile)
   {
      this.includeDeploymentFile = includeDeploymentFile;
   }

   public boolean isBuildManagedObject()
   {
      return buildManagedObject;
   }

   @XmlAttribute(name = "build-managed-object")
   public void setBuildManagedObject(boolean buildManagedObject)
   {
      this.buildManagedObject = buildManagedObject;
   }
}
