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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.common.logging.messages.consumer;

import java.util.logging.Level;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.SQLConsumerException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.logging.Logger;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.util.Time;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.Task;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.content.Diff;

/**
 * This class contains the english localized log messages for SQLConsumers.
 * 
 * TODO: This file should be replaced with resource files.
 * 
 * 
 * 
 */
public class SQLConsumerLogMessages
{

	/**
	 * Logs the processing of a diff task.
	 * 
	 * @param logger
	 *            reference to the logger
	 * @param diff
	 *            reference to the task
	 * @param time
	 *            time
	 */
	public static void logDiffProcessed(final Logger logger,
			final Task<Diff> diff, final long time)
	{

		logger.logMessage(
				Level.INFO,
				"Generated Entry\t" + Time.toClock(time) + "\t"
						+ diff.toString());
	}

	/**
	 * Logs the creation of an output file.
	 * 
	 * @param logger
	 *            reference to the logger
	 * @param path
	 *            path of the output file
	 */
	public static void logFileCreation(final Logger logger, final String path)
	{

		logger.logMessage(Level.INFO, "New File created:\t" + path);
	}

	/**
	 * Logs the occurance of an OutOfMemoryError while reading a task.
	 * 
	 * @param logger
	 *            reference to the logger
	 * @param task
	 *            reference to the revision task
	 * @param e
	 *            reference to the error
	 */
	public static void logReadTaskOutOfMemoryError(final Logger logger,
			final Task<Diff> task, final OutOfMemoryError e)
	{

		if (task != null) {
			logger.logError(Level.WARNING, "Error while reading a task: "
					+ task.toString(), e);
		}
		else {
			logger.logError(Level.WARNING,
					"Error while reading an unknown task", e);
		}
	}

	/**
	 * Logs the occurance of an SqlConsumerException.
	 * 
	 * @param logger
	 *            reference to the logger
	 * @param e
	 *            reference to the exception
	 */
	public static void logSQLConsumerException(final Logger logger,
			final SQLConsumerException e)
	{

		logger.logException(Level.SEVERE, "SQLConsumerException", e);
	}

	/** No object - utility class */
	private SQLConsumerLogMessages()
	{
	}
}
