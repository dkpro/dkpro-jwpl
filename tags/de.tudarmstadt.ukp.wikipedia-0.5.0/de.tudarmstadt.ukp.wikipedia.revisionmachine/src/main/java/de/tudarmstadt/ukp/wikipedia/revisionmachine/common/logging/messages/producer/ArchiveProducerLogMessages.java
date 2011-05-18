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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.common.logging.messages.producer;

import java.util.logging.Level;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.logging.Logger;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.archive.ArchiveDescription;

/**
 * This class contains the english localized log messages for ArchiveProducer.
 * 
 * TODO: This file should be replaced with resource files.
 * 
 * 
 * 
 */
public class ArchiveProducerLogMessages
{

	/**
	 * Logs the return of an archive descriptor.
	 * 
	 * @param logger
	 *            reference to the logger
	 * @param description
	 *            reference to the archve descriptor
	 */
	public static void logArchiveReturned(final Logger logger,
			final ArchiveDescription description)
	{
		logger.logMessage(Level.INFO, "Returned " + description);
	}

	/**
	 * Logs the number of remaining archives.
	 * 
	 * @param logger
	 *            reference to the logger
	 * @param archivesLeft
	 *            number of remaining archives
	 */
	public static void logNumberOfRemainingArchives(final Logger logger,
			final int archivesLeft)
	{
		logger.logMessage(Level.INFO, "Archives remaining: " + archivesLeft);
	}

	/** No object - utility class */
	private ArchiveProducerLogMessages()
	{
	}
}
