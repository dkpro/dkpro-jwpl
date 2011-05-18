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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.consumer.sql;

import java.io.IOException;
import java.sql.SQLException;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.ConfigurationException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.ErrorFactory;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.ErrorKeys;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.LoggingException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.SQLConsumerException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.TimeoutException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.logging.LoggerType;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.logging.messages.consumer.ConsumerLogMessages;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.logging.messages.consumer.DiffConsumerLogMessages;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.logging.messages.consumer.SQLConsumerLogMessages;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.ConfigurationKeys;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.ConfigurationManager;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.consumer.AbstractConsumer;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.consumer.ConsumerStates;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.consumer.sql.writer.SQLArchiveWriter;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.consumer.sql.writer.SQLDatabaseWriter;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.consumer.sql.writer.SQLFileWriter;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.consumer.sql.writer.TimedSQLArchiveWriter;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.consumer.sql.writer.TimedSQLDatabaseWriter;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.consumer.sql.writer.TimedSQLFileWriter;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.OutputType;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.Task;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.content.Diff;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.info.ArticleInformation;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.producer.consumers.ProducerConsumerInterface;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.producer.diffs.DiffProducerInterface;

/**
 * SQLConsumer Consumer of the DiffTool Application
 * 
 * This consumer is used to encode the data contained in the diff tasks.
 * 
 * 
 * 
 */
public class SQLConsumer
	extends AbstractConsumer
{

	/** Reference to the ProducerConsumer */
	private ProducerConsumerInterface consumerProducer;

	/** Reference to the DiffProducer */
	private DiffProducerInterface diffProducer;

	/** Reference to the currently processed task */
	private Task<Diff> diff;

	/** Reference to the SQLWriter */
	private SQLWriterInterface writer;

	/**
	 * Temporary variable - used to store the start time of an encoding
	 * operation
	 */
	private long processingStart;

	/**
	 * Temporary variable - used to store time used for an encoding operation
	 */
	private long processingTime;

	/**
	 * Configuration parameter - Flag which indicates whether the statistical
	 * output is activated or not
	 */
	private final boolean MODE_STATISTICAL_OUTPUT;

	/** Configuration parameter - output mode */
	private final OutputType MODE_OUTPUT;

	/**
	 * (Constructor) Creates a new SQLConsumer object.
	 * 
	 * @param consumerProducer
	 *            Reference to the ProducerConsumer
	 * @param diffProducer
	 *            Reference to the DiffProducer
	 * 
	 * @throws ConfigurationException
	 *             if an error occurred while accessing the configuration
	 * @throws LoggingException
	 *             if an error occurred while creating the logger
	 * @throws IOException
	 *             if the writing failed
	 */
	public SQLConsumer(final ProducerConsumerInterface consumerProducer,
			final DiffProducerInterface diffProducer)
		throws ConfigurationException, LoggingException, IOException
	{

		super(LoggerType.CONSUMER_SQL, consumerProducer);

		this.consumerProducer = consumerProducer;
		this.diffProducer = diffProducer;

		ConfigurationManager config = ConfigurationManager.getInstance();

		MODE_STATISTICAL_OUTPUT = (Boolean) config
				.getConfigParameter(ConfigurationKeys.MODE_STATISTICAL_OUTPUT);

		MODE_OUTPUT = (OutputType) config
				.getConfigParameter(ConfigurationKeys.MODE_OUTPUT);

		if (MODE_STATISTICAL_OUTPUT) {
			switch (MODE_OUTPUT) {
			case SQL:
				this.writer = new TimedSQLFileWriter(this);
				break;
			case SEVENZIP:
			case BZIP2:
			case ALTERNATE:
				this.writer = new TimedSQLArchiveWriter(this);
				break;
			case DATABASE:
				this.writer = new TimedSQLDatabaseWriter(this.logger);
				break;
			default:
				throw ErrorFactory
						.createConfigurationException(ErrorKeys.DELTA_CONSUMERS_SQL_WRITER_OUTPUTFACTORY_ILLEGAL_OUTPUTMODE_VALUE);
			}
		}
		else {
			switch (MODE_OUTPUT) {
			case SQL:
				this.writer = new SQLFileWriter(this);
				break;
			case SEVENZIP:
			case BZIP2:
			case ALTERNATE:
				this.writer = new SQLArchiveWriter(this);
				break;
			case DATABASE:
				this.writer = new SQLDatabaseWriter(this.logger);
				break;
			default:
				throw ErrorFactory
						.createConfigurationException(ErrorKeys.DELTA_CONSUMERS_SQL_WRITER_OUTPUTFACTORY_ILLEGAL_OUTPUTMODE_VALUE);
			}
		}
	}

	/**
	 * Processes a diff task.
	 * 
	 * @throws ConfigurationException
	 *             if an error occurred while accessing the configuration
	 * @throws IOException
	 *             if the writing failed
	 * @throws LoggingException
	 *             if an error occurred while
	 * @throws TimeoutException
	 *             if a timeout occurred
	 * @throws SQLConsumerException
	 *             if the sql consumer failed
	 */
	private void process()
		throws ConfigurationException, IOException, TimeoutException,
		SQLConsumerException
	{

		processingStart = System.currentTimeMillis();

		writer.process(diff);

		processingTime = System.currentTimeMillis() - processingStart;
		this.workingTime += processingTime;

		SQLConsumerLogMessages.logDiffProcessed(logger, diff, processingTime);

		this.sleep = false;
		this.sleep();
	}

	/**
	 * Resets the current task and deletes all reference of this task and its
	 * related tasks.
	 */
	private void reset()
	{

		if (diff != null) {

			switch (diff.getTaskType()) {
			case TASK_PARTIAL:
			case TASK_PARTIAL_FIRST:
				ArticleInformation header = diff.getHeader();
				if (header != null) {

					int articleId = header.getArticleId();
					if (articleId != -1) {
						diffProducer.addBannedTask(articleId);
					}
				}
				break;
			}
		}

		System.gc();
	}

	/**
	 * Requests and processes a DiffTask from the DiffProducer
	 * 
	 * @throws ConfigurationException
	 *             if an error occurred while accessing the configuration
	 * @throws IOException
	 *             if the writing failed
	 */
	private void readDiff()
		throws ConfigurationException, IOException
	{

		try {
			diff = diffProducer.getDiff();
			if (diff != null) {
				switch (diff.getTaskType()) {
				case TASK_FULL:
					this.setConsumerState(ConsumerStates.PROCCESSING_FULL_TASK);
					process();
					sleep = false;
					break;
				case TASK_PARTIAL_FIRST:
					readPartialDiff();
					break;
				case ENDTASK:
					DiffConsumerLogMessages.logEndTaskReceived(logger);
					sendKillSignal();
				default:
				}
			}
		}
		catch (TimeoutException e) {
			ConsumerLogMessages.logTimeoutException(logger, e);
			reset();
		}
		catch (SQLConsumerException e) {
			SQLConsumerLogMessages.logSQLConsumerException(logger, e);
			reset();
		}
		catch (OutOfMemoryError e) {
			SQLConsumerLogMessages.logReadTaskOutOfMemoryError(logger, diff, e);
			reset();
		}
	}

	/**
	 * Requests and processes a partial DiffTask from the DiffProducer
	 * 
	 * @throws ConfigurationException
	 *             if an error occurred while accessing the configuration
	 * @throws IOException
	 *             if the writing failed
	 */
	private void readPartialDiff()
		throws ConfigurationException, IOException, SQLConsumerException,
		TimeoutException
	{

		this.setConsumerState(ConsumerStates.PROCCESSING_PARTIAL_TASK);

		process();
		int uniqueId = diff.getHeader().getArticleId();
		int partCounter = diff.getPartCounter() + 1;

		boolean partial = true;

		do {
			this.sleep = true;

			diff = this.diffProducer.getPartialDiff(uniqueId, partCounter);

			if (diff != null) {
				this.setConsumerState(ConsumerStates.PROCCESSING_PARTIAL_TASK);
				process();

				switch (diff.getTaskType()) {
				case TASK_PARTIAL:
					partCounter++;
					break;
				case TASK_PARTIAL_LAST:
					partial = false;
					break;
				default:
					throw new RuntimeException();
				}

				this.sleep = false;
			}

			this.sleep();

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

				this.sleep = true;

				if (this.start) {
					readDiff();
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

					consumerProducer.sendConsumerInformation(this.getName(),
							workingTime, sleepingTime);

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

			try {
				writer.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
