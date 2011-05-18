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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.producer;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.LoggingException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.logging.Logger;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.logging.LoggerType;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.logging.LoggingFactory;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.logging.messages.producer.ProducerLogMessages;

/**
 * AbstractProducer This class is the superclass of all DiffTool producers.
 * 
 * 
 * 
 */
public abstract class AbstractProducer
{

	/** Flag, which indicates whether the producer should shutdown */
	protected boolean shutdown;

	/** Name of the producer */
	private String name;

	/** Reference to the logger of this producer */
	protected Logger logger;

	/**
	 * (Constructor) Create a new AbstractProducer object.
	 * 
	 * @param type
	 *            Type of logger
	 * @param name
	 *            Name of the producer
	 * @throws LoggingException
	 *             if the creation of the logger fails
	 */
	public AbstractProducer(final LoggerType type, final String name)
		throws LoggingException
	{
		logger = LoggingFactory.createLogger(type, name);

		this.name = name;
		this.shutdown = false;

		ProducerLogMessages.logInitialization(logger);

	}

	/**
	 * Returns the name of the producer.
	 * 
	 * @return name
	 */
	public String getName()
	{
		return this.name;
	}

	/**
	 * Initiates shutdown mode.
	 */
	public void shutdown()
	{

		if (!shutdown) {
			this.shutdown = true;
			ProducerLogMessages.logShutdown(logger);
			logger.flush();
		}
	}

	/**
	 * Returns whether the producer is in shutdown mode or not.
	 * 
	 * @return TRUE | FALSE
	 */
	public boolean isShutdown()
	{
		return this.shutdown;
	}

	/**
	 * Returns a reference to the logger.
	 * 
	 * @return logger
	 */
	public Logger getLogger()
	{
		return this.logger;
	}
}
