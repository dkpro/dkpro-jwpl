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
 * DecodingException Describes an exception that occurred while encoding the
 * diff information.
 * 
 * 
 * 
 */
@SuppressWarnings("serial")
public class EncodingException
	extends Exception
{

	/**
	 * (Constructor) Creates a new EncodingException.
	 * 
	 * @param description
	 *            message
	 */
	public EncodingException(final String description)
	{
		super(description);
	}

	/**
	 * (Constructor) Creates a new EncodingException.
	 * 
	 * @param e
	 *            inner exception
	 */
	public EncodingException(final Exception e)
	{
		super(e);
	}

	/**
	 * (Constructor) Creates a new EncodingException.
	 * 
	 * @param description
	 *            message
	 * @param e
	 *            inner exception
	 */
	public EncodingException(final String description, final Exception e)
	{
		super(description, e);
	}
}
