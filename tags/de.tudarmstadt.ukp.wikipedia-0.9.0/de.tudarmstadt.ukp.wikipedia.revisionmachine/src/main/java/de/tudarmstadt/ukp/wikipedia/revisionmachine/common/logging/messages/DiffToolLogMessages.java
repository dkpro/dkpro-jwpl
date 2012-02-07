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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.common.logging.messages;

import java.util.logging.Level;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.logging.Logger;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.util.Time;

/**
 * This class contains the english localized log messages for DiffTool.
 * 
 * TODO: This file should be replaced with resource files.
 * 
 * 
 * 
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
		logger.logException(Level.SEVERE, "Unexpected Exception", e);
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
		logger.logError(Level.SEVERE, "Unexpected Error", e);
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
