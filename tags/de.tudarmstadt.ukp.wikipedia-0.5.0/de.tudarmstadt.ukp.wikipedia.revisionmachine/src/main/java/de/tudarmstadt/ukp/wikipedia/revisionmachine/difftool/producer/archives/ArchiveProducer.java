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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.producer.archives;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.ConfigurationException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.LoggingException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.logging.LoggerType;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.logging.messages.producer.ArchiveProducerLogMessages;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.archive.ArchiveDescription;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.producer.AbstractProducer;

/**
 * ArchiveProducer This class represents the communication interface for this
 * producer.
 * 
 * 
 * 
 */
public class ArchiveProducer
	extends AbstractProducer
	implements ArchiveProducerInterface
{

	/** Reference to the ArchiveManager */
	private ArchiveManager archives;

	/**
	 * (Constructor) Creates an ArchiveProducer.
	 * 
	 * @throws ConfigurationException
	 *             if an error occurs while accessing the configuration
	 * @throws LoggingException
	 *             if an error occurs while creating the logger
	 */
	public ArchiveProducer()
		throws ConfigurationException, LoggingException
	{

		super(LoggerType.PRODUCER_ARCHIVES, "ArchiveProducer");

		this.archives = new ArchiveManager();
		ArchiveProducerLogMessages.logNumberOfRemainingArchives(logger,
				archives.size());
	}

	/**
	 * Returns an archive.
	 * 
	 * @return ArchiveDescription
	 */
	public ArchiveDescription getArchive()
	{
		if (this.isShutdown()) {
			return null;
		}

		ArchiveDescription description = this.archives.getArchive();

		int size = archives.size();
		ArchiveProducerLogMessages.logArchiveReturned(logger, description);
		ArchiveProducerLogMessages.logNumberOfRemainingArchives(logger, size);
		if (size == 0) {
			shutdown();
		}

		return description;
	}

	/**
	 * Returns whether an archive is available or not.
	 * 
	 * @return TRUE | FALSE
	 */
	public boolean hasArchive()
	{
		if (this.isShutdown()) {
			return false;
		}

		return this.archives.hasArchive();
	}

	/**
	 * Returns a reference to the ArchiveManager.
	 * 
	 * @return ArchiveManager
	 */
	public ArchiveManager getArchiveManager()
	{
		return this.archives;
	}
}
