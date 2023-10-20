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
package org.dkpro.jwpl.revisionmachine.common.logging.messages.consumer;

import org.dkpro.jwpl.revisionmachine.api.Revision;
import org.dkpro.jwpl.revisionmachine.common.exceptions.ArticleReaderException;
import org.dkpro.jwpl.revisionmachine.common.logging.Logger;
import org.dkpro.jwpl.revisionmachine.common.util.MathUtilities;
import org.dkpro.jwpl.revisionmachine.common.util.Time;
import org.dkpro.jwpl.revisionmachine.difftool.consumer.article.ArticleReaderInterface;
import org.dkpro.jwpl.revisionmachine.difftool.data.archive.ArchiveDescription;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.Task;
import org.dkpro.jwpl.revisionmachine.difftool.data.tasks.TaskTypes;
import org.slf4j.event.Level;

/**
 * This class contains the english localized log messages for ArticleConsumers.
 *
 * TODO: This file should be replaced with resource files.
 *
 *
 *
 */
public final class ArticleConsumerLogMessages
{

	/**
	 * Logs the retrieval of an archive descriptor.
	 *
	 * @param logger
	 *            reference to the logger
	 * @param archive
	 *            reference to the archive descriptor
	 */
	public static void logArchiveRetrieved(final Logger logger,
			final ArchiveDescription archive)
	{

		logger.logMessage(Level.INFO, "Retrieved archive " + archive.toString()
				+ " successfully");
	}

	/**
	 * Logs the reading of an revision task.
	 *
	 * @param logger
	 *            reference to the logger
	 * @param article
	 *            reference to the revision task
	 * @param time
	 *            time needed for the operation
	 */
	public static void logArticleRead(final Logger logger,
			final Task<Revision> article, final long time)
	{

		logger.logMessage(Level.INFO, "Read article\t" + Time.toClock(time)
				+ "\t" + article.toString());
	}

	/**
	 * Logs the reading of an revision task.
	 *
	 * @param logger
	 *            reference to the logger
	 * @param article
	 *            reference to the revision task
	 * @param time
	 *            time needed for the operation
	 * @param position
	 *            input file position
	 */
	public static void logArticleRead(final Logger logger,
			final Task<Revision> article, final long time, final long position)
	{

		logger.logMessage(Level.INFO, "Read article\t" + Time.toClock(time)
				+ "\t" + article.toString() + "\t" + position);
	}

	/**
	 * Logs the occurance of an error while retrieving the input file.
	 *
	 * @param logger
	 *            reference to the logger
	 * @param archive
	 *            reference to the archive
	 * @param e
	 *            reference to the error
	 */
	public static void logErrorRetrieveArchive(final Logger logger,
			final ArchiveDescription archive, final Error e)
	{

		logger.logError(Level.ERROR, "Error while accessing archive "
				+ archive.toString(), e);
	}

	/**
	 * Logs the occurance of an exception while retrieving the input file.
	 *
	 * @param logger
	 *            reference to the logger
	 * @param archive
	 *            reference to the archive
	 * @param e
	 *            reference to the exception
	 */
	public static void logExceptionRetrieveArchive(final Logger logger,
			final ArchiveDescription archive, final Exception e)
	{

		logger.logException(Level.ERROR, "Exception while accessing archive "
				+ archive.toString(), e);
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
	 * Logs that no more archives are available.
	 *
	 * @param logger
	 *            reference to the logger
	 */
	public static void logNoMoreArchives(final Logger logger)
	{

		logger.logMessage(Level.INFO,
				"Consumer initiates SHUTDOWN: no more archives available.");
	}

	/**
	 * Logs that no more articles are available.
	 *
	 * @param logger
	 *            reference to the logger
	 * @param archive
	 *            reference to the archive descriptor
	 */
	public static void logNoMoreArticles(final Logger logger,
			final ArchiveDescription archive)
	{

		logger.logMessage(Level.INFO, "Archive " + archive.toString()
				+ " contains no more articles");
	}

	/**
	 * Logs an occurance of an exception while reading a task.
	 *
	 * @param logger
	 *            reference to the logger
	 * @param task
	 *            reference to the task
	 * @param e
	 *            reference to the exception
	 */
	public static void logReadTaskException(final Logger logger,
			final Task<Revision> task, final Exception e)
	{

		if (task != null) {
			logger.logException(Level.ERROR, "Error while reading a task: "
					+ task.toString(), e);
		}
		else {
			logger.logException(Level.ERROR,
					"Error while reading an unknown task", e);
		}
	}

	/**
	 * Logs an occurance of an OutOfMemoryError while reading a task.
	 *
	 * @param logger
	 *            reference to the logger
	 * @param task
	 *            reference to the task
	 * @param e
	 *            reference to the error
	 */
	public static void logReadTaskOutOfMemoryError(final Logger logger,
			final Task<Revision> task, final OutOfMemoryError e)
	{

		if (task != null) {
			logger.logError(Level.WARN, "Error while reading a task: "
					+ task.toString(), e);
		}
		else {
			logger.logError(Level.WARN,
					"Error while reading an unknown task", e);
		}
	}

	/**
	 * Logs the failed retrieval of an archive descriptor.
	 *
	 * @param logger
	 *            reference to the logger
	 */
	public static void logRetrieveArchiveFailed(final Logger logger)
	{

		logger.logMessage(Level.WARN, "Consumer failed to obtain an archive");
	}

	/**
	 * Logs the status of the article consumer.
	 *
	 * @param logger
	 *            reference to the logger
	 * @param articleReader
	 *            reference to the ArticleReader
	 * @param startTime
	 *            start time
	 * @param sleepingTime
	 *            time the consumer has slept
	 * @param workingTime
	 *            time the consumer was working
	 */
	public static void logStatus(final Logger logger,
			final ArticleReaderInterface articleReader, final long startTime,
			final long sleepingTime, final long workingTime)
	{

		String message = "Consumer-Status-Report ["
				+ Time.toClock(System.currentTimeMillis() - startTime) + "]";

		if (articleReader != null) {
			message += "\tPOSITION <" + articleReader.getBytePosition() + ">";
		}

		message += "\tEFFICIENCY\t "
				+ MathUtilities.percentPlus(workingTime, sleepingTime)
				+ "\tWORK  [" + Time.toClock(workingTime) + "]" + "\tSLEEP ["
				+ Time.toClock(sleepingTime) + "]";

		logger.logMessage(Level.DEBUG, message);
	}

	/**
	 * Logs the occurance of an ArticleReaderException.
	 *
	 * @param logger
	 *            reference to the logger
	 * @param e
	 *            reference to the exception
	 */
	public static void logTaskReaderException(final Logger logger,
			final ArticleReaderException e)
	{

		logger.logException(Level.ERROR, "TaskReaderException", e);
	}

	/** No object - utility class */
	private ArticleConsumerLogMessages()
	{
	}
}
