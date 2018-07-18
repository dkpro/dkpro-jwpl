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
 * TimeoutException Describes an exception that occurred because of a timeout
 * event.
 *
 *
 *
 */
@SuppressWarnings("serial")
public class TimeoutException
	extends Exception
{

	/**
	 * (Constructor) Creates a new TimeoutException.
	 *
	 * @param description
	 *            message
	 */
	public TimeoutException(final String description)
	{
		super(description);
	}

	/**
	 * (Constructor) Creates a new TimeoutException.
	 *
	 * @param e
	 *            inner exception
	 */
	public TimeoutException(final Exception e)
	{
		super(e);
	}

	/**
	 * (Constructor) Creates a new TimeoutException.
	 *
	 * @param description
	 *            message
	 * @param e
	 *            inner exception
	 */
	public TimeoutException(final String description, final Exception e)
	{
		super(description, e);
	}
}
