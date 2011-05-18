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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.producer.diffs;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.Task;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.content.Diff;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.producer.data.CapacityValue;

/**
 * Interface of the DiffProducer
 * 
 * 
 * 
 */
public interface DiffProducerInterface
{

	/**
	 * Returns the CapacityValue of the DiffProducer.
	 * 
	 * @return CapacityValue PRODUCER_SHUTDOWN if the DiffProducer has entered
	 *         the shutdown mode CACHE_LIMIT_REACHED if the cache limit has been
	 *         reached TASK_LIMIT_REACHED if the number of tasks limit has been
	 *         reached FREE otherwise
	 */
	CapacityValue hasCapacity();

	/**
	 * Adds a diff task.
	 * 
	 * @param consumerID
	 *            ID of the consumer who processed the task
	 * @param task
	 *            reference to a DiffTask
	 * 
	 * @return TRUE if the task could be added FALSE otherwise
	 */
	boolean addDiff(final String consumerID, final Task<Diff> task);

	/**
	 * Adds a partial diff task.
	 * 
	 * @param consumerID
	 *            ID of the consumer who processed the task
	 * @param task
	 *            reference to a DiffTask
	 * 
	 * @return TRUE if the task could be added FALSE otherwise
	 */
	boolean addPartialDiff(final String consumerID, final Task<Diff> task);

	/**
	 * Returns a DiffTask.
	 * 
	 * @return DiffTask or NULL if no task is available
	 */
	Task<Diff> getDiff();

	/**
	 * Returns the requested partial DiffTask.
	 * 
	 * @param taskId
	 *            ArticleID
	 * @param partCounter
	 *            Number of part
	 * 
	 * @return DiffTask or NULL if the task is not contained in the storage
	 */
	Task<Diff> getPartialDiff(final int taskId, final int partCounter);

	/**
	 * Adds an ArticleID to the blacklist.
	 * 
	 * @param articleId
	 *            ID of an Article
	 */
	void addBannedTask(final int articleId);
}
