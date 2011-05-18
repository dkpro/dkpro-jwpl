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
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.Task;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.producer.data.CapacityValue;

/**
 * ArticleProducerInterface
 * 
 * 
 * 
 */
public interface ArticleProducerInterface
{

	/**
	 * Returns the CapacityValue of the ArticleProducer.
	 * 
	 * @return CapacityValue PRODUCER_SHUTDOWN if the ArticleProducer has
	 *         entered the shutdown mode CACHE_LIMIT_REACHED if the cache limit
	 *         has been reached TASK_LIMIT_REACHED if the number of tasks limit
	 *         has been reached FREE otherwise
	 */
	CapacityValue hasCapacity();

	/**
	 * Adds a revision task.
	 * 
	 * @param task
	 *            Reference to a RevisionTask
	 * 
	 * @return TRUE if the task could be added FALSE otherwise
	 */
	boolean addArticle(final Task<Revision> task);

	/**
	 * Adds a partial revision task.
	 * 
	 * @param task
	 *            Reference to a DiffTask
	 * 
	 * @return TRUE if the task could be added FALSE otherwise
	 */
	boolean addPartialArticle(final Task<Revision> task);

	/**
	 * Returns a RevisionTask.
	 * 
	 * @return RevisionTask or NULL if no task is available
	 */
	Task<Revision> getArticle();

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
	Task<Revision> getPartialArticle(final int taskId, final int partCounter);

	/**
	 * Adds an ArticleID to the blacklist.
	 * 
	 * @param articleId
	 *            ID of an Article
	 */
	void addBannedTask(final int articleId);
}
