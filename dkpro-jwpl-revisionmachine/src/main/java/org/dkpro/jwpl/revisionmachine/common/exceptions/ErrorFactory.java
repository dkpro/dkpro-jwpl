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
package org.dkpro.jwpl.revisionmachine.common.exceptions;

/**
 * This utility class contains method two create exceptions.
 *
 *
 *
 */
public final class ErrorFactory
{

	/** No object - Utility class */
	private ErrorFactory()
	{
	}

	/*
	 * ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	 */

	/**
	 * Creates a RuntimeException object.
	 *
	 * @param errorId
	 *            reference to the error identifier
	 * @return RuntimeException
	 */
	public static RuntimeException createRuntimeException(
			final ErrorKeys errorId)
	{

		return new RuntimeException(errorId.toString());
	}

	/*
	 * +ARTICLE+READER+EXCEPTION+++++++++++++++++++++++++++++++++++++++++++++++++
	 */

	/**
	 * Creates a ArticleReaderException object.
	 *
	 * @param errorId
	 *            reference to the error identifier
	 * @return ArticleReaderException
	 */
	public static ArticleReaderException createArticleReaderException(
			final ErrorKeys errorId)
	{

		return new ArticleReaderException(errorId.toString());
	}

	/*
	 * +CONFIGURATION+EXCEPTION++++++++++++++++++++++++++++++++++++++++++++++++++
	 */

	/**
	 * Creates a ConfigurationException object.
	 *
	 * @param errorId
	 *            reference to the error identifier
	 * @return ConfigurationException
	 */
	public static ConfigurationException createConfigurationException(
			final ErrorKeys errorId)
	{

		return new ConfigurationException(errorId.toString());
	}

	/**
	 * Creates a ConfigurationException object.
	 *
	 * @param errorId
	 *            reference to the error identifier
	 * @param message
	 *            additional error message
	 * @return ConfigurationException
	 */
	public static ConfigurationException createConfigurationException(
			final ErrorKeys errorId, final String message)
	{

		return new ConfigurationException(errorId.toString() + ":\r\n"
				+ message);
	}

	/*
	 * +TIMEOUT+EXCEPTION++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	 */

	/**
	 * Creates a TimeoutException object.
	 *
	 * @param errorId
	 *            reference to the error identifier
	 * @param sleepPeriod
	 *            time value
	 * @return TimeoutException
	 */
	public static TimeoutException createTimeoutException(
			final ErrorKeys errorId, final long sleepPeriod)
	{

		return new TimeoutException(errorId.toString() + "\r\n"
				+ "Timeout after " + sleepPeriod + " miliseconds.");
	}

	/*
	 * +LOGGING+EXCEPTION++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	 */

	/**
	 * Creates a LoggingException object.
	 *
	 * @param errorId
	 *            reference to the error identifier
	 * @return LoggingException
	 */
	public static LoggingException createLoggingException(
			final ErrorKeys errorId)
	{

		return new LoggingException(errorId.toString());
	}

	/**
	 * Creates a LoggingException object.
	 *
	 * @param errorId
	 *            reference to the error identifier
	 * @param e
	 *            inner exception
	 * @return LoggingException
	 */
	public static LoggingException createLoggingException(
			final ErrorKeys errorId, final Exception e)
	{

		return new LoggingException(errorId.toString(), e);
	}

	/*
	 * +DIFF+EXCEPTION+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	 */

	/**
	 * Creates a DiffException object.
	 *
	 * @param errorId
	 *            reference to the error identifier
	 * @param message
	 *            additional message
	 * @return DiffException
	 */
	public static DiffException createDiffException(final ErrorKeys errorId,
			final String message)
	{

		return new DiffException(errorId.toString() + ":\r\n" + message);
	}

	/**
	 * Creates a DiffException object.
	 *
	 * @param errorId
	 *            reference to the error identifier
	 * @param message
	 *            additional message
	 * @param e
	 *            inner exception
	 * @return DiffException
	 */
	public static DiffException createDiffException(final ErrorKeys errorId,
			final String message, final Exception e)
	{

		return new DiffException(errorId.toString() + ":\r\n" + message, e);
	}

	/*
	 * +ENCODING+EXCEPTION+++++++++++++++++++++++++++++++++++++++++++++++++++++++
	 */

	/**
	 * Creates an EncodingException object.
	 *
	 * @param errorId
	 *            reference to the error identifier
	 * @return EncodingException
	 */
	public static EncodingException createEncodingException(
			final ErrorKeys errorId)
	{

		return new EncodingException(errorId.toString());
	}

	/**
	 * Creates an EncodingException object.
	 *
	 * @param errorId
	 *            reference to the error identifier
	 * @param message
	 *            additional message
	 * @return EncodingException
	 */
	public static EncodingException createEncodingException(
			final ErrorKeys errorId, final String message)
	{

		return new EncodingException(errorId.toString() + ":\r\n" + message);
	}

	/**
	 * Creates an EncodingException object.
	 *
	 * @param errorId
	 *            reference to the error identifier
	 * @param message
	 *            additional message
	 * @param e
	 *            inner exception
	 * @return EncodingException
	 */
	public static EncodingException createEncodingException(
			final ErrorKeys errorId, final String message, final Exception e)
	{

		return new EncodingException(errorId.toString() + ":\r\n" + message, e);
	}

	/*
	 * +DECODING+EXCEPTION+++++++++++++++++++++++++++++++++++++++++++++++++++++++
	 */

	/**
	 * Creates a DecodingException object.
	 *
	 * @param errorId
	 *            reference to the error identifier
	 * @return DecodingException
	 */
	public static DecodingException createDecodingException(
			final ErrorKeys errorId)
	{

		return new DecodingException(errorId.toString());
	}

	/**
	 * Creates a DecodingException object.
	 *
	 * @param errorId
	 *            reference to the error identifier
	 * @param message
	 *            additional message
	 * @return DecodingException
	 */
	public static DecodingException createDecodingException(
			final ErrorKeys errorId, final String message)
	{

		return new DecodingException(errorId.toString() + ":\r\n" + message);
	}

	/**
	 * Creates a DecodingException object.
	 *
	 * @param errorId
	 *            reference to the error identifier
	 * @param message
	 *            additional message
	 * @param e
	 *            inner exception
	 * @return DecodingException
	 */
	public static DecodingException createDecodingException(
			final ErrorKeys errorId, final String message, final Exception e)
	{

		return new DecodingException(errorId.toString() + ":\r\n" + message, e);
	}

	/*
	 * +UNCOMPRESSED+CONSUMER+EXCEPTION++++++++++++++++++++++++++++++++++++++++++++++++++++
	 * +
	 */

	/**
	 * Creates a SQLConsumerException object.
	 *
	 * @param errorId
	 *            reference to the error identifier
	 * @param e
	 *            inner exception
	 * @return SQLConsumerException
	 */
	public static SQLConsumerException createSQLConsumerException(
			final ErrorKeys errorId, final Exception e)
	{

		return new SQLConsumerException(errorId.toString(), e);
	}

	/**
	 * Creates a SQLConsumerException object.
	 *
	 * @param errorId
	 *            reference to the error identifier
	 * @param message
	 *            additional message
	 * @return SQLConsumerException
	 */
	public static SQLConsumerException createSQLConsumerException(
			final ErrorKeys errorId, final String message)
	{

		return new SQLConsumerException(errorId.toString() + ":\r\n" + message);
	}

	/**
	 * Creates a SQLConsumerException object.
	 *
	 * @param errorId
	 *            reference to the error identifier
	 * @param message
	 *            additional message
	 * @param e
	 *            inner exception
	 * @return SQLConsumerException
	 */
	public static SQLConsumerException createSQLConsumerException(
			final ErrorKeys errorId, final String message, final Exception e)
	{

		return new SQLConsumerException(errorId.toString() + ":\r\n" + message,
				e);
	}
}
