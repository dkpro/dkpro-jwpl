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

import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.Revision;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.logging.Logger;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.Task;

/**
 * This class contains the english localized log messages for ArticleProducer.
 * 
 * TODO: This file should be replaced with resource files.
 * 
 * 
 * 
 */
public final class ArticleProducerLogMessages
{

	/**
	 * Logs the adding of a revision task
	 * 
	 * @param logger
	 *            reference to the logger
	 * @param article
	 *            reference to the revision task
	 */
	public static void logArticleAdded(final Logger logger,
			final Task<Revision> article)
	{

		logger.logMessage(Level.INFO, "Added Article                   \t"
				+ article.toString());
	}

	/**
	 * Logs the return of a revision task.
	 * 
	 * @param logger
	 *            reference to the logger
	 * @param article
	 *            reference to the revision task
	 */
	public static void logArticleReturned(final Logger logger,
			final Task<Revision> article)
	{

		logger.logMessage(Level.INFO, "Returned Article                \t"
				+ article.toString());
	}

	/**
	 * Logs the banning request.
	 * 
	 * @param logger
	 *            reference to the logger
	 * @param request
	 *            articleId
	 */
	public static void logBannAdded(final Logger logger, final String request)
	{

		logger.logMessage(Level.INFO, "Added bann: " + request);
	}

	/**
	 * Logs the receiving of an adding for a bannished task.
	 * 
	 * @param logger
	 *            reference to the logger
	 * @param articleId
	 *            request
	 */
	public static void logBannedTaskAdded(final Logger logger,
			final String articleId)
	{

		logger.logMessage(Level.INFO,
				"Add-Request for banned PartialArticle received: " + articleId);
	}

	/**
	 * Logs the removal of a banished (partial) task.
	 * 
	 * @param logger
	 *            reference to the logger
	 * @param articleId
	 *            articleId
	 */
	public static void logBannedTaskRemoved(final Logger logger,
			final String articleId)
	{

		logger.logMessage(Level.INFO, "Removed banned PartialArticle: "
				+ articleId);
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
				"Banned PartialArticle removed from blacklist: " + articleId);
	}

	/**
	 * Logs the adding of a partial revision task
	 * 
	 * @param logger
	 *            reference to the logger
	 * @param partialArticle
	 *            reference to the revision task
	 */
	public static void logPartialArticleAdded(final Logger logger,
			final Task<Revision> partialArticle)
	{

		logger.logMessage(
				Level.INFO,
				"Added PartialArticle            \t<"
						+ partialArticle.uniqueIdentifier() + ", "
						+ partialArticle.toString() + ">");
	}

	/**
	 * Logs the request of a partial revision task.
	 * 
	 * @param logger
	 *            reference to the logger
	 * @param request
	 *            request
	 */
	public static void logPartialArticleRequest(final Logger logger,
			final String request)
	{

		logger.logMessage(Level.INFO, "Requested unknown PartialArticle\t<"
				+ request + ">");
	}

	/**
	 * Logs the return of a partial revision task.
	 * 
	 * @param logger
	 *            reference to the logger
	 * @param partialArticle
	 *            reference to the revision task
	 */
	public static void logPartialArticleReturned(final Logger logger,
			final Task<Revision> partialArticle)
	{

		logger.logMessage(
				Level.INFO,
				"Returned PartialArticle         \t<"
						+ partialArticle.uniqueIdentifier() + ", "
						+ partialArticle.toString() + ">");
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
				+ "\tArticlePool: " + sizePool + "\tPartialArticlePool: "
				+ sizePartialPool);
	}

	/** No object - utility class */
	private ArticleProducerLogMessages()
	{
	}
}
