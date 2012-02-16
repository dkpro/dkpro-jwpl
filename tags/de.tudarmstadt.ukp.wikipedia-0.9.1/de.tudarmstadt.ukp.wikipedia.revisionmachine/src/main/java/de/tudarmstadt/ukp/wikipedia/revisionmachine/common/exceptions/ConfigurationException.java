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
