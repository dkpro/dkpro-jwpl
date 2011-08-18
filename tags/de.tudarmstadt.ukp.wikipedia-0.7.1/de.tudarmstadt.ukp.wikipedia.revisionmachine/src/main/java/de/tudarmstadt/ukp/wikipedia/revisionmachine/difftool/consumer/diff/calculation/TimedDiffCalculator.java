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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.consumer.diff.calculation;

import java.io.UnsupportedEncodingException;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.Revision;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.ConfigurationException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.DiffException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.TimeoutException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.consumer.diff.TaskTransmitterInterface;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.Task;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.TaskTypes;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.content.Diff;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.info.ArticleInformation;

/**
 * Calculates the Diff while collecting statistical data.
 * 
 * 
 * 
 */
public class TimedDiffCalculator
	extends DiffCalculator
{

	/** Temporary variable - revision counter */
	private int revisionCounter;

	/** Temporary variable - diff part counter */
	private int diffPartCounter;

	/** Temporary variable - size of the diff */
	private long diffedSize;

	/** Temporary variable - start time of the diff processing */
	private long startTime;

	/** Temporary variable - time used for the diff processing */
	private long processingTimeDiff;

	/** Temporary variable - number of ignored revisions */
	private int ignoredRevisionsCounter;

	/**
	 * (Constructor) Creates a new DiffCalculator object.
	 * 
	 * @param taskTransmitter
	 *            Reference to the TaskTransmitter
	 * 
	 * @throws ConfigurationException
	 *             if an error occurred while accessing the configuration
	 */
	public TimedDiffCalculator(final TaskTransmitterInterface taskTransmitter)
		throws ConfigurationException
	{
		super(taskTransmitter);
	}

	/*--------------------------------------------------------------------------*/

	/**
	 * Initializes the processing of a new RevisionTask.
	 * 
	 * @param taskID
	 *            Article ID
	 */
	protected void initNewTask(final int taskID)
	{

		super.initNewTask(taskID);

		this.processingTimeDiff = 0;

		this.revisionCounter = 0;
		this.ignoredRevisionsCounter = 0;

		this.diffPartCounter = 0;
		this.diffedSize = 0;
	}

	/**
	 * Transmits a partial DiffTask.
	 * 
	 * @param result
	 *            Reference to the DiffTask
	 * 
	 * @throws TimeoutException
	 *             if a timeout occurred
	 */
	protected void transmitPartialTask(final Task<Diff> result)
		throws TimeoutException
	{

		this.diffedSize += result.byteSize();
		this.processingTimeDiff += System.currentTimeMillis() - startTime;

		super.transmitPartialTask(result);

		startTime = System.currentTimeMillis();
	}

	/**
	 * Transmits the DiffTask at the end of the RevisionTask processing.
	 * 
	 * @param task
	 *            Reference to the RevisionTask
	 * @param result
	 *            Reference to the DiffTask
	 * 
	 * @throws TimeoutException
	 *             if a timeout occurred
	 */
	protected void transmitAtEndOfTask(final Task<Revision> task,
			final Task<Diff> result)
		throws TimeoutException
	{

		this.processingTimeDiff += System.currentTimeMillis() - startTime;

		if (task.getTaskType() == TaskTypes.TASK_FULL
				|| task.getTaskType() == TaskTypes.TASK_PARTIAL_LAST) {

			diffedSize += result.byteSize();

			ArticleInformation info = result.getHeader();
			info.setRevisionCounter(revisionCounter);
			info.setIgnoredRevisionsCounter(ignoredRevisionsCounter);
			info.setDiffedSize(diffedSize);
			info.setDiffPartCounter(diffPartCounter);
			info.setProcessingTimeRead(task.getHeader().getProcessingTimeRead());
			info.setProcessingTimeDiff(processingTimeDiff);
		}

		super.transmitAtEndOfTask(task, result);
	}

	/**
	 * Calculates the diff for the given revision.
	 * 
	 * @param revision
	 *            Reference to a revision
	 * @return Diff
	 * 
	 * @throws UnsupportedEncodingException
	 *             if the character encoding is unsupported
	 */
	protected Diff processRevision(final Revision revision)
		throws UnsupportedEncodingException
	{

		Diff diff = super.processRevision(revision);
		if (diff == null) {
			this.ignoredRevisionsCounter++;
		}
		else {
			this.revisionCounter++;
			this.diffPartCounter += diff.size();
		}

		return diff;
	}

	/*--------------------------------------------------------------------------*/

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.tud.ukp.kulessa.delta.consumers.diff.calculation.DiffCalculatorInterface
	 * #process(de.tud.ukp.kulessa.delta.data.Task)
	 */
	public void process(final Task<Revision> task)
		throws DiffException, TimeoutException, UnsupportedEncodingException
	{

		this.startTime = System.currentTimeMillis();
		super.process(task);
	}
}
