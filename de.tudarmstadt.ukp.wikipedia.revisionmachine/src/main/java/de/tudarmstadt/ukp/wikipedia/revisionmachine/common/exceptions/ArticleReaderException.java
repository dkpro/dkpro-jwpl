/*******************************************************************************
 * Copyright 2016
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
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
