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
 * DiffException Describes an exception that occurred while calculating the
 * diff.
 *
 *
 *
 */
@SuppressWarnings("serial")
public class DiffException
	extends Exception
{

	/**
	 * (Constructor) Creates a new DiffException.
	 *
	 * @param description
	 *            message
	 */
	public DiffException(final String description)
	{
		super(description);
	}

	/**
	 * (Constructor) Creates a new DiffException.
	 *
	 * @param e
	 *            inner exception
	 */
	public DiffException(final Exception e)
	{
		super(e);
	}

	/**
	 * (Constructor) Creates a new DiffException.
	 *
	 * @param description
	 *            message
	 * @param e
	 *            inner exception
	 */
	public DiffException(final String description, final Exception e)
	{
		super(description, e);
	}
}
