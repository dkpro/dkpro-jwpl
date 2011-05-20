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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.Revision;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.ConfigurationException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.logging.Logger;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.logging.messages.producer.ArticleProducerLogMessages;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.ConfigurationManager;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.Task;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.TaskTypes;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.producer.data.CapacityValue;

/**
 * ArticleManager Manages the data contained by the ArticleProducer
 *
 *
 *
 */
public class ArticleManager
{

	/**
	 * RevisionTask Pool A - contains full tasks and the first parts of partial
	 * task
	 */
	private final Vector<Task<Revision>> articlePool;

	/** Blacklist - Set containing the id's of banned tasks */
	private final Set<String> bannedTasks;

	/** Map which contains a list of the partial revision task */
	private final Map<String, Set<String>> partialArticleList;

	/**
	 * RevisionTask Pool B - contains partial tasks
	 */
	private final Map<String, Task<Revision>> partialArticlePool;

	/** Reference to the ArticleProducer */
	private final ArticleProducer articleProducer;

	/** Reference to the logger */
	private final Logger logger;

	/** Size of Pool A */
	private int lastPoolSize;

	/** Size of Pool B */
	private int lastPartialPoolSize;

	/** Total size */
	private Long byteTotalSize;

	/**
	 * (Constructor) Creates a new ArticleManager object.
	 *
	 * @param articleProducer
	 *            Reference to the ArticleProducer
	 *
	 * @throws ConfigurationException
	 *             if an error occurs while accessing the configuration
	 */
	public ArticleManager(final ArticleProducer articleProducer)
		throws ConfigurationException
	{

		this.logger = articleProducer.getLogger();

		ConfigurationManager.getInstance();

		this.articleProducer = articleProducer;

		this.bannedTasks = new HashSet<String>();
		this.partialArticleList = new HashMap<String, Set<String>>();

		this.articlePool = new Vector<Task<Revision>>();
		this.partialArticlePool = new HashMap<String, Task<Revision>>();

		this.lastPoolSize = -1;
		this.lastPartialPoolSize = -1;
		this.byteTotalSize = 0l;
	}

	/**
	 * Adds a revision task to Pool A.
	 *
	 * @param article
	 *            reference to a RevisionTask
	 * @return TRUE if the task could be added FALSE otherwise
	 */
	public synchronized boolean addArticle(final Task<Revision> article)
	{

		boolean flag = this.articlePool.add(article);
		if (flag) {
			synchronized (byteTotalSize) {
				this.byteTotalSize += article.byteSize();
			}
		}

		return flag;
	}

	/**
	 * Adds a revision task to Pool B.
	 *
	 * @param article
	 *            reference to a RevisionTask
	 * @return TRUE if the task could be added FALSE otherwise
	 */
	public synchronized boolean addPartialArticle(final Task<Revision> article)
	{

		// Check if the task has been banned
		String articleId = Integer.toString(article.getHeader().getArticleId());
		if (this.bannedTasks.contains(articleId)) {

			ArticleProducerLogMessages.logBannedTaskAdded(logger,
					article.uniqueIdentifier());

			if (article.getTaskType() == TaskTypes.TASK_PARTIAL_LAST) {
				this.bannedTasks.remove(articleId);

				ArticleProducerLogMessages.logBannRemoved(logger, articleId);
			}

			return false;
		}

		// Add Task to partialTask List
		Set<String> articleParts = this.partialArticleList.get(articleId);
		if (articleParts == null) {
			articleParts = new HashSet<String>();
			this.partialArticleList.put(articleId, articleParts);
		}
		articleParts.add(article.uniqueIdentifier());

		// Add Task to partialTask Pool
		if (this.partialArticlePool.put(article.uniqueIdentifier(), article) != null) {
			return false;
		}
		else {
			synchronized (byteTotalSize) {
				this.byteTotalSize += article.byteSize();
			}
		}

		return true;
	}

	/**
	 * Returns the first RevisionTask from Pool A.
	 *
	 * @return RevisionTask
	 */
	public synchronized Task<Revision> getArticle()
	{

		Task<Revision> task = null;
		synchronized (articlePool) {

			if (this.articlePool.size() > 0) {
				task = this.articlePool.remove(0);

				synchronized (byteTotalSize) {
					this.byteTotalSize -= task.byteSize();
				}
			}
		}

		return task;
	}

	/**
	 * Returns the requested partial RevisionTask from Pool B.
	 *
	 * @param articleId
	 *            ArticleID
	 * @param partCounter
	 *            Number of part
	 *
	 * @return RevisionTask
	 */
	@SuppressWarnings("unchecked")
	public synchronized Task<Revision> getPartialTask(final int articleId,
			final int partCounter)
	{

		// Check if Task has been banned
		String request = Integer.toString(articleId);
		if (this.bannedTasks.contains(request)) {
			return Task.createBannedTask();
		}

		Task<Revision> task;
		synchronized (partialArticlePool) {
			task = this.partialArticlePool
					.remove(articleId + "-" + partCounter);
		}

		if (task != null) {

			if (task.getTaskType() == TaskTypes.TASK_PARTIAL_LAST) {
				this.partialArticleList.remove(request);
			}
			else {
				Set<String> articleParts = this.partialArticleList.get(request);
				articleParts.remove(task.uniqueIdentifier());
			}

			synchronized (byteTotalSize) {
				this.byteTotalSize -= task.byteSize();
			}
		}

		return task;
	}

	/**
	 * Returns the State of the ArticleManager.
	 *
	 * @return ArticleManagerStates FINISHED if the ArticleProducer entered the
	 *         shutdown mode NO_TASK if both RevisionTask pools are empty
	 *         TASK_AVAILABLE otherwise
	 */
	public synchronized ArticleManagerStates getState()
	{
		if (this.articlePool.isEmpty() && this.partialArticlePool.isEmpty()) {
			if (this.articleProducer.isShutdown()) {
				return ArticleManagerStates.FINISHED;
			}
			return ArticleManagerStates.NO_TASK;
		}

		return ArticleManagerStates.TASK_AVAILABLE;
	}

	/**
	 * Returns the CapacityValue of the ArticleManager.
	 *
	 * @return CapacityValue PRODUCER_SHUTDOWN if the ArticleProducer has
	 *         entered the shutdown mode CACHE_LIMIT_REACHED if the cache limit
	 *         has been reached TASK_LIMIT_REACHED if the number of tasks limit
	 *         has been reached FREE otherwise
	 */
	public synchronized CapacityValue hasCapacity()
	{

		// check if producer has been shutdown
		if (getState() == ArticleManagerStates.FINISHED) {
			return CapacityValue.PRODUCER_SHUTDOWN;
		}

		// if the state has changed - log the current state
		if (this.lastPoolSize != this.articlePool.size()
				|| this.lastPartialPoolSize != this.partialArticlePool.size()) {

			this.lastPoolSize = this.articlePool.size();
			this.lastPartialPoolSize = this.partialArticlePool.size();

			ArticleProducerLogMessages.logStatus(logger, this.byteTotalSize,
					this.lastPoolSize, this.lastPartialPoolSize);
		}

		return CapacityValue.FREE;
	}

	/**
	 * Adds a ArticleID to the blacklist.
	 *
	 * @param articleId
	 *            ID of an Article
	 */
	public synchronized void addBannedTask(final int articleId)
	{

		String bann = Integer.toString(articleId);
		this.bannedTasks.add(bann);

		ArticleProducerLogMessages.logBannAdded(logger, bann);

		Set<String> articleParts = this.partialArticleList.get(bann);
		if (articleParts != null) {

			boolean end = false;
			String uniqueId;
			Task<Revision> task;

			Iterator<String> sIt = articleParts.iterator();
			while (sIt.hasNext()) {
				uniqueId = sIt.next();

				synchronized (partialArticlePool) {
					task = this.partialArticlePool.remove(uniqueId);

					ArticleProducerLogMessages.logBannedTaskRemoved(logger,
							task.uniqueIdentifier());
				}

				end |= (task.getTaskType() == TaskTypes.ENDTASK);
				synchronized (byteTotalSize) {
					this.byteTotalSize -= task.byteSize();
				}
			}

			if (end) {
				this.bannedTasks.remove(bann);
				this.partialArticleList.remove(bann);

				ArticleProducerLogMessages.logBannRemoved(logger, bann);
			}
		}
	}

	/**
	 * Returns the string representation.
	 *
	 * @return [TotalSize, Size Pool A, Size Pool B]
	 */
	@Override
	public String toString()
	{
		return "ArticleManager:\t[" + this.byteTotalSize + ", "
				+ this.articlePool.size() + ", "
				+ this.partialArticlePool.size() + "]";
	}
}
