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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.consumer.article;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.Revision;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.ArticleReaderException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.ConfigurationException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.LoggingException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.TimeoutException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.logging.LoggerType;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.logging.messages.consumer.ArticleConsumerLogMessages;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.logging.messages.consumer.ConsumerLogMessages;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.consumer.AbstractConsumer;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.consumer.ConsumerStates;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.consumer.article.reader.EnglishArticleNameChecker;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.consumer.article.reader.AbstractNameChecker;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.consumer.article.reader.InputFactory;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.archive.ArchiveDescription;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.Task;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.info.ArticleInformation;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.producer.archives.ArchiveProducerInterface;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.producer.articles.ArticleProducerInterface;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.producer.consumers.ProducerConsumerInterface;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.producer.data.CapacityValue;

/**
 * ArticleConsumer Consumer of the DiffTool Application
 *
 * This consumer is used to parse the articles.
 *
 *
 *
 */
public class ArticleConsumer
	extends AbstractConsumer
{

	/** Reference to the ProducerConsumer */
	private final ProducerConsumerInterface consumerProducer;

	/** Reference to the ArchiveProducer */
	private final ArchiveProducerInterface archiveProducer;

	/** Reference to the ArticleProducer */
	private final ArticleProducerInterface articleProducer;

	/** Reference to the ArticleRader */
	private ArticleReaderInterface articleReader;

	/** Temporary variable - reference to the currently processed task */
	private Task<Revision> article;

	/** Temporary variable - reference to the currently used archive */
	private ArchiveDescription archive;

	/**
	 * Temporary variable - used to store the start time of a parse operation
	 */
	private long processingStart;

	/**
	 * Temporary variable - used to store time used for a parse operation
	 */
	private long processingTime;

	/** Temporary variable - Capacity of the ArticleProducer */
	private CapacityValue capacity;

	/**
	 * (Constructor) Creates a new ArticleConsumer.
	 *
	 * @param consumerProducer
	 *            Reference to the ProducerConsumer
	 * @param archiveProducer
	 *            Reference to the ArchiveProducer
	 * @param articleProducer
	 *            Reference to the ArticleProducer
	 *
	 * @throws ConfigurationException
	 *             if an error occurred while accessing the configuration
	 * @throws LoggingException
	 *             if an error occurred while creating the logger
	 */
	public ArticleConsumer(final ProducerConsumerInterface consumerProducer,
			final ArchiveProducerInterface archiveProducer,
			final ArticleProducerInterface articleProducer)

		throws ConfigurationException, LoggingException
	{

		super(LoggerType.CONSUMER_TASK, consumerProducer);

		this.consumerProducer = consumerProducer;
		this.archiveProducer = archiveProducer;
		this.articleProducer = articleProducer;

		this.capacity = null;
	}

	/**
	 * Retrieves an archive from the ArchiveProducer.
	 */
	private void retrieveArchive()
	{

		this.articleReader = null;
		this.archive = null;

		if (archiveProducer.hasArchive()) {

			archive = archiveProducer.getArchive();
			if (archive != null) {
				try {
					//TODO implementation of name checker depends on wikipedia language - make configurable!
					AbstractNameChecker nameFilter = new EnglishArticleNameChecker();

					this.articleReader = InputFactory.getTaskReader(archive,
							nameFilter);
					ArticleConsumerLogMessages.logArchiveRetrieved(logger,
							archive);

				}
				catch (Exception e) {

					ArticleConsumerLogMessages.logExceptionRetrieveArchive(
							logger, archive, e);

					e.printStackTrace();
					this.articleReader = null;

				}
				catch (Error e) {

					ArticleConsumerLogMessages.logErrorRetrieveArchive(logger,
							archive, e);

					e.printStackTrace();
					this.articleReader = null;
				}

			}
			else {
				ArticleConsumerLogMessages.logRetrieveArchiveFailed(logger);
			}

		}
		else {

			ArticleConsumerLogMessages.logNoMoreArchives(logger);
			this.shutdown = true;
		}
	}

	/**
	 * Resets the current task and deletes all reference of this task and its
	 * related tasks.
	 */
	private void reset()
	{

		articleReader.resetTaskCompleted();

		if (article != null) {

			switch (article.getTaskType()) {
			case TASK_PARTIAL:
			case TASK_PARTIAL_FIRST:
				ArticleInformation header = article.getHeader();
				if (header != null) {

					int articleId = header.getArticleId();
					if (articleId != -1) {
						articleProducer.addBannedTask(articleId);
					}
				}
				break;
			}
		}

		System.gc();
	}

	/**
	 * Reads a revision task from the input archive and transmits it to the
	 * ArticleProducer.
	 */
	private void readArticle()
	{
		CapacityValue capacity = articleProducer.hasCapacity();
		if (capacity == CapacityValue.FREE) {

			this.capacity = null;

			this.consumerProducer.sendConsumerInformation(this.getName(),
					workingTime, sleepingTime);

			ArticleConsumerLogMessages.logStatus(logger, articleReader,
					startTime, sleepingTime, workingTime);

			try {
				if (articleReader.hasNext()) {

					processingStart = System.currentTimeMillis();
					article = articleReader.next();
					processingTime = System.currentTimeMillis()
							- processingStart;

					this.workingTime += processingTime;

					ArticleConsumerLogMessages.logArticleRead(logger, article,
							processingTime);

					switch (article.getTaskType()) {

					case TASK_FULL:
					case TASK_PARTIAL_FIRST:
						this.setConsumerState(ConsumerStates.PROCCESSING_FULL_TASK);
						articleProducer.addArticle(article);
						break;

					case TASK_PARTIAL:
					case TASK_PARTIAL_LAST:
						this.setConsumerState(ConsumerStates.PROCCESSING_PARTIAL_TASK);
						articleProducer.addPartialArticle(article);
						break;

					default:
						ArticleConsumerLogMessages.logInvalidTaskType(logger,
								article.getTaskType());
					}

				}
				else {

					ArticleConsumerLogMessages.logNoMoreArticles(logger,
							archive);
					articleReader = null;
				}

			}
			catch (OutOfMemoryError e) {

				System.err
						.println("[" + this.getName() + "]\tOutOfMemoryError");
				ArticleConsumerLogMessages.logReadTaskOutOfMemoryError(logger,
						article, e);

				reset();

			}
			catch (ArticleReaderException e) {

				e.printStackTrace();
				ArticleConsumerLogMessages.logTaskReaderException(logger, e);

				reset();

			}
			catch (Exception e) {

				e.printStackTrace();
				ArticleConsumerLogMessages.logReadTaskException(logger,
						article, e);

				reset();
			}

			sleep = false;

		}
		else if (this.capacity != capacity) {
			this.capacity = capacity;

			ArticleConsumerLogMessages.logProducerHasNoCapacities(logger,
					capacity);
		}
	}

	/**
	 * Starts the consumer.
	 */
	@Override
	public void run()
	{

		try {
			ConsumerLogMessages.logConsumerRunning(logger);

			while (!this.shutdown) {

				this.retrieveArchive();

				while (!this.shutdown && this.articleReader != null) {

					sleep = true;

					if (start) {
						this.readArticle();
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
}
