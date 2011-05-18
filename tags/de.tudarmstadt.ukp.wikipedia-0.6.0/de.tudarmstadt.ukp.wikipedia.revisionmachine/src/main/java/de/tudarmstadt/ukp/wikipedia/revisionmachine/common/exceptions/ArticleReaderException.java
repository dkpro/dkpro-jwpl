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
 * ArticleReaderException Describes an exception that occurred while reading the
 * articles.
 */
@SuppressWarnings("serial")
public class ArticleReaderException
	extends Exception
{

	/**
	 * (Constructor) Creates a new ArticleReaderException.
	 * 
	 * @param description
	 *            message
	 */
	public ArticleReaderException(final String description)
	{
		super(description);
	}

	/**
	 * (Constructor) Creates a new ArticleReaderException.
	 * 
	 * @param e
	 *            inner exception
	 */
	public ArticleReaderException(final Exception e)
	{
		super(e);
	}

	/**
	 * (Constructor) Creates a new ArticleReaderException.
	 * 
	 * @param description
	 *            message
	 * @param e
	 *            inner exception
	 */
	public ArticleReaderException(final String description, final Exception e)
	{
		super(description, e);
	}
}
