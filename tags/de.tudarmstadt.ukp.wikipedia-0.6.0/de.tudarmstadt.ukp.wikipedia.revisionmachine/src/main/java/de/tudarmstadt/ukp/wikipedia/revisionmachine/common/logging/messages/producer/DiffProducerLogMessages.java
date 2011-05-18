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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.common.logging.messages.producer;

import java.util.logging.Level;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.logging.Logger;
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
public class DiffProducerLogMessages
{

	/**
	 * Logs the banning request.
	 * 
	 * @param logger
	 *            reference to the logger
	 * @param articleId
	 *            articleId
	 */
	public static void logBannAdded(final Logger logger, final String articleId)
	{

		logger.logMessage(Level.INFO, "Added bann: " + articleId);
	}

	/**
	 * Logs the receiving of an adding for a bannished task.
	 * 
	 * @param logger
	 *            reference to the logger
	 * @param request
	 *            request
	 */
	public static void logBannedTaskAdded(final Logger logger,
			final String request)
	{

		logger.logMessage(Level.INFO,
				"Add-Request for banned PartialDiff received: " + request);
	}

	/**
	 * Logs the removal of a banished (partial) task.
	 * 
	 * @param logger
	 *            reference to the logger
	 * @param request
	 *            bann
	 */
	public static void logBannedTaskRemoved(final Logger logger,
			final String request)
	{

		logger.logMessage(Level.INFO, "Removed banned PartialDiff: " + request);
	}

	/**
	 * Logs the removal of a banning.
	 * 
	 * @param logger
	 *            reference to the logger
	 * @param articleId
	 *            articleId
	 */
	public static void logBannRemoved(final Logger logger,
			final String articleId)
	{

		logger.logMessage(Level.INFO,
				"Banned PartialDiff removed from blacklist: " + articleId);
	}

	/**
	 * Logs the adding of a diff task.
	 * 
	 * @param logger
	 *            reference to the logger
	 * @param consumerID
	 *            name of the consumer who added the task
	 * @param diff
	 *            reference to the diff task
	 */
	public static void logDiffAdded(final Logger logger,
			final String consumerID, final Task<Diff> diff)
	{

		logger.logMessage(Level.INFO, "Added Diff                   \t"
				+ consumerID + "\t" + diff.toString());
	}

	/**
	 * Logs the return of the diff tasks.
	 * 
	 * @param logger
	 *            reference to the logger
	 * @param diff
	 *            reference to the diff task
	 */
	public static void logDiffReturned(final Logger logger,
			final Task<Diff> diff)
	{

		logger.logMessage(Level.INFO,
				"Returned Diff                \t" + diff.toString());
	}

	/**
	 * Logs the adding of a partial diff task.
	 * 
	 * @param logger
	 *            reference to the logger
	 * @param consumerID
	 *            name of the consumer who added the task
	 * @param partialDiff
	 *            reference to the diff task
	 */
	public static void logPartialDiffAdded(final Logger logger,
			final String consumerID, final Task<Diff> partialDiff)
	{

		logger.logMessage(Level.INFO, "Added PartialDiff            \t<"
				+ consumerID + "\t" + partialDiff.uniqueIdentifier() + ", "
				+ partialDiff.toString() + ">");
	}

	/**
	 * Logs the request of a partail diff task.
	 * 
	 * @param logger
	 *            reference to the logger
	 * @param request
	 *            request
	 */
	public static void logPartialDiffRequest(final Logger logger,
			final String request)
	{

		logger.logMessage(Level.INFO, "Requested unknown PartialDiff\t<"
				+ request + ">");
	}

	/**
	 * Logs the return of a partial diff tasks.
	 * 
	 * @param logger
	 *            reference to the logger
	 * @param partialDiff
	 *            reference to the diff task
	 */
	public static void logPartialDiffReturned(final Logger logger,
			final Task<Diff> partialDiff)
	{

		logger.logMessage(
				Level.INFO,
				"Returned PartialDiff         \t<"
						+ partialDiff.uniqueIdentifier() + ", "
						+ partialDiff.toString() + ">");
	}

	/**
	 * Logs the status of the producer.
	 * 
	 * @param logger
	 *            reference to the logger
	 * @param byteTotalSize
	 *            total size of storage used
	 * @param sizePool
	 *            size of pool A
	 * @param sizePartialPool
	 *            size of pool B
	 */
	public static void logStatus(final Logger logger, final long byteTotalSize,
			final int sizePool, final int sizePartialPool)
	{

		logger.logMessage(Level.FINE, "TotalSize: " + byteTotalSize
				+ "\tDiffPool: " + sizePool + "\tPartialDiffPool: "
				+ sizePartialPool);
	}

	/** No object - utility class */
	private DiffProducerLogMessages()
	{
	}
}
