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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.consumer.diff;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.Revision;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.ConfigurationException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.DiffException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.LoggingException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.TimeoutException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.logging.LoggerType;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.logging.messages.consumer.ConsumerLogMessages;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.logging.messages.consumer.DiffConsumerLogMessages;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.ConfigurationKeys;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.ConfigurationManager;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.consumer.AbstractConsumer;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.consumer.ConsumerStates;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.consumer.diff.calculation.DiffCalculator;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.consumer.diff.calculation.TimedDiffCalculator;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.Task;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.content.Diff;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.info.ArticleInformation;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.producer.articles.ArticleProducerInterface;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.producer.consumers.ProducerConsumerInterface;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.producer.data.CapacityValue;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.producer.diffs.DiffProducerInterface;

/**
 * DiffConsumer Consumer of the DiffTool Application
 * 
 * This consumer is used to calculate the diffs.
 * 
 * 
 * 
 */
public class DiffConsumer
	extends AbstractConsumer
	implements TaskTransmitterInterface
{

	/** Reference to the ProducerConsumer */
	private ProducerConsumerInterface consumerProducer;

	/** Reference to the DiffProducer */
	private DiffProducerInterface diffProducer;

	/** Reference to the ArticleProducer */
	private ArticleProducerInterface articleProducer;

	/** Reference to the DiffCalculator */
	private DiffCalculatorInterface diffCalc;

	/** Reference to the currently processed task */
	private Task<Revision> task;

	/**
	 * Temporary variable - used to store the start time of a diff calculation
	 */
	private long processingStart;

	/**
	 * Temporary variable - used to store the time used for a diff calculation
	 */
	private long processingTime;

	/**
	 * Temporary variable - used to store the time used for transmitting the
	 * task
	 */
	private long transmittingTime;

	/**
	 * Configuration parameter - Flag which indicates whether the statistical
	 * output is activated or not
	 */
	private final boolean MODE_STATISTICAL_OUTPUT;

	/**
	 * (Constructor) Creates a new DiffConsumer object.
	 * 
	 * @param consumerProducer
	 *            Reference to the ProducerConsumer
	 * @param articleProducer
	 *            Reference to the ArticleProducer
	 * @param diffProducer
	 *            Reference to the DiffProducer
	 * 
	 * @throws ConfigurationException
	 *             if an error occurred while accessing the configuration
	 * @throws LoggingException
	 *             if an error occurred while creating the logger
	 */
	public DiffConsumer(final ProducerConsumerInterface consumerProducer,
			final ArticleProducerInterface articleProducer,
			final DiffProducerInterface diffProducer)
		throws ConfigurationException, LoggingException
	{
		super(LoggerType.CONSUMER_DIFF, consumerProducer);

		this.consumerProducer = consumerProducer;
		this.diffProducer = diffProducer;
		this.articleProducer = articleProducer;

		ConfigurationManager config = ConfigurationManager.getInstance();

		MODE_STATISTICAL_OUTPUT = (Boolean) config
				.getConfigParameter(ConfigurationKeys.MODE_STATISTICAL_OUTPUT);

		if (MODE_STATISTICAL_OUTPUT) {
			this.diffCalc = new TimedDiffCalculator(this);
		}
		else {
			this.diffCalc = new DiffCalculator(this);
		}
	}

	/**
	 * Processes a revision task.
	 * 
	 * @throws DiffException
	 *             if an error occurred while calculating the diff
	 * @throws TimeoutException
	 *             if a timeout occurred
	 * @throws UnsupportedEncodingException
	 *             if the character encoding is unsupported
	 */
	private void process()
		throws DiffException, TimeoutException, UnsupportedEncodingException
	{

		DiffConsumerLogMessages.logStartArticleProcessing(logger, task,
				processingTime, transmittingTime);

		transmittingTime = 0;
		processingStart = System.currentTimeMillis();

		diffCalc.process(task);

		processingTime = System.currentTimeMillis() - processingStart;
		this.workingTime += processingTime;

		DiffConsumerLogMessages.logArticleProcessed(logger, task,
				processingTime, transmittingTime);

		this.sleep = false;
		this.sleep();
	}

	/**
	 * Requests and processes a RevisionTask from the ArticleProducer
	 * 
	 * @throws UnsupportedEncodingException
	 *             if the character encoding is unsupported
	 */
	private void readTask()
		throws UnsupportedEncodingException
	{
		try {

			task = articleProducer.getArticle();
			if (task != null) {

				switch (task.getTaskType()) {
				case TASK_FULL:
					this.setConsumerState(ConsumerStates.PROCCESSING_FULL_TASK);
					process();
					break;
				case TASK_PARTIAL_FIRST:
					readPartialTask();
					break;
				case ENDTASK:
					DiffConsumerLogMessages.logEndTaskReceived(logger);
					sendKillSignal();
					break;
				default:
					DiffConsumerLogMessages.logInvalidTaskType(logger,
							task.getTaskType());
				}

			}

		}
		catch (DiffException e) {

			DiffConsumerLogMessages.logDiffException(logger, e);
			reset();

		}
		catch (TimeoutException e) {
			ConsumerLogMessages.logTimeoutException(logger, e);
			reset();

		}
		catch (OutOfMemoryError e) {
			DiffConsumerLogMessages
					.logReadTaskOutOfMemoryError(logger, task, e);
			reset();
		}
	}

	/**
	 * Resets the current task and deletes all reference of this task and its
	 * related tasks.
	 */
	private void reset()
	{

		this.diffCalc.reset();

		if (task != null) {

			switch (task.getTaskType()) {
			case TASK_PARTIAL:
			case TASK_PARTIAL_FIRST:
				ArticleInformation header = task.getHeader();
				if (header != null) {

					int articleId = header.getArticleId();
					if (articleId != -1) {
						articleProducer.addBannedTask(articleId);
						diffProducer.addBannedTask(articleId);
					}
				}
				break;
			}
		}

		System.gc();
	}

	/**
	 * Requests and processes a partial RevisionTask from the ArticleProducer
	 * 
	 * @throws DiffException
	 *             if an error occurred while calculating the diff
	 * @throws TimeoutException
	 *             if a timeout occurred
	 * @throws UnsupportedEncodingException
	 *             if the character encoding is unsupported
	 */
	private void readPartialTask()
		throws DiffException, TimeoutException, UnsupportedEncodingException
	{

		this.setConsumerState(ConsumerStates.PROCCESSING_PARTIAL_TASK);

		process();
		int uniqueId = task.getHeader().getArticleId();
		int partCounter = task.getPartCounter() + 1;

		boolean partial = true;

		do {
			this.sleep = true;

			task = articleProducer.getPartialArticle(uniqueId, partCounter);

			if (task != null) {
				this.setConsumerState(ConsumerStates.PROCCESSING_PARTIAL_TASK);

				switch (task.getTaskType()) {
				case TASK_PARTIAL:
					process();
					partCounter++;
					break;
				case TASK_PARTIAL_LAST:
					process();
					partial = false;
					break;
				case BANNED_TASK:
					partial = false;
					break;

				default:
					DiffConsumerLogMessages.logInvalidTaskType(logger,
							task.getTaskType());
				}

				this.sleep = false;
			}

			sleep();

		}
		while (partial);
	}

	/**
	 * Starts the consumer.
	 */
	public void run()
	{

		try {
			ConsumerLogMessages.logConsumerRunning(logger);

			while (!this.shutdown) {

				sleep = true;

				if (start) {
					readTask();
				}
				else {
					this.setConsumerState(ConsumerStates.WAITING);
				}

				try {
					this.sleep();
				}
				catch (TimeoutException e) {
					ConsumerLogMessages.logException(logger, e);

					timeoutReset();
				}

				if (!this.sleep) {
					this.consumerProducer.sendConsumerInformation(
							this.getName(), workingTime, sleepingTime);

					ConsumerLogMessages.logStatus(logger, startTime,
							sleepingTime, workingTime);
				}
			}
		}
		catch (Exception e) {

			e.printStackTrace();
			ConsumerLogMessages.logException(logger, e);

		}
		catch (Error e) {

			e.printStackTrace();
			ConsumerLogMessages.logError(logger, e);

		}
		finally {
			System.out.println("SHUTDOWN\t" + this.getName());

			ConsumerLogMessages.logShutdown(logger, System.currentTimeMillis()
					- startTime);
			producer.unregister(this.getName());

			logger.close();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.tudarmstadt.ukp.kulessa.delta.data.tasks.Task#transmitDiff(de.tudarmstadt
	 * .ukp.kulessa.delta.data.tasks.Task)
	 */
	public void transmitDiff(final Task<Diff> result)
		throws TimeoutException
	{

		long startTransmission = System.currentTimeMillis();

		ConsumerStates oldState = this.getConsumerState();
		this.setConsumerState(ConsumerStates.TRANSMITTING);

		boolean success = false;
		CapacityValue capacity;
		CapacityValue oldCapacity = null;

		do {
			sleep = true;

			capacity = diffProducer.hasCapacity();
			if (capacity == CapacityValue.FREE) {

				diffProducer.addDiff(this.getName(), result);

				success = true;
				sleep = false;

			}
			else if (oldCapacity != capacity) {
				DiffConsumerLogMessages.logProducerHasNoCapacities(logger,
						capacity);
			}

			sleep();

			oldCapacity = capacity;

		}
		while (!success);

		transmittingTime += System.currentTimeMillis() - startTransmission;
		this.setConsumerState(oldState);
		return;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.tudarmstadt.ukp.kulessa.delta.data.tasks.Task#transmitParialDiff(de
	 * .tudarmstadt.ukp.kulessa.delta.data.tasks.Task)
	 */
	public void transmitPartialDiff(final Task<Diff> result)
		throws TimeoutException
	{

		long startTransmission = System.currentTimeMillis();

		ConsumerStates oldState = this.getConsumerState();
		this.setConsumerState(ConsumerStates.TRANSMITTING);

		boolean success = false;
		CapacityValue capacity;
		CapacityValue oldCapacity = null;

		do {
			sleep = true;

			capacity = diffProducer.hasCapacity();
			if (capacity == CapacityValue.FREE) {

				diffProducer.addPartialDiff(this.getName(), result);

				success = true;
				sleep = false;

			}
			else if (oldCapacity != capacity) {
				DiffConsumerLogMessages.logProducerHasNoCapacities(logger,
						capacity);
			}

			sleep();

		}
		while (!success);

		transmittingTime += System.currentTimeMillis() - startTransmission;
		this.setConsumerState(oldState);

		return;
	}
	
	
	@Override
	public void close()
		throws IOException, SQLException
	{
		// TODO Auto-generated method stub
		
	}
}
