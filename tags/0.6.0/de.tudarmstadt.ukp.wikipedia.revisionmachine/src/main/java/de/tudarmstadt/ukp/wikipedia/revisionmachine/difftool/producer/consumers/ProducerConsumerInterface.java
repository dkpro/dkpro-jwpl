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

import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.consumer.ConsumerInterface;

/**
 * Interface of the ProducerConsumer
 * 
 * 
 * 
 */
public interface ProducerConsumerInterface
{

	/**
	 * Registers the consumer.
	 * 
	 * @param consumer
	 *            ConsumerInterface
	 * @return Name of the consumer
	 */
	String register(final ConsumerInterface consumer);

	/**
	 * Unregisters the consumer with the given name.
	 * 
	 * @param name
	 *            name of the consumer
	 * @return TRUE if the consumer could be removed FALSE otherwise
	 */
	boolean unregister(final String name);

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
	void sendConsumerInformation(final String name, final long workingTime,
			final long sleepingTime);
}
