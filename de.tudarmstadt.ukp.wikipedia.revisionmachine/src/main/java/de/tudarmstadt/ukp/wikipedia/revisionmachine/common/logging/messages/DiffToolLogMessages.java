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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.common.logging.messages;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.logging.Logger;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.util.Time;
import org.slf4j.event.Level;

/**
 * This class contains the english localized log messages for DiffTool.
 *
 * TODO: This file should be replaced with resource files.
 */
public class DiffToolLogMessages
{

	/** No object - utility class */
	private DiffToolLogMessages()
	{
	}

	/**
	 * Logs the start of the diff tool.
	 *
	 * @param logger
	 *            reference to the logger
	 */
	public static void logInitialization(final Logger logger)
	{
		logger.logMessage(Level.INFO, "DiffTool initialized [LogLevel: "
				+ logger.getLogLevel() + "]");
	}

	/**
	 * Logs the status of the diff tool.
	 *
	 * @param logger
	 *            reference to the logger
	 * @param time
	 *            time since start
	 * @param articleConsumer
	 *            number of active article consumers
	 * @param diffConsumer
	 *            number of active diff consumers
	 * @param sqlConsumer
	 *            number of active sql consumers
	 * @param archiveState
	 *            state of the arcive producer
	 * @param articleState
	 *            state of the article producer
	 * @param diffState
	 *            state of the diff producer
	 */
	public static void logStatus(final Logger logger, final long time,
			final int articleConsumer, final int diffConsumer,
			final int sqlConsumer, final boolean archiveState,
			final boolean articleState, final boolean diffState)
	{

		logger.logMessage(Level.INFO,
				"\r\nDiffTool-Status-Report [" + Time.toClock(time) + "]"
						+ "\r\nConsumerProducer \t[" + articleConsumer + " | "
						+ diffConsumer + " | " + sqlConsumer + "]"
						+ "\r\nArchiveProducer\t" + archiveState
						+ "\r\nArticleProducer\t" + articleState
						+ "\r\nDiffProducer   \t" + diffState + "\r\n");
	}

	/**
	 * Logs an exception.
	 *
	 * @param logger
	 *            reference to the logger
	 * @param e
	 *            reference to the exception
	 */
	public static void logException(final Logger logger, final Exception e)
	{
		logger.logException(Level.ERROR, "Unexpected Exception", e);
	}

	/**
	 * Logs an error.
	 *
	 * @param logger
	 *            reference to the logger
	 * @param e
	 *            reference to the error
	 */
	public static void logError(final Logger logger, final Error e)
	{
		logger.logError(Level.ERROR, "Unexpected Error", e);
	}

	/**
	 * Logs the shutdown of the logger.
	 *
	 * @param logger
	 *            reference to the logger
	 * @param endTime
	 *            time since start
	 */
	public static void logShutdown(final Logger logger, final long endTime)
	{
		logger.logMessage(Level.INFO,
				"DiffTool initiates SHUTDOWN\t" + Time.toClock(endTime));
	}
}
