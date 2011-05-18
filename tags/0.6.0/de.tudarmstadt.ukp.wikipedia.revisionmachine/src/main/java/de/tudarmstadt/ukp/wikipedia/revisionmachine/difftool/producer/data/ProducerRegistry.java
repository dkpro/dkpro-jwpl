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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.producer.data;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.ConfigurationException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.LoggingException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.producer.archives.ArchiveProducer;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.producer.articles.ArticleProducer;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.producer.consumers.ProducerConsumer;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.producer.diffs.DiffProducer;

/**
 * ProducerRegistry Part of the DiffTool MultiThread Application
 *
 * Contains the reference to the Producer
 *
 * TODO: Should be used as singleton
 *
 */
public class ProducerRegistry
{

	/** Reference to the ProducerConsumer */
	private final ProducerConsumer producerConsumer;

	/** Reference to the ArchiveProducer */
	private final ArchiveProducer archiveProducer;

	/** Reference to the ArticleProducer */
	private final ArticleProducer articleProducer;

	/** Reference to the DiffProducer */
	private final DiffProducer diffProducer;

	/**
	 * (Constructor) Creates the ProducerRegistry.
	 *
	 * @throws ConfigurationException
	 *             if an error occurs while accessing the configuration
	 * @throws LoggingException
	 *             if an error occurred while creating the logger
	 */
	public ProducerRegistry()
		throws ConfigurationException, LoggingException
	{

		this.producerConsumer = new ProducerConsumer();
		this.archiveProducer = new ArchiveProducer();
		this.articleProducer = new ArticleProducer();
		this.diffProducer = new DiffProducer();
	}

	/**
	 * Returns the reference to the ArchiveProducer.
	 *
	 * @return reference to the ArchiveProducer
	 */
	public ArchiveProducer getArchiveProducer()
	{
		return archiveProducer;
	}

	/**
	 * Returns the reference to the ArticleProducer.
	 *
	 * @return reference to the ArticleProducer
	 */
	public ArticleProducer getArticleProducer()
	{
		return articleProducer;
	}

	/**
	 * Returns the reference to the ProducerConsumer.
	 *
	 * @return reference to the ProducerConsumer
	 */
	public ProducerConsumer getConsumerProducer()
	{
		return producerConsumer;
	}

	/**
	 * Returns the reference to the DiffProducer.
	 *
	 * @return reference to the DiffProducer
	 */
	public DiffProducer getDiffProducer()
	{
		return diffProducer;
	}

}
