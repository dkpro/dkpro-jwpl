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

/**
 * This class contains the english localized log messages for Producers.
 * 
 * TODO: This file should be replaced with resource files.
 * 
 * 
 * 
 */
public class ProducerLogMessages
{

	/**
	 * Logs the start of the producer.
	 * 
	 * @param logger
	 *            reference to the logger
	 */
	public static void logInitialization(final Logger logger)
	{
		logger.logMessage(Level.INFO, "Producer initialized [LogLevel: "
				+ logger.getLogLevel() + "]");
	}

	/**
	 * Logs the shutdown of the producer.
	 * 
	 * @param logger
	 *            reference to the logger
	 */
	public static void logShutdown(final Logger logger)
	{
		logger.logMessage(Level.INFO, "Producer is set into SHUTDOWN mode");
	}

	/** No object - utility class */
	private ProducerLogMessages()
	{
	}
}
