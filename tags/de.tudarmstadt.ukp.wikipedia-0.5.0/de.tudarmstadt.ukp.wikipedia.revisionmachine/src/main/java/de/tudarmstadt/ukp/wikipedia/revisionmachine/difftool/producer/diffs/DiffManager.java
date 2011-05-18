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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.ConfigurationException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.logging.Logger;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.logging.messages.producer.DiffProducerLogMessages;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.Task;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.TaskTypes;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.content.Diff;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.producer.data.CapacityValue;

/**
 * DiffManager Manages the data contained by the DiffProducer
 *
 *
 *
 */
public class DiffManager
{

	/**
	 * DiffTask Pool A - contains full tasks and the first parts of partial task
	 */
	private final List<Task<Diff>> diffPool;

	/** Blacklist - Set containing the id's of banned tasks */
	private final Set<String> bannedTasks;

	/** Map which contains a list of the partial diff task */
	private final Map<String, Set<String>> partialDiffList;

	/**
	 * DiffTask Pool B - contains partial tasks
	 */
	private final Map<String, Task<Diff>> partialDiffPool;

	/** Reference to the DiffProducer */
	private final DiffProducer diffProducer;

	/** Reference to the logger */
	private final Logger logger;

	/** Size of Pool A */
	private int lastPoolSize;

	/** Size of Pool B */
	private int lastPartialPoolSize;

	/** Total size */
	private Long byteTotalSize;

	/**
	 * (Constructor) Creates a new DiffManager object.
	 *
	 * @param diffProducer
	 *            Reference to the DiffProducer
	 *
	 * @throws ConfigurationException
	 *             if an error occurs while accessing the configuration
	 */
	public DiffManager(final DiffProducer diffProducer)
		throws ConfigurationException
	{

		this.logger = diffProducer.getLogger();

		this.diffProducer = diffProducer;

		this.bannedTasks = new HashSet<String>();
		this.partialDiffList = new HashMap<String, Set<String>>();

		this.diffPool = new Vector<Task<Diff>>();
		this.partialDiffPool = new HashMap<String, Task<Diff>>();

		this.lastPoolSize = -1;
		this.lastPartialPoolSize = -1;
		this.byteTotalSize = 0l;
	}

	/**
	 * Adds a diff task to Pool A.
	 *
	 * @param diff
	 *            reference to a DiffTask
	 * @return TRUE if the task could be added FALSE otherwise
	 */
	public synchronized boolean addDiff(final Task<Diff> diff)
	{

		boolean flag = this.diffPool.add(diff);
		if (flag) {
			synchronized (byteTotalSize) {
				this.byteTotalSize += diff.byteSize();
			}
		}

		return flag;
	}

	/**
	 * Adds a diff task to Pool B.
	 *
	 * @param diff
	 *            reference to a DiffTask
	 * @return TRUE if the task could be added FALSE otherwise
	 */
	public synchronized boolean addPartialDiff(final Task<Diff> diff)
	{

		// Check if the task has been banned
		String diffId = Integer.toString(diff.getHeader().getArticleId());
		if (this.bannedTasks.contains(diffId)) {

			DiffProducerLogMessages.logBannedTaskAdded(logger,
					diff.uniqueIdentifier());

			if (diff.getTaskType() == TaskTypes.TASK_PARTIAL_LAST) {
				this.bannedTasks.remove(diffId);

				DiffProducerLogMessages.logBannRemoved(logger, diffId);
			}

			return false;
		}

		// Add Task to partialTask List
		Set<String> diffParts = this.partialDiffList.get(diffId);
		if (diffParts == null) {
			diffParts = new HashSet<String>();
			this.partialDiffList.put(diffId, diffParts);
		}
		diffParts.add(diff.uniqueIdentifier());

		// Add Task to partialTask Pool
		if (this.partialDiffPool.put(diff.uniqueIdentifier(), diff) != null) {
			return false;
		}
		else {
			synchronized (byteTotalSize) {
				this.byteTotalSize += diff.byteSize();
			}
		}

		return true;
	}

	/**
	 * Returns the first DiffTask from Pool A.
	 *
	 * @return DiffTask
	 */
	public synchronized Task<Diff> getDiff()
	{

		Task<Diff> task = null;
		synchronized (diffPool) {
			if (this.diffPool.size() > 0) {
				task = this.diffPool.remove(0);

				synchronized (byteTotalSize) {
					this.byteTotalSize -= task.byteSize();
				}
			}
		}

		return task;
	}

	/**
	 * Returns the requested partial DiffTask from Pool B.
	 *
	 * @param diffId
	 *            ArticleID
	 * @param partCounter
	 *            Number of part
	 *
	 * @return DiffTask
	 */
	@SuppressWarnings("unchecked")
	public synchronized Task<Diff> getPartialDiff(final int diffId,
			final int partCounter)
	{

		// Check if Task has been banned
		String request = Integer.toString(diffId);
		if (this.bannedTasks.contains(request)) {
			return Task.createBannedTask();
		}

		Task<Diff> task;
		synchronized (partialDiffPool) {
			task = this.partialDiffPool.remove(diffId + "-" + partCounter);
		}

		if (task != null) {

			if (task.getTaskType() == TaskTypes.TASK_PARTIAL_LAST) {
				this.partialDiffList.remove(request);
			}
			else {
				Set<String> diffParts = this.partialDiffList.get(request);
				diffParts.remove(task.uniqueIdentifier());
			}

			synchronized (byteTotalSize) {
				this.byteTotalSize -= task.byteSize();
			}
		}

		return task;
	}

	/**
	 * Returns the State of the DiffManager.
	 *
	 * @return DiffManagerStates FINISHED if the DiffProducer entered the
	 *         shutdown mode NO_TASK if both DiffTasks pools are empty
	 *         TASK_AVAILABLE otherwise
	 */
	public synchronized DiffManagerStates getState()
	{
		if (this.diffPool.isEmpty() && this.partialDiffPool.isEmpty()) {
			if (this.diffProducer.isShutdown()) {
				return DiffManagerStates.FINISHED;
			}
			return DiffManagerStates.NO_TASK;
		}

		return DiffManagerStates.TASK_AVAILABLE;
	}

	/**
	 * Returns the CapacityValue of the DiffManager.
	 *
	 * @return CapacityValue PRODUCER_SHUTDOWN if the DiffProducer has entered
	 *         the shutdown mode CACHE_LIMIT_REACHED if the cache limit has been
	 *         reached TASK_LIMIT_REACHED if the number of tasks limit has been
	 *         reached FREE otherwise
	 */
	public synchronized CapacityValue hasCapacity()
	{

		if (getState() == DiffManagerStates.FINISHED) {
			return CapacityValue.PRODUCER_SHUTDOWN;
		}

		// if the state has changed - log the current state
		if (this.lastPoolSize != this.diffPool.size()
				|| this.lastPartialPoolSize != this.partialDiffPool.size()) {

			this.lastPoolSize = this.diffPool.size();
			this.lastPartialPoolSize = this.partialDiffPool.size();

			DiffProducerLogMessages.logStatus(logger, this.byteTotalSize,
					this.lastPoolSize, this.lastPartialPoolSize);
		}

		return CapacityValue.FREE;
	}

	/**
	 * Adds an ArticleID to the blacklist.
	 *
	 * @param articleId
	 *            ID of an Article
	 */
	public synchronized void addBannedTask(final int articleId)
	{

		String bann = Integer.toString(articleId);
		this.bannedTasks.add(bann);

		DiffProducerLogMessages.logBannAdded(logger, bann);

		Set<String> diffParts = this.partialDiffList.get(bann);
		if (diffParts != null) {

			boolean end = false;
			String uniqueId;
			Task<Diff> task;

			Iterator<String> sIt = diffParts.iterator();
			while (sIt.hasNext()) {
				uniqueId = sIt.next();

				synchronized (partialDiffPool) {
					task = this.partialDiffPool.remove(uniqueId);

					DiffProducerLogMessages.logBannedTaskRemoved(logger,
							task.uniqueIdentifier());
				}

				end |= (task.getTaskType() == TaskTypes.ENDTASK);
				synchronized (byteTotalSize) {
					this.byteTotalSize -= task.byteSize();
				}
			}

			if (end) {
				this.bannedTasks.remove(bann);
				this.partialDiffList.remove(bann);

				DiffProducerLogMessages.logBannRemoved(logger, bann);
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
		return "DiffManager:\t[" + this.byteTotalSize + ", "
				+ this.diffPool.size() + ", " + this.partialDiffPool.size()
				+ "]";
	}
}
