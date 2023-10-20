/*
 * Licensed to the Technische Universität Darmstadt under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Technische Universität Darmstadt 
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dkpro.jwpl.revisionmachine.difftool.config;

import java.util.HashSet;
import java.util.List;

import org.dkpro.jwpl.revisionmachine.common.exceptions.ConfigurationException;
import org.dkpro.jwpl.revisionmachine.common.exceptions.ErrorFactory;
import org.dkpro.jwpl.revisionmachine.common.exceptions.ErrorKeys;
import org.dkpro.jwpl.revisionmachine.difftool.config.gui.control.ConfigSettings;
import org.dkpro.jwpl.revisionmachine.difftool.data.archive.ArchiveDescription;

/**
 * Singleton - Manages the configuration settings for the DiffTool.
 *
 *
 *
 */
public class ConfigurationManager
{

	/** Reference to the created instance */
	private static ConfigurationManager instance;

	/**
	 * Returns the reference to the instance of the ConfigurationManager.
	 *
	 * @return ConfigurationManager
	 *
	 * @throws ConfigurationException
	 *             if the ConfigurationManager has not been created during the
	 *             startup of the application.
	 */
	public static ConfigurationManager getInstance()
		throws ConfigurationException
	{

		if (instance == null) {
			throw ErrorFactory
					.createConfigurationException(ErrorKeys.CONFIGURATION_CONFIGURATIONMANAGER_NOT_INITIALIZED);
		}
		return instance;
	}

	/** Reference to the ConfigurationSettings */
	private final ConfigSettings config;

	/**
	 * (Constructor) Creates the Configuration Manager - This constructor should
	 * only be called during the startup of the DiffTool Application.
	 *
	 * @param config
	 *            Reference to the ConfigurationSettings
	 */
	public ConfigurationManager(final ConfigSettings config)
	{
		instance = this;
		this.config = config;
	}

	/**
	 * Returns the list of input archives.
	 *
	 * @return list of input archives
	 */
	public List<ArchiveDescription> getArchiveList()
	{
		return this.config.getArchiveList();
	}

	/**
	 * Returns the value of the configuration parameter.
	 *
	 * @param configParameter
	 *            Key for the configuration parameter.
	 * @return Value of the configuration parameter
	 *
	 * @throws ConfigurationException
	 *             if the configuration value was not defined or was not set.
	 */
	public Object getConfigParameter(final ConfigurationKeys configParameter)
		throws ConfigurationException
	{


		Object o = this.config.getConfigParameter(configParameter);
		if (o != null) {
			return o;
		}
		//return standard values for some of the parameters if they
		//are not set in the configuration
		//this is only done for uncritical settings, e.g. debug or logging
		//For other parameters, missing settings will produce an exception
		else if(configParameter==ConfigurationKeys.LIMIT_SQL_ARCHIVE_SIZE){
			return Long.MAX_VALUE;
		}
		else if (configParameter==ConfigurationKeys.LIMIT_SQL_FILE_SIZE){
			return Long.MAX_VALUE;
		}
		else if (configParameter==ConfigurationKeys.MODE_STATISTICAL_OUTPUT){
			return false;
		}
		else if (configParameter==ConfigurationKeys.MODE_DEBUG_OUTPUT){
			return false;
		}
		else if (configParameter==ConfigurationKeys.VERIFICATION_ENCODING){
			return false;
		}
		else if (configParameter==ConfigurationKeys.VERIFICATION_DIFF){
			return false;
		}
		else if (configParameter==ConfigurationKeys.LOGGING_PATH_DEBUG){
			return "";
		}
		else if (configParameter==ConfigurationKeys.NAMESPACES_TO_KEEP){
			return new HashSet<Integer>();
		}
		else if (configParameter==ConfigurationKeys.MODE_DATAFILE_OUTPUT){
			return false;
		}
		else{
			throw ErrorFactory
					.createConfigurationException(
							ErrorKeys.CONFIGURATION_CONFIGURATIONMANAGER_UNKNOWN_CONFIG_PARAMETER,
							configParameter.toString());
		}
	}
}
