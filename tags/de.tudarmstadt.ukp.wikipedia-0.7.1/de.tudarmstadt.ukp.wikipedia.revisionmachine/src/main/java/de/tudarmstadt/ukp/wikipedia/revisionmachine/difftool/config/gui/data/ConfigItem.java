/*******************************************************************************
 * Copyright (c) 2011 Ubiquitous Knowledge Processing Lab
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 * 
 * Project Website:
 * 	http://jwpl.googlecode.com
 * 
 * Contributors:
 * 	Torsten Zesch
 * 	Simon Kulessa
 * 	Oliver Ferschke
 ******************************************************************************/
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
