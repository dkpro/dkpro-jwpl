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

import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.ConfigurationException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.LoggingException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.logging.LoggerType;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.logging.messages.producer.DiffProducerLogMessages;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.Task;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.content.Diff;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.producer.AbstractProducer;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.producer.data.CapacityValue;

/**
 * DiffProducer This class represents the communication interface for this
 * producer.
 * 
 * 
 * 
 */
public class DiffProducer
	extends AbstractProducer
	implements DiffProducerInterface
{

	/** Reference to the DiffManager */
	private final DiffManager diffs;

	/**
	 * (Constructor) Creates a new DiffProducer
	 * 
	 * @throws ConfigurationException
	 *             if an error occurs while accessing the configuration
	 * @throws LoggingException
	 *             if an error occurred while creating the logger
	 */
	public DiffProducer()
		throws ConfigurationException, LoggingException
	{

		super(LoggerType.PRODUCER_DIFFS, "DiffProducer");

		this.diffs = new DiffManager(this);
	}

	/**
	 * Adds a diff task.
	 * 
	 * @param consumerID
	 *            ID of the consumer who processed the task
	 * @param diff
	 *            reference to a DiffTask
	 * 
	 * @return TRUE if the task could be added FALSE otherwise
	 */
	@Override
	public boolean addDiff(final String consumerID, final Task<Diff> diff)
	{

		DiffProducerLogMessages.logDiffAdded(logger, consumerID, diff);
		return this.diffs.addDiff(diff);
	}

	/**
	 * Adds a partial diff task.
	 * 
	 * @param consumerID
	 *            ID of the consumer who processed the task
	 * @param diff
	 *            reference to a DiffTask
	 * 
	 * @return TRUE if the task could be added FALSE otherwise
	 */
	@Override
	public boolean addPartialDiff(final String consumerID, final Task<Diff> diff)
	{

		DiffProducerLogMessages.logPartialDiffAdded(logger, consumerID, diff);
		return this.diffs.addPartialDiff(diff);
	}

	/**
	 * Returns a DiffTask.
	 * 
	 * @return DiffTask or NULL if no task is available
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Task<Diff> getDiff()
	{

		Task<Diff> diff;

		switch (this.diffs.getState()) {
		case FINISHED:
			return Task.createEndTask();
		case NO_TASK:
			return null;
		case TASK_AVAILABLE:
			diff = this.diffs.getDiff();
			if (diff != null) {
				DiffProducerLogMessages.logDiffReturned(logger, diff);
			}
			return diff;
		default:
			// TODO: Log Error
		}

		return null;
	}

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
	@Override
	public Task<Diff> getPartialDiff(final int taskId, final int partCounter)
	{

		Task<Diff> diff = this.diffs.getPartialDiff(taskId, partCounter);

		if (diff != null) {
			DiffProducerLogMessages.logPartialDiffReturned(logger, diff);
		}
		else {
			DiffProducerLogMessages.logPartialDiffRequest(logger, taskId + "-"
					+ partCounter);
		}
		return diff;
	}

	/**
	 * Returns the CapacityValue of the DiffProducer.
	 * 
	 * @return CapacityValue PRODUCER_SHUTDOWN if the DiffProducer has entered
	 *         the shutdown mode CACHE_LIMIT_REACHED if the cache limit has been
	 *         reached TASK_LIMIT_REACHED if the number of tasks limit has been
	 *         reached FREE otherwise
	 */
	@Override
	public CapacityValue hasCapacity()
	{
		return this.diffs.hasCapacity();
	}

	/**
	 * Adds an ArticleID to the blacklist.
	 * 
	 * @param articleId
	 *            ID of an Article
	 */
	@Override
	public void addBannedTask(final int articleId)
	{
		this.diffs.addBannedTask(articleId);
	}

	/**
	 * Returns a reference to the DiffManager.
	 * 
	 * @return DiffManager
	 */
	public DiffManager getDiffManager()
	{
		return this.diffs;
	}
}
