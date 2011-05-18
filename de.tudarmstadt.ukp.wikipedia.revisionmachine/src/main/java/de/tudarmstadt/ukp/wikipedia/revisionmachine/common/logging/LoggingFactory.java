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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.common.logging;

import java.util.HashMap;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.ErrorFactory;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.ErrorKeys;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.LoggingException;

/**
 * The static references in this 'class' creates and controlls all loggers.
 * 
 * 
 */
public class LoggingFactory
{

	/** Reference Map Consumer(-Name) -> Logger */
	private static HashMap<String, Logger> consumerLoggingIndex;

	/** Name for the DiffTool Output Logger */
	public final static String NAME_ARTICLE_OUTPUT_LOGGER = "DiffToolOutput";

	/** Name for the DiffTool Error Logger */
	public final static String NAME_ERROR_LOGGER = "DiffToolErrors";

	/** Creates the static logging factory components */
	static {
		consumerLoggingIndex = new HashMap<String, Logger>();

		try {
			createLogger(LoggerType.DIFF_TOOL_ERROR, NAME_ERROR_LOGGER);
			createLogger(LoggerType.ARTICLE_OUTPUT, NAME_ARTICLE_OUTPUT_LOGGER);
		}
		catch (LoggingException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	/** No class */
	private LoggingFactory()
	{
	}

	/**
	 * Creates a new Logger.
	 * 
	 * @param consumerName
	 *            Consumer Name
	 * @return The referenced Logger
	 * 
	 * @throws LoggingException
	 */
	public static Logger createLogger(final LoggerType type,
			final String consumerName)
		throws LoggingException
	{

		Logger log = new Logger(type, consumerName);
		if (consumerLoggingIndex.put(consumerName, log) != null) {
			throw ErrorFactory
					.createLoggingException(ErrorKeys.LOGGING_LOGGINGFACTORY_LOGGER_ALREADY_EXIST);
		}

		return log;
	}

	/**
	 * Returns an already created Logger.
	 * 
	 * @param consumerName
	 *            Consumer Name
	 * @return The referenced Logger
	 * 
	 * @throws LoggingException
	 */
	public static Logger getLogger(final String consumerName)
		throws LoggingException
	{

		Logger log = consumerLoggingIndex.get(consumerName);
		if (log == null) {
			throw ErrorFactory
					.createLoggingException(ErrorKeys.LOGGING_LOGGINGFACTORY_NO_SUCH_LOGGER);
		}

		return log;
	}
}
