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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.producer.consumers;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.LoggingException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.logging.LoggerType;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.consumer.ConsumerInterface;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.producer.AbstractProducer;

/**
 * ProducerConsumer This class represents the communication interface for this
 * producer.
 * 
 * 
 * 
 */
public class ProducerConsumer
	extends AbstractProducer
	implements ProducerConsumerInterface
{

	/** Reference to the ConsumerManager */
	private ConsumerManager consumers;

	/**
	 * (Constructor) Creates the ProducerConsumer.
	 * 
	 * @throws LoggingException
	 *             if the creation of the logger failed
	 */
	public ProducerConsumer()
		throws LoggingException
	{

		super(LoggerType.PRODUCER_CONSUMERS, "ProducerConsumer");
		this.consumers = new ConsumerManager();
	}

	/**
	 * Registers the consumer.
	 * 
	 * @param consumer
	 *            ConsumerInterface
	 * @return Name of the cleint
	 */
	public String register(final ConsumerInterface consumer)
	{

		return consumers.register(consumer);
	}

	/**
	 * This method should be called to add additional data for a consumer.
	 * 
	 * @param name
	 *            name of a consumer
	 * @param workingTime
	 *            time the consumer was working
	 * @param sleepingTime
	 *            time the consumer was sleeping
	 */
	public void sendConsumerInformation(final String name,
			final long workingTime, final long sleepingTime)
	{

		consumers.sendConsumerInformation(name, workingTime, sleepingTime);
	}

	/**
	 * Unregisters the consumer with the given name.
	 * 
	 * @param name
	 *            name of the consumer
	 * @return TRUE if the consumer could be removed FALSE otherwise
	 */
	public boolean unregister(final String name)
	{
		return consumers.unregister(name);
	}

	/**
	 * Returns a reference to the ConsumerManager.
	 * 
	 * @return ConsumerManager
	 */
	public ConsumerManager getConsumerManager()
	{
		return this.consumers;
	}

	/**
	 * Initiates shutdown mode.
	 */
	public void shutdown()
	{

		super.shutdown();
		if (!this.shutdown) {
			this.consumers.shutdown();
		}
	}
}
