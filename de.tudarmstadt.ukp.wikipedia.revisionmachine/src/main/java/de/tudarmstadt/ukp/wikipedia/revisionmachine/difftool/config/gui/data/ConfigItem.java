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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.gui.data;

/**
 * This class represents configuration verfication messages.
 *
 *
 *
 */
public class ConfigItem
{

	/** Type of message */
	private ConfigItemTypes type;

	/** Type of error */
	private ConfigErrorKeys key;

	/** Message */
	private String message;

	/**
	 * (Constructor) Creates a new ConfigItem
	 *
	 * @param type
	 *            Type of message
	 * @param key
	 *            Type of error
	 * @param message
	 *            Message
	 */
	public ConfigItem(final ConfigItemTypes type, final ConfigErrorKeys key,
			final String message)
	{

		this.type = type;
		this.key = key;
		this.message = message;
	}

	/**
	 * Returns the type of error.
	 *
	 * @return type of error
	 */
	public ConfigErrorKeys getKey()
	{
		return key;
	}

	/**
	 * Returns the message.
	 *
	 * @return message
	 */
	public String getMessage()
	{
		return message;
	}

	/**
	 * Returns the item type.
	 *
	 * @return item type
	 */
	public ConfigItemTypes getType()
	{
		return type;
	}
}
