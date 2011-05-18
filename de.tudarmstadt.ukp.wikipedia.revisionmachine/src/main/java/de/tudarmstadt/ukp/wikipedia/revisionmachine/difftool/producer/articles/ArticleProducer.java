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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.producer.articles;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.Revision;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.ConfigurationException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.LoggingException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.logging.LoggerType;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.logging.messages.producer.ArticleProducerLogMessages;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.Task;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.producer.AbstractProducer;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.producer.data.CapacityValue;

/**
 * ArticleProducer This class represents the communication interface for this
 * producer.
 * 
 * 
 * 
 */
public class ArticleProducer
	extends AbstractProducer
	implements ArticleProducerInterface
{

	/** Reference to the ArticleManager */
	private final ArticleManager articles;

	/**
	 * (Constructor) Creates a new ArticleProducer
	 * 
	 * @throws ConfigurationException
	 *             if an error occurs while accessing the configuration
	 * @throws LoggingException
	 *             if an error occurred while creating the logger
	 */
	public ArticleProducer()
		throws ConfigurationException, LoggingException
	{

		super(LoggerType.PRODUCER_ARTICLES, "ArticleProducer");

		this.articles = new ArticleManager(this);
	}

	/**
	 * Adds a revision task.
	 * 
	 * @param article
	 *            reference to a RevisionTask
	 * 
	 * @return TRUE if the task could be added FALSE otherwise
	 */
	@Override
	public boolean addArticle(final Task<Revision> article)
	{
		ArticleProducerLogMessages.logArticleAdded(logger, article);
		return this.articles.addArticle(article);
	}

	/**
	 * Adds a partial revision task.
	 * 
	 * @param article
	 *            Reference to a RevisionTask
	 * 
	 * @return TRUE if the task could be added FALSE otherwise
	 */
	@Override
	public boolean addPartialArticle(final Task<Revision> article)
	{
		ArticleProducerLogMessages.logPartialArticleAdded(logger, article);
		return this.articles.addPartialArticle(article);
	}

	/**
	 * Returns a RevisionTask.
	 * 
	 * @return RevisionTask or NULL if no task is available
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Task<Revision> getArticle()
	{
		Task<Revision> article;
		switch (this.articles.getState()) {
		case FINISHED:
			return Task.createEndTask();
		case NO_TASK:
			return null;
		case TASK_AVAILABLE:
			article = this.articles.getArticle();
			if (article != null) {
				ArticleProducerLogMessages.logArticleReturned(logger, article);
			}
			return article;
		default:
			// TODO: Log Error
		}

		return null;
	}

	/**
	 * Returns the requested partial RevisionTask.
	 * 
	 * @param taskId
	 *            ArticleID
	 * @param partCounter
	 *            Number of part
	 * 
	 * @return RevisionTask or NULL if the task is not contained in the storage
	 */
	@Override
	public Task<Revision> getPartialArticle(final int taskId,
			final int partCounter)
	{

		Task<Revision> article = this.articles.getPartialTask(taskId,
				partCounter);

		if (article != null) {
			ArticleProducerLogMessages.logPartialArticleReturned(logger,
					article);
		}
		else {
			ArticleProducerLogMessages.logPartialArticleRequest(logger, taskId
					+ "-" + partCounter);
		}

		return article;
	}

	/**
	 * Returns the CapacityValue of the ArticleProducer.
	 * 
	 * @return CapacityValue PRODUCER_SHUTDOWN if the ArticleProducer has
	 *         entered the shutdown mode CACHE_LIMIT_REACHED if the cache limit
	 *         has been reached TASK_LIMIT_REACHED if the number of tasks limit
	 *         has been reached FREE otherwise
	 */
	@Override
	public CapacityValue hasCapacity()
	{
		return this.articles.hasCapacity();
	}

	/**
	 * Adds an ArticleID to the blacklist.
	 * 
	 * @param articleId
	 *            ID of an Article
	 */
	@Override
	public void addBannedTask(int articleId)
	{
		this.articles.addBannedTask(articleId);
	}

	public ArticleManager getArticleManager()
	{
		return this.articles;
	}
}
