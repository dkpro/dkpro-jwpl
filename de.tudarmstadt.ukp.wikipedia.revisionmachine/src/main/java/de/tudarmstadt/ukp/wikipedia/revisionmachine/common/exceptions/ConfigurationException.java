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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions;

/**
 * ConfigurationException Describes an exception that occurred while accessing
 * the configuration.
 *
 *
 *
 */
@SuppressWarnings("serial")
public class ConfigurationException
	extends Exception
{

	/** Reference to the error key */
	private ErrorKeys key;

	/**
	 * (Constructor) Creates a new ConfigurationException.
	 *
	 * @param description
	 *            message
	 */
	public ConfigurationException(final String description)
	{
		super(description);
	}

	/**
	 * (Constructor) Creates a new ConfigurationException.
	 *
	 * @param e
	 *            inner exception
	 */
	public ConfigurationException(final Exception e)
	{
		super(e);
	}

	/**
	 * (Constructor) Creates a new ConfigurationException.
	 *
	 * @param description
	 *            message
	 * @param e
	 *            inner exception
	 */
	public ConfigurationException(final String description, final Exception e)
	{
		super(description, e);
	}

	/**
	 * (Constructor) Creates a new ConfigurationException.
	 *
	 * @param key
	 *            error key
	 * @param description
	 *            message
	 */
	public ConfigurationException(final ErrorKeys key, final String description)
	{
		super(description);
		this.key = key;
	}

	/**
	 * Returns the error key.
	 *
	 * @return error key
	 */
	public ErrorKeys getKey()
	{
		return this.key;
	}
}
