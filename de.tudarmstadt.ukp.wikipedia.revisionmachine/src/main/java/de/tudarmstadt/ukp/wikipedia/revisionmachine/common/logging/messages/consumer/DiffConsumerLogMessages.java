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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.common.logging.messages.consumer;

import java.util.logging.Level;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.Revision;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.DiffException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.logging.Logger;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.util.Time;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.Task;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.TaskTypes;

/**
 * This class contains the english localized log messages for DiffConsumers.
 *
 * TODO: This file should be replaced with resource files.
 *
 *
 *
 */
public class DiffConsumerLogMessages
{

	/**
	 * Logs the processing of a revision task.
	 *
	 * @param logger
	 *            reference to the logger
	 * @param article
	 *            reference to the revision task
	 * @param time
	 *            time
	 */
	public static void logArticleProcessed(final Logger logger,
			final Task<Revision> article, long time)
	{

		logger.logMessage(Level.INFO, "Generated Diff\t" + Time.toClock(time)
				+ "\t" + article.toString());
	}

	/**
	 * Logs the processing of a revision task.
	 *
	 * @param logger
	 *            reference to the logger
	 * @param article
	 *            reference to the revision task
	 * @param time
	 *            time
	 * @param transmittingTime
	 *            time that the transfer of data to the producer needed
	 */
	public static void logArticleProcessed(final Logger logger,
			final Task<Revision> article, long time, long transmittingTime)
	{

		logger.logMessage(
				Level.INFO,
				"Generated Diff\t" + Time.toClock(time) + "\t"
						+ Time.toClock(transmittingTime) + "\t"
						+ article.toString());
	}

	/**
	 * Logs the occurance of a DiffException.
	 *
	 * @param logger
	 *            reference to the logger
	 * @param e
	 *            reference to the exception
	 */
	public static void logDiffException(final Logger logger,
			final DiffException e)
	{

		logger.logException(Level.SEVERE, "DiffException", e);
	}

	/**
	 * Logs the receival of an end task.
	 *
	 * @param logger
	 *            reference to the logger
	 */
	public static void logEndTaskReceived(final Logger logger)
	{

		logger.logMessage(Level.INFO,
				"Consumer initiates SHUTDOWN: EndTask received");
	}

	/**
	 * Logs the occurance of an invalid task type.
	 *
	 * @param logger
	 *            reference to the logger
	 * @param type
	 *            type of task
	 */
	public static void logInvalidTaskType(final Logger logger,
			final TaskTypes type)
	{

		logger.logMessage(Level.INFO, "Invalid TaskType: " + type);
	}

	/**
	 * Logs the occurance of an TaskOutOfMemoryError while reading a revision
	 * task.
	 *
	 * @param logger
	 *            reference to the logger
	 * @param task
	 *            reference to the revision task
	 * @param e
	 *            reference to the error
	 */
	public static void logReadTaskOutOfMemoryError(final Logger logger,
			final Task<Revision> task, final OutOfMemoryError e)
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
	 * Logs the start of the processing of an revision task.
	 *
	 * @param logger
	 *            reference to the logger
	 * @param article
	 *            reference to the revision task
	 * @param time
	 *            time
	 * @param transmittingTime
	 *            time that the transfer of data to the producer needed
	 */
	public static void logStartArticleProcessing(final Logger logger,
			final Task<Revision> article, long time, long transmittingTime)
	{

		logger.logMessage(Level.FINE,
				"Start Procssing Task\t" + article.toString());
	}

	/** No object - utility class */
	private DiffConsumerLogMessages()
	{
	}
}
